package com.oneroadtrip.matcher.handlers;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.data.SpotPlanner;
import com.oneroadtrip.matcher.proto.SpotPlanRequest;
import com.oneroadtrip.matcher.proto.SpotPlanResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.util.LogUtil;
import com.oneroadtrip.matcher.util.ProtoUtil;

public class SpotRequestHandler implements RequestHandler {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  ImmutableMap<Long, SpotPlanner> spotPlanners;

  @Inject
  @Named(Constants.INTEREST_NAME_TO_ID)
  ImmutableMap<String, Long> interestNameToId;

  @Override
  public String process(String post) {
    try {
      SpotPlanRequest request = ProtoUtil.GetRequest(post, SpotPlanRequest.newBuilder());
      return JsonFormat.printToString(process(request));
    } catch (ParseException e) {
      LOG.error("failed to parse the json correctly: {}", e);
      return JsonFormat.printToString(SpotPlanResponse.newBuilder()
          .setStatus(Status.INCORRECT_REQUEST).build());
    }
  }

  SpotPlanResponse process(SpotPlanRequest request) {
    List<Long> interestIds = getInterestIdsByName(request.getInterestList());
    SpotPlanner planner = spotPlanners.get(request.getCityId());
    SpotPlanResponse response = (planner == null) ? SpotPlanResponse.newBuilder()
        .setStatus(Status.INCORRECT_REQUEST).build() : planner.planSpot(interestIds, request);
    return LogUtil.logAndReturnResponse("/api/spot", request, response);
  }

  private List<Long> getInterestIdsByName(List<String> interestList) {
    List<Long> ids = Lists.newArrayList();
    for (String name : interestList) {
      if (!interestNameToId.containsKey(name)) {
        LOG.error("No corresponding id for interest ({})", name);
        continue;
      }
      ids.add(interestNameToId.get(name));
    }
    return ids;
  }

}
