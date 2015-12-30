package com.oneroadtrip.matcher.handlers;

import java.io.File;

import org.javatuples.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import com.oneroadtrip.matcher.proto.GuidePlanRequest;
import com.oneroadtrip.matcher.proto.GuidePlanResponse;
import com.oneroadtrip.matcher.testutil.TestingDataProcessor;

public class GuidePlanRequestHandlerTest extends DbTest {
  @Test
  public void basic() throws Exception {
    TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
        Files.toString(new File(TESTDATA_PATH + "guide_plan.data"), Charsets.UTF_8));
    
    for (Pair<String, String> entry : processor.getCases()) {
      GuidePlanRequest.Builder reqBuilder = GuidePlanRequest.newBuilder();
      TextFormat.merge(entry.getValue0(), reqBuilder);
      GuidePlanResponse.Builder respBuilder = GuidePlanResponse.newBuilder();
      TextFormat.merge(entry.getValue1(), respBuilder);
      GuidePlanRequestHandler handler = injector.getInstance(GuidePlanRequestHandler.class);
      Assert.assertEquals(respBuilder.build(), handler.process(reqBuilder.build()));
    }
  }

}
