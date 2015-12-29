package com.oneroadtrip.matcher.data;

import java.util.Optional;

import javax.inject.Named;
import javax.inject.Singleton;

import org.javatuples.Pair;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.oneroadtrip.matcher.CityResponse.City;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.internal.CityConnectionInfo;

public class PreloadedDataModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(PreloadedDataReloader.class);
    bind(PreloadedData.Manager.class).in(Singleton.class);
    bind(PreloadedData.class).toProvider(PreloadedData.Manager.class);

    bind(CityPlanner.class);
  }
  
  @Provides
  ImmutableMap<Long, City> provideCityIdToInfo(PreloadedData data) {
    return data.getCityIdToInfo();
  }

  @Provides
  @Named(Constants.CITY_NETWORK)
  ImmutableMap<Pair<Long, Long>, CityConnectionInfo> provideCityNetwork(PreloadedData data) {
    return data.getCityNetwork();
  }

  @Provides
  @Named(Constants.SUGGEST_DAYS_FOR_CITIES)
  ImmutableMap<Long, Integer> getSuggestDaysForCities(PreloadedData data) {
    return data.getSuggestDaysForCities();
  }
  
  @Provides
  ImmutableMap<Long, SpotPlanner> provideSpotPlannerProvider(PreloadedData data) {
    return data.getCityIdToSpotPlanner();
  }
  
  @Provides
  @Named(Constants.INTEREST_NAME_TO_ID)
  ImmutableMap<String, Long> provideInterestNameToId(PreloadedData data) {
    return data.getInterestNameToId();
  }
  
  @Provides
  @Named(Constants.CITY_TO_GUIDES)
  ImmutableMap<Long, ImmutableSet<Long>> providesCityToGuides(PreloadedData data) {
    return data.getCityToGuides();
  }
  
  @Provides
  @Named(Constants.GUIDE_TO_INTERESTS)
  ImmutableMap<Long, ImmutableSet<Long>> providesGuideToInterests(PreloadedData data) {
    return data.getGuideToInterests();
  }
  
  @Provides
  @Named(Constants.GUIDE_TO_SCORE)
  ImmutableMap<Long, Float> providesGuideToScore(PreloadedData data) {
    return data.getGuideToScore();
  }

  @Provides
  Optional<CityPlanner> provideOptionalCityPlanner(CityPlanner cityPlanner) {
    return Optional.ofNullable(cityPlanner);
  }
}
