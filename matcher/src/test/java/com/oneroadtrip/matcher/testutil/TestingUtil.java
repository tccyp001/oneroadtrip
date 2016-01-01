package com.oneroadtrip.matcher.testutil;

import java.io.IOException;
import java.net.ServerSocket;

import com.google.common.collect.ImmutableMap;
import com.oneroadtrip.matcher.proto.CityInfo;

public class TestingUtil {
  public static int findFreePort() throws IOException {
    int port = 0;
    try (ServerSocket s = new ServerSocket(0)) {
      port = s.getLocalPort();
    }
    return port;
  }

  public static void addCity(ImmutableMap.Builder<Long, CityInfo> builder, long cityId,
      String name, String cnName) {
    builder.put(cityId, CityInfo.newBuilder().setCityId(cityId).setName(name).setCnName(cnName)
        .build());
  }
}
