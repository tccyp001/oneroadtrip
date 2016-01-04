package com.oneroadtrip.matcher.handlers;

import java.io.File;

import org.javatuples.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import com.oneroadtrip.matcher.proto.QuoteRequest;
import com.oneroadtrip.matcher.proto.QuoteResponse;
import com.oneroadtrip.matcher.resources.QuoteResource;
import com.oneroadtrip.matcher.testutil.TestingDataProcessor;

public class QuoteResourceTest extends DbTest {

  @Test
  public void test() throws Exception {
    TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
        Files.toString(new File(TESTDATA_PATH + "quote.data"), Charsets.UTF_8));
    
    for (Pair<String, String> entry : processor.getCases()) {
      QuoteRequest.Builder reqBuilder = QuoteRequest.newBuilder();
      TextFormat.merge(entry.getValue0(), reqBuilder);
      QuoteResponse.Builder respBuilder = QuoteResponse.newBuilder();
      TextFormat.merge(entry.getValue1(), respBuilder);
      QuoteResource handler = injector.getInstance(QuoteResource.class);
      Assert.assertEquals(handler.process(reqBuilder.build()), respBuilder.build());
    }

  }
}
