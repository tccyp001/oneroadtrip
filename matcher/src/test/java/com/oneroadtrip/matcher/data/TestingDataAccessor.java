package com.oneroadtrip.matcher.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.StringJoiner;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.testng.Assert;

import com.google.common.base.Preconditions;
import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.util.ItineraryUtil;
import com.oneroadtrip.matcher.util.SqlUtil;

public class TestingDataAccessor {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  DataSource dataSource;

  private static final String SELECT_ITINERARY_BY_ID = "SELECT content FROM Itineraries "
      + "WHERE itinerary_id = (?)";
  private static final String SELECT_RESERVATION_BY_IDS = "SELECT "
      + "reservation_id, guide_id, itinerary_id, reserved_date, is_permanent, update_timestamp "
      + "FROM GuideReservations " + "WHERE update_timestamp >= (?) AND reservation_id IN ";
  private static final String SELECT_ORDER_BY_ID = "SELECT order_id, user_id, itinerary_id, cost_usd "
      + "FROM Orders WHERE order_id = ?";
  public void validateBooking(Itinerary itin, long orderId, long itinId, List<Long> reservationIds) {
    try (Connection conn = dataSource.getConnection()) {
      try (PreparedStatement pStmt = conn.prepareStatement(SELECT_ITINERARY_BY_ID)) {
        pStmt.setLong(1, itinId);
        try (ResultSet rs = pStmt.executeQuery()) {
          Itinerary.Builder builder = Itinerary.newBuilder();
          Preconditions.checkArgument(rs.next());
          String content = rs.getString(1);
          TextFormat.merge(content, builder);
          Preconditions.checkArgument(!rs.next());
          
          Assert.assertEquals(builder.build(), itin);
        }
      }

      StringJoiner joiner = new StringJoiner(", ", "(", ")");
      for (int i = 0; i < reservationIds.size(); ++i) {
        joiner.add("?");
      }
      try (PreparedStatement pStmt = conn.prepareStatement(SELECT_RESERVATION_BY_IDS + joiner.toString())) {
        pStmt.setTimestamp(1, SqlUtil.getTimestampToNow(-10));
        int index = 2;
        for (long reservationId : reservationIds) {
          pStmt.setLong(index++, reservationId);
        }
        try (ResultSet rs = pStmt.executeQuery()) {
          List<Pair<Long, Integer>> expectedGuideAndDates = DatabaseAccessor.getGuideReservationMap(itin);
          for (int i = 0; i < reservationIds.size(); ++i) {
            Preconditions.checkArgument(rs.next());
            long actualId = rs.getLong(1);
            long guideId = rs.getLong(2);
            int date = rs.getInt(4);
            boolean isPermanent = rs.getBoolean(5);
            Timestamp ts = rs.getTimestamp(6);
            LOG.debug("Info can be validated is dumpped here: "
                + "reservationId = {}, isPermanent = {}, ts = {}", actualId, isPermanent, ts);
            Assert.assertFalse(isPermanent);
            Assert.assertEquals(rs.getLong(3), itinId);
            Assert.assertEquals(Pair.with(guideId, date), expectedGuideAndDates.get(i));
          }
          Preconditions.checkArgument(!rs.next());
        }
      }
      try (PreparedStatement pStmt = conn.prepareStatement(SELECT_ORDER_BY_ID)) {
        pStmt.setLong(1, orderId);
        try (ResultSet rs = pStmt.executeQuery()) {
          Preconditions.checkArgument(rs.next());
          Assert.assertEquals(rs.getLong(1), orderId);
          Assert.assertEquals(rs.getLong(2), itin.getUserId());
          Assert.assertEquals(rs.getLong(3), itinId);
          Assert.assertEquals(rs.getFloat(4), ItineraryUtil.getCostUsd(itin));
          Preconditions.checkArgument(!rs.next());
        }
      }
    } catch (ParseException e) {
      LOG.error("Errors in parsing itinerary content", e);
    } catch (SQLException e) {
      LOG.error("Error in getting itinerary content", e);
    }
  }
}
