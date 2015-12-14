package com.oneroadtrip.matcher;

import com.beust.jcommander.Parameter;

public class ArgumentManager {
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

  @Parameter(names = { "-h", "--help" }, description = "print help message")
  public boolean help = false;
}
