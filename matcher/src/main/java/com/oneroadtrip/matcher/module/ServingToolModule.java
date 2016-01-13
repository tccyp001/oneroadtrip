package com.oneroadtrip.matcher.module;

import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.oneroadtrip.matcher.data.Curl;
import com.oneroadtrip.matcher.data.Curl.CurlImpl;
import com.oneroadtrip.matcher.util.HashUtil.Hasher;
import com.oneroadtrip.matcher.util.HashUtil.HasherImpl;

public class ServingToolModule implements Module {

  @Override
  public void configure(Binder binder) {
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
