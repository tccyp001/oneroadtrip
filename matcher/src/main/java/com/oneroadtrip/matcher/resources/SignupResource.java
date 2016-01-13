package com.oneroadtrip.matcher.resources;

import java.sql.SQLException;

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
import com.oneroadtrip.matcher.data.UserAdmin;
import com.oneroadtrip.matcher.proto.SignupRequest;
import com.oneroadtrip.matcher.proto.SignupResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.util.ProtoUtil;

@Path("signup")
public class SignupResource {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  private UserAdmin userAdmin;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String post(String post) throws SQLException {
    try {
      SignupRequest request = ProtoUtil.GetRequest(post, SignupRequest.newBuilder());
      return JsonFormat.printToString(userAdmin.signUp(request));
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", post, e);
      return JsonFormat.printToString(SignupResponse.newBuilder()
          .setStatus(Status.INCORRECT_REQUEST).build());
    }
  }
}
