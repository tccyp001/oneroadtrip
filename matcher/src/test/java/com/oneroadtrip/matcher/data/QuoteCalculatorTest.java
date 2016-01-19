package com.oneroadtrip.matcher.data;

import java.util.List;
import java.util.Map;

import javax.inject.Named;

import junit.framework.Assert;

import org.javatuples.Pair;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.CityInfo;
import com.oneroadtrip.matcher.proto.Edge;
import com.oneroadtrip.matcher.proto.GuideInfo;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.VisitCity;
import com.oneroadtrip.matcher.proto.internal.CityConnectionInfo;
import com.oneroadtrip.matcher.testutil.GraphTestingUtil;
import com.oneroadtrip.matcher.util.Util;

public class QuoteCalculatorTest {
  Injector injector;

  @BeforeClass
  void setUp() {
    Map<Pair<Long, Long>, CityConnectionInfo> network = Maps.newTreeMap();
    GraphTestingUtil.addLink(network, 1, 2, 100, 0);
    GraphTestingUtil.addLink(network, 2, 3, 200, 0);
    GraphTestingUtil.addLink(network, 3, 4, 300, 0);
    GraphTestingUtil.addLink(network, 4, 5, 400, 0);
    GraphTestingUtil.addLink(network, 2, 5, 200, 0);
    
    injector = Guice.createInjector(new AbstractModule() {
      protected void configure() {}

      @Provides
      @Named(Constants.CITY_NETWORK)
      ImmutableMap<Pair<Long, Long>, CityConnectionInfo> provideCityNetwork() {
        return ImmutableMap.copyOf(Util.propagateNetwork(Lists.newArrayList(1L, 2L, 3L, 4L, 5L),
            network));
      }
    });
  }

  class ItineraryStruct {
    long startCity;
    long endCity;
    List<Long> visits;
    List<Integer> days;
    List<GuideInfo> guides;
    GuideInfo guideForWholeTrip;
    int numRoom;

    public Itinerary buildItinerary() {
      Preconditions.checkArgument(visits.size() == days.size());
      Preconditions.checkArgument(visits.size() == guides.size());
      List<VisitCity> cities = Lists.newArrayList();
      for (int i = 0; i < visits.size(); ++i) {
        cities.add(VisitCity.newBuilder().setCity(CityInfo.newBuilder().setCityId(visits.get(i)))
            .setNumDays(days.get(i)).addGuide(guides.get(i)).build());
      }
      List<Edge> edges = Lists.newArrayList(newEdge(startCity, visits.get(0)));
      for (int i = 0; i < visits.size() - 1; ++i) {
        edges.add(newEdge(visits.get(i), visits.get(i + 1)));
      }
      edges.add(newEdge(visits.get(visits.size() - 1), endCity));

      return Itinerary.newBuilder().addAllCity(cities).addAllEdge(edges).setNumRoom(numRoom)
          .addGuideForWholeTrip(guideForWholeTrip).build();
    }

    private Edge newEdge(long x, long y) {
      return Edge.newBuilder().setFromCity(CityInfo.newBuilder().setCityId(x).build())
          .setToCity(CityInfo.newBuilder().setCityId(y).build()).build();
    }
  }

  @Test
  public void test() throws OneRoadTripException {
    QuoteCalculator calc = injector.getInstance(QuoteCalculator.class);
    GuideInfo g0 = buildGuide(2L);
    GuideInfo g1 = buildGuide(4L);
    Itinerary itin = new ItineraryStruct() {
      {
        startCity = 1L;
        endCity = 5L;
        visits = Lists.newArrayList(2L, 3L, 4L);
        days = Lists.newArrayList(2, 3, 2);
        guides = Lists.newArrayList(g0, g0, g1);
        guideForWholeTrip = g0;
        numRoom = 2;
      }
    }.buildItinerary();

    Assert.assertEquals(QuoteCalculator.buildQuote(1950.0f, 1400.0f, 500.0f),
        calc.makeQuoteForOneGuide(itin));
    Assert.assertEquals(QuoteCalculator.buildQuote(3000.0f, 1700.0f, 300.0f),
        calc.makeQuoteForMultipleGuides(itin));
  }

  private GuideInfo buildGuide(long cityId) {
    return GuideInfo.newBuilder().setHostCity(CityInfo.newBuilder().setCityId(cityId).build())
        .build();
  }
}
