package com.oneroadtrip.matcher.module;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.oneroadtrip.matcher.common.Constants;

public class DbModule extends AbstractModule {
  private static final Logger LOG = LogManager.getLogger();

  @Override
  public void configure() {
  }

  @Provides
  @Singleton
  DataSource providePoolDataSource(@Named(Constants.CONNECTION_URI) String connectionUri,
      @Named(Constants.PRELOADED_JDBC_DRIVER) boolean preloaded) {
    LOG.info("Connection uri: {}, jdbc is preloaded: {}", connectionUri, preloaded);
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
