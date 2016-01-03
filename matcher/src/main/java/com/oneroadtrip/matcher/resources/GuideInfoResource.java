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
import com.oneroadtrip.matcher.proto.GuideInfo;
import com.oneroadtrip.matcher.proto.GuideInfoResponse;
import com.oneroadtrip.matcher.proto.Status;

@Path("guideinfo")
public class GuideInfoResource {
  private static final Logger LOG = LogManager.getLogger();
  
  @Inject
  ImmutableMap<Long, GuideInfo> guideData;

  @GET
  @Path("/{param}")
  @Produces(MediaType.APPLICATION_JSON)
  public String info(@PathParam("param") Long id) {
    LOG.info("xfguo: /guideinfo/{}", id);
    GuideInfo guideInfo = guideData.get(id);
    if (guideInfo == null) {
      return JsonFormat.printToString(GuideInfoResponse.newBuilder()
          .setStatus(Status.INCORRECT_GUIDE_ID).build());
    }
    return JsonFormat.printToString(GuideInfoResponse.newBuilder().setStatus(Status.SUCCESS)
        .setInfo(guideInfo).build());
  }
}
