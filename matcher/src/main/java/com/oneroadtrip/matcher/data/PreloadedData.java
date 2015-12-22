package com.oneroadtrip.matcher.data;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.google.common.collect.ImmutableMap;
import com.oneroadtrip.matcher.OneRoadTripConfig;
import com.oneroadtrip.matcher.internal.CityConnectionInfo;

// Thread-safe
public class PreloadedData {
  private static final Logger LOG = LogManager.getLogger();

  public static class Manager implements Provider<PreloadedData> {
    OneRoadTripConfig config;
    PreloadedData data_ = null;

    synchronized PreloadedData atomicGetData() {
      return data_;
    }

    synchronized void atomicSetData(PreloadedData data) {
      this.data_ = data;
    }

    @Inject
    public Manager(OneRoadTripConfig config, PreloadedDataReloader reloader) {
      this.config = config;
      Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          // TODO(xfguo): confirm the replacement is atomic. (Do we need to make
          // sure this?)
          atomicSetData(reloader.reload());
        }
      }, 0, config.preload_period_in_seconds, TimeUnit.SECONDS);
    }

    @Override
    public PreloadedData get() {
      while (true) {
        PreloadedData data = atomicGetData();
        if (data != null) {
          return data;
        }
        // Sleep one second to check data again.
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (InterruptedException e) {
          // No interrupt in the sleep.
          LOG.error("Catch up an interrupted exception in sleep of waiting of preloaded data", e);
        }
      }
    }
  }

  ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork;
  ImmutableMap<Long, Integer> suggestDaysForCities;
  ImmutableMap<Long, SpotPlanner> cityIdToSpotPlanner;
  ImmutableMap<String, Long> interestNameToId;
  
  PreloadedData(
      ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork,
      ImmutableMap<Long, Integer> suggestDaysForCities,
      ImmutableMap<Long, SpotPlanner> cityIdToSpotPlanner,
      ImmutableMap<String, Long> interestNameToId) {
    this.cityNetwork = cityNetwork;
    this.suggestDaysForCities = suggestDaysForCities;
    this.cityIdToSpotPlanner = cityIdToSpotPlanner;
    this.interestNameToId = interestNameToId;
  }

  public ImmutableMap<Pair<Long, Long>, CityConnectionInfo> getCityNetwork() {
    return cityNetwork;
  }

  public ImmutableMap<Long, Integer> getSuggestDaysForCities() {
    return suggestDaysForCities;
  }
  
  public ImmutableMap<Long, SpotPlanner> getCityIdToSpotPlanner() {
    return cityIdToSpotPlanner;
  }

  public ImmutableMap<String, Long> getInterestNameToId() {
    return interestNameToId;
  }
}
