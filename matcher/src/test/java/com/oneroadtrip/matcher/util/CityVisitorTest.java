package com.oneroadtrip.matcher.util;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.oneroadtrip.matcher.proto.VisitCity;
import com.oneroadtrip.matcher.proto.internal.CityConnectionInfo;
import com.oneroadtrip.matcher.testutil.GraphTestingUtil;

public class CityVisitorTest {
  private static final Logger LOG = LogManager.getLogger();

  @Test
  public void test() throws Exception {
    long startId = 1;
    long endId = 2;
    List<VisitCity> visitCities = GraphTestingUtil.createVisitCities(1L, 2L, 3L, 4L, 5L);
    Map<Pair<Long, Long>, CityConnectionInfo> cityNetwork = Maps.newTreeMap();
    GraphTestingUtil.addLink(cityNetwork, 1L, 2L, 10, 1);
    GraphTestingUtil.addLink(cityNetwork, 1L, 3L, 10, 1);
    GraphTestingUtil.addLink(cityNetwork, 2L, 3L, 17, 1);
    GraphTestingUtil.addLink(cityNetwork, 2L, 4L, 10, 1);
    GraphTestingUtil.addLink(cityNetwork, 2L, 5L, 15, 1);
    GraphTestingUtil.addLink(cityNetwork, 3L, 4L, 20, 1);
    GraphTestingUtil.addLink(cityNetwork, 4L, 5L, 10, 1);
    GraphTestingUtil.addLink(cityNetwork, 4L, 6L, 10, 1);
    GraphTestingUtil.addLink(cityNetwork, 5L, 7L, 10, 1);
    GraphTestingUtil.addLink(cityNetwork, 6L, 7L, 20, 1);

    // 需要加起点终点和自身的0连接，大多数情况一下飞机就先玩本地城市。
    GraphTestingUtil.addLink(cityNetwork, 1L, 1L, 0, 0);
    GraphTestingUtil.addLink(cityNetwork, 2L, 2L, 0, 0);

    CityVisitor visitor = new CityVisitor(startId, endId, visitCities,
        ImmutableMap.copyOf(cityNetwork));
    visitor.visit(startId, 0L);

    List<Long> expectPath = Lists.newArrayList();
    expectPath.add(1L);
    expectPath.add(1L);
    expectPath.add(3L);
    expectPath.add(4L);
    expectPath.add(5L);
    expectPath.add(2L);
    expectPath.add(2L);
    List<Long> actualPath = visitor.getMinDistancePath();
    LOG.info("xfguo: actualPath: {}", actualPath);
    Assert.assertEquals(visitor.getMinDistance(), 55L);
    Assert.assertEquals(expectPath, visitor.getMinDistancePath());
  }
  
  // 以后要是有寻路的错，请记得把case加到这个测试中来，保证错误只犯一次。
}
