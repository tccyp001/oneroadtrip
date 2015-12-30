package com.oneroadtrip.matcher.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.oneroadtrip.matcher.DayPlan;
import com.oneroadtrip.matcher.ErrorInfo;
import com.oneroadtrip.matcher.SpotPlanRequest;
import com.oneroadtrip.matcher.SpotPlanResponse;
import com.oneroadtrip.matcher.Status;
import com.oneroadtrip.matcher.VisitSpot;

// 景点计划是根据城市来制定的，所以这个类对应的instance不是injectable的，而是根据城市生成的。
//
// 那么，有些外面的景点，譬如黄石什么的，是不是需要在对应景点附近弄个城市，然后把景点assign上去？
//
// thread-safe
public class SpotPlanner {
  private static final Logger LOG = LogManager.getLogger();

  private static final int VISIT_HOURS_PER_DAY = 7;

  final ImmutableMap<String, Long> spotNameToId;
  final ImmutableMap<Long, VisitSpot> spotIdToData;
  final ImmutableMap<Long, Set<Long>> interestToSpots;
  final ImmutableMap<Long, Float> spotToScore;

  public SpotPlanner(ImmutableMap<String, Long> spotNameToId,
      ImmutableMap<Long, VisitSpot> spotIdToData, ImmutableMap<Long, Set<Long>> interestToSpots,
      ImmutableMap<Long, Float> spotToScore) {
    this.spotNameToId = spotNameToId;
    this.spotIdToData = spotIdToData;
    this.interestToSpots = interestToSpots;
    this.spotToScore = spotToScore;
  }

  final Comparator<Long> comparator = new Comparator<Long>() {
    @Override
    public int compare(Long o1, Long o2) {
      if (!spotToScore.containsKey(o1)) {
        return 1;
      } else if (!spotToScore.containsKey(o2)) {
        return -1;
      }
      return (int) ((spotToScore.get(o2) - spotToScore.get(o1)) * 1000);
    }
  };

  int selectCandidates(DayPlan.Builder builder, Collection<Long> ids, int leftHours) {
    List<Long> candidates = Lists.newArrayList(ids);
    Collections.sort(candidates, comparator);
    for (Long candidate : candidates) {
      VisitSpot data = Preconditions.checkNotNull(spotIdToData.get(candidate));
      if (leftHours < data.getHours()) {
        continue;
      }
      builder.addSpot(data);
      leftHours -= data.getHours();
      if (leftHours == 0) {
        break;
      }
    }
    return leftHours;
  }

  DayPlan updateDayPlan(int dayId, DayPlan currentDayPlan, List<Long> interestIds,
      Set<Long> reservedSpotIds) {
    int leftHours = VISIT_HOURS_PER_DAY;

    DayPlan.Builder builder = DayPlan.newBuilder().setDayId(dayId);
    if (currentDayPlan != null) {
      for (VisitSpot spot : currentDayPlan.getSpotList()) {
        String spotName = spot.getSpotName();
        if (!spotNameToId.containsKey(spotName)) {
          builder.addSpot(VisitSpot.newBuilder(spot).setErrorInfo(ErrorInfo.UNKNOWN_SPOT_NAME));
          builder.addErrorInfo(ErrorInfo.UNKNOWN_SPOT_NAME);
          continue;
        }
        long spotId = spotNameToId.get(spotName);
        VisitSpot data = Preconditions.checkNotNull(spotIdToData.get(spotId));
        int hours = spot.getHours() == 0 ? data.getHours() : spot.getHours();
        builder.addSpot(VisitSpot.newBuilder(data).setHours(hours));
        leftHours -= hours;
      }
      if (leftHours < 0) {
        builder.addErrorInfo(ErrorInfo.OVER_ALLOCATED);
      }
    }

    if (leftHours <= 0) {
      return builder.build();
    }
    // 按照兴趣安排景点
    Set<Long> spotIds = Sets.newTreeSet();
    for (Long interest : interestIds) {
      if (!interestToSpots.containsKey(interest)) {
        LOG.error("Can't find interest id({}) in interest list", interest);
        continue;
      }
      spotIds.addAll(interestToSpots.get(interest));
    }
    spotIds.removeAll(reservedSpotIds);
    leftHours = selectCandidates(builder, spotIds, leftHours);

    if (leftHours <= 0) {
      return builder.build();
    }
    // 根据剩余景点按固定分数安排。
    Set<Long> otherSpots = Sets.newTreeSet(spotIdToData.keySet());
    otherSpots.removeAll(spotIds);
    otherSpots.removeAll(reservedSpotIds);
    leftHours = selectCandidates(builder, otherSpots, leftHours);

    if (leftHours > 0) {
      builder.addErrorInfo(ErrorInfo.LEFT_HOURS);
    }
    return builder.build();
  }

  public SpotPlanResponse planSpot(List<Long> interestIds, SpotPlanRequest request) {
    SpotPlanResponse.Builder builder = SpotPlanResponse.newBuilder().setCityId(request.getCityId());
    builder.addAllInterest(request.getInterestList());
    builder.setNumDays(request.getNumDays());
    if (request.getNumDays() <= 0) {
      builder.setStatus(Status.INCORRECT_REQUEST);
      return builder.build();
    }

    Set<Long> reservedSpotIds = Sets.newTreeSet();
    for (int i = 0; i < request.getNumDays(); ++i) {
      DayPlan currentDayPlan = DayPlan.newBuilder().build();
      if (i < request.getDayPlanCount()) {
        currentDayPlan = request.getDayPlan(i);
      }
      DayPlan newDayPlan = updateDayPlan(i + 1, currentDayPlan, interestIds, reservedSpotIds);
      builder.addDayPlan(newDayPlan);
      for (VisitSpot spot : newDayPlan.getSpotList()) {
        reservedSpotIds.add(spot.getSpotId());
      }
    }
    return builder.build();
  }
}
