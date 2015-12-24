package com.oneroadtrip.matcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.beust.jcommander.JCommander;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.oneroadtrip.matcher.data.PreloadedData;
import com.squarespace.jersey2.guice.BootstrapUtils;

/**
 * Hello world!
 *
 */
public class App {
  private static final Logger LOG = LogManager.getLogger();

  private static final int PORT = 8080;

  // private static final URI BASE_URI = URI.create("http://0.0.0.0:8080/api/");
  public static final String ROOT_PATH = "helloworld";

  public static void main(String[] args) throws Exception {
    OneRoadTripConfig config = new OneRoadTripConfig();
    JCommander jc = new JCommander(config, args);
    if (config.help) {
      jc.usage();
      return;
    }

    LOG.info("mysql_host = {}, mysql_port = {}", config.mysql_host, config.mysql_port);

    String connectionUrl = String.format(
        "jdbc:mysql://%s:%d/%s?characterEncoding=UTF-8&user=%s&password=%s", config.mysql_host,
        config.mysql_port, config.mysql_db, config.mysql_user, config.mysql_password);
    LOG.info("mysql connect: {}", connectionUrl);

    try {
      ServiceLocator locator = BootstrapUtils.newServiceLocator();
      Injector injector = BootstrapUtils.newInjector(locator, Arrays.asList(new AbstractModule() {
        @Override
        protected void configure() {
          bind(OneRoadTripConfig.class).toInstance(config);
        }
      }, new ServletModule(), new TripModule(connectionUrl)));
      BootstrapUtils.install(locator);

      // Initialize periodical reloader.
      PreloadedData.Manager dataManager = injector.getInstance(PreloadedData.Manager.class);

      Server server = new Server(PORT);

      ResourceConfig resourceConfig = ResourceConfig.forApplication(new TripApplication());
      ServletContainer servletContainer = new ServletContainer(resourceConfig);

      ServletHolder sh = new ServletHolder(servletContainer);
      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setContextPath("/api");

      FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
      context.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));
      context.addFilter(new FilterHolder(CrossOriginFilter.class), "/*",
          EnumSet.allOf(DispatcherType.class));

      context.addServlet(sh, "/*");
      server.setHandler(context);

      ResourceHandler resource_handler = new ResourceHandler();
      resource_handler.setResourceBase("src/main/webapp");

      HandlerList handlers = new HandlerList();
      handlers.setHandlers(new Handler[] { resource_handler, context, new DefaultHandler() });
      server.setHandler(handlers);

      try {
        server.start();
        server.join();
      } finally {
        server.destroy();
      }
    } catch (IOException | InterruptedException ex) {
      LOG.fatal("incorrect", ex);
    }

  }
}
