package com.oneroadtrip.matcher;

import java.util.concurrent.TimeUnit;

import com.beust.jcommander.Parameter;

// TODO(xfguo): (P1) Move the file to common dir.
public class OneRoadTripConfig {
  @Parameter(names = "--port", description = "Service port", required = false)
  public Integer port = 8080;

  @Parameter(names = "--jdbcDriver", description = "jdbcDriver", required = false)
  public String jdbcDriver = "com.mysql.jdbc.Driver";
  
  @Parameter(names = "--connectionUri", description = "DB connection Uri", required = false)
  public String connectionUri = "jdbc:mysql://54.213.193.97:3306/onemoretrip"
	        + "?characterEncoding=UTF-8&user=onetrip&password=onetrip123";
  //public String connectionUri = "jdbc:mysql://tech-meetup.com:4407/oneroadtrip"
    //    + "?characterEncoding=UTF-8&user=oneroadtrip&password=oneroadtrip123";

  @Parameter(names = "--preload_period_in_second",
      description = "Period in second for preloading DB data", required = false)
  public Long preload_period_in_seconds = TimeUnit.MINUTES.toSeconds(5);

  @Parameter(names = "--guide_reserved_seconds_for_book",
      description = "Period in second for preloading DB data", required = false)
  public Long guideReservedSecondsForBook = TimeUnit.MINUTES.toSeconds(5);

  @Parameter(names = "--multiple_guide_limit",
      description = "最多可以查询预留时间的导游的数目", required = false)
  public Integer multiGuideLimit = 4;
  
  @Parameter(names = "--single_guide_limit", description = "最多可以查询预留时间的一个导游到底的导游数目", required = false)
  public Integer singleGuideLimit = 8;
  
  @Parameter(names = "--stripe_secure_key",
      description = "Secure Key of Stripe", required = false)
  public String stripeSecureKey = "sk_test_CWZPSjduJUAArcBrrIZplPRU";
  // My key "sk_test_x7J2qxqTLBNo4WQoYkRNMEGx"

  @Parameter(names = { "-h", "--help" }, description = "print help message", required = false)
  public boolean help = false;
}
