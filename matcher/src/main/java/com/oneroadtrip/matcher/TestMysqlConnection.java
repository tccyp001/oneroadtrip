package com.oneroadtrip.matcher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO(xfguo): Move the class to tools directory.
public class TestMysqlConnection {
  private static final Logger LOG = LogManager.getLogger();

  public static void main(String[] args) throws Exception {
    
    try {
      // Notice, do not import com.mysql.jdbc.*
      // or you will have problems!

      // The newInstance() call is a work around for some
      // broken Java implementations

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    } catch (Exception ex) {
      // handle the error
      LOG.error("Failed to load the driver");
    }

    Connection conn = DriverManager
        .getConnection("jdbc:mysql://tech-meetup.com:4407/oneroadtrip?user=oneroadtrip&password=oneroadtrip123");
    
    if (conn == null) {
      LOG.info("xfguo: null mysql connection");
      return;
    }
    
    LOG.info("xfguo: start to query");
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT user_id FROM Users");
    while (rs.next()) {
      LOG.info("xfguo: row {}", rs);
    }
    rs.close();
    stmt.close();
    conn.close();
    LOG.info("xfguo: done");
  }

}
