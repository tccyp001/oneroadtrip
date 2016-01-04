package com.oneroadtrip.matcher.data;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.Edge;
import com.oneroadtrip.matcher.proto.GuideInfo;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.Quote;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.proto.VisitCity;
import com.oneroadtrip.matcher.proto.internal.CityConnectionInfo;

public class QuoteCalculator {
  private static final Logger LOG = LogManager.getLogger();

  // TODO(xfguo): We temporarily set hotel price as $100 before we enable the
  // hotel API.
  private static final float TEMP_HOTEL_COST_UNIT = 100.0f;
  private static final float TEMP_GUIDE_HOTEL_COST_UNIT = 100.0f;
  private static final float GUIDE_COST_PER_MILE = 1.5f;

  @Inject
  @Named(Constants.CITY_NETWORK)
  ImmutableMap<Pair<Long, Long>, CityConnectionInfo> cityNetwork;

  private int getConnectionDistance(long from, long to) throws OneRoadTripException {
    CityConnectionInfo connInfo = cityNetwork.get(Pair.with(from, to));
    if (connInfo == null) {
      throw new OneRoadTripException(Status.NO_CONNECTION_BETWEEN_CITIES,
          new Exception(String.format("no connection for edge (%d => %d)", from, to)));
    }
    return connInfo.getDistance();
  }

  private int getDistanceForGuide(long cityId, Edge edge) throws OneRoadTripException {
    long from = edge.getFromCity().getCityId();
    long to = edge.getToCity().getCityId();
    int line1 = getConnectionDistance(cityId, from);
    int line2 = getConnectionDistance(from, to);
    int line3 = getConnectionDistance(to, cityId);
    return line1 + line2 + line3;
  }


  public Quote makeQuoteForOneGuide(Itinerary itinerary) throws OneRoadTripException {
    if (!itinerary.hasGuideForWholeTrip()) {
      return null;
    }

    try {
      // 计算导游走的路线长度，然后乘以COST_PER_MILE.
      int distance = 0;
      List<Edge> edges = itinerary.getEdgeList();
      for (Edge edge : edges) {
        long from = edge.getFromCity().getCityId();
        long to = edge.getToCity().getCityId();
        distance += getConnectionDistance(from, to);
      }
      long startCityId = edges.get(0).getFromCity().getCityId();
      long endCityId = edges.get(edges.size() - 1).getToCity().getCityId();
      long guideCityId = itinerary.getGuideForWholeTrip().getHostCity().getCityId();
      distance += getConnectionDistance(guideCityId, startCityId);
      distance += getConnectionDistance(endCityId, guideCityId);

      float routeCost = GUIDE_COST_PER_MILE * distance;
      float hotelCost = 0.0f;
      float hotelCostForGuide = 0.0f;
      int num_room = itinerary.getNumRoom();
      for (VisitCity city : itinerary.getCityList()) {
        int days = city.getNumDays();
        long cityId = city.getCity().getCityId();
        hotelCost += TEMP_HOTEL_COST_UNIT * days * num_room;
        if (cityId != guideCityId) {
          hotelCostForGuide += TEMP_GUIDE_HOTEL_COST_UNIT * days;
        }
      }
      return buildQuote(routeCost, hotelCost, hotelCostForGuide);
    } catch (NullPointerException e) {
      LOG.error("incorrect request for calculating single guide quote, some field is missed", e);
      throw new OneRoadTripException(Status.INCORRECT_REQUEST, e);
    }
  }

  @VisibleForTesting
  static Quote buildQuote(float routeCost, float hotelCost, float hotelCostForGuide) {
    return Quote.newBuilder().setCostUsd(routeCost + hotelCost + hotelCostForGuide)
        .setRouteCost(routeCost).setHotelCost(hotelCost).setHotelCostForGuide(hotelCostForGuide).build();
  }

  public Quote makeQuoteForMultipleGuides(Itinerary itinerary) throws OneRoadTripException {
    if (itinerary.getEdgeCount() != itinerary.getCityCount() + 1) {
      LOG.info("incorrect request: {}", itinerary);
      // We don't have a correct connection.
      return null;
    }
    if (itinerary.getCityCount() == 1) {
      LOG.info("Only one city, please use the one guide solution: {}", itinerary);
      return null;
    }

    try {
      List<Edge> edges = itinerary.getEdgeList();
      Preconditions.checkArgument(edges.size() >= 3);
      
      GuideInfo prevGuide = null;
      int distance = 0;
      for (int i = 0; i < edges.size(); ++i) {
        GuideInfo guide = (prevGuide == null ?
            itinerary.getCity(0).getGuide(0) : itinerary.getCity(i - 1).getGuide(0));
        Edge edge = edges.get(i);
        distance += getDistanceForGuide(guide.getHostCity().getCityId(), edge);
        if (guide.equals(prevGuide)) {
          distance -= 2 * getConnectionDistance(edge.getFromCity().getCityId(), guide.getHostCity().getCityId());
        }
        prevGuide = guide;
      }
      float routeCost = GUIDE_COST_PER_MILE * distance;
      float hotelCost = 0.0f;
      float hotelCostForGuide = 0.0f;
      
      for (VisitCity city : itinerary.getCityList()) {
        int days = city.getNumDays();
        long cityId = city.getCity().getCityId();
        long guideCityId = city.getGuide(0).getHostCity().getCityId();
        if (cityId != guideCityId) {
          hotelCostForGuide += TEMP_GUIDE_HOTEL_COST_UNIT * days;
        }
        hotelCost += TEMP_HOTEL_COST_UNIT * days * city.getNumDays();
      }
      return buildQuote(routeCost, hotelCost, hotelCostForGuide);
    } catch (NullPointerException e) {
      LOG.error("incorrect request for calculating multiple guide quote, some field is missed", e);
      throw new OneRoadTripException(Status.INCORRECT_REQUEST, e);
    }
  }
}
