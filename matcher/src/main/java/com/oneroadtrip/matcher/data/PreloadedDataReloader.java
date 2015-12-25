package com.oneroadtrip.matcher.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oneroadtrip.matcher.VisitSpot;
import com.oneroadtrip.matcher.internal.CityConnectionInfo;
import com.oneroadtrip.matcher.util.Util;

public class PreloadedDataReloader {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  DataSource dataSource;

  private static final String GET_ALL_CITY_DATA = "SELECT city_id, suggest FROM Cities";
  private static final String GET_ALL_CITY_CONNECTIONS = "SELECT from_city_id, to_city_id, distance, hours FROM CityConnections";
  private static final String GET_ALL_SPOTS = "SELECT city_id, spot_id, name, hours, score, interests "
      + "FROM Spots " + "ORDER BY city_id";
  private static final String GET_ALL_INTERSTS = "SELECT interest_id, interest_name FROM Interests";

  PreloadedData reload() {
    LOG.info("start to reload database...");
    ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork = null;
    ImmutableMap<Long, Integer> suggestDaysForCities = null;
    Map<String, Long> interestNameToId = Maps.newTreeMap();
    // TODO(xfguo): (P1) Use ImmutableMap.Builder instead.
    Map<Long, SpotPlanner> cityIdToSpotPlanner = null;

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

        for (Long cityId : suggestDaysForCities.keySet()) {
          cn.put(Pair.with(cityId, cityId), Util.createConnectionInfo(0, 0));
        }
        cityNetwork = ImmutableMap.copyOf(Util.propagateNetwork(suggestDaysForCities.keySet(), cn));
      }

      try (PreparedStatement pStmt = conn.prepareStatement(GET_ALL_INTERSTS);
          ResultSet rs = pStmt.executeQuery()) {
        while (rs.next()) {
          interestNameToId.put(rs.getString(2), rs.getLong(1));
        }
      }

      try (PreparedStatement pStmt = conn.prepareStatement(GET_ALL_SPOTS);
          ResultSet rs = pStmt.executeQuery()) {
        cityIdToSpotPlanner = Maps.newTreeMap();
        long currentCityId = -1;
        Map<String, Long> spotNameToId = null;
        Map<Long, VisitSpot> spotIdToData = null;
        Map<Long, Float> spotToScore = null;
        Map<Long, Set<Long>> interestToSpots = null;
        while (rs.next()) {
          Long cityId = rs.getLong(1);
          Long spotId = rs.getLong(2);
          String name = rs.getString(3);
          Integer hours = rs.getInt(4);
          Float score = rs.getFloat(5);
          String interests = rs.getString(6);
          if (cityId != currentCityId) {
            buildSpotPlanner(cityIdToSpotPlanner, currentCityId, spotNameToId, spotIdToData,
                spotToScore, interestToSpots);
            currentCityId = cityId;
            spotNameToId = Maps.newTreeMap();
            spotIdToData = Maps.newTreeMap();
            spotToScore = Maps.newTreeMap();
            interestToSpots = Maps.newTreeMap();
          }
          spotNameToId.put(name, spotId);
          spotIdToData.put(spotId, Util.createVisitSpot(hours, spotId, name, null));
          spotToScore.put(spotId, score);
          for (Long interestId : Util.getInterestIds(interests, interestNameToId)) {
            if (!interestToSpots.containsKey(interestId)) {
              interestToSpots.put(interestId, Sets.newTreeSet());
            }
            interestToSpots.get(interestId).add(spotId);
          }
        }
        buildSpotPlanner(cityIdToSpotPlanner, currentCityId, spotNameToId, spotIdToData,
            spotToScore, interestToSpots);
      }
    } catch (NoSuchElementException e) {
      LOG.error("No DB connection in preloading...");
    } catch (SQLException e1) {
      LOG.error("DB query errors in reloading city data...", e1);
    }

    ImmutableMap.Builder<Long, ImmutableSet<Long>> cityToGuides = ImmutableMap.builder();
    ImmutableMap.Builder<Long, ImmutableSet<Long>> guideToInterests = ImmutableMap.builder();
    ImmutableMap.Builder<Long, Float> guideToScore = ImmutableMap.builder();
    reloadGuideData(interestNameToId, cityToGuides, guideToInterests, guideToScore);

    if (cityNetwork == null || suggestDaysForCities == null || cityNetwork.size() == 0
        || suggestDaysForCities.size() == 0) {
      LOG.info("Errors in reloading city data");
    }
    LOG.info("Database is reloaded");
    return new PreloadedData(cityNetwork, suggestDaysForCities,
        ImmutableMap.copyOf(cityIdToSpotPlanner), ImmutableMap.copyOf(interestNameToId),
        cityToGuides.build(), guideToInterests.build(), guideToScore.build());
  }

  private static final String LOAD_CITY_TO_GUIDES = "SELECT city_id, guide_id FROM GuideCities ORDER BY city_id";
  private static final String LOAD_GUIDE_DATA = "SELECT guide_id, score, interests FROM Guides";

  private void reloadGuideData(Map<String, Long> interestNameToId,
      Builder<Long, ImmutableSet<Long>> cityToGuides,
      Builder<Long, ImmutableSet<Long>> guideToInterests, Builder<Long, Float> guideToScore) {
    Preconditions.checkNotNull(cityToGuides);
    Preconditions.checkNotNull(guideToInterests);
    Preconditions.checkNotNull(guideToScore);
    try (Connection conn = dataSource.getConnection()) {
      long currentCityId = -1;
      ImmutableSet.Builder<Long> guides = ImmutableSet.builder();
      try (PreparedStatement pStmt = conn.prepareStatement(LOAD_CITY_TO_GUIDES);
          ResultSet rs = pStmt.executeQuery()) {
        while (rs.next()) {
          long cityId = rs.getLong(1);
          long guideId = rs.getLong(2);
          if (cityId != currentCityId) {
            if (currentCityId != -1) {
              cityToGuides.put(currentCityId, guides.build());
            }
            currentCityId = cityId;
            guides = ImmutableSet.builder();
          }
          guides.add(guideId);
        }
        if (currentCityId != -1) {
          cityToGuides.put(currentCityId, guides.build());
        }
      }
      try (PreparedStatement pStmt = conn.prepareStatement(LOAD_GUIDE_DATA);
          ResultSet rs = pStmt.executeQuery()) {
        while (rs.next()) {
          long guide_id = rs.getLong(1);
          float score = rs.getFloat(2);
          List<Long> interests = Util.getInterestIds(rs.getString(3), interestNameToId);
          guideToScore.put(guide_id, score);
          guideToInterests.put(guide_id, ImmutableSet.copyOf(interests));
        }
      }
      LOG.info("guide data reloaded");
    } catch (SQLException e) {
      LOG.error("DB error in reload guide related data", e);
    }
  }

  private void buildSpotPlanner(Map<Long, SpotPlanner> cityIdToSpotPlanner, long cityId,
      Map<String, Long> spotNameToId, Map<Long, VisitSpot> spotIdToData,
      Map<Long, Float> spotToScore, Map<Long, Set<Long>> interestToSpots) {
    if (cityId == -1) {
      return;
    }
    cityIdToSpotPlanner.put(cityId,
        new SpotPlanner(ImmutableMap.copyOf(spotNameToId), ImmutableMap.copyOf(spotIdToData),
            ImmutableMap.copyOf(interestToSpots), ImmutableMap.copyOf(spotToScore)));
  }
}
