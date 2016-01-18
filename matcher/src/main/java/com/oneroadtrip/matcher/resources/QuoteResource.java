package com.oneroadtrip.matcher.resources;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.data.QuoteCalculator;
import com.oneroadtrip.matcher.proto.GuidePlanType;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.QuoteRequest;
import com.oneroadtrip.matcher.proto.QuoteResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.util.LogUtil;
import com.oneroadtrip.matcher.util.ProtoUtil;

@Path("quote")
public class QuoteResource {
  private static final Logger LOG = LogManager.getLogger();
  
  @Inject
  QuoteCalculator quoteCalculator;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String post(String post) {
    try {
      LOG.info("/api/quote: '{}'", post);
      QuoteRequest request = ProtoUtil.GetRequest(post, QuoteRequest.newBuilder());
      return JsonFormat.printToString(process(request));
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", e);
      return JsonFormat.printToString(QuoteResponse.newBuilder()
          .setStatus(Status.INCORRECT_REQUEST).build());
    }
  }

  public QuoteResponse process(QuoteRequest request) {
    // TODO(xfguo): Should catch exception in the function.
    QuoteResponse.Builder builder = QuoteResponse.newBuilder().setStatus(Status.SUCCESS);
    try {
      for (Itinerary itin : request.getItineraryList()) {
        Itinerary.Builder itinBuilder = Itinerary.newBuilder(itin);
        if (itin.getGuidePlanType() == GuidePlanType.ONE_GUIDE_FOR_THE_WHOLE_TRIP) {
          itinBuilder.setQuote(quoteCalculator.makeQuoteForOneGuide(itin));
        } else if (itin.getGuidePlanType() == GuidePlanType.ONE_GUIDE_FOR_EACH_CITY) {
          itinBuilder.setQuote(quoteCalculator.makeQuoteForMultipleGuides(itin));
        }
        builder.addItinerary(itinBuilder);
      }
    } catch (OneRoadTripException e) {
      builder.clear().setStatus(e.getStatus());
    }
    QuoteResponse response = builder.build();
    return LogUtil.logAndReturnResponse("/api/quote", request, response);
  }

}
