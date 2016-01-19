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
import com.oneroadtrip.matcher.data.DatabaseAccessor;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.proto.UserInfoRequest;
import com.oneroadtrip.matcher.proto.UserInfoResponse;
import com.oneroadtrip.matcher.util.LogUtil;
import com.oneroadtrip.matcher.util.ProtoUtil;

@Path("userinfo")
public class UserInfoResource {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  DatabaseAccessor dbAccessor;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String post(String post) {
    try {
      LOG.info("/api/userinfo: '{}'", post);
      UserInfoRequest request = ProtoUtil.GetRequest(post, UserInfoRequest.newBuilder());
      return JsonFormat.printToString(process(request));
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", e);
      return JsonFormat.printToString(UserInfoResponse.newBuilder()
          .setStatus(Status.INCORRECT_REQUEST).build());
    }
  }

  public UserInfoResponse process(UserInfoRequest request) {
    UserInfoResponse resp = null;
    try {
      resp = dbAccessor.retrieveUserInfo(request.getUserToken());
    } catch (OneRoadTripException e) {
      resp = UserInfoResponse.newBuilder().setStatus(e.getStatus()).build();
    }
    return LogUtil.logAndReturnResponse("/api/userinfo", request, resp);
  }

}
