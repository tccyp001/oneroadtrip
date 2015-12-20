package com.oneroadtrip.matcher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.oneroadtrip.matcher.data.PreloadedDataModule;
import com.oneroadtrip.matcher.resources.LoginResource;

// TODO(xfguo): All modules should be installed here, or have an abstract module in main().
public class TripModule extends AbstractModule {
  private static final Logger LOG = LogManager.getLogger();

  private final String connection_str;

  public TripModule(String connection_str) {
    this.connection_str = connection_str;
  }

  @Override
  protected void configure() {
    bind(LoginResource.class);
    
    // TODO(xfguo): Install DB module
    
    // install preloaded data module.
    install(new PreloadedDataModule());
  }

  @Provides
  @Nullable
  public Optional<Connection> provideConnection() {
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(connection_str);
    } catch (SQLException e) {
      LOG.info("xfguo: failed to connect the database, connection_str = {}", connection_str);
    }
    return Optional.ofNullable(conn);
  }

  // TODO(xfguo): clean up.
  @Provides
  @Named("content")
  public String provideContent() {
    return "xfguo test";
  }
}
