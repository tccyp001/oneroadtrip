package com.oneroadtrip.matcher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.oneroadtrip.matcher.util.ScriptRunner;

public class InMemoryTest {
  private static final Logger LOG = LogManager.getLogger();

  @Test
  public void test() throws Exception {

    Class.forName("org.h2.Driver");

    // Connection conn = DriverManager
    // .getConnection("jdbc:h2:mem:test;MODE=MySQL;IGNORECASE=TRUE;INIT=RUNSCRIPT FROM 'src/main/resources/create_database.sql'");
    Connection conn = DriverManager.getConnection("jdbc:h2:mem:test;MODE=MySQL;IGNORECASE=TRUE");

    // TODO(lamuguo): Add run sql script.
    ScriptRunner runner = new ScriptRunner(conn, true, false);
    runner.runScript(new BufferedReader(new FileReader("src/main/resources/create_tables.sql")));
    runner.runScript(new BufferedReader(new FileReader("src/test/resources/test.sql")));
    LOG.info("xfguo: testing database is created");

    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT user_id FROM Users");

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
