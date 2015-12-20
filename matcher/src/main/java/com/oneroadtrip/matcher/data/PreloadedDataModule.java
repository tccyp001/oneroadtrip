package com.oneroadtrip.matcher.data;

import java.util.Optional;

import javax.inject.Named;
import javax.inject.Singleton;

import org.javatuples.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.handlers.CityRequestHandler;
import com.oneroadtrip.matcher.handlers.PlanRequestHandler;
import com.oneroadtrip.matcher.internal.CityConnectionInfo;

public class PreloadedDataModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(PreloadedData.Reloader.class);
    bind(PreloadedData.Manager.class).in(Singleton.class);
    bind(PreloadedData.class).toProvider(PreloadedData.Manager.class);

    bind(CityPlanner.class);

    bind(CityRequestHandler.class);
    bind(PlanRequestHandler.class);
  }

  @Provides
  @Named(Constants.ALL_CITY_IDS)
  ImmutableList<Long> provideAllCityIds(PreloadedData data) {
    return data.getAllCityIds();
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
  Optional<CityPlanner> provideOptionalCityPlanner(CityPlanner cityPlanner) {
    return Optional.ofNullable(cityPlanner);
  }
}
