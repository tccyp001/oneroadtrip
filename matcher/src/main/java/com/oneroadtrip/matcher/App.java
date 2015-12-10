package com.oneroadtrip.matcher;

import java.io.IOException;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Hello world!
 *
 */
public class App {
  private static final Logger LOG = LogManager.getLogger();

  private static final URI BASE_URI = URI.create("http://localhost:8080/base/");
  public static final String ROOT_PATH = "helloworld";

  public static void main(String[] args) {
    try {
//      App app = new App();  // Just a dummy one.
//      URL resourceUrl = app.getClass().getResource("src/main/webapp/index.html");
//
//      LOG.info("\"Hello World\" Jersey Example App");
//      LOG.info("xfguo: webapp: {}", resourceUrl);

      final ResourceConfig resourceConfig = new ResourceConfig(HelloWorldResource.class);
      final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig,
          false);
//      server.getServerConfiguration().addHttpHandler(
//          new StaticHttpHandler(resourceUrl.getPath()), "/static");
      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        public void run() {
          server.shutdownNow();
        }
      }));
      server.start();

      System.out.println(String.format(
          "Application started.\nTry out %s%s\nStop the application using CTRL+C", BASE_URI,
          ROOT_PATH));
      Thread.currentThread().join();
    } catch (IOException | InterruptedException ex) {
      LOG.fatal("incorrect", ex);
    }

  }
}
