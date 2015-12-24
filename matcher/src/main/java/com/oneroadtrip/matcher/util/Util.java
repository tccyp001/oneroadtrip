package com.oneroadtrip.matcher.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jersey.repackaged.com.google.common.collect.Maps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.oneroadtrip.matcher.ErrorInfo;
import com.oneroadtrip.matcher.VisitSpot;
import com.oneroadtrip.matcher.internal.CityConnectionInfo;
import com.oneroadtrip.matcher.internal.EngageType;
import com.oneroadtrip.matcher.internal.SuggestCityInfo;

public class Util {
  private static final Logger LOG = LogManager.getLogger();

  public static int getDays(int startdate, int enddate) {
    // TODO Auto-generated method stub
    return 0;
  }

  public static Map<Pair<Long, Long>, CityConnectionInfo> propagateNetwork(Collection<Long> nodes,
      Map<Pair<Long, Long>, CityConnectionInfo> network) {
    for (Long k : nodes) {
      for (Long i : nodes) {
        for (Long j : nodes) {
          CityConnectionInfo ij = network.get(Pair.with(i, j));
          CityConnectionInfo ik = network.get(Pair.with(i, k));
          CityConnectionInfo kj = network.get(Pair.with(k, j));
          if (ik == null || kj == null || i == j) {
            continue;
          }

          if (ij == null || ij.getDistance() > ik.getDistance() + kj.getDistance()) {
            network.put(Pair.with(i, j),
                CityConnectionInfo.newBuilder().setDistance(ik.getDistance() + kj.getDistance())
                    .setHours(ik.getHours() + kj.getHours()).build());
          }
        }
      }
    }

    return network;
  }

  public static SuggestCityInfo createSuggestCityInfo(Long cityId, int index,
      EngageType engageType, int min) {
    return SuggestCityInfo.newBuilder().setCityId(cityId).setEngageToPathIndex(index)
        .setEngageType(engageType).setAdditionalDistance(min).build();
  }

  public static CityConnectionInfo createConnectionInfo(int distance, int hours) {
    return CityConnectionInfo.newBuilder().setDistance(distance).setHours(hours).build();
  }

  public static VisitSpot createVisitSpot(int hours, long spotId, String spotName,
      ErrorInfo errorInfo) {
    VisitSpot.Builder builder = VisitSpot.newBuilder().setHours(hours).setSpotName(spotName);
    if (spotId != 0L) {
      builder.setSpotId(spotId);
    }
    if (errorInfo != null) {
      builder.setErrorInfo(errorInfo);
    }
    return builder.build();
  }

  private static final String INTEREST_SPLITTOR = Pattern.quote("|");
  public static List<Long> getInterestIds(String interests, Map<String, Long> interestNameToId) {
    List<Long> ids = Lists.newArrayList();
    if (interests == null) {
      return ids;
    }
    for (String name : interests.split(INTEREST_SPLITTOR)) {
      if (name.isEmpty()) {
        continue;
      }
      if (!interestNameToId.containsKey(name)) {
        LOG.error("Can't find interest by name ({})", name);
        continue;
      }
      ids.add(interestNameToId.get(name));
    }
    return ids;
  }

  public static ImmutableMap<Long, ImmutableSet<Long>> rotateMatrix(
      ImmutableMap<Long, ImmutableSet<Long>> a) {
    Map<Long, ImmutableSet.Builder<Long>> b = Maps.newTreeMap();
    for (Map.Entry<Long, ImmutableSet<Long>> e : a.entrySet()) {
      Long x = e.getKey();
      for (Long y : e.getValue()) {
        if (!b.containsKey(y)) {
          b.put(y, ImmutableSet.builder());
        }
        b.get(y).add(x);
      }
    }

    ImmutableMap.Builder<Long, ImmutableSet<Long>> builder = ImmutableMap.builder();
    for (Map.Entry<Long, ImmutableSet.Builder<Long>> e : b.entrySet()) {
      builder.put(e.getKey(), e.getValue().build());
    }
    return builder.build();
  }
}