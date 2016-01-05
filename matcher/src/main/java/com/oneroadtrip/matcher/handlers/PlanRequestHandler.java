package com.oneroadtrip.matcher.handlers;

import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.data.CityPlanner;
import com.oneroadtrip.matcher.proto.PlanRequest;
import com.oneroadtrip.matcher.proto.PlanResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.util.LogUtil;
import com.oneroadtrip.matcher.util.ProtoUtil;

public class PlanRequestHandler implements RequestHandler {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  private Optional<CityPlanner> cityPlanner;

  // TODO(xfguo): (P4) in case we use grpc in the future.
  @Override
  public String process(String post) {
    try {
      PlanRequest request = ProtoUtil.GetRequest(post, PlanRequest.newBuilder());
      return JsonFormat.printToString(process(request));
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", e);
      return JsonFormat.printToString(PlanResponse.newBuilder().setStatus(Status.INCORRECT_REQUEST)
          .build());
    }
  }

  PlanResponse process(PlanRequest request) {
    try {
      PlanResponse response = cityPlanner.get().makePlan(request.getStartCityId(),
          request.getEndCityId(), request.getVisitCityList(), request.getKeepOrderOfViaCities());
      return LogUtil.logAndReturnResponse("/api/plan", request, response);
    } catch (NoSuchElementException e) {
      LOG.error("No city planner");
      return PlanResponse.newBuilder().setStatus(Status.SERVER_ERROR).build();
    }
  }

}
