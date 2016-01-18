package com.oneroadtrip.matcher.handlers;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import com.oneroadtrip.matcher.proto.PlanRequest;
import com.oneroadtrip.matcher.proto.PlanResponse;
import com.oneroadtrip.matcher.testutil.TestingDataProcessor;

public class PlanRequestHandlerTest extends DbTest {
  private static final Logger LOG = LogManager.getLogger();

  @Test
  public void basic() throws Exception {
    TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
        Files.toString(new File(TESTDATA_PATH + "plan_request_handler.data"), Charsets.UTF_8));

    for (Pair<String, String> entry : processor.getCases()) {
      PlanRequest.Builder reqBuilder = PlanRequest.newBuilder();
      TextFormat.merge(entry.getValue0(), reqBuilder);
      PlanResponse.Builder respBuilder = PlanResponse.newBuilder();
      TextFormat.merge(entry.getValue1(), respBuilder);
      PlanRequestHandler handler = injector.getInstance(PlanRequestHandler.class);
      Assert.assertEquals(handler.process(reqBuilder.build()), respBuilder.build());
//      LOG.info("\n==========\n==TESTCASE_DATA==\n\n=REQUEST\n{}=RESPONSE\n{}",
//          TextFormat.printToUnicodeString(reqBuilder.build()),
//          TextFormat.printToUnicodeString(handler.process(reqBuilder.build())));
    }
  }
}
