package com.oneroadtrip.matcher.testutil;

import java.io.IOException;
import java.net.ServerSocket;

public class TestingUtil {
  public static int findFreePort() throws IOException {
    int port = 0;
    try (ServerSocket s = new ServerSocket(0)) {
      port = s.getLocalPort();
    }
    return port;
  }
}
