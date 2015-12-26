package com.oneroadtrip.matcher;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oneroadtrip.matcher.testutil.DockerProcess;
import com.oneroadtrip.matcher.testutil.TestingUtil;
import com.oneroadtrip.matcher.util.HashUtil;
import com.oneroadtrip.matcher.util.ScriptRunner;

import junit.framework.Assert;

public class FirstIntegrationTest {
  private static final Logger LOG = LogManager.getLogger();

  public static class OneRoadTripTestingEnv implements Closeable {
    final File testingDirectory = Files.createTempDir();

    public File getTestingDirectory() {
      return testingDirectory;
    }

    @Override
    public void close() {
      // try {
      // FileUtils.deleteDirectory(testingDirectory);
      // } catch (IOException e) {
      // LOG.info("Directory {} isn't clean up correctly, possible have access
      // issue.",
      // testingDirectory);
      // }
    }

    public static OneRoadTripTestingEnv create() {
      return new OneRoadTripTestingEnv();
    }

    public File createOneTestingDir() {
      return createOneTestingDir(HashUtil.getRandomString(20));
    }

    public File createOneTestingDir(String name) {
      return new File(testingDirectory, name);
    }
  }

  public static class MysqlDockerProcess extends DockerProcess {

    public MysqlDockerProcess(String imageName, String postfix, int port, int incomingPort,
        String volumnPair, String otherParams) {
      super(imageName, postfix, port, incomingPort, volumnPair, otherParams);
    }

    private static final int MAX_RETRIES = 60;

    @Override
    public boolean expectStarted() throws InterruptedException {
      try (BasicDataSource ds = new BasicDataSource()) {
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl(getConnectionUri());

        for (int i = 0; i < MAX_RETRIES; ++i) {
          Thread.sleep(TimeUnit.SECONDS.toMillis(1));
          LOG.info("{} times to try", i);
          try (Connection conn = ds.getConnection()) {
          } catch (SQLException e) {
            continue;
          }
          LOG.info("Successfully connected mysql");
          return true;
        }
      } catch (SQLException e1) {
        LOG.info("Errors in connecting DB");
      }
      return false;
    }

    public static MysqlDockerProcess createAndStart(File testingDir, int port, String mysqlName,
        String dbPassword, String dbName) throws IOException {
      MysqlDockerProcess mysqlProcess = new MysqlDockerProcess("mysql:5.7", mysqlName,
          TestingUtil.findFreePort(), 3306,
          String.format("%s:/var/lib/mysql", testingDir.getCanonicalPath()),
          String.format("-e MYSQL_ROOT_PASSWORD=%s -e MYSQL_DATABASE=%s", dbPassword, dbName));
      mysqlProcess.start();
      return mysqlProcess;
    }

    public String getConnectionUri() {
      return String.format(
          "jdbc:mysql://127.0.0.1:%d/oneroadtrip?characterEncoding=UTF-8&user=root&password=password",
          getPort());
    }
  }

  public static class MysqlEnvironment implements Closeable {
    private final DataSource dataSource;
    private final Collection<String> initSqls;
    private final Collection<String> cleanupSqls;

    public MysqlEnvironment(String driverClassName, String connectionUri,
        Collection<String> initSqls, Collection<String> cleanupSqls) {
      LOG.info("connectionUri: '{}'", connectionUri);
      BasicDataSource ds = new BasicDataSource();
      ds.setDriverClassName(driverClassName);
      ds.setUrl(connectionUri);
      dataSource = ds;

      this.initSqls = initSqls;
      this.cleanupSqls = cleanupSqls;
    }

    public void runSqls(Collection<String> sqlPaths) {
      LOG.info("Start to connect mysql...");
      try (Connection conn = dataSource.getConnection()) {
        ScriptRunner runner = new ScriptRunner(conn, true, false);
        for (String sqlPath : sqlPaths) {
          LOG.info("Start to run script '{}'", sqlPath);
          runner.runScript(new BufferedReader(
              new InputStreamReader(new FileInputStream(sqlPath), Charsets.UTF_8)));
        }
      } catch (SQLException e) {
        LOG.info("Errors in connection DB", e);
      } catch (FileNotFoundException e) {
        LOG.info("Errors of sql path", e);
      } catch (IOException e) {
        LOG.info("Errors in executing SQLs", e);
      }
    }

    public void start() {
      runSqls(initSqls);
    }

    @Override
    public void close() throws IOException {
      runSqls(cleanupSqls);
    }

    public static MysqlEnvironment create(String driver, String connectionUri,
        Collection<String> initSqls, Collection<String> cleanupSqls) {
      MysqlEnvironment env = new MysqlEnvironment(driver, connectionUri, initSqls, cleanupSqls);
      env.start();
      return env;
    }
  }

  public static abstract class AllInOneJarProcess implements Closeable {
    private static final int MAX_RETRIES = 60;

    private final String jarPath;
    private final String args;
    private final String logPath;

    protected Process process;

    public AllInOneJarProcess(String jarPath, String args, String logPath) {
      this.jarPath = jarPath;
      this.args = args;
      this.logPath = logPath;
    }

    public void start() throws IOException, InterruptedException {
      String cmd = String.format("java -jar %s %s", jarPath, args);
      LOG.info("Starting jar process: %s", cmd);
      ProcessBuilder builder = new ProcessBuilder(cmd.split(" ")).redirectErrorStream(true)
          .redirectOutput(new File(logPath));
      process = builder.start();

      boolean started = false;
      for (int i = 0; i < MAX_RETRIES; ++i) {
        if (expectStarted()) {
          started = true;
          break;
        }
        // Wait one second to retry.
        LOG.info("{} tries", i);
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
      }

      if (!started)
        throw new IOException(
            String.format("Errors in starting the jar process: %s, %s", jarPath, args));
    }

    @Override
    public void close() throws IOException {
      if (process.isAlive()) {
        process.destroy();
      }
    }

    // TODO(xfguo): Merge with DockerProcess.
    public abstract boolean expectStarted();
  }

  public static class OneRoadTripService extends AllInOneJarProcess {
    private final int port;

    public OneRoadTripService(int port, String jarPath, String args, String logPath)
        throws IOException {
      super(jarPath, String.format("--port %d %s", port, args), logPath);
      this.port = port;
    }

    @Override
    public boolean expectStarted() {
      boolean started = false;
      String url = String.format("http://127.0.0.1:%d/api/helloworld", port);
      try {
        LOG.info("url = {}", url);
        HttpURLConnection conn = (HttpURLConnection) (new URL(url).openConnection());
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) {
          LOG.info("Service is connected");
          started = true;
        }
      } catch (IOException e) {
        LOG.error("Errors in connecting url '{}'", url);
      }
      return started;
    }

    public int getPort() {
      return port;
    }

    public static OneRoadTripService create(int port, String connectionUri, File testingDir)
        throws IOException, InterruptedException {
      OneRoadTripService service = new OneRoadTripService(port,
          "target/oneroadtrip-jar-with-dependencies.jar",
          String.format("--jdbcDriver com.mysql.jdbc.Driver --connectionUri %s", connectionUri),
          new File(testingDir, "oneroadtrip.log").getCanonicalPath());
      service.start();
      return service;
    }
  }

  @Test
  public void test() throws Exception {
    try (OneRoadTripTestingEnv testingEnv = OneRoadTripTestingEnv.create();
        MysqlDockerProcess mysqlProcess = MysqlDockerProcess.createAndStart(
            testingEnv.createOneTestingDir("mysql"), TestingUtil.findFreePort(), "mysql",
            "password", "oneroadtrip");
        MysqlEnvironment mysqlEnv = MysqlEnvironment.create("com.mysql.jdbc.Driver",
            mysqlProcess.getConnectionUri(),
            Lists.newArrayList("src/main/resources/create_tables.sql",
                "src/main/resources/init_table.sql"),
            Lists.newArrayList("src/main/resources/drop_tables.sql"));
        OneRoadTripService service = OneRoadTripService.create(TestingUtil.findFreePort(),
            mysqlProcess.getConnectionUri(), testingEnv.getTestingDirectory())) {
      runTests(String.format("http://127.0.0.1:%d/api", service.getPort()));
    }
  }

  public void runTests(String url) throws IOException {
    // String url = "http://127.0.0.1:8080/api";
    File dir = new File("src/test/resources/integration");
    JsonParser parser = new JsonParser();
    for (String json : Lists.newArrayList("city_planning.json", "guide_plan.json", "login.json")) {
      LOG.info("Json file: {}", json);
      String content = Files.toString(new File(dir, json), Charsets.UTF_8);
      JsonArray entries = (JsonArray) parser.parse(content);
      List<Pair<JsonObject, JsonObject>> pairs = Lists.newArrayList();
      JsonObject x = null;
      JsonObject y = null;
      for (JsonElement element : entries) {
        Assert.assertTrue(element instanceof JsonObject);
        if (x == null)
          x = (JsonObject) element;
        else if (y == null)
          y = (JsonObject) element;
        if (x != null && y != null) {
          pairs.add(Pair.with(x, y));
          x = null;
          y = null;
        }
      }
      Assert.assertNull(x);
      Assert.assertNull(y);

      String tokenInSignUp = null;
      for (Pair<JsonObject, JsonObject> p : pairs) {
        JsonObject req = p.getValue0();
        String reqType = req.remove("request").getAsString();
        String reqUrl = String.format("%s/%s", url, reqType);
        LOG.info("req = '{}'", reqUrl);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
          HttpPost post = new HttpPost(reqUrl);
          post.setEntity(new StringEntity(req.toString(), ContentType.APPLICATION_JSON));
          LOG.info("http post request: '{}'", post);
          HttpResponse response = client.execute(post);
          Assert.assertEquals("error in postRequest " + reqUrl, HttpStatus.SC_OK,
              response.getStatusLine().getStatusCode());
          try (InputStream bodyStream = response.getEntity().getContent()) {
            JsonObject actual = (JsonObject) parser
                .parse(new InputStreamReader(bodyStream, Charsets.UTF_8));
            if (actual.get("token") != null) {
              String token = actual.remove("token").getAsString();
              if (tokenInSignUp == null) {
                tokenInSignUp = token;
              }
              Assert.assertEquals(tokenInSignUp, token);
            }
            Assert.assertEquals(
                String.format("reqUrl = %s\nexpected=%s\nactual=%s\n", reqUrl, p.getValue1(), actual),
                p.getValue1(), actual);
          }
        }
      }
    }
  }
}