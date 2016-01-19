package com.oneroadtrip.matcher.handlers;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.data.GuidePlanner;
import com.oneroadtrip.matcher.proto.GuidePlanRequest;
import com.oneroadtrip.matcher.proto.GuidePlanResponse;
import com.oneroadtrip.matcher.proto.GuidePlanType;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.proto.VisitCity;
import com.oneroadtrip.matcher.util.LogUtil;
import com.oneroadtrip.matcher.util.ProtoUtil;
import com.oneroadtrip.matcher.util.Util;

// TODO(xfguo): Maybe we don't need RequestHandler for most of the request, it introduces complexities.
public class GuidePlanRequestHandler implements RequestHandler {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  private GuidePlanner guidePlanner;

  // TODO(xfguo): (P4) in case we use grpc in the future.
  @Override
  public String process(String post) {
    try {
      GuidePlanRequest request = ProtoUtil.GetRequest(post, GuidePlanRequest.newBuilder());
      return JsonFormat.printToString(process(request));
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", post, e);
      return JsonFormat.printToString(GuidePlanResponse.newBuilder()
          .setStatus(Status.INCORRECT_REQUEST).build());
    }
  }

  GuidePlanResponse process(GuidePlanRequest request) {
    GuidePlanResponse.Builder builder = GuidePlanResponse.newBuilder().setStatus(Status.SUCCESS);
    try {
      Itinerary itinerary = request.getItinerary();
      GuidePlanType type = request.getRequestGuidePlanType();
      Itinerary processedItin = processRequestItinerary(itinerary);
      if (type == GuidePlanType.ONE_GUIDE_FOR_EACH_CITY || type == GuidePlanType.BOTH) {
        builder.addItinerary(guidePlanner.makeMultiGuidePlan(processedItin));
      }
      if (type == GuidePlanType.ONE_GUIDE_FOR_THE_WHOLE_TRIP || type == GuidePlanType.BOTH) {
        builder.addItinerary(guidePlanner.makeSingleGuidePlan(processedItin));
      }
    } catch (OneRoadTripException e) {
      LOG.error("OneRoadTrip exception: ", e);
      return builder.setStatus(e.getStatus()).build();
    }
    return LogUtil.logAndReturnResponse("/api/guide", request, builder.build());
  }

  private Itinerary processRequestItinerary(Itinerary itinerary) throws OneRoadTripException {
    Itinerary.Builder builder = Itinerary.newBuilder(itinerary);
    if (!builder.hasStartdate()) {
      throw new OneRoadTripException(Status.INCORRECT_REQUEST, null);
    }
    int currentDate = builder.getStartdate();
    for (VisitCity.Builder cityBuilder : builder.getCityBuilderList()) {
      cityBuilder.setStartDate(currentDate);
      currentDate = Util.advanceDays(currentDate, cityBuilder.getNumDays());
    }
    return builder.build();
  }

}
