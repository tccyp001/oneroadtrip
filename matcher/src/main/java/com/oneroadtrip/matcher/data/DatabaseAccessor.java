package com.oneroadtrip.matcher.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.sql.DataSource;

import jersey.repackaged.com.google.common.collect.Maps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.oneroadtrip.matcher.Status;
import com.oneroadtrip.matcher.common.OneRoadTripException;

public class DatabaseAccessor {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  DataSource dataSource;

  private static final String LOAD_GUIDE_RESERVE_DAYS =
      "SELECT guide_id, reserved_date FROM GuideReservations "
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
        // TODO(xfguo): Don't use System.currentTimeMillis() in util.
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

  private static final String INSERT_ONE_RESERVATION = "INSERT INTO GuideReservations (guide_id, reserved_date, is_permanent, insert_time) VALUES (?, ?, ?, ?)";

  public void insertOneReservation(long guideId, int reservedDate, boolean isPermanent,
      long timestamp) throws OneRoadTripException {
    try (Connection conn = dataSource.getConnection()) {
      try (PreparedStatement pStmt = conn.prepareStatement(INSERT_ONE_RESERVATION)) {
        pStmt.setLong(1, guideId);
        pStmt.setInt(2, reservedDate);
        pStmt.setBoolean(3, isPermanent);
        pStmt.setTimestamp(4, new Timestamp(timestamp));
        Preconditions.checkArgument(pStmt.executeUpdate() == 1);
      }
    } catch (SQLException e) {
      LOG.error("DB query errors in inserting reservation...", e);
      throw new OneRoadTripException(Status.ERR_INSERT_GUIDE_RESERVATION, e);
    }
  }
}
