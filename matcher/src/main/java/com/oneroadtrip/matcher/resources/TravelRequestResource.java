package com.oneroadtrip.matcher.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.TravelRequest;

@Path("travelrequest")
public class TravelRequestResource {
  private static final Logger LOG = LogManager.getLogger();

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String post(String post) {
    LOG.info("xfguo: start parsing travelrequest: {}", post);
    TravelRequest.Builder builder = TravelRequest.newBuilder();
    try {
      JsonFormat.merge(post, builder);
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", e);
    }
    
    TravelRequest request = builder.build();
    LOG.info("xfguo: get travelrequest proto: {}", builder.build());
    return JsonFormat.printToString(request) + "\n";
  }
}
