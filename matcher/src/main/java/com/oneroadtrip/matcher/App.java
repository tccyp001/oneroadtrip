package com.oneroadtrip.matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hello world!
 *
 */
public class App {
  private static final Logger LOG = LogManager.getLogger();

  public static void main(String[] args) {
    System.out.println("Hello World!");
    LOG.info("Hello world {}", 123);
  }
}
