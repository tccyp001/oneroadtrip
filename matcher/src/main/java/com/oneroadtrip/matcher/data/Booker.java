package com.oneroadtrip.matcher.data;

import java.sql.Connection;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.javatuples.Triplet;

import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.BookingRequest;
import com.oneroadtrip.matcher.proto.BookingResponse;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.Order;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.util.SqlUtil;

public class Booker {
  @Inject
  DataSource dataSource;

  public BookingResponse process(BookingRequest request) {
    try {
      Triplet<Long, Long, List<Long>> result = SqlUtil.executeTransaction(dataSource,
          (Connection conn) -> DatabaseAccessor.prepareForOrder(request.getItinerary(), conn));
      Itinerary itin = Itinerary.newBuilder(request.getItinerary())
          .setOrder(Order.newBuilder().setOrderId(result.getValue0()))
          .setItineraryId(result.getValue1()).addAllReservationId(result.getValue2()).build();
      return BookingResponse.newBuilder().setStatus(Status.SUCCESS).setItinerary(itin).build();
    } catch (NullPointerException e) {
      // Error handling.
      return BookingResponse.newBuilder().setStatus(Status.ERROR_IN_SQL).build();
    } catch (OneRoadTripException e) {
      return BookingResponse.newBuilder().setStatus(e.getStatus()).build();
    }
  }
}
