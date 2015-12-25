package com.oneroadtrip.matcher.handlers;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.GuidePlanRequest;
import com.oneroadtrip.matcher.GuidePlanResponse;
import com.oneroadtrip.matcher.Status;
import com.oneroadtrip.matcher.data.GuidePlanner;
import com.oneroadtrip.matcher.util.ProtoUtil;

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
      LOG.error("failed to parse the json: {}", e);
      return JsonFormat.printToString(GuidePlanResponse.newBuilder()
          .setStatus(Status.INCORRECT_REQUEST).build());
    }
  }

  GuidePlanResponse process(GuidePlanRequest request) {
    return request.getOneGuideForWholeTrip() ? guidePlanner.makeSingleGuidePlan(request)
        : guidePlanner.makeMultiGuidePlan(request);
  }

}
