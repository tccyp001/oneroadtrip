package com.oneroadtrip.matcher.data;

import java.io.File;
import java.util.List;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import com.oneroadtrip.matcher.handlers.DbTest;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.testutil.TestingDataProcessor;

public class DataAccessorDbTest extends DbTest {
//  @Test
//  public void test() throws Exception {
//    DatabaseAccessor accessor = injector.getInstance(DatabaseAccessor.class);
//    accessor.insertOneReservation(1L, 20151225, true, 0L);
//    accessor.insertOneReservation(3L, 20151226, true, 0L);
//    accessor.insertOneReservation(3L, 20151227, false, 1L);
//    accessor.insertOneReservation(4L, 20151227, false, 2L);
//    accessor.insertOneReservation(7L, 20151225, true, 0L);
//
//    Assert.assertEquals(accessor.loadGuideToReserveDays(Lists.newArrayList(1L, 3L, 4L, 7L), 2L),
//        ImmutableMap.of(1L, ImmutableSet.of(20151225), 3L, ImmutableSet.of(20151226), 4L,
//            ImmutableSet.of(20151227), 7L, ImmutableSet.of(20151225)));
//  }
  
  @Test
  public void test() throws Exception {
    TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
        Files.toString(new File(TESTDATA_PATH + "data_accessor.data"), Charsets.UTF_8));
    DatabaseAccessor accessor = injector.getInstance(DatabaseAccessor.class);
    TestingDataAccessor testingAccessor = injector.getInstance(TestingDataAccessor.class);
    
    for (Pair<String, String> entry : processor.getCases()) {
      Itinerary.Builder itinBuilder = Itinerary.newBuilder();
      TextFormat.merge(entry.getValue0(), itinBuilder);
      Itinerary itin = itinBuilder.build();
      Triplet<Long, Long, List<Long>> out = accessor.appendOrder(itin);
      
//      Itinerary expected = respBuilder.build();
//      Assert.assertEquals(expected.getOrderId(), out.getValue0().longValue());
//      Assert.assertEquals(expected.getItineraryId(), out.getValue1().longValue());
//      Assert.assertEquals(expected.getReservationIdList(), out.getValue2());
//
      testingAccessor.validateBooking(itin, out.getValue0(), out.getValue1(), out.getValue2());
    }
  }
}
