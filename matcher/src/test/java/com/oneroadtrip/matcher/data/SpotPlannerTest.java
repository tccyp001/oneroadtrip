package com.oneroadtrip.matcher.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oneroadtrip.matcher.DayPlan;
import com.oneroadtrip.matcher.ErrorInfo;
import com.oneroadtrip.matcher.VisitSpot;
import com.oneroadtrip.matcher.util.Util;

public class SpotPlannerTest {
  SpotPlanner spotPlanner = null;

  @BeforeClass
  void setUp() {
    List<String> spotNames = Lists.newArrayList("金门大桥", "九曲花街", "渔人码头", "旧金山艺术宫", "旧金山唐人街", "联合广场",
        "双子峰", "恶魔岛", "金门公园", "旧金山市政厅", "Castro St", "阿拉莫广场");
    List<Integer> hours = Lists.newArrayList(2, 1, 2, 1, 2, 1, 2, 4, 7, 2, 2, 2);
    List<Float> scores = Lists.newArrayList(0.9f, 0.9f, 0.9f, 0.8f, 0.7f, 0.7f, 0.7f, 0.8f, 0.8f,
        0.6f, 0.7f, 0.7f);
    Map<String, Long> spotNameToId = Maps.newTreeMap();
    Map<Long, VisitSpot> spotIdToData = Maps.newTreeMap();
    Map<Long, Float> spotToScore = Maps.newTreeMap();
    for (int i = 0; i < spotNames.size(); ++i) {
      long id = i + 1;
      spotNameToId.put(spotNames.get(i), id);
      spotIdToData.put(id, Util.createVisitSpot(hours.get(i), id, spotNames.get(i), null));
      spotToScore.put(id, scores.get(i));
    }

    // Interest: 1--浪漫, 2--商务
    Map<Long, Set<Long>> interestToSpots = Maps.newTreeMap();
    interestToSpots.put(1L, ImmutableSet.of(1L, 2L, 3L, 4L, 6L, 7L, 12L));
    interestToSpots.put(2L, ImmutableSet.of(10L));

    spotPlanner = new SpotPlanner(ImmutableMap.copyOf(spotNameToId),
        ImmutableMap.copyOf(spotIdToData), ImmutableMap.copyOf(interestToSpots),
        ImmutableMap.copyOf(spotToScore));
  }

  @Test
  public void test1() throws Exception {
    DayPlan.Builder builder = DayPlan.newBuilder().setDayId(1)
        .addSpot(Util.createVisitSpot(0, 0L, "金门大桥", null))
        .addSpot(Util.createVisitSpot(0, 0L, "渔人码头", null))
        .addSpot(Util.createVisitSpot(0, 0L, "恶魔岛", null));
    DayPlan actual = spotPlanner.updateDayPlan(2, builder.build(), Lists.newArrayList(1L),
        Sets.newTreeSet());

    DayPlan.Builder expect = DayPlan.newBuilder().setDayId(2)
        .addSpot(Util.createVisitSpot(2, 1L, "金门大桥", null))
        .addSpot(Util.createVisitSpot(2, 3L, "渔人码头", null))
        .addSpot(Util.createVisitSpot(4, 8L, "恶魔岛", null)).addErrorInfo(ErrorInfo.OVER_ALLOCATED);
    Assert.assertEquals(actual, expect.build());
  }

  @Test
  public void test2() throws Exception {
    DayPlan.Builder builder = DayPlan.newBuilder().setDayId(1)
        .addSpot(Util.createVisitSpot(0, 0L, "金门大桥", null))
        .addSpot(Util.createVisitSpot(1, 0L, "渔人码头", null))
        .addSpot(Util.createVisitSpot(0, 0L, "恶魔岛", null));
    DayPlan actual = spotPlanner.updateDayPlan(2, builder.build(), Lists.newArrayList(1L),
        Sets.newTreeSet());

    DayPlan.Builder expect = DayPlan.newBuilder().setDayId(2)
        .addSpot(Util.createVisitSpot(2, 1L, "金门大桥", null))
        .addSpot(Util.createVisitSpot(1, 3L, "渔人码头", null))
        .addSpot(Util.createVisitSpot(4, 8L, "恶魔岛", null));
    Assert.assertEquals(actual, expect.build());
  }

  @Test
  public void test3() throws Exception {
    DayPlan.Builder builder = DayPlan.newBuilder().setDayId(2);
    DayPlan actual = spotPlanner.updateDayPlan(2, builder.build(), Lists.newArrayList(1L),
        ImmutableSet.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L));

    DayPlan.Builder expect = DayPlan.newBuilder().setDayId(2)
        .addSpot(Util.createVisitSpot(2, 1L, "金门大桥", null)).addErrorInfo(ErrorInfo.LEFT_HOURS);
    Assert.assertEquals(actual, expect.build());
  }

  @Test
  public void test4() throws Exception {
    DayPlan.Builder builder = DayPlan.newBuilder().setDayId(2)
        .addSpot(Util.createVisitSpot(0, 0L, "Castro St", null));
    DayPlan actual = spotPlanner.updateDayPlan(2, builder.build(), Lists.newArrayList(2L),
        ImmutableSet.of(1L, 3L, 8L));

    DayPlan.Builder expect = DayPlan.newBuilder().setDayId(2)
        .addSpot(Util.createVisitSpot(2, 11L, "Castro St", null))
        .addSpot(Util.createVisitSpot(2, 10L, "旧金山市政厅", null))
        .addSpot(Util.createVisitSpot(1, 2L, "九曲花街", null))
        .addSpot(Util.createVisitSpot(1, 4L, "旧金山艺术宫", null))
        .addSpot(Util.createVisitSpot(1, 6L, "联合广场", null));
    Assert.assertEquals(actual, expect.build());
  }
}
