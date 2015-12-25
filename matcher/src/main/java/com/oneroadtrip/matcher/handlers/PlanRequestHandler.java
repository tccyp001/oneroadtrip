package com.oneroadtrip.matcher.handlers;

import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.PlanRequest;
import com.oneroadtrip.matcher.PlanResponse;
import com.oneroadtrip.matcher.Status;
import com.oneroadtrip.matcher.data.CityPlanner;
import com.oneroadtrip.matcher.util.ProtoUtil;
import com.oneroadtrip.matcher.util.Util;

public class PlanRequestHandler implements RequestHandler {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  private Optional<CityPlanner> cityPlanner;

  // TODO(xfguo): (P4) in case we use grpc in the future.
  @Override
  public String process(String post) {
    PlanResponse.Builder respBuilder = PlanResponse.newBuilder();
    try {
      PlanRequest request = ProtoUtil.GetRequest(post, PlanRequest.newBuilder());
      respBuilder = process(request);
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", e);
      respBuilder.setStatus(Status.INCORRECT_REQUEST);
    }
    return JsonFormat.printToString(respBuilder.build());
  }

  PlanResponse.Builder process(PlanRequest request) {
    PlanResponse.Builder respBuilder = PlanResponse.newBuilder().setStatus(Status.SUCCESS);

    try {
      respBuilder = cityPlanner.get().makePlan(request.getStartCityId(),
          request.getEndCityId(), request.getVisitCityList(), request.getKeepOrderOfViaCities());
    } catch (NoSuchElementException e) {
      LOG.error("No city planner");
      respBuilder.setStatus(Status.SERVER_ERROR);
    }

    return respBuilder;
  }

}
