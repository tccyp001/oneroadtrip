package com.oneroadtrip.matcher;

import java.util.concurrent.TimeUnit;

import com.beust.jcommander.Parameter;

// TODO(xfguo): (P1) Move the file to common dir.
public class OneRoadTripConfig {
  @Parameter(names = "--mysql_host", description = "Hostname of mysql", required = false)
  public String mysql_host = "tech-meetup.com";

  @Parameter(names = "--mysql_port", description = "Port of mysql", required = false)
  public Integer mysql_port = 4407;

  @Parameter(names = "--mysql_db", description = "Hostname of mysql", required = false)
  public String mysql_db = "oneroadtrip";

  @Parameter(names = "--mysql_user", description = "Hostname of mysql", required = false)
  public String mysql_user = "oneroadtrip";

  @Parameter(names = "--mysql_password", description = "Hostname of mysql", required = false)
  public String mysql_password = "oneroadtrip123";

  @Parameter(names = "--preload_period_in_second", description = "Period in second for preloading DB data", required = false)
  public Long preload_period_in_seconds = TimeUnit.MINUTES.toSeconds(5);
  
  @Parameter(names = "--guide_reserved_seconds_for_book", description = "Period in second for preloading DB data", required = false)
  public Long guideReservedSecondsForBook = TimeUnit.MINUTES.toSeconds(5);
  
  @Parameter(names = "--guide_reservation_query_limit", description = "最多可以查询预留时间的导游的数目")
  public Integer guideReservationQueryLimit = 20;

  @Parameter(names = { "-h", "--help" }, description = "print help message")
  public boolean help = false;
}
