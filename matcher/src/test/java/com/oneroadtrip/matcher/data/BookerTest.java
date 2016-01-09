package com.oneroadtrip.matcher.data;

import java.io.File;

import junit.framework.Assert;

import org.javatuples.Pair;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import com.oneroadtrip.matcher.handlers.DbTest;
import com.oneroadtrip.matcher.proto.BookingRequest;
import com.oneroadtrip.matcher.proto.BookingResponse;
import com.oneroadtrip.matcher.testutil.TestingDataProcessor;

public class BookerTest extends DbTest {
  @Test
  public void test() throws Exception {
    TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
        Files.toString(new File(TESTDATA_PATH + "booking.data"), Charsets.UTF_8));
    
    Booker booker = injector.getInstance(Booker.class);
    for (Pair<String, String> entry : processor.getCases()) {
      BookingRequest.Builder reqBuilder = BookingRequest.newBuilder();
      TextFormat.merge(entry.getValue0(), reqBuilder);
      BookingResponse.Builder respBuilder = BookingResponse.newBuilder();
      TextFormat.merge(entry.getValue1(), respBuilder);
      Assert.assertEquals(respBuilder.build(), booker.process(reqBuilder.build()));
    }
  }
}
