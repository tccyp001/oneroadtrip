package com.oneroadtrip.matcher.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oneroadtrip.matcher.Edge;
import com.oneroadtrip.matcher.PlanResponse;
import com.oneroadtrip.matcher.Status;
import com.oneroadtrip.matcher.VisitCity;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.internal.CityConnectionInfo;
import com.oneroadtrip.matcher.internal.EngageType;
import com.oneroadtrip.matcher.internal.SuggestCityInfo;
import com.oneroadtrip.matcher.util.CityVisitor;
import com.oneroadtrip.matcher.util.Util;

// Not thread-safe, need to create a new one for each request.
public class CityPlanner {
  private static final Logger LOG = LogManager.getLogger();
  // TODO(xfguo): (P3) Make this as constant in protobuf enum.
  private static final float MUST_SELECT_CITY_RATE = 1.0f;

  // Incoming data
  final ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork;
  final ImmutableMap<Long, Integer> suggestDaysForCities;

  @Inject
  public CityPlanner(
      @Named(Constants.CITY_NETWORK) ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork,
      @Named(Constants.SUGGEST_DAYS_FOR_CITIES) ImmutableMap<Long, Integer> suggestDaysForCities) {
    this.cityNetwork = cityNetwork;
    this.suggestDaysForCities = suggestDaysForCities;
  }

  public PlanResponse.Builder makePlan(long startCityId, long endCityId, List<VisitCity> visitCities,
      boolean keepOrderOrViaCities) {
    CityVisitor visitor = new CityVisitor(startCityId, endCityId, visitCities, cityNetwork);
    visitor.visit(startCityId, 0L);

    long minDistance = visitor.getMinDistance();
    List<Long> minDistancePath = visitor.getMinDistancePath();

    Map<Long, SuggestCityInfo> suggestCityToData = chooseOtherCities(minDistance, minDistancePath);

    return buildResponse(startCityId, endCityId, visitCities, minDistance, minDistancePath, suggestCityToData);
  }

  PlanResponse.Builder buildResponse(long startCityId, long endCityId, List<VisitCity> visitCities,
      long minDistance, List<Long> path, Map<Long, SuggestCityInfo> suggestCityToData) {
    PlanResponse.Builder builder = PlanResponse.newBuilder().setStatus(Status.SUCCESS)
        .setStartCityId(startCityId).setEndCityId(endCityId);

    for (VisitCity city : visitCities) {
      VisitCity.Builder cityBuilder = VisitCity.newBuilder(city);
      if (city.getNumDays() == 0) {
        cityBuilder.setNumDays(suggestDaysForCities.get(city.getCityId()));
      }
      cityBuilder.setSuggestRate(MUST_SELECT_CITY_RATE);
      builder.addVisit(cityBuilder);
    }

    for (int i = 0; i < path.size() - 1; ++i) {
      long from = path.get(i);
      long to = path.get(i + 1);
      CityConnectionInfo info = cityNetwork.get(Pair.with(from, to));
      Preconditions.checkNotNull(info);

      builder.addEdge(Edge.newBuilder().setFromCityId(from).setToCityId(to)
          .setDistance(info.getDistance()).setHours(info.getHours()));
    }
    
    for (SuggestCityInfo suggest : suggestCityToData.values()) {
      Integer suggestDays = suggestDaysForCities.get(suggest.getCityId());
      builder.addSuggestCity(VisitCity.newBuilder().setCityId(suggest.getCityId())
          .setNumDays(suggestDays == null ? 0 : suggestDays)
          .setSuggestRate(getSuggestRate(minDistance, suggest)));
    }
    return builder;
  }

  private float getSuggestRate(long minDistance, SuggestCityInfo suggest) {
    return 1.0f - suggest.getAdditionalDistance() * 1.0f / minDistance;
  }

  // 把其它城市放到现在的路径当中。
  //
  // - 目前还是不要用太复杂的算法了，就是每个城市在每两个城市之间试一下，找一个增加距离最短的地方放进去。当然，放进去后可能还不是
  // 最短的距离，可能还需要我们调整的，这个以后再做。
  // - 一个简单的Filter是：如果这个城市添加到起点和终点城市之间会让路程加倍，那么我们就不把它放进来。
  public Map<Long, SuggestCityInfo> chooseOtherCities(long acceptableAdditionalDistance,
      List<Long> chosenPath) {
    Map<Long, SuggestCityInfo> suggestCityToData = Maps.newTreeMap();
    Set<Long> selectedCities = Sets.newTreeSet();
    // 起点和终点并不一定是要玩的，我们这里要把它们挖掉。
    for (int i = 1; i < chosenPath.size() - 1; ++i) {
      selectedCities.add(chosenPath.get(i));
    }
    for (Long cityId : suggestDaysForCities.keySet()) {
      if (selectedCities.contains(cityId)) {
        continue;
      }
      int min = Integer.MAX_VALUE;
      int index = -1;
      EngageType engageType = EngageType.ON_NODE;

      // 下面这个循环检查把节点挂到路径中一个节点的话，是否不用显著增加旅行距离。
      for (int i = 0; i < chosenPath.size(); ++i) {
        long x = chosenPath.get(i);
        Pair<Long, Long> e = Pair.with(x, cityId);
        if (!cityNetwork.containsKey(e)) {
          continue;
        }
        int eDistance = cityNetwork.get(e).getDistance();
        if (2 * eDistance > acceptableAdditionalDistance || 2 * eDistance >= min) {
          continue;
        }
        min = 2 * eDistance;
        index = i;
      }

      // 下面这个循环检查把节点作为路径中一个边的中间节点，是否不显著增加旅行距离。
      for (int i = 0; i < chosenPath.size() - 1; ++i) {
        long x = chosenPath.get(i);
        long y = chosenPath.get(i + 1);
        if (x == y) {
          // 我们不检查在回环路径。
          continue;
        }
        Pair<Long, Long> e1 = Pair.with(x, cityId);
        Pair<Long, Long> e2 = Pair.with(cityId, y);
        if (!cityNetwork.containsKey(e1) || !cityNetwork.containsKey(e2)) {
          continue;
        }
        int e1Distance = cityNetwork.get(e1).getDistance();
        int e2Distance = cityNetwork.get(e2).getDistance();
        int originDistance = cityNetwork.get(Pair.with(x, y)).getDistance();
        if (e1Distance + e2Distance > originDistance + acceptableAdditionalDistance) {
          // 路程加倍，不予选中。
          continue;
        }
        if (e1Distance + e2Distance - originDistance < min) {
          engageType = EngageType.ON_EDGE;
          min = e1Distance + e2Distance - originDistance;
          index = i;
        }
      }

      if (index == -1) {
        // skip the city.
        continue;
      }

      suggestCityToData.put(cityId, Util.createSuggestCityInfo(cityId, index, engageType, min));
    }
    return suggestCityToData;
  }
}
