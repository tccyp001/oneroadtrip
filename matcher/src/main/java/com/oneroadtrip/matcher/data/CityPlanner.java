package com.oneroadtrip.matcher.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oneroadtrip.matcher.VisitCity;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.internal.CityConnectionInfo;

// Not thread-safe, need to create a new one for each request.
public class CityPlanner {
  private static final Logger LOG = LogManager.getLogger();
  // TODO(xiaofengguo): Make this as constant in protobuf enum.
  private static final int MUST_SELECT_CITY_RATE = 100;

  @Inject
  @Named(Constants.ALL_CITY_IDS)
  ImmutableList<Long> allCityIds;

  @Inject
  @Named(Constants.CITY_NETWORK)
  ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork;

  @Inject
  @Named(Constants.SUGGEST_DAYS_FOR_CITIES)
  ImmutableMap<Long, Integer> suggestDaysForCities;

  public List<VisitCity> makePlan(long startCityId, long endCityId, List<VisitCity> visitCities,
      boolean keepOrderOrViaCities, int days) {
    CityVisitor visitor = new CityVisitor(startCityId, endCityId, visitCities, cityNetwork);
    visitor.visit(startCityId, 0L);

    long minDistance = visitor.getMinDistance();
    List<Long> minDistancePath = visitor.getMinDistancePath();

    Set<Long> dispatchedCities = dispatchOtherCitiesIntoThePath(minDistance, minDistancePath);

    // TODO(xfguo): Arrive here.
    List<VisitCity> citiesOnPath = convertPathToCities(minDistancePath, dispatchedCities);
    // setNumDaysAndSuggestRate(citiesOnPath, dispatchedCities);

    return citiesOnPath;
  }

  private List<VisitCity> convertPathToCities(List<Long> path, Set<Long> dispatchedCities) {
    List<VisitCity> result = Lists.newArrayList();
    for (Long cityId : path) {
      int suggestDays = 0;
      if (!suggestDaysForCities.containsKey(cityId)) {
        LOG.info("No suggest days for city ({})", cityId);
      }
      suggestDays = suggestDaysForCities.get(cityId);
      result.add(VisitCity.newBuilder().setCityId(cityId).setSuggestRate(MUST_SELECT_CITY_RATE)
          .setNumDays(suggestDays).build());
    }
    return result;
  }

  //
  // private void setNumDaysAndSuggestRate(List<VisitCity> cities, Set<Long>
  // dispatchedCities) {
  // }

  private VisitCity createDefaultVisitCity(long cityId) {
    return VisitCity.newBuilder().setCityId(cityId).setSuggestRate(MUST_SELECT_CITY_RATE).build();
  }

  // 寻找途径所有这些城市的最短路径。用全遍历即可，因为输入的城市不可能超过十个，如果这里碰到问题我们再优化。
  // 这是哈密尔顿通路问题，所以我们就先别费事在这里优化了，先给出一个解再说。
  static class CityVisitor {
    ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork;

    long startId;
    long endId;
    List<VisitCity> visitCities;

    Set<Long> visited = Sets.newTreeSet();
    List<Long> visitPath = Lists.newArrayList();

    long minDistance = Long.MAX_VALUE;
    List<Long> minDistancePath;

    public CityVisitor(long startId, long endId, List<VisitCity> visitCities,
        ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork) {
      this.startId = startId;
      this.endId = endId;
      this.visitCities = visitCities;
      this.cityNetwork = cityNetwork;
    }

    public List<Long> getMinDistancePath() {
      return minDistancePath;
    }

    public long getMinDistance() {
      return minDistance;
    }

    private static final long REJECT_EDGE = -1;

    long chooseEdge(long from, long to, long distance) {
      Pair<Long, Long> edge = Pair.with(from, to);
      if (!cityNetwork.containsKey(edge)) {
        return REJECT_EDGE;
      }
      int edgeLength = cityNetwork.get(edge).getDistance();
      if (edgeLength + distance > minDistance) {
        return REJECT_EDGE;
      }
      return edgeLength;
    }

    void visit(long current, long distance) {
      if (visited.size() == visitCities.size()) {
        long edge = chooseEdge(current, endId, distance);
        if (edge == REJECT_EDGE) {
          return;
        }
        minDistancePath = Lists.newLinkedList();
        for (Long cityId : visitPath) {
          minDistancePath.add(cityId);
        }
        minDistancePath.add(endId);
        minDistance = distance + edge;
        return;
      }
      for (VisitCity city : visitCities) {
        long cityId = city.getCityId();
        long edge = chooseEdge(current, cityId, distance);
        if (edge == REJECT_EDGE) {
          return;
        }
        visitPath.add(cityId);
        visited.add(cityId);
        visit(cityId, distance + edge);
        visited.remove(cityId);
        visitPath.remove(cityId);
      }
    }
  }

  // 把其它城市放到现在的路径当中。
  //
  // - 目前还是不要用太复杂的算法了，就是每个城市在每两个城市之间试一下，找一个增加距离最短的地方放进去。当然，放进去后可能还不是
  // 最短的距离，可能还需要我们调整的，这个以后再做。
  // - 一个简单的Filter是：如果这个城市添加到起点和终点城市之间会让路程加倍，那么我们就不把它放进来。
  private Set<Long> dispatchOtherCitiesIntoThePath(long minDistance, List<Long> cities) {
    Map<Integer, List<Long>> indexToCities = Maps.newTreeMap();

    Set<Long> selectedCities = Sets.newTreeSet(cities);
    for (Long cityId : allCityIds) {
      if (selectedCities.contains(cityId)) {
        continue;
      }

      long min = Long.MAX_VALUE;
      int index = -1;
      for (int i = 0; i < cities.size() - 1; ++i) {
        Pair<Long, Long> e1 = Pair.with(cities.get(i), cityId);
        Pair<Long, Long> e2 = Pair.with(cityId, cities.get(i + 1));
        if (!cityNetwork.containsKey(e1) || !cityNetwork.containsKey(e2)) {
          continue;
        }
        int e1Distance = cityNetwork.get(e1).getDistance();
        int e2Distance = cityNetwork.get(e2).getDistance();
        int originDistance = cityNetwork.get(Pair.with(cities.get(i), cities.get(i + 1)))
            .getDistance();
        if (e1Distance + e2Distance > originDistance + minDistance) {
          // 路程加倍，不予选中。
          continue;
        }
        if (e1Distance + e2Distance - originDistance < min) {
          min = e1Distance + e2Distance - originDistance;
          index = i;
        }
      }

      if (index == -1) {
        // skip the city.
        continue;
      }

      if (!indexToCities.containsKey(index)) {
        indexToCities.put(index, Lists.<Long> newArrayList());
      }
      indexToCities.get(index).add(cityId);
    }

    Set<Long> result = Sets.newTreeSet();
    for (int i = cities.size() - 2; i >= 0; --i) {
      if (!indexToCities.containsKey(i)) {
        continue;
      }
      for (Long cityId : indexToCities.get(i)) {
        cities.add(i, cityId);
        result.add(cityId);
      }
    }

    return result;
  }
}
