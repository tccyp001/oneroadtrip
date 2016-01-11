package com.oneroadtrip.matcher.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringJoiner;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oneroadtrip.matcher.proto.CityInfo;
import com.oneroadtrip.matcher.proto.GuideInfo;
import com.oneroadtrip.matcher.proto.SpotInfo;
import com.oneroadtrip.matcher.proto.VisitSpot;
import com.oneroadtrip.matcher.proto.internal.CityConnectionInfo;
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

    ImmutableMap.Builder<Long, SpotInfo> spotIdToInfoBuilder = ImmutableMap.builder();
    reloadSpotInfo(spotIdToInfoBuilder);
    ImmutableMap<Long, SpotInfo> spotIdToInfo = spotIdToInfoBuilder.build();

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
        Set<Long> cities = Sets.newTreeSet();
        while (rs.next()) {
          long from = rs.getLong(1);
          long to = rs.getLong(2);
          CityConnectionInfo info = CityConnectionInfo.newBuilder().setDistance(rs.getInt(3))
              .setHours(rs.getInt(4)).build();
          // 无向图
          cn.put(Pair.with(from, to), info);
          cn.put(Pair.with(to, from), info);
          cities.add(from);
          cities.add(to);
        }

        for (Long cityId : suggestDaysForCities.keySet()) {
          cn.put(Pair.with(cityId, cityId), Util.createConnectionInfo(0, 0));
        }
        cityNetwork = ImmutableMap.copyOf(Util.propagateNetwork(cities, cn));
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
            buildSpotPlanner(cityIdToSpotPlanner, currentCityId, spotIdToData, spotToScore,
                interestToSpots);
            currentCityId = cityId;
            spotNameToId = Maps.newTreeMap();
            spotIdToData = Maps.newTreeMap();
            spotToScore = Maps.newTreeMap();
            interestToSpots = Maps.newTreeMap();
          }
          SpotInfo info = spotIdToInfo.get(spotId);
          if (info == null) {
            LOG.error("Error in loading spot data for id {}", spotId);
            continue;
          }
          spotNameToId.put(name, spotId);
          spotIdToData.put(spotId, Util.createVisitSpot(hours, info, null));
          spotToScore.put(spotId, score);
          for (Long interestId : Util.getInterestIds(interests, interestNameToId)) {
            if (!interestToSpots.containsKey(interestId)) {
              interestToSpots.put(interestId, Sets.newTreeSet());
            }
            interestToSpots.get(interestId).add(spotId);
          }
        }
        buildSpotPlanner(cityIdToSpotPlanner, currentCityId, spotIdToData, spotToScore,
            interestToSpots);
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

    ImmutableMap.Builder<Long, CityInfo> cityIdToInfoBuilder = ImmutableMap.builder();
    reloadCityIdToInfo(cityIdToInfoBuilder);
    ImmutableMap<Long, CityInfo> cityIdToInfo = cityIdToInfoBuilder.build();

    ImmutableMap.Builder<Long, GuideInfo> guideIdToInfo = ImmutableMap.builder();
    reloadGuideInfo(cityIdToInfo, guideIdToInfo);

    if (cityNetwork == null || suggestDaysForCities == null || cityNetwork.size() == 0
        || suggestDaysForCities.size() == 0) {
      LOG.info("Errors in reloading city data");
    }
    LOG.info("Database is reloaded");
    return new PreloadedData(cityNetwork, suggestDaysForCities,
        ImmutableMap.copyOf(cityIdToSpotPlanner), ImmutableMap.copyOf(interestNameToId),
        cityToGuides.build(), guideToInterests.build(), guideToScore.build(), cityIdToInfo,
        guideIdToInfo.build(), spotIdToInfo);
  }

  private static final String LOAD_CITY_TO_GUIDES = "SELECT city_id, guide_id FROM GuideCities ORDER BY city_id";
  private static final String LOAD_GUIDE_DATA = "SELECT guide_id, score, interests FROM Guides";

  private void reloadGuideData(Map<String, Long> interestNameToId,
      ImmutableMap.Builder<Long, ImmutableSet<Long>> cityToGuides,
      ImmutableMap.Builder<Long, ImmutableSet<Long>> guideToInterests,
      ImmutableMap.Builder<Long, Float> guideToScore) {
    Preconditions.checkNotNull(cityToGuides);
    Preconditions.checkNotNull(guideToInterests);
    Preconditions.checkNotNull(guideToScore);
    try (Connection conn = dataSource.getConnection()) {
      long currentCityId = -1;
      ImmutableSet.Builder<Long> guides = ImmutableSet.builder();
      try (PreparedStatement pStmt = conn.prepareStatement(LOAD_CITY_TO_GUIDES);
          ResultSet rs = pStmt.executeQuery()) {
        while (rs.next()) {
          // TODO(xfguo): Add incorrect data protection.
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
      Map<Long, VisitSpot> spotIdToData, Map<Long, Float> spotToScore,
      Map<Long, Set<Long>> interestToSpots) {
    if (cityId == -1) {
      return;
    }
    cityIdToSpotPlanner.put(cityId,
        new SpotPlanner(ImmutableMap.copyOf(spotIdToData), ImmutableMap.copyOf(interestToSpots),
            ImmutableMap.copyOf(spotToScore)));
  }

  private static final String QUERY_CITIES = "SELECT city_id, city_name, cn_name, suggest, min FROM Cities";
  private static final String QUERY_CITY_ALIASES = "SELECT city_id, alias FROM CityAliases";

  private void reloadCityIdToInfo(ImmutableMap.Builder<Long, CityInfo> builder) {
    Preconditions.checkNotNull(builder);
    Map<Long, CityInfo.Builder> data = Maps.newHashMap();
    try (Connection conn = dataSource.getConnection()) {
      try (PreparedStatement pStmt = conn.prepareStatement(QUERY_CITIES);
          ResultSet rs = pStmt.executeQuery()) {
        while (rs.next()) {
          CityInfo.Builder cityBuilder = CityInfo.newBuilder();
          long cityId = rs.getLong(1);
          cityBuilder.setCityId(rs.getLong(1));
          cityBuilder.setName(rs.getString(2));
          cityBuilder.setCnName(rs.getString(3));
          cityBuilder.setSuggest(rs.getInt(4));
          cityBuilder.setMin(rs.getInt(5));
          data.put(cityId, cityBuilder);
        }
      }
      try (PreparedStatement pStmt = conn.prepareStatement(QUERY_CITY_ALIASES);
          ResultSet rs = pStmt.executeQuery()) {
        while (rs.next()) {
          long cityId = rs.getLong(1);
          CityInfo.Builder sub = data.get(cityId);
          if (sub == null) {
            continue;
          }
          sub.addAlias(rs.getString(2));
        }
      }
      for (Map.Entry<Long, CityInfo.Builder> e : data.entrySet()) {
        builder.put(e.getKey(), e.getValue().build());
      }
    } catch (SQLException e) {
      LOG.error("DB error in reload city related data", e);
    }
  }

  private static final String QUERY_GUIDE_INFO =
      "SELECT guide_id, user_name, description, max_persons, has_car, score, host_city_id, interests, phone "
      + "FROM Guides g INNER JOIN Users u ON (g.user_id = u.user_id)";
  private static final String QUERY_GUIDE_CITY_INFO =
      "SELECT guide_id, city_id FROM GuideCities";

  private void reloadGuideInfo(ImmutableMap<Long, CityInfo> cityIdToInfo,
      ImmutableMap.Builder<Long, GuideInfo> builder) {
    Preconditions.checkNotNull(builder);
    try (Connection conn = dataSource.getConnection()) {
      Map<Long, Set<Long>> guideToCityIds = Maps.newTreeMap();
      try (PreparedStatement pStmt = conn.prepareStatement(QUERY_GUIDE_CITY_INFO);
          ResultSet rs = pStmt.executeQuery()) {
        while (rs.next()) {
          try {
            long guideId = rs.getLong(1);
            long cityId = rs.getLong(2);
            if (!guideToCityIds.containsKey(guideId)) {
              guideToCityIds.put(guideId, Sets.newTreeSet());
            }
            guideToCityIds.get(guideId).add(cityId);
          } catch (NullPointerException e) {
            StringJoiner joiner = new StringJoiner(",");
            for (int i = 0; i < 2; ++i) {
              joiner.add(rs.getString(i + 1));
            }
            LOG.info("xfguo: guide error {}?", joiner.toString());
          }
        }
      }
      try (PreparedStatement pStmt = conn.prepareStatement(QUERY_GUIDE_INFO);
          ResultSet rs = pStmt.executeQuery()) {
        while (rs.next()) {
          try {
            long guideId = rs.getLong(1);
            String name = rs.getString(2);
            String desc = rs.getString(3);
            int maxPeople = rs.getInt(4);
            boolean hasCar = rs.getBoolean(5);
            float score = rs.getFloat(6);
            CityInfo hostCityInfo = cityIdToInfo.get(rs.getLong(7));
            List<String> topics = Util.splitString(rs.getString(8));
            long phone = rs.getLong(9);
            Set<Long> cities = guideToCityIds.get(guideId);
            List<CityInfo> coverCities = Lists.newArrayList();
            if (cities != null) {
              for (long city : cities) {
                CityInfo cityInfo = cityIdToInfo.get(city);
                if (cityInfo != null) {
                  coverCities.add(cityInfo);
                }
              }
            }
            GuideInfo info = GuideInfo.newBuilder().setGuideId(guideId).setName(name)
                .setDescription(desc).setMaxPeople(maxPeople).setHasCar(hasCar).setScore(score)
                .setHostCity(hostCityInfo).setPhone(phone).addAllTopic(topics)
                .addAllCoverCity(coverCities).build();
            builder.put(guideId, info);
          } catch (NullPointerException e) {
            StringJoiner joiner = new StringJoiner(",");
            for (int i = 1; i < 9; ++i) {
              joiner.add(rs.getString(i));
            }
            LOG.info("xfguo: guide error {}?", joiner.toString());
          }
        }
      }
    } catch (SQLException e) {
      LOG.error("DB error in reloading guide info", e);
    }
  }

  private static final String LOAD_SPOT_DATA =
      "SELECT spot_id, city_id, name, description, hours, score, interests FROM Spots";

  private void reloadSpotInfo(ImmutableMap.Builder<Long, SpotInfo> spotIdToInfo) {
    Preconditions.checkNotNull(spotIdToInfo);
    try (Connection conn = dataSource.getConnection()) {
      try (PreparedStatement pStmt = conn.prepareStatement(LOAD_SPOT_DATA);
          ResultSet rs = pStmt.executeQuery()) {
        int incorrectRows = 0;
        while (rs.next()) {
          try {
            long spotId = rs.getLong(1);
            SpotInfo.Builder builder = SpotInfo.newBuilder().setSpotId(spotId)
                .setCityId(rs.getLong(2)).setName(rs.getString(3)).setDescription(rs.getString(4))
                .setHours(rs.getFloat(5)).setScore(rs.getFloat(6))
                .addAllTopics(Util.splitString(rs.getString(7)));
            spotIdToInfo.put(spotId, builder.build());
          } catch (NullPointerException e) {
            incorrectRows++;
          }
        }
        LOG.info("Get {} lines of incorrect rows", incorrectRows);
      }
    } catch (SQLException e) {
      LOG.error("DB error in reload spot data", e);
    }
  }
}
