package com.oneroadtrip.matcher.module;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.oneroadtrip.matcher.data.Curl;
import com.oneroadtrip.matcher.data.Curl.CurlImpl;
import com.oneroadtrip.matcher.data.Payer;
import com.oneroadtrip.matcher.data.StripePayer;
import com.oneroadtrip.matcher.util.HashUtil.Hasher;
import com.oneroadtrip.matcher.util.HashUtil.HasherImpl;

public class ServingToolModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Payer.class).to(StripePayer.class).in(Singleton.class);
  }

  @Provides
  @Singleton
  Curl getCurl() {
    return new CurlImpl();
  }

  @Provides
  @Singleton
  Hasher getHasher() {
    return new HasherImpl();
  }
}
