package com.oneroadtrip.matcher.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.googlecode.protobuf.format.JsonFormat;
import com.oneroadtrip.matcher.proto.GuideInfoResponse;
import com.oneroadtrip.matcher.proto.SpotInfo;
import com.oneroadtrip.matcher.proto.SpotInfoResponse;
import com.oneroadtrip.matcher.proto.Status;

@Path("spotinfo")
public class SpotInfoResource {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  ImmutableMap<Long, SpotInfo> spotIdToInfo;

  @GET
  @Path("/{param}")
  @Produces(MediaType.APPLICATION_JSON)
  public String info(@PathParam("param") Long id) {
    LOG.info("xfguo: /spotinfo/{}", id);
    SpotInfo info = spotIdToInfo.get(id);
    if (info == null) {
      return JsonFormat.printToString(GuideInfoResponse.newBuilder()
          .setStatus(Status.INCORRECT_SPOT_ID).build());
    }
    return JsonFormat.printToString(SpotInfoResponse.newBuilder().setStatus(Status.SUCCESS)
        .setInfo(info).build());
  }
}
