package com.oneroadtrip.matcher.util;

import java.util.List;
import java.util.Set;

import org.javatuples.Pair;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.oneroadtrip.matcher.proto.VisitCity;
import com.oneroadtrip.matcher.proto.internal.CityConnectionInfo;

// 寻找途径所有这些城市的最短路径。用全遍历即可，因为输入的城市不可能超过十个，如果这里碰到问题我们再优化。
// 这是哈密尔顿通路问题，所以我们就先别费事在这里优化了，先给出一个解再说。
public class CityVisitor {
  final ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork;
  final long startId;
  final long endId;
  final List<VisitCity> visitCities;

  Set<Long> visited = Sets.newTreeSet();
  List<Long> visitPath = Lists.newArrayList();

  long minDistance = Long.MAX_VALUE;
  List<Long> minDistancePath = Lists.newArrayList();

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

  public void visit(long current, long distance) {
    if (visited.size() == visitCities.size()) {
      long edge = chooseEdge(current, endId, distance);
      if (edge == REJECT_EDGE) {
        return;
      }
      minDistancePath = Lists.newLinkedList();
      minDistancePath.add(startId);
      for (Long cityId : visitPath) {
        minDistancePath.add(cityId);
      }
      minDistancePath.add(endId);
      minDistance = distance + edge;
      return;
    }
    for (VisitCity city : visitCities) {
      long cityId = city.getCityId();
      if (visited.contains(cityId)) {
        continue;
      }
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
