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
import com.oneroadtrip.matcher.proto.LoginRequest;
import com.oneroadtrip.matcher.proto.LoginResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.util.ProtoUtil;

@Path("login")
public class LoginResource {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  private UserAdmin userAdmin;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String post(String post) throws SQLException {
    try {
      LoginRequest request = ProtoUtil.GetRequest(post, LoginRequest.newBuilder());
      return JsonFormat.printToString(userAdmin.login(request));
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", post, e);
      return JsonFormat.printToString(LoginResponse.newBuilder()
          .setStatus(Status.INCORRECT_REQUEST).build());
    }
  }
}
