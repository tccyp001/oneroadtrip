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
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.Quote;
import com.oneroadtrip.matcher.proto.QuoteRequest;
import com.oneroadtrip.matcher.proto.QuoteResponse;
import com.oneroadtrip.matcher.proto.Status;
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
    try {
      Itinerary unquotedItinerary = request.getItinerary();
      Itinerary.Builder builder = Itinerary.newBuilder(unquotedItinerary);
      Quote multiQuote = quoteCalculator.makeQuoteForMultipleGuides(unquotedItinerary);
      if (multiQuote != null) {
        builder.setQuoteForMultipleGuides(multiQuote);
      }

      Quote singleQuote = quoteCalculator.makeQuoteForOneGuide(unquotedItinerary);
      if (singleQuote != null) {
        builder.setQuoteForOneGuide(singleQuote);
      }
      QuoteResponse response = QuoteResponse.newBuilder().setItinerary(builder)
          .setStatus(Status.SUCCESS).build();
      return response;
    } catch (OneRoadTripException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return QuoteResponse.newBuilder().setStatus(e.getStatus()).build();
    }
  }

}
