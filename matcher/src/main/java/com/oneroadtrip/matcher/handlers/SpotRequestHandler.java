package com.oneroadtrip.matcher.handlers;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.SpotPlanRequest;
import com.oneroadtrip.matcher.SpotPlanResponse;
import com.oneroadtrip.matcher.Status;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.data.SpotPlanner;
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
    SpotPlanResponse.Builder respBuilder = SpotPlanResponse.newBuilder();
    try {
      SpotPlanRequest request = ProtoUtil.GetRequest(post, SpotPlanRequest.newBuilder());
      respBuilder = process(request);
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", e);
      respBuilder.setStatus(Status.INCORRECT_REQUEST);
    }
    return JsonFormat.printToString(respBuilder.build());
  }

  public SpotPlanResponse.Builder process(SpotPlanRequest request) {
    SpotPlanResponse.Builder respBuilder = SpotPlanResponse.newBuilder().setStatus(Status.SUCCESS);
    List<Long> interestIds = getInterestIdsByName(request.getInterestList());
    if (request.hasCityId()) {
      respBuilder = Preconditions.checkNotNull(spotPlanners.get(request.getCityId())).planSpot(
          interestIds, request);
      respBuilder.setCityId(request.getCityId());
    } else {
      respBuilder.setStatus(Status.INCORRECT_REQUEST);
    }
    return respBuilder;
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
