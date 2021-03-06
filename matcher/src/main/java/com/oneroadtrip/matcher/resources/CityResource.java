package com.oneroadtrip.matcher.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.oneroadtrip.matcher.handlers.CityRequestHandler;

@Path("city")
public class CityResource {
  @Inject
  private CityRequestHandler handler;
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String get() {
    return handler.handleGet();
  }

}
