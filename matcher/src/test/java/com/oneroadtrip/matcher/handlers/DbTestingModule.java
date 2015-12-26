package com.oneroadtrip.matcher.handlers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.oneroadtrip.matcher.common.Constants;

public class DbTestingModule extends AbstractModule {
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

  }

  @Provides
  @Named(H2_TESTING_DIRECTORY)
  @Singleton
  public File provideTestingDirectory() {
    return Files.createTempDir();
  }

  @Provides
  @Named(Constants.CONNECTION_URI)
  String provideConnectionUrl(@Named(H2_TESTING_DIRECTORY) File testingDir) throws IOException {
    return String
        .format("jdbc:h2:mem:%s;MODE=MySQL;IGNORECASE=TRUE", testingDir.getCanonicalPath());
  }

  @Provides
  @Named(Constants.PRELOADED_JDBC_DRIVER)
  @Singleton
  boolean providePreloadedJdbcDriver() {
    try {
      Class.forName("org.h2.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return true;
  }
}
