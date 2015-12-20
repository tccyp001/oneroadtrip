package com.oneroadtrip.matcher.handlers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.oneroadtrip.matcher.common.Constants;

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
    bind(PlanRequestHandler.class);
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

  // @Provides
  // @Singleton
  // DataSource provideDataSource(@Named(Constants.CONNECTION_URI) String
  // connectionUri)
  // throws ClassNotFoundException {
  // LOG.info("h2ConnStr: {}", connectionUri);
  // BasicDataSource ds = new BasicDataSource();
  // ds.setDriverClassName("org.h2.Driver");
  // ds.setUrl(connectionUri);
  // return ds;
  // }

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

  @Provides
  @Singleton
  DataSource providePoolDataSource(@Named(Constants.CONNECTION_URI) String connectionUri,
      @Named(Constants.PRELOADED_JDBC_DRIVER) boolean preloaded) {
    LOG.info("h2 connection uri: {}, jdbc is preloaded: {}", connectionUri, preloaded);
    if (!preloaded) {
      return null;
    }

    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectionUri, null);
    PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
        connectionFactory, null);
    ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(
        poolableConnectionFactory);
    poolableConnectionFactory.setPool(connectionPool);
    PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>(connectionPool);
    return dataSource;
  }

  @Provides
  public Optional<Connection> provideTestingConnection(DataSource dataSource) throws SQLException {
    return Optional.ofNullable(dataSource.getConnection());
  }
}
