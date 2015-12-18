package com.oneroadtrip.matcher.testutil;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.google.common.collect.Lists;
import com.oneroadtrip.matcher.util.ScriptRunner;

public class TestingDataProcessor {
  private static final Logger LOG = LogManager.getLogger();

  private static final String CASE_SEPARATOR = "==========";

  private final Connection conn;
  private List<Pair<String, String>> cases;

  public TestingDataProcessor(Connection conn) {
    this.conn = conn;
    cases = Lists.newArrayList();
  }

  public void insertData(String data) throws IOException, SQLException {
    ScriptRunner runner = new ScriptRunner(conn, true, false);
    runner.runScript(new StringReader(data));
  }

  public Collection<Pair<String, String>> getCases() {
    return cases;
  }

  private static final String INSERT_DATA = "INSERT_DATA";
  private static final String TESTCASE_DATA = "TESTCASE_DATA";
  private static final String PATTERN_STR = String.format("(%s|%s)(.*)", INSERT_DATA, TESTCASE_DATA);
  private static final Pattern COMMAND_PATTERN = Pattern.compile(
      String.format(".*==(%s|%s)==(.*)", INSERT_DATA, TESTCASE_DATA), Pattern.DOTALL);

  private static final Pattern CASE_PATTERN = Pattern.compile(".*=REQUEST(.*)=RESPONSE(.*)", Pattern.DOTALL);
  
  public void loadData(String content) throws IOException, SQLException {
    LOG.info("xfguo: PATTERN_STR = {}", PATTERN_STR);
    for (String input : content.split(CASE_SEPARATOR)) {
      Matcher cmdMatcher = COMMAND_PATTERN.matcher(input);
      if (!cmdMatcher.matches()) {
        throw new IOException(String.format("(xfguo) Incorrect format of data '%s'", input));
      }

      String command = cmdMatcher.group(1);
      String data = cmdMatcher.group(2);
      if (command.equals(INSERT_DATA)) {
        insertData(data);
      } else if (command.equals(TESTCASE_DATA)) {
        Matcher m = CASE_PATTERN.matcher(data);
        if (!m.matches()) {
          throw new IOException(String.format("(xfguo) Incorrect format of data '%s'", data));
        }
        cases.add(Pair.with(m.group(1), m.group(2)));
      }
    }
  }

  public static TestingDataProcessor loadData(Connection conn, String content) throws IOException,
      SQLException {
    TestingDataProcessor processor = new TestingDataProcessor(conn);
    processor.loadData(content);
    return processor;
  }

}
