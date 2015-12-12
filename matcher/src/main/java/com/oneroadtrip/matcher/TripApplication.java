package com.oneroadtrip.matcher;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class TripApplication extends ResourceConfig {
  
  public TripApplication() {
    packages(TripApplication.class.getPackage().getName());
  }
}
