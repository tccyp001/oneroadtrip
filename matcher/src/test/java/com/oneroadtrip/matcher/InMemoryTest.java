package com.oneroadtrip.matcher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class InMemoryTest {
  private static final Logger LOG = LogManager.getLogger();

  @Test
  public void test() throws Exception {

    Class.forName("org.h2.Driver");

    Connection conn = DriverManager
        .getConnection("jdbc:h2:mem:test;MODE=MySQL;IGNORECASE=TRUE;INIT=RUNSCRIPT FROM 'src/test/resources/test.sql'");

    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT ID FROM TestT");
    
    int i = 0;
    while (rs.next()) {
      LOG.info("xfguo: row {}, rs {}", i, rs);
    }
    
    rs.close();
    stmt.close();
    conn.close();

    LOG.info("xfguo: done");
  }
}
