package com.oneroadtrip.matcher;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("helloworld")
public class HelloWorldResource {
  private static final Logger LOG = LogManager.getLogger();
  public static final String CLICHED_MESSAGE = "Hello World!\n";

  @Inject
  @Named("content")
  private String content;

  @GET
  @Produces("text/plain")
  public String getHello() {
    LOG.info("xfguo: Hello world, content = {}", content);
    return CLICHED_MESSAGE + "haha|" + content + "\n";
  }

}