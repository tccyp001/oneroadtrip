package com.oneroadtrip.matcher.testutil;

import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.google.common.collect.Lists;
import com.oneroadtrip.matcher.proto.VisitCity;
import com.oneroadtrip.matcher.proto.internal.CityConnectionInfo;

public class GraphTestingUtil {
  public static CityConnectionInfo weight(int distance, int hours) {
    return CityConnectionInfo.newBuilder().setDistance(distance).setHours(hours).build();
  }

  public static void addLink(Map<Pair<Long, Long>, CityConnectionInfo> network, long x, long y,
      int distance, int hours) {
    network.put(Pair.with(x, y), weight(distance, hours));
    network.put(Pair.with(y, x), weight(distance, hours));
  }

  public static List<VisitCity> createVisitCities(long... cityIds) {
    List<VisitCity> result = Lists.newArrayList();
    for (long id : cityIds) {
      result.add(VisitCity.newBuilder().setCityId(id).build());
    }
    return result;
  }

}
