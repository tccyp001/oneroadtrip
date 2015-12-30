package com.oneroadtrip.matcher.handlers;

import java.io.File;

import org.javatuples.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import com.oneroadtrip.matcher.proto.SpotPlanRequest;
import com.oneroadtrip.matcher.proto.SpotPlanResponse;
import com.oneroadtrip.matcher.testutil.TestingDataProcessor;

public class SpotRequestHandlerTest extends DbTest {
  @Test
  public void basic() throws Exception {
    TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
        Files.toString(new File(TESTDATA_PATH + "spot_plan.data"), Charsets.UTF_8));

    for (Pair<String, String> entry : processor.getCases()) {
      SpotPlanRequest.Builder reqBuilder = SpotPlanRequest.newBuilder();
      TextFormat.merge(entry.getValue0(), reqBuilder);
      SpotPlanResponse.Builder respBuilder = SpotPlanResponse.newBuilder();
      TextFormat.merge(entry.getValue1(), respBuilder);
      SpotRequestHandler handler = injector.getInstance(SpotRequestHandler.class);
      Assert.assertEquals(handler.process(reqBuilder.build()), respBuilder.build());
    }
  }

}
