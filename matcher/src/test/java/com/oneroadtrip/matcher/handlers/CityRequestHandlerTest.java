package com.oneroadtrip.matcher.handlers;

import java.io.File;
import java.sql.Connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import com.oneroadtrip.matcher.CityRequest;
import com.oneroadtrip.matcher.CityResponse;
import com.oneroadtrip.matcher.testutil.TestingDataProcessor;

public class CityRequestHandlerTest extends DbTest {
  private static final Logger LOG = LogManager.getLogger();

  @Test
  public void test() throws Exception {
    CityRequestHandler handler = injector.getInstance(CityRequestHandler.class);

    // TODO(xfguo): Add run sql script.
    Connection conn = h2Info.connection.get();

    File testFile = new File(TESTDATA_PATH + "city_request_handler.data");
    TestingDataProcessor processor = TestingDataProcessor.loadData(conn,
        Files.toString(new File(TESTDATA_PATH + "city_request_handler.data"), Charsets.UTF_8));

    for (Pair<String, String> entry : processor.getCases()) {
      LOG.info("xfguo: req text = '{}', resp text = '{}'", entry.getValue0(), entry.getValue1());
      CityRequest.Builder reqBuilder = CityRequest.newBuilder();
      TextFormat.merge(entry.getValue0(), reqBuilder);
      CityResponse.Builder respBuilder = CityResponse.newBuilder();
      TextFormat.merge(entry.getValue1(), respBuilder);
      CityRequest req = reqBuilder.build();
      CityResponse resp = respBuilder.build();
      LOG.info("xfguo: parsed req = '{}', parsed resp = '{}'", req, resp);
      Assert.assertEquals(resp, handler.process(req).build());
    }
  }
}
