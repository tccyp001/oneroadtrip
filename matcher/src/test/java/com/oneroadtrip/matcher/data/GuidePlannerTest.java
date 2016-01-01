package com.oneroadtrip.matcher.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.util.Util;

public class GuidePlannerTest {
  private static final Logger LOG = LogManager.getLogger();

  GuidePlanner guidePlanner;

  @BeforeClass
  void setUp() {
    ImmutableMap.Builder<Long, ImmutableSet<Long>> guideToCities = ImmutableMap.builder();
    guideToCities.put(1L, ImmutableSet.of(11L, 12L, 13L));
    guideToCities.put(2L, ImmutableSet.of(11L, 14L, 15L));
    guideToCities.put(3L, ImmutableSet.of(11L, 12L, 13L, 14L, 15L));
    guideToCities.put(4L, ImmutableSet.of(11L, 12L));
    guideToCities.put(5L, ImmutableSet.of(11L, 13L));
    guideToCities.put(6L, ImmutableSet.of(12L, 13L));
    guideToCities.put(7L, ImmutableSet.of(12L, 13L, 14L));

    ImmutableMap.Builder<Long, ImmutableSet<Long>> guideToInterests = ImmutableMap.builder();
    guideToInterests.put(1L, ImmutableSet.of(101L));
    guideToInterests.put(2L, ImmutableSet.of(101L, 102L));
    guideToInterests.put(3L, ImmutableSet.of(101L, 102L, 103L));
    guideToInterests.put(4L, ImmutableSet.of(102L));
    guideToInterests.put(5L, ImmutableSet.of(103L));
    guideToInterests.put(6L, ImmutableSet.of(101L, 103L));
    guideToInterests.put(7L, ImmutableSet.of(102L, 103L));

    ImmutableMap.Builder<Long, Float> guideToScore = ImmutableMap.builder();
    guideToScore.put(1L, 0.9f);
    guideToScore.put(2L, 0.9f);
    guideToScore.put(3L, 0.8f);
    guideToScore.put(4L, 0.7f);
    guideToScore.put(5L, 0.7f);
    guideToScore.put(6L, 0.6f);
    guideToScore.put(7L, 0.5f);

    guidePlanner = new GuidePlanner(Util.rotateMatrix(guideToCities.build()),
        guideToInterests.build(), guideToScore.build(), null, null);
  }

  @Test
  public void testCityFiltering() throws Exception {
    Assert.assertEquals(
        guidePlanner.matchGuidesByCities(Lists.newArrayList(11L, 12L, 13L), Lists.newArrayList()),
        ImmutableSet.of(1L, 3L));
    Assert.assertEquals(
        guidePlanner.matchGuidesByCities(Lists.newArrayList(11L, 14L, 15L), Lists.newArrayList()),
        ImmutableSet.of(2L, 3L));
    Assert.assertEquals(
        guidePlanner.matchGuidesByCities(Lists.newArrayList(11L, 13L, 15L), Lists.newArrayList()),
        ImmutableSet.of(3L));
    Assert.assertEquals(
        guidePlanner.matchGuidesByCities(Lists.newArrayList(16L), Lists.newArrayList()),
        ImmutableSet.of());
    Assert.assertEquals(
        guidePlanner.matchGuidesByCities(Lists.newArrayList(16L, 11L, 14L), Lists.newArrayList()),
        ImmutableSet.of(2L, 3L));

    Assert.assertEquals(
        guidePlanner.matchGuidesByCities(Lists.newArrayList(16L, 11L, 14L), Lists.newArrayList(2L)),
        ImmutableSet.of(3L));
  }

  @Test
  public void testSortCandidates() {
    Assert.assertEquals(
        guidePlanner.sortCandidates(ImmutableSet.of(1L, 3L, 5L), ImmutableSet.of(101L)),
        Lists.newArrayList(1L, 3L, 5L));
    Assert.assertEquals(
        guidePlanner.sortCandidates(ImmutableSet.of(1L, 3L, 5L), ImmutableSet.of(102L)),
        Lists.newArrayList(3L, 1L, 5L));
    Assert.assertEquals(
        guidePlanner.sortCandidates(ImmutableSet.of(1L, 5L, 6L), ImmutableSet.of(101L, 103L)),
        Lists.newArrayList(6L, 1L, 5L));
    Assert.assertEquals(
        guidePlanner.sortCandidates(ImmutableSet.of(4L, 5L, 6L), ImmutableSet.of(101L, 103L)),
        Lists.newArrayList(6L, 5L, 4L));

    Assert.assertEquals(
        guidePlanner.sortCandidates(ImmutableSet.of(1L, 2L, 3L, 4L, 5L, 6L, 7L),
            ImmutableSet.of(101L)), Lists.newArrayList(1L, 2L, 3L, 6L, 4L, 5L, 7L));
    Assert.assertEquals(
        guidePlanner.sortCandidates(ImmutableSet.of(1L, 2L, 3L, 4L, 5L, 6L, 7L),
            ImmutableSet.of(102L, 103L)), Lists.newArrayList(3L, 7L, 2L, 4L, 5L, 6L, 1L));
    Assert.assertEquals(
        guidePlanner.sortCandidates(ImmutableSet.of(1L, 2L, 3L, 4L, 5L, 6L, 7L),
            ImmutableSet.of(101L, 102L, 103L)), Lists.newArrayList(3L, 2L, 6L, 7L, 1L, 4L, 5L));
  }

  @Test
  public void testAcceptCandidateByDates() {
    try {
      Assert.assertEquals(
          GuidePlanner.acceptCandidateByDates(Lists.newArrayList(1L, 2L, 3L),
              ImmutableSet.of(20151225), ImmutableMap.of(1L, ImmutableSet.of(20151225))),
          Lists.newArrayList(2L, 3L));
      Assert.assertEquals(GuidePlanner.acceptCandidateByDates(Lists.newArrayList(1L, 2L, 3L),
          ImmutableSet.of(20151225, 20151226), ImmutableMap.of(1L, ImmutableSet.of(20151225), 2L,
              ImmutableSet.of(20151228), 3L, ImmutableSet.of(20151226))), Lists.newArrayList(2L));
    } catch (OneRoadTripException e) {
      LOG.error("testAcceptCandidateByDates", e);
      Assert.fail();
    }
    try {
      GuidePlanner.acceptCandidateByDates(Lists.newArrayList(1L, 2L, 3L),
          ImmutableSet.of(20151225, 20151226), ImmutableMap.of(1L, ImmutableSet.of(20151225), 2L,
              ImmutableSet.of(20151226, 20151228), 3L, ImmutableSet.of(20151226)));
      LOG.error("Should fail to find a guide");
      Assert.fail();
    } catch (OneRoadTripException e) {
      // pass
    }
  }
}
