package com.oneroadtrip.matcher.resources;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.oneroadtrip.matcher.handlers.PlanRequestHandler;

@Path("plan")
public class PlanResource {
  @Inject
  private PlanRequestHandler handler;
  
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String post(String post) {
    return handler.process(post);
  }


}
