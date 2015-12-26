package com.oneroadtrip.matcher;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO(xfguo): (P1) Move the Resource to resources/samples directory
@Path("another")
public class AnotherResource {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  @Named("content")
  private String content;
  
  @GET
  public String another() {
    LOG.info("xfguo: another");
    return "another " + content;
  }

}
