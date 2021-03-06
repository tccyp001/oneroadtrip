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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.CityInfo;
import com.oneroadtrip.matcher.proto.Edge;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.PlanRequest;
import com.oneroadtrip.matcher.proto.PlanResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.proto.VisitCity;
import com.oneroadtrip.matcher.proto.internal.CityConnectionInfo;
import com.oneroadtrip.matcher.proto.internal.EngageType;
import com.oneroadtrip.matcher.proto.internal.SuggestCityInfo;
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
  final ImmutableMap<Long, CityInfo> cityIdToInfo;

  @Inject
  public CityPlanner(
      @Named(Constants.CITY_NETWORK) ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork,
      @Named(Constants.SUGGEST_DAYS_FOR_CITIES) ImmutableMap<Long, Integer> suggestDaysForCities,
      ImmutableMap<Long, CityInfo> cityIdToInfo) {
    this.cityNetwork = cityNetwork;
    this.suggestDaysForCities = suggestDaysForCities;
    this.cityIdToInfo = cityIdToInfo;
  }

  private List<VisitCity> reorderVisitCities(List<VisitCity> visitCities, List<Long> minDistancePath) {
    if (minDistancePath.size() == 0) {
      return visitCities;
    }
    List<VisitCity> result = Lists.newArrayList();
    Map<Long, VisitCity> cityMap = Maps.newTreeMap();
    for (VisitCity city : visitCities) {
      cityMap.put(city.getCity().getCityId(), city);
    }
    
    for (int i = 1; i < minDistancePath.size() - 1; ++i) {
      result.add(cityMap.get(minDistancePath.get(i)));
    }
    return result;
  }

  String getNameById(long id) {
    CityInfo city = cityIdToInfo.get(id);
    return city != null ? city.getName() : "";
  }
  
  CityInfo getCityInfo(long cityId) {
    return Util.getCityInfo(cityIdToInfo, cityId);
  }

  List<Integer> calculateDaysForVisit(int totalDays, List<VisitCity> visitCities) {
    List<Integer> suggestDays = Lists.newArrayList();
    int totalSuggestDays = 0;
    int reservedDays = 0;
    for (VisitCity city : visitCities) {
      int numDays = city.getNumDays();
      if (numDays > 0) {
        suggestDays.add(city.getNumDays());
        totalSuggestDays += city.getNumDays();
        reservedDays += city.getNumDays();
        continue;
      }

      long cityId = city.getCity().getCityId();
      Integer suggest = suggestDaysForCities.get(cityId);
      if (suggest == null) {
        suggestDays.add(0);
        continue;
      }
      suggestDays.add(suggest);
      totalSuggestDays += suggest;
    }
    
    if (totalSuggestDays <= totalDays) {
      return suggestDays;
    }
    
    List<Integer> adjustedDays = Lists.newArrayList();
    {
      int total = 0;
      for (int i = 0; i < suggestDays.size(); ++i) {
        int numDays = visitCities.get(i).getNumDays();
        if (numDays > 0) {
          int days = visitCities.get(i).getNumDays();
          adjustedDays.add(days);
          total += days;
          continue;
        }
        int days = suggestDays.get(i);
        float t = ((float) days) * (totalDays - reservedDays) / (totalSuggestDays - reservedDays);
        adjustedDays.add(Math.round(t));
        total += suggestDays.get(i);
      }
      if (total != totalDays) {
        LOG.error("xfguo: error case: totalDays: {}, reservedDays: {}; totalSuggestDays: {}, "
            + "suggest days: {}, adjusted days: {}", totalDays, reservedDays, totalSuggestDays,
            suggestDays, adjustedDays);
      }
    }
    return adjustedDays;
  }

  PlanResponse buildResponse(Itinerary itin, int totalDays, long startCityId, long endCityId, List<VisitCity> visitCities,
      long minDistance, List<Long> path, Map<Long, SuggestCityInfo> suggestCityToData) throws OneRoadTripException {
    Itinerary.Builder builder = Itinerary.newBuilder(cleanupCityPlanRelatedFields(itin))
        .setStartCity(getCityInfo(startCityId)).setEndCity(getCityInfo(endCityId));

    List<Integer> numDaysForVisit = calculateDaysForVisit(totalDays, visitCities);
    Preconditions.checkArgument(numDaysForVisit.size() == visitCities.size());
    int index = 0;
    for (VisitCity city : visitCities) {
      long cityId = city.getCity().getCityId();
      VisitCity.Builder cityBuilder = VisitCity.newBuilder(city).setCity(getCityInfo(cityId))
          .setSuggestRate(MUST_SELECT_CITY_RATE).setNumDays(numDaysForVisit.get(index++));
      builder.addCity(cityBuilder);
    }

    if (path.isEmpty()) {
      // Can't find a right path for the cities.
      throw new OneRoadTripException(Status.ERR_PATH_NOT_FOUND, null);
    }
    for (int i = 0; i < path.size() - 1; ++i) {
      long from = path.get(i);
      long to = path.get(i + 1);
      CityConnectionInfo info = cityNetwork.get(Pair.with(from, to));
      Preconditions.checkNotNull(info);

      builder.addEdge(Edge.newBuilder().setFromCity(getCityInfo(from))
          .setToCity(getCityInfo(to)).setDistance(info.getDistance())
          .setHours(info.getHours()));
    }

    for (SuggestCityInfo suggest : suggestCityToData.values()) {
      Integer suggestDays = suggestDaysForCities.get(suggest.getCityId());
      builder.addSuggestCity(VisitCity.newBuilder().setCity(getCityInfo(suggest.getCityId()))
          .setNumDays(suggestDays == null ? 0 : suggestDays)
          .setSuggestRate(getSuggestRate(minDistance, suggest)));
    }
    return PlanResponse.newBuilder().setStatus(Status.SUCCESS).setItinerary(builder).build();
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

  public PlanResponse makePlan(PlanRequest request) throws OneRoadTripException {
    Itinerary itin = request.getItinerary();
    long startCityId = itin.getStartCity().getCityId();
    long endCityId = itin.getEndCity().getCityId();
    CityVisitor visitor = new CityVisitor(startCityId, endCityId, itin.getCityList(), cityNetwork);
    visitor.visit(startCityId, 0L);
    
    long minDistance = visitor.getMinDistance();
    List<Long> minDistancePath = visitor.getMinDistancePath();
    List<VisitCity> orderedVisits = reorderVisitCities(itin.getCityList(), minDistancePath);

    Map<Long, SuggestCityInfo> suggestCityToData = chooseOtherCities(minDistance, minDistancePath);
    int totalDays = Util.calculateDaysByStartEndDate(itin.getStartdate(), itin.getEnddate());

    return buildResponse(itin, totalDays, startCityId, endCityId, orderedVisits, minDistance,
        minDistancePath, suggestCityToData);
  }

  public static Itinerary cleanupCityPlanRelatedFields(Itinerary oriItin) {
    return Itinerary.newBuilder(oriItin).clearCity().clearEdge().clearSuggestCity().build();
  }
}
