package com.oneroadtrip.matcher.resources;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.oneroadtrip.matcher.handlers.GuidePlanRequestHandler;

@Path("guide")
public class GuidePlanResource {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  private GuidePlanRequestHandler handler;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String post(String post) {
    return handler.process(post);
  }
}
