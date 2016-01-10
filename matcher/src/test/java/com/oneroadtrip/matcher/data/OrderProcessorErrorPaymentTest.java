package com.oneroadtrip.matcher.data;

import java.io.File;

import junit.framework.Assert;

import org.javatuples.Pair;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import com.oneroadtrip.matcher.handlers.DbTest;
import com.oneroadtrip.matcher.proto.OrderRequest;
import com.oneroadtrip.matcher.proto.OrderResponse;
import com.oneroadtrip.matcher.testutil.TestingDataProcessor;

public class OrderProcessorErrorPaymentTest extends DbTest {

  @Test
  public void test() throws Exception {
    TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
        Files.toString(new File(TESTDATA_PATH + "order_fail.data"), Charsets.UTF_8));
    
    ((MockPayer) injector.getInstance(Payer.class)).reset(false);
    OrderProcessor orderProcessor = injector.getInstance(OrderProcessor.class);
    for (Pair<String, String> entry : processor.getCases()) {
      OrderRequest.Builder reqBuilder = OrderRequest.newBuilder();
      TextFormat.merge(entry.getValue0(), reqBuilder);
      OrderResponse.Builder respBuilder = OrderResponse.newBuilder();
      TextFormat.merge(entry.getValue1(), respBuilder);
      Assert.assertEquals(respBuilder.build(), orderProcessor.process(reqBuilder.build()));
    }
  }

}
