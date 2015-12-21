package com.oneroadtrip.matcher;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.data.PreloadedDataModule;
import com.oneroadtrip.matcher.module.DbModule;
import com.oneroadtrip.matcher.module.HandlerModule;
import com.oneroadtrip.matcher.resources.LoginResource;

// TODO(xfguo): All modules should be installed here, or have an abstract module in main().
public class TripModule extends AbstractModule {
  private final String connectionUrl;

  public TripModule(String connectionUrl) {
    this.connectionUrl = connectionUrl;
  }

  @Override
  protected void configure() {
    bind(LoginResource.class);

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
    install(new HandlerModule());
  }

  // TODO(xfguo): clean up.
  @Provides
  @Named("content")
  public String provideContent() {
    return "xfguo test";
  }
}
