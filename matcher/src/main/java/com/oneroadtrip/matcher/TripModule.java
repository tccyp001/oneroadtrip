package com.oneroadtrip.matcher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.oneroadtrip.matcher.resources.LoginResource;

public class TripModule extends AbstractModule {
  private static final Logger LOG = LogManager.getLogger();

  @Override
  protected void configure() {
    bind(LoginResource.class);
  }

  @Provides
  public Connection provideConnection() {
    Connection conn = null;
    try {
      conn = DriverManager
          .getConnection("jdbc:mysql://tech-meetup.com:4407/oneroadtrip"
              + "?characterEncoding=UTF-8&user=oneroadtrip&password=oneroadtrip123");
    } catch (SQLException e) {
      LOG.info("xfguo: failed to connect the database");
      return null;
    }
    return conn;
  }

  @Provides
  @Named("content")
  public String provideContent() {
    return "xfguo test";
  }
}
