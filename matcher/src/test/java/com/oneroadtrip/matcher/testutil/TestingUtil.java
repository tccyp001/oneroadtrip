package com.oneroadtrip.matcher.testutil;

import java.io.IOException;
import java.net.ServerSocket;

import com.google.common.collect.ImmutableMap;
import com.oneroadtrip.matcher.proto.CityInfo;
import com.oneroadtrip.matcher.proto.ErrorInfo;
import com.oneroadtrip.matcher.proto.SpotInfo;
import com.oneroadtrip.matcher.proto.VisitSpot;

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

  public static VisitSpot createTestingVisitSpot(int hours, long spotId, String spotName) {
    SpotInfo.Builder spotBuilder = SpotInfo.newBuilder().setSpotId(spotId);
    if (spotName != null) {
      spotBuilder.setName(spotName);
    }
    return VisitSpot.newBuilder().setHours(hours).setInfo(spotBuilder).build();
  }
}
