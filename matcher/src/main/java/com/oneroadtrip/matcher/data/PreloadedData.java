package com.oneroadtrip.matcher.data;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.oneroadtrip.matcher.OneRoadTripConfig;
import com.oneroadtrip.matcher.proto.CityInfo;
import com.oneroadtrip.matcher.proto.GuideInfo;
import com.oneroadtrip.matcher.proto.SpotInfo;
import com.oneroadtrip.matcher.proto.internal.CityConnectionInfo;

// Thread-safe
public class PreloadedData {
  private static final Logger LOG = LogManager.getLogger();

  public static class Manager implements Provider<PreloadedData> {
    final OneRoadTripConfig config;
    
    // TODO(xfguo): (P2) Use "volatile" to handle the variable, corresponding doc:
    // http://www.ibm.com/developerworks/cn/java/j-jtp06197.html
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
      LOG.info("Reload database every {} seconds", config.preload_period_in_seconds);
      // TODO(xfguo): (P1) Name the threads.
      Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          try {
            atomicSetData(reloader.reload());
          } catch (RuntimeException re) {
            LOG.error("Runtime errors in reloading data", re);
          }
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
//          LOG.info("Wait for reloading data...");
          Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (InterruptedException e) {
          // No interrupt in the sleep.
          LOG.error("Catch up an interrupted exception in sleep of waiting of preloaded data", e);
        }
      }
    }
  }

  final ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork;
  final ImmutableMap<Long, Integer> suggestDaysForCities;
  final ImmutableMap<Long, SpotPlanner> cityIdToSpotPlanner;
  final ImmutableMap<String, Long> interestNameToId;
  final ImmutableMap<Long, ImmutableSet<Long>> cityToGuides;
  final ImmutableMap<Long, ImmutableSet<Long>> guideToInterests;
  final ImmutableMap<Long, Float> guideToScore;
  final ImmutableMap<Long, CityInfo> cityIdToInfo;
  final ImmutableMap<Long, GuideInfo> guideIdToInfo;
  final ImmutableMap<Long, SpotInfo> spotIdToInfo;
  final ImmutableMap<String, Long> tokenToUserId;

  PreloadedData(ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork,
      ImmutableMap<Long, Integer> suggestDaysForCities,
      ImmutableMap<Long, SpotPlanner> cityIdToSpotPlanner,
      ImmutableMap<String, Long> interestNameToId,
      ImmutableMap<Long, ImmutableSet<Long>> cityToGuides,
      ImmutableMap<Long, ImmutableSet<Long>> guideToInterests,
      ImmutableMap<Long, Float> guideToScore,
      ImmutableMap<Long, CityInfo> cityIdToInfo,
      ImmutableMap<Long, GuideInfo> guideIdToInfo,
      ImmutableMap<Long, SpotInfo> spotIdToInfo,
      ImmutableMap<String, Long> tokenToUserId) {
    this.cityNetwork = cityNetwork;
    this.suggestDaysForCities = suggestDaysForCities;
    this.cityIdToSpotPlanner = cityIdToSpotPlanner;
    this.interestNameToId = interestNameToId;
    this.cityToGuides = cityToGuides;
    this.guideToInterests = guideToInterests;
    this.guideToScore = guideToScore;
    this.cityIdToInfo = cityIdToInfo;
    this.guideIdToInfo = guideIdToInfo;
    this.spotIdToInfo = spotIdToInfo;
    this.tokenToUserId = tokenToUserId;
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

  public ImmutableMap<Long, ImmutableSet<Long>> getCityToGuides() {
    return cityToGuides;
  }

  public ImmutableMap<Long, ImmutableSet<Long>> getGuideToInterests() {
    return guideToInterests;
  }

  public ImmutableMap<Long, Float> getGuideToScore() {
    return guideToScore;
  }
  
  public ImmutableMap<Long, CityInfo> getCityIdToInfo() {
    return cityIdToInfo;
  }
  
  public ImmutableMap<Long, GuideInfo> getGuideIdToInfo() {
    return guideIdToInfo;
  }
  
  public ImmutableMap<Long, SpotInfo> getSpotIdToInfo() {
    return spotIdToInfo;
  }
  
  public ImmutableMap<String, Long> getTokenToUserId() {
    return tokenToUserId;
  }
}
