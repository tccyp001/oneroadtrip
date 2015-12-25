package com.oneroadtrip.matcher.module;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.oneroadtrip.matcher.OneRoadTripConfig;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.data.PreloadedDataModule;
import com.oneroadtrip.matcher.resources.CityResource;
import com.oneroadtrip.matcher.resources.GuidePlanResource;
import com.oneroadtrip.matcher.resources.LoginResource;
import com.oneroadtrip.matcher.resources.SignupResource;
import com.oneroadtrip.matcher.resources.SpotResource;

public class OneRoadTripModule extends AbstractModule {
  private static final Logger LOG = LogManager.getLogger();
  private final OneRoadTripConfig config;

  public OneRoadTripModule(OneRoadTripConfig config) {
    this.config = config;
  }

  @Override
  protected void configure() {
    String connectionUrl = String.format(
        "jdbc:mysql://%s:%d/%s?characterEncoding=UTF-8&user=%s&password=%s", config.mysql_host,
        config.mysql_port, config.mysql_db, config.mysql_user, config.mysql_password);
    LOG.info("mysql connect: {}", connectionUrl);

    bind(OneRoadTripConfig.class).toInstance(config);

    install(new AbstractModule() {
      // DB required info module
      @Override
      protected void configure() {
      }

      @Provides
      @Named(Constants.CONNECTION_URI)
      String provideConnectionUrl() throws IOException {
        return connectionUrl;
      }

      @Provides
      @Named(Constants.PRELOADED_JDBC_DRIVER)
      @Singleton
      boolean providePreloadedJdbcDriver() {
        try {
          Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
          return false;
        }
        return true;
      }
    });
    install(new DbModule());
    install(new PreloadedDataModule());
    
    // Bind application resources
    bind(CityResource.class);
    bind(GuidePlanResource.class);
    bind(SpotResource.class);
    bind(LoginResource.class);
    bind(SignupResource.class);
  }
}
