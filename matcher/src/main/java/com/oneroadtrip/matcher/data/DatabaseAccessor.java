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
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.proto.VisitCity;
import com.oneroadtrip.matcher.util.ItineraryUtil;
import com.oneroadtrip.matcher.util.SqlUtil;
import com.oneroadtrip.matcher.util.Util;

public class DatabaseAccessor {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  DataSource dataSource;

  private static final String LOAD_GUIDE_RESERVE_DAYS = "SELECT guide_id, reserved_date FROM GuideReservations "
      + "WHERE (is_permanent = true OR insert_time >= ?) AND guide_id IN (%s)";

  public Map<Long, Set<Integer>> loadGuideToReserveDays(Collection<Long> guides,
      long cutoffTimestamp) throws OneRoadTripException {
    if (guides.size() == 0) {
      return Maps.newTreeMap();
    }
    // Create SQL
    String sql = buildGuideReserveSql(guides);

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

  private String buildGuideReserveSql(Collection<Long> subList) {
    Preconditions.checkArgument(subList.size() > 0);
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < subList.size() - 1; ++i) {
      builder.append("?, ");
    }
    String result = String.format(LOAD_GUIDE_RESERVE_DAYS, builder.append("?").toString());
    return result;
  }
//
//  private static final String INSERT_ONE_RESERVATION = "INSERT INTO GuideReservations (guide_id, reserved_date, is_permanent, insert_time) VALUES (?, ?, ?, ?)";
//
//  public void insertOneReservation(long guideId, int reservedDate, boolean isPermanent,
//      long timestamp) throws OneRoadTripException {
//    try (Connection conn = dataSource.getConnection()) {
//      try (PreparedStatement pStmt = conn.prepareStatement(INSERT_ONE_RESERVATION)) {
//        pStmt.setLong(1, guideId);
//        pStmt.setInt(2, reservedDate);
//        pStmt.setBoolean(3, isPermanent);
//        pStmt.setTimestamp(4, new Timestamp(timestamp));
//        Preconditions.checkArgument(pStmt.executeUpdate() == 1);
//      }
//    } catch (SQLException e) {
//      LOG.error("DB query errors in inserting reservation...", e);
//      throw new OneRoadTripException(Status.ERR_INSERT_GUIDE_RESERVATION, e);
//    }
//  }

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

  public Triplet<Long, Long, List<Long>> appendOrder(Itinerary itin) {
    try (Connection conn = dataSource.getConnection()) {
      // 1. disable conn auto-commit
      conn.setAutoCommit(false);

      // 2. add itinerary
      Long itineraryId = null;
      try (PreparedStatement pStmt = conn.prepareStatement(ADD_ITINERARY,
          Statement.RETURN_GENERATED_KEYS)) {
        pStmt.setString(1, TextFormat.printToUnicodeString(itin));
        itineraryId = SqlUtil.executeStatementAndReturnId(pStmt);
      }

      // 3. add reservations
      List<Long> reservedGuideIds = Lists.newArrayList();
      for (Pair<Long, Integer> guideAndDate : getGuideReservationMap(itin)) {
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
      conn.commit();
      return Triplet.with(orderId, itineraryId, reservedGuideIds);
      // 4. add order
    } catch (SQLException e) {
      LOG.error("Error in booking itinerary", e);
      return Triplet.with(-1L, -1L, Lists.newArrayList());
    }
  }

  public static List<Pair<Long, Integer>> getGuideReservationMap(Itinerary itin) {
    List<Pair<Long, Integer>> result = Lists.newArrayList();
    for (VisitCity visit : itin.getCityList()) {
      for (int i = 0; i < visit.getNumDays(); ++i) {
        int date = Util.advanceDays(visit.getStartDate(), i);
        if (itin.getChooseOneGuideSolution()) {
          result.add(Pair.with(ItineraryUtil.getGuideId(itin.getGuideForWholeTrip()), date));
        } else {
          result.add(Pair.with(ItineraryUtil.getGuideId(visit.getGuide(0)), date));
        }
      }
    }
    return result;
  }
}
