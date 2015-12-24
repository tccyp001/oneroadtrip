package com.oneroadtrip.matcher;

import java.util.EnumSet;

import javax.inject.Inject;
import javax.servlet.DispatcherType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.beust.jcommander.JCommander;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.oneroadtrip.matcher.data.PreloadedData;
import com.oneroadtrip.matcher.resources.samples.FileUploadResource;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * Hello world!
 *
 */
public class App {
  private static final Logger LOG = LogManager.getLogger();

  private static final int PORT = 8080;

  public static void main(String[] args) throws Exception {
    OneRoadTripConfig config = new OneRoadTripConfig();
    JCommander jc = new JCommander(config, args);
    if (config.help) {
      jc.usage();
      return;
    }

    Injector injector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
      @Override
      protected void configure() {
        bind(App.class);
        bind(GuiceFilter.class);
        install(new JerseyServletModule());
        install(new ServletModule() {
          @Override
          protected void configureServlets() {
            bind(GuiceContainer.class);
            serve("/*").with(GuiceContainer.class);
          }
        });

        // Testing resources
        bind(FileUploadResource.class);
        bind(HelloWorldResource.class);
        bind(AnotherResource.class);  // TODO(xfguo): Clean up.
        
        // Bind OneRoadTrip modules
        install(new TripModule(config));
      }
    });

    // Initialize the first database loading.
    injector.getInstance(PreloadedData.Manager.class).get();
    injector.getInstance(App.class).go();
  }

  final GuiceFilter guiceFilter;

  @Inject
  App(GuiceFilter guiceFilter) {
    this.guiceFilter = guiceFilter;
  }

  void go() throws Exception {
    ServletContextHandler servletHandler = new ServletContextHandler();
    servletHandler.setContextPath("/api");

    // jetty always wants one servlet
    servletHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");

    // add guice servlet filter
    FilterHolder filterHolder = new FilterHolder(guiceFilter);
    servletHandler.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));

    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setResourceBase("src/main/webapp");

    HandlerCollection handlerCollection = new HandlerCollection();
    handlerCollection.addHandler(servletHandler);
    handlerCollection.addHandler(resourceHandler);

    Server server = new Server(PORT);
    server.setHandler(handlerCollection);

    try {
      LOG.info("staring server...");
      server.start();
      server.join();
    } finally {
      server.destroy();
    }
  }
}
