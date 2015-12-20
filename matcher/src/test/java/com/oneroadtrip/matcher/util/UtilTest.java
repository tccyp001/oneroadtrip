package com.oneroadtrip.matcher.util;

import java.util.Map;

import org.javatuples.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.oneroadtrip.matcher.internal.CityConnectionInfo;

public class UtilTest {
  private CityConnectionInfo weight(int distance, int hours) {
    return CityConnectionInfo.newBuilder().setDistance(distance).setHours(hours).build();
  }

  @Test
  public void propagateNetwork() throws Exception {
    ImmutableList<Long> nodes = ImmutableList.of(1L, 2L, 3L, 4L, 5L);
    Map<Pair<Long, Long>, CityConnectionInfo> network = Maps.newTreeMap();
    network.put(Pair.with(1L, 2L), weight(3, 1));
    network.put(Pair.with(1L, 3L), weight(8, 1));
    network.put(Pair.with(1L, 5L), weight(-4, 1));
    network.put(Pair.with(2L, 4L), weight(1, 1));
    network.put(Pair.with(2L, 5L), weight(7, 1));
    network.put(Pair.with(3L, 2L), weight(4, 1));
    network.put(Pair.with(4L, 1L), weight(2, 1));
    network.put(Pair.with(4L, 3L), weight(-5, 1));
    network.put(Pair.with(5L, 4L), weight(6, 1));

    Map<Pair<Long, Long>, CityConnectionInfo> expected = Maps.newTreeMap();
    expected.put(Pair.with(1L, 2L), weight(1, 4));
    expected.put(Pair.with(1L, 3L), weight(-3, 3));
    expected.put(Pair.with(1L, 4L), weight(2, 2));
    expected.put(Pair.with(1L, 5L), weight(-4, 1));
    expected.put(Pair.with(2L, 1L), weight(3, 2));
    expected.put(Pair.with(2L, 3L), weight(-4, 2));
    expected.put(Pair.with(2L, 4L), weight(1, 1));
    expected.put(Pair.with(2L, 5L), weight(-1, 3));
    expected.put(Pair.with(3L, 1L), weight(7, 3));
    expected.put(Pair.with(3L, 2L), weight(4, 1));
    expected.put(Pair.with(3L, 4L), weight(5, 2));
    expected.put(Pair.with(3L, 5L), weight(3, 4));
    expected.put(Pair.with(4L, 1L), weight(2, 1));
    expected.put(Pair.with(4L, 2L), weight(-1, 2));
    expected.put(Pair.with(4L, 3L), weight(-5, 1));
    expected.put(Pair.with(4L, 5L), weight(-2, 2));
    expected.put(Pair.with(5L, 1L), weight(8, 2));
    expected.put(Pair.with(5L, 2L), weight(5, 3));
    expected.put(Pair.with(5L, 3L), weight(1, 2));
    expected.put(Pair.with(5L, 4L), weight(6, 1));
    Assert.assertEquals(expected, Util.propagateNetwork(nodes, network));
  }
}
