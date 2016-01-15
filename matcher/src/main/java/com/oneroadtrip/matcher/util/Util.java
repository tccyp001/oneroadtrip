package com.oneroadtrip.matcher.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.CityInfo;
import com.oneroadtrip.matcher.proto.ErrorInfo;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.SpotInfo;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.proto.VisitCity;
import com.oneroadtrip.matcher.proto.VisitSpot;
import com.oneroadtrip.matcher.proto.internal.CityConnectionInfo;
import com.oneroadtrip.matcher.proto.internal.EngageType;
import com.oneroadtrip.matcher.proto.internal.SuggestCityInfo;

public class Util {
  private static final Logger LOG = LogManager.getLogger();

  public static Map<Pair<Long, Long>, CityConnectionInfo> propagateNetwork(Collection<Long> nodes,
      Map<Pair<Long, Long>, CityConnectionInfo> network) {
    for (Long k : nodes) {
      for (Long i : nodes) {
        for (Long j : nodes) {
          CityConnectionInfo ij = network.get(Pair.with(i, j));
          CityConnectionInfo ik = network.get(Pair.with(i, k));
          CityConnectionInfo kj = network.get(Pair.with(k, j));
          if (ik == null || kj == null || i == j) {
            continue;
          }

          if (ij == null || ij.getDistance() > ik.getDistance() + kj.getDistance()) {
            network.put(Pair.with(i, j),
                CityConnectionInfo.newBuilder().setDistance(ik.getDistance() + kj.getDistance())
                    .setHours(ik.getHours() + kj.getHours()).build());
          }
        }
      }
    }
    
    for (long i : nodes) {
      network.put(Pair.with(i, i), CityConnectionInfo.newBuilder().setDistance(0).setHours(0)
          .build());
    }

    return network;
  }

  public static SuggestCityInfo createSuggestCityInfo(Long cityId, int index,
      EngageType engageType, int min) {
    return SuggestCityInfo.newBuilder().setCityId(cityId).setEngageToPathIndex(index)
        .setEngageType(engageType).setAdditionalDistance(min).build();
  }

  public static CityConnectionInfo createConnectionInfo(int distance, int hours) {
    return CityConnectionInfo.newBuilder().setDistance(distance).setHours(hours).build();
  }

  public static VisitSpot createVisitSpot(int hours, SpotInfo info, ErrorInfo errorInfo) {
    VisitSpot.Builder builder = VisitSpot.newBuilder().setHours(hours).setInfo(info);
    if (errorInfo != null) {
      builder.setErrorInfo(errorInfo);
    }
    return builder.build();
  }

  private static final String INTEREST_SPLITTOR = Pattern.quote("|");

  public static List<Long> getInterestIds(String interests, Map<String, Long> interestNameToId) {
    List<Long> ids = Lists.newArrayList();
    if (interests == null) {
      return ids;
    }
    for (String name : interests.split(INTEREST_SPLITTOR)) {
      if (name.isEmpty()) {
        continue;
      }
      if (!interestNameToId.containsKey(name)) {
        LOG.error("Can't find interest by name ({})", name);
        continue;
      }
      ids.add(interestNameToId.get(name));
    }
    return ids;
  }

  public static ImmutableMap<Long, ImmutableSet<Long>> rotateMatrix(
      ImmutableMap<Long, ImmutableSet<Long>> a) {
    Map<Long, ImmutableSet.Builder<Long>> b = Maps.newTreeMap();
    for (Map.Entry<Long, ImmutableSet<Long>> e : a.entrySet()) {
      Long x = e.getKey();
      for (Long y : e.getValue()) {
        if (!b.containsKey(y)) {
          b.put(y, ImmutableSet.builder());
        }
        b.get(y).add(x);
      }
    }

    ImmutableMap.Builder<Long, ImmutableSet<Long>> builder = ImmutableMap.builder();
    for (Map.Entry<Long, ImmutableSet.Builder<Long>> e : b.entrySet()) {
      builder.put(e.getKey(), e.getValue().build());
    }
    return builder.build();
  }

  private static final CityInfo UNKNOWN_CITY = CityInfo.newBuilder().setCityId(0L)
      .setName("UNKNWON").setCnName("无名").build();

  public static CityInfo getCityInfo(ImmutableMap<Long, CityInfo> cityIdToInfo, long cityId) {
    CityInfo city = cityIdToInfo.get(cityId);
    if (city == null) {
      LOG.info("Can't find city info for id {}", cityId);
      return UNKNOWN_CITY;
    }
    return CityInfo.newBuilder().setCityId(cityId).setName(city.getName())
        .setCnName(city.getCnName()).build();
  }

  public static int advanceDays(int currentDate, int numDays) {
    int year = currentDate / 10000;
    int month = currentDate / 100 % 100;
    int date = currentDate % 100;
    Calendar cal = new GregorianCalendar(year, month - 1, date);
    cal.add(Calendar.DATE, numDays);

    int nYear = cal.get(Calendar.YEAR);
    int nMonth = cal.get(Calendar.MONTH) + 1;
    int nDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
    return nYear * 10000 + nMonth * 100 + nDayOfMonth;
  }

  public static String trimString(String origin) {
    return origin.replaceAll("\\p{Cntrl}", "").trim();
  }

  public static List<String> splitString(String origin) {
    List<String> result = Lists.newArrayList();
    for (String part : origin.split("[ /|]")) {
      result.add(trimString(part));
    }
    return result;
  }

  public static List<Pair<Long, Integer>> getGuideReservationMap(Itinerary itin) {
    List<Pair<Long, Integer>> result = Lists.newArrayList();
    for (VisitCity visit : itin.getCityList()) {
      for (int i = 0; i < visit.getNumDays(); ++i) {
        int date = Util.advanceDays(visit.getStartDate(), i);
        if (itin.getChooseOneGuideSolution()) {
          result.add(Pair.with(ItineraryUtil.getGuideId(itin.getGuideForWholeTrip()), date));
        } else {
          result.add(Pair.with(ItineraryUtil.getGuideId(visit.getGuide(0)), date));
        }
      }
    }
    return result;
  }
  
  public static String getQuestionMarksForSql(int n) {
    StringJoiner joiner = new StringJoiner(",");
    for (int i = 0; i < n; ++i) {
      joiner.add("?");
    }
    return joiner.toString();
  }
  
  public static LocalDate getDateByInt(int date) {
    return LocalDate.of(date / 10000, date / 100 % 100, date % 100);
  }

  public static int calculateDaysByStartEndDate(int startdate, int enddate) throws OneRoadTripException {
    try {
      LocalDate start = getDateByInt(startdate);
      LocalDate end = getDateByInt(enddate);

      return (int) ChronoUnit.DAYS.between(start, end) + 1;
    } catch (RuntimeException e) {
      throw new OneRoadTripException(Status.ERROR_IN_GUIDE_PLAN, e);
    }
  }
}
