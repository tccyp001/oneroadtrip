package com.oneroadtrip.matcher.data;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import com.oneroadtrip.matcher.handlers.DbTest;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.testutil.TestingDataProcessor;
import com.oneroadtrip.matcher.util.SqlUtil;

public class DataAccessorDbTest extends DbTest {
  @Test
  public void test() throws Exception {
    TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
        Files.toString(new File(TESTDATA_PATH + "data_accessor.data"), Charsets.UTF_8));
    TestingDataAccessor testingAccessor = injector.getInstance(TestingDataAccessor.class);
    DataSource dataSource = injector.getInstance(DataSource.class);
    
    for (Pair<String, String> entry : processor.getCases()) {
      Itinerary.Builder itinBuilder = Itinerary.newBuilder();
      TextFormat.merge(entry.getValue0(), itinBuilder);
      Itinerary itin = itinBuilder.build();
      Triplet<Long, Long, List<Long>> out = SqlUtil.executeTransaction(dataSource,
          (Connection conn) -> DatabaseAccessor.prepareForOrder(itin, conn));
      testingAccessor.validateBooking(itin, out.getValue0(), out.getValue1(), out.getValue2());
    }
  }
}
