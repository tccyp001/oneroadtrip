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
import com.oneroadtrip.matcher.proto.UserInfoRequest;
import com.oneroadtrip.matcher.proto.UserInfoResponse;
import com.oneroadtrip.matcher.resources.UserInfoResource;
import com.oneroadtrip.matcher.testutil.TestingDataProcessor;

public class UserInfoResourceTest extends DbTest {
  private static final Logger LOG = LogManager.getLogger();

  @Test
  public void test() throws Exception {
    TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
        Files.toString(new File(TESTDATA_PATH + "user_info.data"), Charsets.UTF_8));

    UserInfoResource resource = injector.getInstance(UserInfoResource.class);
    for (Pair<String, String> entry : processor.getCases()) {
      UserInfoRequest.Builder reqBuilder = UserInfoRequest.newBuilder();
      TextFormat.merge(entry.getValue0(), reqBuilder);
      UserInfoResponse.Builder respBuilder = UserInfoResponse.newBuilder();
      TextFormat.merge(entry.getValue1(), respBuilder);

      UserInfoResponse resp = resource.process(reqBuilder.build());
      Assert.assertEquals(respBuilder.build(), resp);
//      LOG.info("\n==========\n==TESTCASE_DATA==\n\n=REQUEST\n{}=RESPONSE\n{}",
//          TextFormat.printToUnicodeString(reqBuilder.build()),
//          TextFormat.printToUnicodeString(resp));
    }
  }
}
