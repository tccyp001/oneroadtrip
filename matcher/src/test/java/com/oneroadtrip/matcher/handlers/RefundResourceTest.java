package com.oneroadtrip.matcher.handlers;

import java.io.File;

import junit.framework.Assert;

import org.javatuples.Pair;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import com.oneroadtrip.matcher.data.OrderProcessor;
import com.oneroadtrip.matcher.proto.OrderRequest;
import com.oneroadtrip.matcher.proto.OrderResponse;
import com.oneroadtrip.matcher.testutil.TestingDataProcessor;

public class RefundResourceTest extends DbTest {
  @Test
  public void test() throws Exception {
    TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
        Files.toString(new File(TESTDATA_PATH + "refund.data"), Charsets.UTF_8));

    OrderProcessor orderProcessor = injector.getInstance(OrderProcessor.class);
    for (Pair<String, String> entry : processor.getCases()) {
      OrderRequest.Builder reqBuilder = OrderRequest.newBuilder();
      TextFormat.merge(entry.getValue0(), reqBuilder);
      OrderResponse.Builder respBuilder = OrderResponse.newBuilder();
      TextFormat.merge(entry.getValue1(), respBuilder);

      Assert.assertEquals(respBuilder.build(), orderProcessor.refund(reqBuilder.build()));
    }
  }
}
