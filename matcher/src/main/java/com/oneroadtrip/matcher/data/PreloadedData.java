package com.oneroadtrip.matcher.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.oneroadtrip.matcher.internal.CityConnectionInfo;
import com.oneroadtrip.matcher.util.Util;

// Thread-safe
public class PreloadedData {
  private static final Logger LOG = LogManager.getLogger();

  public static class Manager implements Provider<PreloadedData> {
    //
    // @Inject
    // OneRoadTripConfig config;

    PreloadedData data_ = null;

    synchronized PreloadedData atomicGetData() {
      return data_;
    }

    synchronized void atomicSetData(PreloadedData data) {
      this.data_ = data;
    }

    @Inject
    public Manager(Reloader reloader) {
      Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          // TODO(xfguo): confirm the replacement is atomic. (Do we need to make
          // sure this?)
          atomicSetData(reloader.reload());
        }
      }, 0, TimeUnit.SECONDS.toSeconds(5), TimeUnit.SECONDS);
    }

    @Override
    public PreloadedData get() {
      while (true) {
        PreloadedData data = atomicGetData();
        if (data != null) {
          return data;
        }
        // Sleep one second to check data again.
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (InterruptedException e) {
          // No interrupt in the sleep.
          LOG.error("Catch up an interrupted exception in sleep of waiting of preloaded data", e);
        }
      }
    }
  }

  public static class Reloader {
    @Inject
    DataSource dataSource;

    private static final String GET_ALL_CITY_DATA = "SELECT city_id, suggest FROM Cities";
    private static final String GET_ALL_CITY_CONNECTIONS = "SELECT from_city_id, to_city_id, distance, hours FROM CityConnections";

    private PreloadedData reload() {
      ImmutableList<Long> allCityIds = null;
      ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork = null;
      ImmutableMap<Long, Integer> suggestDaysForCities = null;

      try (Connection conn = dataSource.getConnection()) {
        try (PreparedStatement pStmt = conn.prepareStatement(GET_ALL_CITY_DATA);
            ResultSet rs = pStmt.executeQuery()) {
          ImmutableList.Builder<Long> b = ImmutableList.builder();
          ImmutableMap.Builder<Long, Integer> b2 = ImmutableMap.builder();
          while (rs.next()) {
            Long cityId = rs.getLong(1);
            Integer suggest = rs.getInt(2);
            b.add(cityId);
            b2.put(cityId, suggest);
          }
          allCityIds = b.build();
          suggestDaysForCities = b2.build();
        }

        try (PreparedStatement pStmt = conn.prepareStatement(GET_ALL_CITY_CONNECTIONS);
            ResultSet rs = pStmt.executeQuery()) {
          Map<Pair<Long, Long>, CityConnectionInfo> cn = Maps.newTreeMap();
          while (rs.next()) {
            long from = rs.getLong(1);
            long to = rs.getLong(2);
            CityConnectionInfo info = CityConnectionInfo.newBuilder().setDistance(rs.getInt(3))
                .setHours(rs.getInt(4)).build();
            // 无向图
            cn.put(Pair.with(from, to), info);
            cn.put(Pair.with(to, from), info);
          }
          cityNetwork = ImmutableMap.copyOf(Util.propagateNetwork(allCityIds, cn));
        }
      } catch (NoSuchElementException e) {
        LOG.error("No DB connection in preloading...");
      } catch (SQLException e1) {
        LOG.error("DB query errors in reloading city data...");
      }

      if (allCityIds == null || cityNetwork == null || suggestDaysForCities == null
          || allCityIds.size() == 0 || cityNetwork.size() == 0 || suggestDaysForCities.size() == 0) {
        LOG.info("Errors in reloading data");
        return null;
      }
      return new PreloadedData(allCityIds, cityNetwork, suggestDaysForCities);
    }

  }

  ImmutableList<Long> allCityIds;
  ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork;
  ImmutableMap<Long, Integer> suggestDaysForCities;

  PreloadedData() {
    allCityIds = ImmutableList.of();
    cityNetwork = ImmutableMap.of();
    suggestDaysForCities = ImmutableMap.of();
  }

  PreloadedData(ImmutableList<Long> allCityIds,
      ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork,
      ImmutableMap<Long, Integer> suggestDaysForCities) {
    this.allCityIds = allCityIds;
    this.cityNetwork = cityNetwork;
    this.suggestDaysForCities = suggestDaysForCities;
  }

  public ImmutableList<Long> getAllCityIds() {
    return allCityIds;
  }

  public ImmutableMap<Pair<Long, Long>, CityConnectionInfo> getCityNetwork() {
    return cityNetwork;
  }

  public ImmutableMap<Long, Integer> getSuggestDaysForCities() {
    return suggestDaysForCities;
  }
}
