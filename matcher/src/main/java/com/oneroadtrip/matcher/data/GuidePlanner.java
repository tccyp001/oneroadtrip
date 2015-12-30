package com.oneroadtrip.matcher.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.oneroadtrip.matcher.OneRoadTripConfig;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.CityPlan;
import com.oneroadtrip.matcher.proto.GuidePlanErrorInfo;
import com.oneroadtrip.matcher.proto.GuidePlanRequest;
import com.oneroadtrip.matcher.proto.GuidePlanResponse;
import com.oneroadtrip.matcher.proto.Status;

public class GuidePlanner {
  private static final Logger LOG = LogManager.getLogger();
  public static final long NO_ACCEPTABLE_CANDIDATE = -1L;

  static class IntersectPredicate implements Predicate<Long> {
    final Set<Long> anotherSet;

    IntersectPredicate(Set<Long> anotherSet) {
      this.anotherSet = anotherSet;
    }

    @Override
    public boolean test(Long t) {
      return !anotherSet.contains(t);
    }

    public static IntersectPredicate of(Set<Long> anotherSet) {
      return new IntersectPredicate(anotherSet);
    }
  }

  final ImmutableMap<Long, ImmutableSet<Long>> cityToGuides;
  final ImmutableMap<Long, ImmutableSet<Long>> guideToInterests;
  final ImmutableMap<Long, Float> guideToScore;
  final DatabaseAccessor dbAccessor;
  final OneRoadTripConfig config;

  @Inject
  GuidePlanner(
      @Named(Constants.CITY_TO_GUIDES) ImmutableMap<Long, ImmutableSet<Long>> cityToGuides,
      @Named(Constants.GUIDE_TO_INTERESTS) ImmutableMap<Long, ImmutableSet<Long>> guideToInterests,
      @Named(Constants.GUIDE_TO_SCORE) ImmutableMap<Long, Float> guideToScore,
      DatabaseAccessor dbAccessor, OneRoadTripConfig config) {
    this.cityToGuides = cityToGuides;
    this.guideToInterests = guideToInterests;
    this.guideToScore = guideToScore;
    this.dbAccessor = dbAccessor;
    this.config = config;
  }

  // Filters
  Set<Long> matchGuidesByCities(List<Long> cityIds, List<Long> rejectedGuides) {
    Set<Long> guides = null;
    for (Long cityId : cityIds) {
      if (!cityToGuides.containsKey(cityId)) {
        LOG.error("Unknown city id ({})", cityId);
        continue;
      }
      Set<Long> guidesForCity = cityToGuides.get(cityId);
      if (guides == null) {
        guides = Sets.newTreeSet(guidesForCity);
      } else {
        guides.retainAll(guidesForCity);
      }
    }
    if (guides == null) {
      return Sets.newTreeSet();
    }

    guides.removeAll(rejectedGuides);
    return guides;
  }

  // Sorts
  List<Long> sortCandidates(Set<Long> guides, Set<Long> interests) {
    List<Long> sortedGuides = Lists.newArrayList(guides);
    Collections.sort(sortedGuides, new Comparator<Long>() {
      @Override
      public int compare(Long x, Long y) {
        Set<Long> xx = guideToInterests.get(x);
        Set<Long> yy = guideToInterests.get(y);
        if (xx == null && yy == null) {
          return compareScore(x, y);
        } else if (xx == null) {
          return 1;
        } else if (yy == null) {
          return -1;
        }
        int xSize = Sets.intersection(xx, interests).size();
        int ySize = Sets.intersection(yy, interests).size();
        return xSize == ySize ? compareScore(x, y) : ySize - xSize;
      }

      private int compareScore(Long x, Long y) {
        Float xs = guideToScore.get(x);
        Float ys = guideToScore.get(y);
        if (xs == null && ys == null) {
          return 0;
        } else if (xs == null) {
          return 1;
        } else if (ys == null) {
          return -1;
        }
        return (int) ((ys - xs) * 1000);
      }
    });

    return sortedGuides;
  }

  public GuidePlanResponse makeSingleGuidePlan(GuidePlanRequest request) {
    GuidePlanResponse.Builder builder = GuidePlanResponse.newBuilder();
    try {
      List<Long> cityIds = Lists.newArrayList();
      Set<Integer> days = Sets.newTreeSet();
      for (CityPlan cityPlan : request.getCityPlanList()) {
        if (!cityPlan.hasCityId() || !cityPlan.hasStartDate() || !cityPlan.hasNumDays())
          continue;
        cityIds.add(cityPlan.getCityId());
        for (int i = 0; i < cityPlan.getNumDays(); ++i) {
          days.add(cityPlan.getStartDate() + i);
        }
        builder.addCityPlan(cityPlan);
      }

      Set<Long> candidates = matchGuidesByCities(cityIds, request.getExcludedGuideIdList());
      List<Long> orderedCandidates = sortCandidates(candidates,
          Sets.newTreeSet(request.getInterestIdList()));
      int querySize = Math.min(orderedCandidates.size(), config.guideReservationQueryLimit);
      orderedCandidates = orderedCandidates.subList(0, querySize);
      long cutoffTimestamp = System.currentTimeMillis()
          - TimeUnit.SECONDS.toMillis(config.guideReservedSecondsForBook);
      Map<Long, Set<Integer>> guideToReserveDays = dbAccessor.loadGuideToReserveDays(
          orderedCandidates, cutoffTimestamp);
      builder.setGuideIdForWholeTrip(acceptCandidateByDates(
          orderedCandidates, days, guideToReserveDays));
    } catch (OneRoadTripException e) {
      builder.setStatus(Status.ERROR_IN_GUIDE_PLAN);
    }

    return builder.build();
  }

  public GuidePlanResponse makeMultiGuidePlan(GuidePlanRequest request) {
    GuidePlanResponse.Builder builder = GuidePlanResponse.newBuilder().setStatus(Status.SUCCESS);
    try {
      List<List<Long>> guides = Lists.newArrayList();
      Set<Long> allCandidates = Sets.newTreeSet();
      for (CityPlan cityPlan : request.getCityPlanList()) {
        Set<Long> candidates = matchGuidesByCities(Lists.newArrayList(cityPlan.getCityId()),
            cityPlan.getExcludedGuideIdList());
        List<Long> orderedCandidates = sortCandidates(candidates,
            Sets.newTreeSet(request.getInterestIdList()));
        List<Long> cutoffCandidates = orderedCandidates.subList(0,
            Math.min(orderedCandidates.size(), config.guideReservationQueryLimit));
        guides.add(cutoffCandidates);
        allCandidates.addAll(cutoffCandidates);
      }
      Preconditions.checkArgument(guides.size() == request.getCityPlanCount());
      long cutoffTimestamp = System.currentTimeMillis()
          - TimeUnit.SECONDS.toMillis(config.guideReservedSecondsForBook);
      Map<Long, Set<Integer>> guideToReservedDays = dbAccessor.loadGuideToReserveDays(
          allCandidates, cutoffTimestamp);

      for (int i = 0; i < guides.size(); ++i) {
        CityPlan old = request.getCityPlan(i);
        CityPlan.Builder subBuilder = CityPlan.newBuilder(old);
        try {
          Set<Integer> days = Sets.newTreeSet();
          for (int j = 0; j < old.getNumDays(); ++j) {
            days.add(old.getStartDate() + j);
          }
          subBuilder.setGuideId(acceptCandidateByDates(guides.get(i), days, guideToReservedDays));
        } catch (OneRoadTripException e) {
          // internal error
          subBuilder.setErrorInfo(GuidePlanErrorInfo.NOT_FOUND);
        }
        builder.addCityPlan(subBuilder);
      }
    } catch (OneRoadTripException e) {
      LOG.error("Errors in making multiple guide plan: {}", e);
      builder.setStatus(e.getStatus());
    }

    return builder.build();
  }

  static long acceptCandidateByDates(List<Long> candidates, Set<Integer> days,
      Map<Long, Set<Integer>> guideToReserveDays) throws OneRoadTripException {
    for (Long guide : candidates) {
      Set<Integer> reserved = guideToReserveDays.get(guide);
      if (reserved == null || Sets.intersection(reserved, days).size() == 0) {
        // Accepted
        return guide;
      }
    }
    throw new OneRoadTripException(Status.GUIDE_NOT_FOUND, null);
  }
}
