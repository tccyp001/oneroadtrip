package com.oneroadtrip.matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InMemoryTest {
  private static final Logger LOG = LogManager.getLogger();
  //
  // @Test
  // public void test() throws Exception {
  //
  // Class.forName("org.h2.Driver");
  //
  // // Connection conn = DriverManager
  // //
  // .getConnection("jdbc:h2:mem:test;MODE=MySQL;IGNORECASE=TRUE;INIT=RUNSCRIPT
  // FROM 'src/main/resources/create_database.sql'");
  // Connection conn =
  // DriverManager.getConnection("jdbc:h2:mem:test;MODE=MySQL;IGNORECASE=TRUE");
  //
  // ScriptRunner runner = new ScriptRunner(conn, true, false);
  // runner.runScript(new BufferedReader(new
  // FileReader("src/main/resources/create_tables.sql")));
  // runner.runScript(new BufferedReader(new
  // FileReader("src/test/resources/test.sql")));
  // LOG.info("xfguo: testing database is created");
  //
  // Statement stmt = conn.createStatement();
  // ResultSet rs = stmt.executeQuery("SELECT user_id FROM Users");
  //
  // int i = 0;
  // while (rs.next()) {
  // LOG.info("xfguo: row {}, rs {}", i, rs);
  // }
  //
  // rs.close();
  // stmt.close();
  // conn.close();
  //
  // LOG.info("xfguo: done");
  // }
//
//  @Test
//  public void anotherTest() {
//    BasicDataSource ds = new BasicDataSource();
//    ds.setDriverClassName("com.mysql.jdbc.Driver");
//    ds.setUrl("jdbc:mysql://127.0.0.1:42216/oneroadtrip"
//        + "?characterEncoding=UTF-8&user=root&password=password");
//    LOG.info("xfguo: 1");
//    try (Connection conn = ds.getConnection()) {
//      LOG.info("xfguo: 2");
//      ScriptRunner runner = new ScriptRunner(conn, true, false);
//      LOG.info("xfguo: 3");
//      runner.runScript(new BufferedReader(new InputStreamReader(
//          new FileInputStream("src/main/resources/create_tables.sql"), Charsets.UTF_8)));
//      LOG.info("xfguo: 4");
//
//    } catch (FileNotFoundException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (IOException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (SQLException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//  }
}
