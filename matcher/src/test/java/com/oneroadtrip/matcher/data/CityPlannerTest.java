package com.oneroadtrip.matcher.data;

import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.javatuples.Pair;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.oneroadtrip.matcher.CityResponse.City;
import com.oneroadtrip.matcher.Edge;
import com.oneroadtrip.matcher.PlanResponse;
import com.oneroadtrip.matcher.Status;
import com.oneroadtrip.matcher.VisitCity;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.internal.CityConnectionInfo;
import com.oneroadtrip.matcher.internal.EngageType;
import com.oneroadtrip.matcher.testutil.GraphTestingUtil;
import com.oneroadtrip.matcher.util.Util;

public class CityPlannerTest {
  Injector injector;
  ImmutableMap<Long, City> cityIdToInfo;

  @BeforeClass
  void setUp() {
    ImmutableMap.Builder<Long, City> cityIdToInfoBuilder = ImmutableMap.builder();
    cityIdToInfoBuilder.put(1L, City.newBuilder().setName("AA").build());
    cityIdToInfoBuilder.put(2L, City.newBuilder().setName("BB").build());
    cityIdToInfoBuilder.put(3L, City.newBuilder().setName("CC").build());
    cityIdToInfoBuilder.put(4L, City.newBuilder().setName("DD").build());
    cityIdToInfoBuilder.put(5L, City.newBuilder().setName("EE").build());
    cityIdToInfoBuilder.put(6L, City.newBuilder().setName("FF").build());
    cityIdToInfoBuilder.put(7L, City.newBuilder().setName("GG").build());
    cityIdToInfoBuilder.put(8L, City.newBuilder().setName("HH").build());
    cityIdToInfo = cityIdToInfoBuilder.build();
    
    Map<Long, Integer> suggestDaysForCities = Maps.newTreeMap();
    suggestDaysForCities.put(1L, 2);
    suggestDaysForCities.put(2L, 3);
    suggestDaysForCities.put(3L, 4);
    suggestDaysForCities.put(4L, 2);
    suggestDaysForCities.put(5L, 4);
    suggestDaysForCities.put(6L, 1);
    suggestDaysForCities.put(7L, 2);
    suggestDaysForCities.put(8L, 2);
    
    Map<Pair<Long, Long>, CityConnectionInfo> cityNetwork = Maps.newTreeMap();
    GraphTestingUtil.addLink(cityNetwork, 1L, 2L, 10, 1);
    GraphTestingUtil.addLink(cityNetwork, 1L, 3L, 10, 1);
    GraphTestingUtil.addLink(cityNetwork, 2L, 3L, 17, 1);
    GraphTestingUtil.addLink(cityNetwork, 2L, 4L, 10, 1);
    GraphTestingUtil.addLink(cityNetwork, 2L, 5L, 15, 1);
    GraphTestingUtil.addLink(cityNetwork, 3L, 4L, 20, 1);
    GraphTestingUtil.addLink(cityNetwork, 4L, 5L, 10, 1);
    GraphTestingUtil.addLink(cityNetwork, 4L, 6L, 10, 1);
    GraphTestingUtil.addLink(cityNetwork, 5L, 7L, 11, 1);
    GraphTestingUtil.addLink(cityNetwork, 6L, 7L, 20, 1);
    GraphTestingUtil.addLink(cityNetwork, 2L, 8L, 15, 1);
    GraphTestingUtil.addLink(cityNetwork, 5L, 8L, 15, 1);
    
    for (Long cityId : suggestDaysForCities.keySet()) {
      GraphTestingUtil.addLink(cityNetwork, cityId, cityId, 0, 0);
    }

    injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
      }

      @Provides
      ImmutableMap<Long, City> provideCityIdToInfo() {
        return cityIdToInfo;
      }
      
      @Provides
      @Named(Constants.CITY_NETWORK)
      ImmutableMap<Pair<Long, Long>, CityConnectionInfo> provideCityNetwork() {
        return ImmutableMap.copyOf(cityNetwork);
      }

      @Provides
      @Named(Constants.SUGGEST_DAYS_FOR_CITIES)
      ImmutableMap<Long, Integer> getSuggestDaysForCities() {
        return ImmutableMap.copyOf(suggestDaysForCities);
      }
    });
  }

  @Test
  public void testChooseOtherCities() throws Exception {
    List<Long> chosenPath = Lists.newArrayList(1L, 1L, 3L, 4L, 5L, 2L, 2L);
    CityPlanner cityPlanner = injector.getInstance(CityPlanner.class);
    Assert.assertEquals(
        cityPlanner.chooseOtherCities(20L, chosenPath),
        ImmutableMap.of(6L, Util.createSuggestCityInfo(6L, 3, EngageType.ON_NODE, 20), 8L,
            Util.createSuggestCityInfo(8L, 4, EngageType.ON_EDGE, 15)));
  }
  
  String getNameById(long id) {
    return cityIdToInfo.get(id).getName();
  }

  VisitCity createVisitCity(long cityId, int numDays, float suggestRate) {
    return VisitCity.newBuilder().setCityId(cityId).setCityName(getNameById(cityId))
        .setNumDays(numDays).setSuggestRate(suggestRate).build();
  }

  private Edge createEdge(long from, long to, int distance, int hours) {
    return Edge.newBuilder().setFromCityId(from).setFromCity(getNameById(from)).setToCityId(to)
        .setToCity(getNameById(to)).setDistance(distance).setHours(hours).build();
  }

  @Test
  public void testBuildResponse() throws Exception {
    CityPlanner cityPlanner = injector.getInstance(CityPlanner.class);

    PlanResponse expected = PlanResponse.newBuilder().setStatus(Status.SUCCESS)
        .setStartCityId(1L).setStartCity("AA")
        .setEndCityId(2L).setEndCity("BB")
        .addVisit(createVisitCity(1L, 2, 1.0f))
        .addVisit(createVisitCity(2L, 3, 1.0f))
        .addVisit(createVisitCity(3L, 2, 1.0f))
        .addVisit(createVisitCity(4L, 2, 1.0f))
        .addVisit(createVisitCity(5L, 4, 1.0f))
        .addEdge(createEdge(1L, 1L, 0, 0))
        .addEdge(createEdge(1L, 3L, 10, 1))
        .addEdge(createEdge(3L, 4L, 20, 1))
        .addEdge(createEdge(4L, 5L, 10, 1))
        .addEdge(createEdge(5L, 2L, 15, 1))
        .addEdge(createEdge(2L, 2L, 0, 0))
        .addSuggestCity(createVisitCity(6L, 1, 0.6363636f))
        .addSuggestCity(createVisitCity(8L, 2, 0.72727275f))
        .build();
    Assert.assertEquals(
        cityPlanner.buildResponse(
            1L,
            2L,
            Lists.newArrayList(
                createVisitCity(1L, 0, 0.0f),
                createVisitCity(2L, 0, 0.0f),
                createVisitCity(3L, 2, 0.0f),
                createVisitCity(4L, 0, 0.5f),
                createVisitCity(5L, 0, 0.0f)),
            55,
            Lists.newArrayList(1L, 1L, 3L, 4L, 5L, 2L, 2L),
            ImmutableMap.of(6L, Util.createSuggestCityInfo(6L, 3, EngageType.ON_NODE, 20),
                8L, Util.createSuggestCityInfo(8L, 4, EngageType.ON_EDGE, 15))).build(),
        expected);
  }

}
