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
import com.oneroadtrip.matcher.proto.OrderStatus;
import com.oneroadtrip.matcher.proto.SignupType;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.proto.UserInfo;
import com.oneroadtrip.matcher.util.HashUtil;
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
      throws SQLException {
    Long itineraryId = null;
    try (PreparedStatement pStmt = conn.prepareStatement(ADD_ITINERARY,
        Statement.RETURN_GENERATED_KEYS)) {
      pStmt.setString(1, TextFormat.printToUnicodeString(itin));
      itineraryId = SqlUtil.executeStatementAndReturnId(pStmt);
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
      }
    }

    Long orderId = null;
    try (PreparedStatement pStmt = conn.prepareStatement(ADD_ORDER,
        Statement.RETURN_GENERATED_KEYS)) {
      // TODO(xiaofengguo):
      pStmt.setLong(1, itin.getUserId());
      pStmt.setLong(2, itineraryId);
      pStmt.setFloat(3, ItineraryUtil.getCostUsd(itin));
      pStmt.executeUpdate();
      orderId = SqlUtil.executeStatementAndReturnId(pStmt);
    }
    return Triplet.with(orderId, itineraryId, reservedGuideIds);
  }

  private static final String RESERVER_GUIDES_PERMANENTLY = "INSERT INTO GuideReservations "
      + "(guide_id, itinerary_id, reserved_date, is_permanent) VALUES "
      + "(?, ?, ?, true)";
  public static List<Long> reserveGuides(Itinerary itin, Connection conn) throws SQLException {
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
      }
    }
    return reservedGuideIds;
  }

  private static final String REVERT_RESERVED_GUDIES_BY_IDS = "DELETE FROM GuideReservations "
      + "WHERE reservation_id IN (%s)";
  public static int revertReservedGuides(List<Long> guideReservationIds, Connection conn) throws SQLException {
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
    }
  }

  private static final String UPDATE_ORDER_BY_ID = "UPDATE Orders SET status = ? WHERE order_id = ?";
  public static int updateOrder(Itinerary itin, Connection conn) throws SQLException {
    try (PreparedStatement pStmt = conn.prepareStatement(UPDATE_ORDER_BY_ID)) {
      pStmt.setInt(1, OrderStatus.PAID.getNumber());
      pStmt.setLong(2, itin.getOrder().getOrderId());
      return pStmt.executeUpdate();
    }
  }
  
  // ============== SIGN-UP / LOGIN related =================
  private static final String INSERT_OAUTH_USER = "INSERT INTO OAuthUsers "
      + "(user_id, source, access_token, client_id, openid) VALUES "
      + "(?, ?, ?, ?, ?)";

  private UserInfo addOAuthUser(Connection conn, UserInfo userInfo, SignupType type, String accessToken,
      String clientId, String openId) throws SQLException {
    try (PreparedStatement pStmt = conn.prepareStatement(INSERT_OAUTH_USER, Statement.RETURN_GENERATED_KEYS)) {
      pStmt.setLong(1, userInfo.getUserId());
      pStmt.setInt(2, type.getNumber());
      pStmt.setString(3, accessToken);
      pStmt.setString(4, clientId);
      pStmt.setString(5, openId);
      SqlUtil.executeStatementAndReturnId(pStmt);
      return userInfo;
    }
  }
  
  private static final String INSERT_USER = "INSERT INTO Users "
      + "(user_name, nick_name, password) VALUES (?, ?, ?)";
  public UserInfo addUser(String username, String nickname, String password, Connection conn)
      throws SQLException {
    try (PreparedStatement pStmt = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
      pStmt.setString(1, username);
      pStmt.setString(2, nickname);
      pStmt.setString(3, password);
      UserInfo user = UserInfo.newBuilder().setUserId(SqlUtil.executeStatementAndReturnId(pStmt))
          .setUserName(username).setNickName(nickname).build();
      return user;
    }
  }

  public UserInfo addOAuthUser(String nickname, SignupType type, String accessToken,
      String clientId, String openId) throws OneRoadTripException {
    UserInfo user = SqlUtil.executeTransaction(dataSource,
        (Connection conn) -> addUser("", nickname, "", conn));
    return SqlUtil.executeTransaction(dataSource,
        (Connection conn) -> addOAuthUser(conn, user, type, accessToken, clientId, openId));
  }

  public UserInfo addUser(String username, String nickname, String password)
      throws OneRoadTripException {
    return SqlUtil.executeTransaction(dataSource,
        (Connection conn) -> addUser(username, nickname, password, conn));
  }

  private UserInfo getUserBySql(PreparedStatement pStmt) throws OneRoadTripException {
    try (ResultSet rs = pStmt.executeQuery()) {
      if (!rs.next()) {
        return null;
      }
      UserInfo user = UserInfo.newBuilder().setUserId(rs.getLong(1)).setUserName(rs.getString(2))
          .setNickName(rs.getString(3)).setPassword(rs.getString(4)).build();
      Preconditions.checkArgument(!rs.next());
      return user;
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERROR_IN_SQL, e);
    } catch (IllegalArgumentException e) {
      // Multiple users having the same (clientId, openId).
      throw new OneRoadTripException(Status.ERROR_IN_SQL, e);
    }
  }

  private static final String LOOKUP_OAUTH_USER = "SELECT u.user_id, u.user_name, u.nick_name, u.password "
      + "FROM OAuthUsers ou INNER JOIN Users u ON (ou.user_id = u.user_id) "
      + "WHERE client_id = ? AND openid = ?";
  
  public UserInfo lookupOAuthUser(String clientId, String openId) throws OneRoadTripException {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement pStmt = conn.prepareStatement(LOOKUP_OAUTH_USER)) {
      pStmt.setString(1, clientId);
      pStmt.setString(2, openId);
      return getUserBySql(pStmt);
    } catch (SQLException e) {
      throw new OneRoadTripException(Status.ERROR_IN_SQL, e);
    }
  }

  private static final String LOOKUP_USER = "SELECT user_id, user_name, nick_name, password "
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
  private int expireTokensOfUser(Connection conn, long userId) throws SQLException {
    try (PreparedStatement pStmt = conn.prepareStatement(EXPIRE_ALL_TOKENS_OF_USER)) {
      pStmt.setLong(1, userId);
      return pStmt.executeUpdate();
    }
  }
  public void expireTokensOfUser(UserInfo user) throws OneRoadTripException {
    int updateRows = SqlUtil.executeTransaction(dataSource,
        (Connection conn) -> expireTokensOfUser(conn, user.getUserId()));
    LOG.info("Expires {} tokens for user: {} ({})", updateRows, user.getUserName(), user.getNickName());
  }
  
  private static final String INSERT_ONE_TOKEN_FOR_USER = "INSERT INTO Tokens "
      + "(token, user_id, expired_ts, is_expired) VALUES (?, ?, ?, false)";
  private int insertOneTokenForUser(Connection conn, UserInfo user, String token) throws SQLException {
    try (PreparedStatement pStmt = conn.prepareStatement(INSERT_ONE_TOKEN_FOR_USER)) {
      pStmt.setString(1, token);
      pStmt.setLong(2, user.getUserId());
      pStmt.setTimestamp(3, SqlUtil.getTimestampToNow((int) TimeUnit.DAYS.toSeconds(30)));
      return pStmt.executeUpdate();
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
}
