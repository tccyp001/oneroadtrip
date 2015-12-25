package com.oneroadtrip.matcher;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

// TODO(xfguo): Remove the class, it isn't used anymore.
@ApplicationPath("/")
public class TripApplication extends ResourceConfig {

  public TripApplication() {
    packages(TripApplication.class.getPackage().getName());
  }
}
