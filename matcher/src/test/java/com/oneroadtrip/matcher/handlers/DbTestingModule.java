package com.oneroadtrip.matcher.handlers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class DbTestingModule extends AbstractModule {
  private static final Logger LOG = LogManager.getLogger();
  private static final String H2_TESTING_DIRECTORY = "h2_testing_directory";

  public static class H2Info {
    @Inject
    @Named(H2_TESTING_DIRECTORY)
    File testingDir;
    
    @Inject
    Optional<Connection> connection;
  }

  @Override
  protected void configure() {
    bind(H2Info.class);

    bind(CityRequestHandler.class);
  }

  @Provides
  @Named(H2_TESTING_DIRECTORY)
  @Singleton
  public File provideTestingDirectory() {
    return Files.createTempDir();
  }

  @Provides
  public Optional<Connection> provideTestingConnection(
      @Named(H2_TESTING_DIRECTORY) File testingDir) throws IOException, SQLException {
    String h2ConnStr = String.format("jdbc:h2:mem:%s;MODE=MySQL;IGNORECASE=TRUE",
        testingDir.getCanonicalPath());
    LOG.info("h2ConnStr: {}", h2ConnStr);
    return Optional.ofNullable(DriverManager.getConnection(h2ConnStr));
  }
}
