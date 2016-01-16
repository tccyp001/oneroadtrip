package com.oneroadtrip.matcher.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.protobuf.TextFormat;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.Order;
import com.oneroadtrip.matcher.proto.OrderStatus;
import com.oneroadtrip.matcher.proto.SignupType;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.proto.UserInfo;
import com.oneroadtrip.matcher.util.HashUtil.Hasher;
import com.oneroadtrip.matcher.util.ItineraryUtil;
import com.oneroadtrip.matcher.util.SqlUtil;
import com.oneroadtrip.matcher.util.Util;

public class DatabaseAccessor {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  DataSource dataSource;

  private static final String LOAD_GUIDE_RESERVE_DAYS = "SELECT guide_id, reserved_date FROM GuideReservations "
      + "WHERE (is_permanent = true OR update_timestamp >= ?) AND guide_id IN (%s)";

  public Map<Long, Set<Integer>> loadGuideToReserveDays(Collection<Long> guides,
      long cutoffTimestamp) throws OneRoadTripException {
    if (guides.size() == 0) {
      return Maps.newTreeMap();
    }

    // TODO(xfguo): Make the sql generation same as code in prepareOrder().
    // Create SQL
    String sql = String.format(LOAD_GUIDE_RESERVE_DAYS, Util.getQuestionMarksForSql(guides.size()));

    // Query and return
    Map<Long, Set<Integer>> result = Maps.newTreeMap();
    try (Connection conn = dataSource.getConnection()) {
      try (PreparedStatement pStmt = conn.prepareStatement(sql)) {
        int index = 1;
        pStmt.setTimestamp(index++, new Timestamp(cutoffTimestamp));
        for (Long guide : guides) {
          pStmt.setLong(index++, guide);
        }
        LOG.info("xfguo: index = {}", index);

        try (ResultSet rs = pStmt.executeQuery()) {
          while (rs.next()) {
            Long guideId = rs.getLong(1);
            Integer reserveDate = rs.getInt(2);

            if (!result.containsKey(guideId)) {
              result.put(guideId, Sets.newTreeSet());
            }
            result.get(guideId).add(reserveDate);
          }
        }
      }
    } catch (SQLException e) {
      LOG.error("DB query errors in loading guide reservation data...", e);
      throw new OneRoadTripException(Status.ERR_LOAD_GUIDE_TO_RESERVED_DAYS, e);
    }
    return result;
  }

  /**
   * Transactionally append order / itinerary / guide reservation ids.
   * 
   * @return (orderId, itineraryId, {reservationIds})
   */
  private static final String ADD_ITINERARY = "INSERT INTO Itineraries (content) VALUES (?)";
  private static final String ADD_RESERVATION = "INSERT INTO GuideReservations "
      + "(guide_id, itinerary_id, reserved_date, is_permanent, update_timestamp) "
      + "VALUES (?, ?, ?, false, default)";
  private static final String ADD_ORDER = "INSERT INTO Orders "
      + "(user_id, itinerary_id, cost_usd) VALUES (?, ?, ?)";

  public static Triplet<Long, Long, List<Long>> prepareForOrder(Itinerary itin, Connection conn)
      throws OneRoadTripException {
    // TODO(xfguo): Use another service to keep itinerary.
    Long itineraryId = null;
    try (PreparedStatement pStmt = conn.prepareStatement(ADD_ITINERARY,
        Statement.RETURN_GENERATED_KEYS)) {
      pStmt.setString(1, TextFormat.printToUnicodeString(itin));
      itineraryId = SqlUtil.executeStatementAndReturnId(pStmt);
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERR_ADD_ITINERARY_FOR_ORDER, e);
    }

    // 3. add reservations
    List<Long> reservedGuideIds = Lists.newArrayList();
    for (Pair<Long, Integer> guideAndDate : Util.getGuideReservationMap(itin)) {
      try (PreparedStatement pStmt = conn.prepareStatement(ADD_RESERVATION,
          Statement.RETURN_GENERATED_KEYS)) {
        pStmt.setLong(1, guideAndDate.getValue0());
        pStmt.setLong(2, itineraryId);
        pStmt.setInt(3, guideAndDate.getValue1());
        pStmt.addBatch();
        reservedGuideIds.add(SqlUtil.executeStatementAndReturnId(pStmt));
      } catch (SQLException e) {
        throw new OneRoadTripException(Status.ERR_ADD_RESERVATION_FOR_ORDER, e);
      }
    }

    Long orderId = null;
    try (PreparedStatement pStmt = conn.prepareStatement(ADD_ORDER,
        Statement.RETURN_GENERATED_KEYS)) {
      long userId = getUserId(conn, itin.getUserToken());
      pStmt.setLong(1, userId);
      pStmt.setLong(2, itineraryId);
      pStmt.setFloat(3, ItineraryUtil.getCostUsd(itin));
      pStmt.executeUpdate();
      orderId = SqlUtil.executeStatementAndReturnId(pStmt);
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERR_ADD_ORDER_FOR_ORDER, e);
    }
    return Triplet.with(orderId, itineraryId, reservedGuideIds);
  }
  
  private static final String GET_USER_ID_BY_TOKEN = "SELECT user_id FROM Tokens "
      + "WHERE token = ? AND is_expired = false AND expired_ts > ?";
  private static long getUserId(Connection conn, String userToken) throws OneRoadTripException {
    try (PreparedStatement pStmt = conn.prepareStatement(GET_USER_ID_BY_TOKEN)) {
      pStmt.setString(1, userToken);
      pStmt.setTimestamp(2, SqlUtil.getTimestampToNow(0));
      try (ResultSet rs = pStmt.executeQuery()) {
        Preconditions.checkArgument(rs.next());
        long userId = rs.getLong(1);
        Preconditions.checkArgument(!rs.next());
        return userId;
      }
    } catch (IllegalArgumentException| SQLException e) {
      throw new OneRoadTripException(Status.ERR_GET_USER_ID, e);
    }
  }

  private static final String RESERVER_GUIDES_PERMANENTLY = "INSERT INTO GuideReservations "
      + "(guide_id, itinerary_id, reserved_date, is_permanent) VALUES " + "(?, ?, ?, true)";

  public static List<Long> reserveGuides(Itinerary itin, Connection conn) throws OneRoadTripException {
    List<Long> reservedGuideIds = Lists.newArrayList();
    long itineraryId = itin.getItineraryId();
    for (Pair<Long, Integer> guideAndDate : Util.getGuideReservationMap(itin)) {
      try (PreparedStatement pStmt = conn.prepareStatement(RESERVER_GUIDES_PERMANENTLY,
          Statement.RETURN_GENERATED_KEYS)) {
        pStmt.setLong(1, guideAndDate.getValue0());
        pStmt.setLong(2, itineraryId);
        pStmt.setInt(3, guideAndDate.getValue1());
        pStmt.addBatch();
        reservedGuideIds.add(SqlUtil.executeStatementAndReturnId(pStmt));
      } catch (SQLException e) {
        throw new OneRoadTripException(Status.ERR_INSERT_GUIDE_RESERVATION, e);
      }
    }
    return reservedGuideIds;
  }

  private static final String REVERT_RESERVED_GUDIES_BY_IDS = "DELETE FROM GuideReservations "
      + "WHERE reservation_id IN (%s)";

  public static int revertReservedGuides(List<Long> guideReservationIds, Connection conn)
      throws OneRoadTripException {
    if (guideReservationIds.isEmpty()) {
      return 0;
    }
    try (PreparedStatement pStmt = conn.prepareStatement(String.format(
        REVERT_RESERVED_GUDIES_BY_IDS, Util.getQuestionMarksForSql(guideReservationIds.size())))) {
      int index = 1;
      for (long reservationId : guideReservationIds) {
        pStmt.setLong(index++, reservationId);
      }
      return pStmt.executeUpdate();
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERR_DELETE_GUIDE_RESERVATIONS, e);
    }
  }

  private static final String UPDATE_ORDER_BY_ID = "UPDATE Orders "
      + "SET status = ?, charge_id = ? WHERE order_id = ?";

  private int updateOrder(Connection conn, long orderId, String chargeId)
      throws OneRoadTripException {
    try (PreparedStatement pStmt = conn.prepareStatement(UPDATE_ORDER_BY_ID)) {
      pStmt.setInt(1, OrderStatus.PAID.getNumber());
      pStmt.setString(2, chargeId);
      pStmt.setLong(3, orderId);
      return pStmt.executeUpdate();
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERR_UPDATE_ORDER_STATUS, e);
    }
  }
  public void updateOrder(long orderId, String chargeId) throws OneRoadTripException {
    int updateRows = SqlUtil.executeTransaction(dataSource,
        (Connection conn) -> updateOrder(conn, orderId, chargeId));
    if (updateRows != 1) {
      throw new OneRoadTripException(Status.ERR_UPDATE_ORDER_STATUS, null);
    }
  }

  // ============== SIGN-UP / LOGIN related =================
  private static final String INSERT_OAUTH_USER = "INSERT INTO OAuthUsers "
      + "(user_id, source, access_token, unique_id) VALUES " + "(?, ?, ?, ?)";

  private UserInfo addOAuthUser(Connection conn, UserInfo userInfo, SignupType type,
      String accessToken, String uniqueId) throws OneRoadTripException {
    try (PreparedStatement pStmt = conn.prepareStatement(INSERT_OAUTH_USER,
        Statement.RETURN_GENERATED_KEYS)) {
      pStmt.setLong(1, userInfo.getUserId());
      pStmt.setInt(2, type.getNumber());
      pStmt.setString(3, accessToken);
      pStmt.setString(4, uniqueId);
      SqlUtil.executeStatementAndReturnId(pStmt);
      return userInfo;
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERR_ADD_OAUTH_USER, e);
    }
  }

  private static final String INSERT_USER = "INSERT INTO Users "
      + "(user_name, nick_name, picture_url, password) VALUES (?, ?, ?, ?)";

  public UserInfo addUser(String username, String nickname, String pictureUrl, String password,
      Connection conn) throws OneRoadTripException {
    try (PreparedStatement pStmt = conn.prepareStatement(INSERT_USER,
        Statement.RETURN_GENERATED_KEYS)) {
      pStmt.setString(1, username);
      pStmt.setString(2, nickname);
      pStmt.setString(3, pictureUrl);
      pStmt.setString(4, password);
      UserInfo user = UserInfo.newBuilder().setUserId(SqlUtil.executeStatementAndReturnId(pStmt))
          .setUserName(username).setNickName(nickname).setPictureUrl(pictureUrl).build();
      return user;
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERR_ADD_USER, e);
    }
  }

  public UserInfo addOAuthUser(UserInfo oauthUser, SignupType type, String accessToken,
      String uniqueId) throws OneRoadTripException {
    UserInfo user = SqlUtil.executeTransaction(
        dataSource,
        (Connection conn) -> addUser("", oauthUser.getNickName(), oauthUser.getPictureUrl(), "",
            conn));
    return SqlUtil.executeTransaction(dataSource,
        (Connection conn) -> addOAuthUser(conn, user, type, accessToken, uniqueId));
  }

  public UserInfo addUser(String username, String nickname, String pictureUrl, String password)
      throws OneRoadTripException {
    return SqlUtil.executeTransaction(dataSource,
        (Connection conn) -> addUser(username, nickname, pictureUrl, password, conn));
  }

  private UserInfo getUserBySql(PreparedStatement pStmt) throws OneRoadTripException {
    try (ResultSet rs = pStmt.executeQuery()) {
      if (!rs.next()) {
        return null;
      }
      UserInfo user = UserInfo.newBuilder().setUserId(rs.getLong(1)).setUserName(rs.getString(2))
          .setNickName(rs.getString(3)).setPassword(rs.getString(4)).setPictureUrl(rs.getString(5))
          .build();
      Preconditions.checkArgument(!rs.next());
      return user;
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERROR_IN_SQL, e);
    } catch (IllegalArgumentException e) {
      // Multiple users having the same (clientId, openId).
      throw new OneRoadTripException(Status.ERROR_IN_SQL, e);
    }
  }

  private static final String LOOKUP_OAUTH_USER = "SELECT u.user_id, u.user_name, u.nick_name, u.password, u.picture_url "
      + "FROM OAuthUsers ou INNER JOIN Users u ON (ou.user_id = u.user_id) "
      + "WHERE source = ? AND unique_id = ?";

  public UserInfo lookupOAuthUser(SignupType type, String uniqueId) throws OneRoadTripException {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement pStmt = conn.prepareStatement(LOOKUP_OAUTH_USER)) {
      pStmt.setInt(1, type.getNumber());
      pStmt.setString(2, uniqueId);
      return getUserBySql(pStmt);
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERROR_IN_SQL, e);
    }
  }
  
  private static final String UPDATE_USER = "UPDATE Users "
      + "SET user_name=?,nick_name=?,email=?,password=?,picture_url=? "
      + "WHERE user_id = ?";
  private int updateUser(Connection conn, UserInfo user) throws OneRoadTripException {
    try (PreparedStatement pStmt = conn.prepareStatement(UPDATE_USER)) {
      pStmt.setString(1, user.getUserName());
      pStmt.setString(2, user.getNickName());
      pStmt.setString(3, user.getEmail());
      pStmt.setString(4, user.getPassword());
      pStmt.setString(5, user.getPictureUrl());
      pStmt.setLong(6, user.getUserId());
      return pStmt.executeUpdate(); 
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERR_UPDATE_USER, e);
    }
  }
  
  public UserInfo updateUser(UserInfo user) throws OneRoadTripException {
    int updatedRows = SqlUtil.executeTransaction(dataSource,
        (Connection conn) -> updateUser(conn, user));
    if (updatedRows != 1) {
      throw new OneRoadTripException(Status.ERR_UPDATE_USER, null);
    }
    return user;
  }

  private static final String LOOKUP_USER = "SELECT user_id, user_name, nick_name, password, picture_url "
      + "FROM Users WHERE user_name = ?";

  public UserInfo lookupUser(String username) throws OneRoadTripException {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement pStmt = conn.prepareStatement(LOOKUP_USER)) {
      pStmt.setString(1, username);
      return getUserBySql(pStmt);
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERROR_IN_SQL, e);
    }
  }

  private static final String EXPIRE_ALL_TOKENS_OF_USER = "UPDATE Tokens SET is_expired = True "
      + "WHERE user_id = ?";

  private int expireTokensOfUser(Connection conn, long userId) throws OneRoadTripException {
    try (PreparedStatement pStmt = conn.prepareStatement(EXPIRE_ALL_TOKENS_OF_USER)) {
      pStmt.setLong(1, userId);
      return pStmt.executeUpdate();
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERR_EXPIRE_ALL_USER_TOKENS, e);
    }
  }

  public void expireTokensOfUser(UserInfo user) throws OneRoadTripException {
    int updateRows = SqlUtil.executeTransaction(dataSource,
        (Connection conn) -> expireTokensOfUser(conn, user.getUserId()));
    LOG.info("Expires {} tokens for user: {} ({})", updateRows, user.getUserName(),
        user.getNickName());
  }

  private static final String INSERT_ONE_TOKEN_FOR_USER = "INSERT INTO Tokens "
      + "(token, user_id, expired_ts, is_expired) VALUES (?, ?, ?, false)";

  private int insertOneTokenForUser(Connection conn, UserInfo user, String token)
      throws OneRoadTripException {
    try (PreparedStatement pStmt = conn.prepareStatement(INSERT_ONE_TOKEN_FOR_USER)) {
      pStmt.setString(1, token);
      pStmt.setLong(2, user.getUserId());
      pStmt.setTimestamp(3, SqlUtil.getTimestampToNow((int) TimeUnit.DAYS.toSeconds(30)));
      return pStmt.executeUpdate();
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERR_INSERT_TOKEN, e);
    }
  }

  public String addOneValidToken(Hasher hasher, UserInfo user) throws OneRoadTripException {
    try {
      Preconditions.checkArgument(user.hasUserId());
      String token = hasher.getRandomString();
      int insertRows = SqlUtil.executeTransaction(dataSource,
          (Connection conn) -> insertOneTokenForUser(conn, user, token));
      Preconditions.checkArgument(insertRows > 0);
      return token;
    } catch (IllegalArgumentException e) {
      throw new OneRoadTripException(Status.INCORRECT_REQUEST, e);
    }
  }
  
  private static final String CANCEL_ORDER = "UPDATE Orders SET is_cancel = True "
      + "WHERE order_id = ?";
  private int cancelOrder(Connection conn, long orderId) throws OneRoadTripException {
    try (PreparedStatement pStmt = conn.prepareStatement(CANCEL_ORDER)) {
      pStmt.setLong(1, orderId);
      return pStmt.executeUpdate();
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERR_CANCEL_ORDER, e);
    }
  }

  public void cancelOrder(Order order) throws OneRoadTripException {
    int updateRows = SqlUtil.executeTransaction(dataSource,
        (Connection conn) -> cancelOrder(conn, order.getOrderId()));
    if (updateRows != 1) {
      throw new OneRoadTripException(Status.ERR_CANCEL_ORDER, null);
    }
  }

  private static final String GET_CHARGE_ID = "SELECT charge_id FROM Orders WHERE order_id = ?";
  public String getChargeId(long orderId) throws OneRoadTripException {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement pStmt = conn.prepareStatement(GET_CHARGE_ID)) {
      pStmt.setLong(1, orderId);
      try (ResultSet rs = pStmt.executeQuery()) {
        Preconditions.checkArgument(rs.next());
        String chargeId = rs.getString(1);
        Preconditions.checkArgument(!rs.next());
        return chargeId;
      }
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERR_GET_CHARGE_ID, e);
    }
  }
}
