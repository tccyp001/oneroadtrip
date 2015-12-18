package com.oneroadtrip.matcher.handlers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.oneroadtrip.matcher.handlers.DbTestingModule.H2Info;
import com.oneroadtrip.matcher.util.ScriptRunner;

public abstract class DbTest {
  protected final String TESTDATA_PATH = "src/test/resources/testdata/";
  
  Injector injector;
  H2Info h2Info;

  @BeforeClass
  protected void setUp() throws IOException, SQLException, ClassNotFoundException {
    Class.forName("org.h2.Driver");

    injector = Guice.createInjector(new DbTestingModule());
    h2Info = injector.getInstance(H2Info.class);
    
    // Create tables in the database.
    Connection conn = h2Info.connection.get();
    ScriptRunner runner = new ScriptRunner(conn, true, false);
    runner.runScript(new BufferedReader(new FileReader("src/main/resources/create_tables.sql")));
  }

  @AfterClass
  protected void tearDown() throws IOException {
//    FileUtils.deleteDirectory(h2Info.testingDir);
  }
}
