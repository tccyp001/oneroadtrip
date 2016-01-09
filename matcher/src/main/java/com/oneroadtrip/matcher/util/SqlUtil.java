package com.oneroadtrip.matcher.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import com.google.common.base.Preconditions;

public class SqlUtil {

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
}
