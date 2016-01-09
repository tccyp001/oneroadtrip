package com.oneroadtrip.matcher.data;

import java.util.List;

import org.javatuples.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.oneroadtrip.matcher.proto.GuideInfo;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.VisitCity;

public class DatabaseAccessorTest {
  @Test
  public void testGetGuideReservationMap() throws Exception {
    Itinerary itin = Itinerary
        .newBuilder()
        .addCity(
            VisitCity.newBuilder().setStartDate(20151229).setNumDays(2)
                .addGuide(GuideInfo.newBuilder().setGuideId(1L)))
        .addCity(
            VisitCity.newBuilder().setStartDate(20151231).setNumDays(3)
                .addGuide(GuideInfo.newBuilder().setGuideId(2L)))
        .addCity(
            VisitCity.newBuilder().setStartDate(20160103).setNumDays(2)
                .addGuide(GuideInfo.newBuilder().setGuideId(1L))).build();
    int[] dates = { 20151229, 20151230, 20151231, 20160101, 20160102, 20160103, 20160104 };
    {
      long[] guideIds = { 1L, 1L, 2L, 2L, 2L, 1L, 1L };
      List<Pair<Long, Integer>> expected = Lists.newArrayList();
      for (int i = 0; i < guideIds.length; ++i) {
        expected.add(Pair.with(guideIds[i], dates[i]));
      }
      Assert.assertEquals(expected, DatabaseAccessor.getGuideReservationMap(itin));
    }

    {
      Itinerary itin2 = Itinerary.newBuilder(itin).setChooseOneGuideSolution(true)
          .setGuideForWholeTrip(GuideInfo.newBuilder().setGuideId(3L)).build();
      List<Pair<Long, Integer>> expected = Lists.newArrayList();
      for (int i = 0; i < dates.length; ++i) {
        expected.add(Pair.with(3L, dates[i]));
      }
      Assert.assertEquals(expected, DatabaseAccessor.getGuideReservationMap(itin2));
    }
  }
}
