package com.oneroadtrip.matcher;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("helloworld")
public class HelloWorldResource {
  public static final String CLICHED_MESSAGE = "Hello World!\n";

  @GET
  @Produces("text/plain")
  public String getHello() {
    return CLICHED_MESSAGE;
  }

}