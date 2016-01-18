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
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.proto.CityInfo;
import com.oneroadtrip.matcher.proto.Edge;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.PlanResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.proto.VisitCity;
import com.oneroadtrip.matcher.proto.internal.CityConnectionInfo;
import com.oneroadtrip.matcher.proto.internal.EngageType;
import com.oneroadtrip.matcher.testutil.GraphTestingUtil;
import com.oneroadtrip.matcher.testutil.TestingUtil;
import com.oneroadtrip.matcher.util.Util;

public class CityPlannerTest {
  Injector injector;
  ImmutableMap<Long, CityInfo> cityIdToInfo;
  
  CityInfo getCityInfo(long cityId) {
    return Util.getCityInfo(cityIdToInfo, cityId);
  }

  @BeforeClass
  void setUp() {
    ImmutableMap.Builder<Long, CityInfo> cityIdToInfoBuilder = ImmutableMap.builder();
    TestingUtil.addCity(cityIdToInfoBuilder, 1L, "AA", "甲");
    TestingUtil.addCity(cityIdToInfoBuilder, 2L, "BB", "乙");
    TestingUtil.addCity(cityIdToInfoBuilder, 3L, "CC", "丙");
    TestingUtil.addCity(cityIdToInfoBuilder, 4L, "DD", "丁");
    TestingUtil.addCity(cityIdToInfoBuilder, 5L, "EE", "戊");
    TestingUtil.addCity(cityIdToInfoBuilder, 6L, "FF", "己");
    TestingUtil.addCity(cityIdToInfoBuilder, 7L, "GG", "庚");
    TestingUtil.addCity(cityIdToInfoBuilder, 8L, "HH", "辛");
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
      ImmutableMap<Long, CityInfo> provideCityIdToInfo() {
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

  VisitCity createVisitCity(long cityId, int numDays, float suggestRate) {
    VisitCity.Builder builder = VisitCity.newBuilder();
    if (numDays > 0) {
      builder.setNumDays(numDays);
    }
    return builder.setCity(getCityInfo(cityId)).setSuggestRate(suggestRate).build();
  }

  private Edge createEdge(long from, long to, int distance, int hours) {
    return Edge.newBuilder().setFromCity(getCityInfo(from)).setToCity(getCityInfo(to))
        .setDistance(distance).setHours(hours).build();
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

  @Test
  public void testBuildResponse() throws Exception {
    CityPlanner cityPlanner = injector.getInstance(CityPlanner.class);
    Itinerary oriItin = Itinerary.newBuilder().setEndCity(CityInfo.newBuilder().setCityId(1))
        .setNumPeople(2).setNumRoom(2).setHotel(5).setStartdate(20160121).setEnddate(20160128)
        .addCity(createVisitCity(1L, 2, 1.0f)).build();

    Itinerary itin = Itinerary.newBuilder(CityPlanner.cleanupCityPlanRelatedFields(oriItin))
        .setStartCity(getCityInfo(1L))
        .setEndCity(getCityInfo(2L))
        .addCity(createVisitCity(1L, 2, 1.0f))
        .addCity(createVisitCity(2L, 3, 1.0f))
        .addCity(createVisitCity(3L, 2, 1.0f))
        .addCity(createVisitCity(4L, 2, 1.0f))
        .addCity(createVisitCity(5L, 4, 1.0f))
        .addEdge(createEdge(1L, 1L, 0, 0))
        .addEdge(createEdge(1L, 3L, 10, 1))
        .addEdge(createEdge(3L, 4L, 20, 1))
        .addEdge(createEdge(4L, 5L, 10, 1))
        .addEdge(createEdge(5L, 2L, 15, 1))
        .addEdge(createEdge(2L, 2L, 0, 0))
        .addSuggestCity(createVisitCity(6L, 1, 0.6363636f))
        .addSuggestCity(createVisitCity(8L, 2, 0.72727275f))
        .build();
    PlanResponse expected = PlanResponse.newBuilder().setStatus(Status.SUCCESS).setItinerary(itin)
        .build();
    Assert.assertEquals(
        cityPlanner.buildResponse(
            CityPlanner.cleanupCityPlanRelatedFields(oriItin),
            100,
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
                8L, Util.createSuggestCityInfo(8L, 4, EngageType.ON_EDGE, 15))),
        expected);
  }

  @Test
  public void testCalculateDaysForVisit() throws Exception {
    CityPlanner cityPlanner = injector.getInstance(CityPlanner.class);
    List<VisitCity> cities = Lists.newArrayList(
        createVisitCity(1L, 2, 1.0f),
        createVisitCity(2L, 0, 1.0f),
        createVisitCity(3L, 0, 1.0f));
    Assert.assertEquals(cityPlanner.calculateDaysForVisit(3, cities),
        Lists.newArrayList(2, 0, 1));
    Assert.assertEquals(cityPlanner.calculateDaysForVisit(4, cities),
        Lists.newArrayList(2, 1, 1));
    Assert.assertEquals(cityPlanner.calculateDaysForVisit(5, cities),
        Lists.newArrayList(2, 1, 2));
    Assert.assertEquals(cityPlanner.calculateDaysForVisit(6, cities),
        Lists.newArrayList(2, 2, 2));
    Assert.assertEquals(cityPlanner.calculateDaysForVisit(7, cities),
        Lists.newArrayList(2, 2, 3));
    Assert.assertEquals(cityPlanner.calculateDaysForVisit(8, cities),
        Lists.newArrayList(2, 3, 3));
    Assert.assertEquals(cityPlanner.calculateDaysForVisit(10, cities),
        Lists.newArrayList(2, 3, 4));
  }
}
