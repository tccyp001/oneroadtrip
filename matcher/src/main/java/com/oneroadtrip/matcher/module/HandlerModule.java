package com.oneroadtrip.matcher.module;

import com.google.inject.AbstractModule;
import com.oneroadtrip.matcher.handlers.CityRequestHandler;
import com.oneroadtrip.matcher.handlers.PlanRequestHandler;

public class HandlerModule extends AbstractModule {
  @Override
  public void configure() {
    bind(CityRequestHandler.class);
    bind(PlanRequestHandler.class);
  }
}
