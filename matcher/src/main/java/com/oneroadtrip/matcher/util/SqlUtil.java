package com.oneroadtrip.matcher.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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

}
