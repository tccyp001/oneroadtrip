package com.oneroadtrip.matcher.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.Status;

public class SqlUtil {
  private static final Logger LOG = LogManager.getLogger();

  @FunctionalInterface
  public static interface DatabaseFunction<INPUT_TYPE, RESULT_TYPE> {
    RESULT_TYPE apply(INPUT_TYPE input) throws SQLException;
  }

  public static PreparedStatement addPreparedStatement(Connection conn, String str,
      List<PreparedStatement> statements) throws SQLException {
    PreparedStatement stmt = conn.prepareStatement(str);
    statements.add(stmt);
    return stmt;
  }
  
  public static ResultSet addResultSet(PreparedStatement pStmt, List<ResultSet> resultSets)
      throws SQLException {
    ResultSet rs = pStmt.executeQuery();
    resultSets.add(rs);
    return rs;
  }

  public static long executeStatementAndReturnId(PreparedStatement pStmt) throws SQLException {
    int affectedRows = pStmt.executeUpdate();
    if (affectedRows != 1) {
      throw new SQLException("Error in inserting itinerary");
    }
    ResultSet rs = pStmt.getGeneratedKeys();
    Preconditions.checkArgument(rs.next());
    long result = rs.getLong(1);
    Preconditions.checkArgument(!rs.next());
    return result;
  }
  
  public static Timestamp getCurrentJavaSqlTimestamp() {
    java.util.Date date = new java.util.Date();
    return new java.sql.Timestamp(date.getTime());
  }

  public static Timestamp getTimestampToNow(int seconds) {
    Timestamp now = getCurrentJavaSqlTimestamp();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(now.getTime());
    cal.add(Calendar.SECOND, seconds);
    return new Timestamp(cal.getTime().getTime());
  }
  
  public static <R> R executeTransaction(DataSource dataSource, DatabaseFunction<Connection, R> callback)
      throws OneRoadTripException {
    try (Connection conn = dataSource.getConnection()) {
      boolean originAutoCommit = conn.getAutoCommit();
      try {
        // 1. disable conn auto-commit
        conn.setAutoCommit(false);
        R result = callback.apply(conn);
        conn.commit();
        return result;
      } catch (SQLException e) {
        conn.rollback();
        LOG.info("SQL error", e);
        throw new OneRoadTripException(Status.ERROR_IN_SQL, e);
      } finally {
        conn.setAutoCommit(originAutoCommit);
      }
    } catch (SQLException e) {
      // TODO(xfguo): Create ERROR_IN_CONNECTING
      throw new OneRoadTripException(Status.ERROR_IN_SQL, e);
    }
  }

}
