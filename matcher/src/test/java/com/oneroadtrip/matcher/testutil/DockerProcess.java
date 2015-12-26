package com.oneroadtrip.matcher.testutil;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.oneroadtrip.matcher.util.HashUtil;

import junit.framework.Assert;

public abstract class DockerProcess implements Closeable {
  private static final Logger LOG = LogManager.getLogger();

  // Only tested in ubuntu.
  private static final String DOCKER_BIN = "/usr/bin/docker";

  private final String imageName;
  private final String dockerName;
  private final int port;
  private final int incomingPort;
  private final String volumnPair;
  private final String otherParams;

  protected Process process = null;

  public DockerProcess(String imageName, String postfix) {
    this(imageName, postfix, "");
  }

  public DockerProcess(String imageName, String postfix, String otherParams) {
    this(imageName, postfix, "", otherParams);
  }

  public DockerProcess(String imageName, String postfix, String volumnPair, String otherParams) {
    this(imageName, postfix, 0, 0, volumnPair, otherParams);
  }

  public DockerProcess(String imageName, String postfix, int port, int incomingPort) {
    this(imageName, postfix, port, incomingPort, "");
  }

  public DockerProcess(String imageName, String postfix, int port, int incomingPort,
      String otherParams) {
    this(imageName, postfix, port, incomingPort, "", otherParams);
  }

  public DockerProcess(String imageName, String postfix, int port, int incomingPort,
      String volumnPair, String otherParams) {
    this.imageName = imageName;
    dockerName = String.format("%s-%s", HashUtil.getRandomString(40), postfix);
    this.port = port;
    this.incomingPort = incomingPort;
    this.volumnPair = Preconditions.checkNotNull(volumnPair);
    this.otherParams = Preconditions.checkNotNull(otherParams);
  }
  
  public int getPort() {
    return port;
  }

  public abstract boolean expectStarted() throws InterruptedException, ExecutionException, IOException;

  public void start() {
    StringBuilder builder = new StringBuilder()
        .append(String.format("%s run -i --name %s", DOCKER_BIN, dockerName));
    if (port > 0 && incomingPort > 0) {
      builder.append(String.format(" -p %d:%d", port, incomingPort));
    }
    if (!volumnPair.isEmpty()) {
      builder.append(" -v ").append(volumnPair);
    }
    if (!otherParams.isEmpty()) {
      builder.append(" ").append(otherParams);
    }
    builder.append(" ").append(imageName);

    try {
      String cmd = builder.toString();
      LOG.info("docker command: '{}'", cmd);
      process = new ProcessBuilder(cmd.split(" ")).redirectErrorStream(true).start();
      Assert.assertTrue(expectStarted());
      LOG.info("{} is successfully started", dockerName);
      Thread.sleep(TimeUnit.SECONDS.toMillis(5));
    } catch (IOException | InterruptedException | ExecutionException e) {
      LOG.info("Errors in starting docker job %s", dockerName, e);
    }
  }

  @Override
  public void close() throws IOException {
    if (process != null && process.isAlive()) {
      process.destroy();
    }

    try {
      Process stopProcess = new ProcessBuilder(
          String.format("%s rm -f %s", DOCKER_BIN, dockerName).split(" ")).start();
      stopProcess.waitFor();
    } catch (InterruptedException e) {
      LOG.info("Errors in interrupting docker job %s", dockerName, e);
    }
  }

//  private static final Pattern MYSQL_STARTED_LOG_PATTERN = Pattern
//      .compile(".*mysqld: ready for connections.*");
//@Override
//public boolean expectStarted() throws InterruptedException, ExecutionException, IOException {
//  int maxTries = 10;
//  boolean successfullyStarted = false;
//  ExecutorService executor = Executors.newFixedThreadPool(1);
//  try (BufferedReader br = new BufferedReader(
//      new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
//    String line = null;
//    int count = 0;
//    while (!process.waitFor(1, TimeUnit.SECONDS)) {
//      Callable<String> readLineTask = new Callable<String>() {
//        @Override
//        public String call() throws Exception {
//          return br.readLine();
//        }
//      };
//      try {
//        do {
//          Future<String> future = executor.submit(readLineTask);
//          line = future.get(1, TimeUnit.SECONDS);
//          LOG.info("line: {}", line);
//          if (MYSQL_STARTED_LOG_PATTERN.matcher(line).matches())
//            successfullyStarted = true;
//        } while (!successfullyStarted);
//      } catch (TimeoutException e) {
//        LOG.info("xfguo: last line: {}", line);
//      }
//      LOG.info("Check status of mysql {} times", ++count);
//      if (successfullyStarted || count == maxTries)
//        break;
//    }
//  }
//  return successfullyStarted;
//}

}
