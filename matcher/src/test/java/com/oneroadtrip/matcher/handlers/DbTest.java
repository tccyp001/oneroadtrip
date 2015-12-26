package com.oneroadtrip.matcher.handlers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.oneroadtrip.matcher.OneRoadTripConfig;
import com.oneroadtrip.matcher.data.PreloadedDataModule;
import com.oneroadtrip.matcher.handlers.DbTestingModule.H2Info;
import com.oneroadtrip.matcher.module.DbModule;
import com.oneroadtrip.matcher.util.ScriptRunner;

public abstract class DbTest {
  protected final String TESTDATA_PATH = "src/test/resources/testdata/";

  protected Injector injector;
  H2Info h2Info;

  @BeforeClass
  protected void setUp() throws IOException, SQLException, ClassNotFoundException {

    injector = Guice.createInjector(new AbstractModule() {
      // Test config module
      @Override
      protected void configure() {
        OneRoadTripConfig config = new OneRoadTripConfig();
        // Same as default, can adjust is necessary.
        config.preload_period_in_seconds = TimeUnit.MINUTES.toSeconds(5);
        bind(OneRoadTripConfig.class).toInstance(config);

        install(new DbTestingModule());
        install(new DbModule());
        install(new PreloadedDataModule());
      }
    });
    h2Info = injector.getInstance(H2Info.class);

    // Create tables in the database.
    Connection conn = h2Info.connection.get();
    ScriptRunner runner = new ScriptRunner(conn, true, false);
    runner.runScript(new BufferedReader(new FileReader("src/main/resources/create_tables.sql")));
  }

  @AfterClass
  protected void tearDown() throws IOException {
     FileUtils.deleteDirectory(h2Info.testingDir);
  }
}
