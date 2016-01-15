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
import com.oneroadtrip.matcher.resources.BookingResource;
import com.oneroadtrip.matcher.resources.CityResource;
import com.oneroadtrip.matcher.resources.GuideInfoResource;
import com.oneroadtrip.matcher.resources.GuidePlanResource;
import com.oneroadtrip.matcher.resources.LoginResource;
import com.oneroadtrip.matcher.resources.OrderResource;
import com.oneroadtrip.matcher.resources.PlanResource;
import com.oneroadtrip.matcher.resources.QuoteResource;
import com.oneroadtrip.matcher.resources.RefundResource;
import com.oneroadtrip.matcher.resources.SignupResource;
import com.oneroadtrip.matcher.resources.SpotInfoResource;
import com.oneroadtrip.matcher.resources.SpotResource;

public class OneRoadTripModule extends AbstractModule {
  private static final Logger LOG = LogManager.getLogger();
  private final OneRoadTripConfig config;

  public OneRoadTripModule(OneRoadTripConfig config) {
    this.config = config;
  }

  @Override
  protected void configure() {
    LOG.info("mysql connect: {}", config.connectionUri);

    bind(OneRoadTripConfig.class).toInstance(config);

    install(new AbstractModule() {
      // DB required info module
      @Override
      protected void configure() {
      }

      @Provides
      @Named(Constants.CONNECTION_URI)
      String provideConnectionUrl() throws IOException {
        return config.connectionUri;
      }

      @Provides
      @Named(Constants.PRELOADED_JDBC_DRIVER)
      @Singleton
      boolean providePreloadedJdbcDriver() {
        try {
          Class.forName(config.jdbcDriver);
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
          return false;
        }
        return true;
      }
    });
    install(new DbModule());
    install(new PreloadedDataModule());
    install(new ServingToolModule());

    // Bind application resources
    bind(BookingResource.class);
    bind(CityResource.class);
    bind(GuideInfoResource.class);
    bind(GuidePlanResource.class);
    bind(LoginResource.class);
    bind(OrderResource.class);
    bind(PlanResource.class);
    bind(QuoteResource.class);
    bind(RefundResource.class);
    bind(SignupResource.class);
    bind(SpotInfoResource.class);
    bind(SpotResource.class);
  }
}
