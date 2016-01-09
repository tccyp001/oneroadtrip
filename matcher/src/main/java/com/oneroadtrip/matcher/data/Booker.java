package com.oneroadtrip.matcher.data;

import java.util.List;

import javax.inject.Inject;

import org.javatuples.Triplet;

import com.oneroadtrip.matcher.proto.BookingRequest;
import com.oneroadtrip.matcher.proto.BookingResponse;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.Status;

public class Booker {
  @Inject
  DatabaseAccessor dbAccessor;

  public BookingResponse process(BookingRequest request) {
    try {
      Triplet<Long, Long, List<Long>> result = dbAccessor.appendOrder(request.getItinerary());
      Itinerary itin = Itinerary.newBuilder(request.getItinerary()).setOrderId(result.getValue0())
          .setItineraryId(result.getValue1()).addAllReservationId(result.getValue2()).build();
      return BookingResponse.newBuilder().setStatus(Status.SUCCESS).setItinerary(itin).build();
    } catch (NullPointerException e) {
      // Error handling.
      return BookingResponse.newBuilder().setStatus(Status.ERROR_IN_SQL).build();
    }
  }

}
