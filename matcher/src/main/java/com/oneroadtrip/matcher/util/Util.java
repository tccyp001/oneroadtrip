package com.oneroadtrip.matcher.util;

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.oneroadtrip.matcher.internal.CityConnectionInfo;
import com.oneroadtrip.matcher.internal.EngageType;
import com.oneroadtrip.matcher.internal.SuggestCityInfo;

public class Util {
  private static final Logger LOG = LogManager.getLogger();

  public static int getDays(int startdate, int enddate) {
    // TODO Auto-generated method stub
    return 0;
  }

  public static Map<Pair<Long, Long>, CityConnectionInfo> propagateNetwork(
      Collection<Long> nodes, Map<Pair<Long, Long>, CityConnectionInfo> network) {
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
}
