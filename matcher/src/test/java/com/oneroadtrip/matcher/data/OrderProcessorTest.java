package com.oneroadtrip.matcher.data;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.javatuples.Pair;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.handlers.DbTest;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.OrderRequest;
import com.oneroadtrip.matcher.proto.OrderResponse;
import com.oneroadtrip.matcher.testutil.TestingDataProcessor;
import com.oneroadtrip.matcher.util.SqlUtil;
import com.oneroadtrip.matcher.util.Util;

public class OrderProcessorTest {

  public static class SuccessPaymentTest extends DbTest {
    @Test
    public void test() throws Exception {
      TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
          Files.toString(new File(TESTDATA_PATH + "order_success.data"), Charsets.UTF_8));

      OrderProcessor orderProcessor = injector.getInstance(OrderProcessor.class);
      for (Pair<String, String> entry : processor.getCases()) {
        OrderRequest.Builder reqBuilder = OrderRequest.newBuilder();
        TextFormat.merge(entry.getValue0(), reqBuilder);
        OrderResponse.Builder respBuilder = OrderResponse.newBuilder();
        TextFormat.merge(entry.getValue1(), respBuilder);

        Assert.assertEquals(respBuilder.build(), orderProcessor.process(reqBuilder.build()));

        validateDatabase(injector.getInstance(DataSource.class), reqBuilder.getItinerary());

        // Only process one case.
        break;
      }
    }
    
    static void validateDatabase(DataSource dataSource, Itinerary itin) throws OneRoadTripException,
        SQLException {
      List<Pair<Long, Integer>> actual = SqlUtil.executeTransaction(dataSource,
          (Connection conn) -> validateReserveGuide(itin, conn));
      int status = SqlUtil.executeTransaction(dataSource,
          (Connection conn) -> validateOrderStatus(itin.getOrder().getOrderId(), conn));
      Assert.assertEquals(Util.getGuideReservationMap(itin), actual);
      Assert.assertEquals(2, status); // PAID
    }
  }
  
  public static class FailedPaymentTest extends DbTest {
    @Test
    public void test() throws Exception {
      TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
          Files.toString(new File(TESTDATA_PATH + "order_fail.data"), Charsets.UTF_8));
      
      ((MockPayer) injector.getInstance(Payer.class)).reset(false);
      OrderProcessor orderProcessor = injector.getInstance(OrderProcessor.class);
      DataSource dataSource = injector.getInstance(DataSource.class);
      for (Pair<String, String> entry : processor.getCases()) {
        OrderRequest.Builder reqBuilder = OrderRequest.newBuilder();
        TextFormat.merge(entry.getValue0(), reqBuilder);
        OrderResponse.Builder respBuilder = OrderResponse.newBuilder();
        TextFormat.merge(entry.getValue1(), respBuilder);
        Assert.assertEquals(respBuilder.build(), orderProcessor.process(reqBuilder.build()));
        
        Itinerary itin = reqBuilder.getItinerary();
        List<Pair<Long, Integer>> actual = SqlUtil.executeTransaction(dataSource,
            (Connection conn) -> validateReserveGuide(itin, conn));
        Assert.assertEquals(0, actual.size());
        
        // only process one case.
        break;
      }
    }
  }

  private static final String VALIDATE_RESERVED_GUIDES = "SELECT guide_id, reserved_date "
      + "FROM GuideReservations WHERE is_permanent = true AND itinerary_id = ?";
  static List<Pair<Long, Integer>> validateReserveGuide(Itinerary itin, Connection conn)
      throws SQLException {
    List<Pair<Long, Integer>> actual = Lists.newArrayList();
    try (PreparedStatement pStmt = conn.prepareStatement(VALIDATE_RESERVED_GUIDES)) {
      pStmt.setLong(1, itin.getItineraryId());
      ResultSet rs = pStmt.executeQuery();
      while (rs.next()) {
        actual.add(Pair.with(rs.getLong(1), rs.getInt(2)));
      }
    }
    return actual;
  }

  private static final String VALIDATE_ORDER_STATUS = "SELECT status FROM Orders WHERE order_id = ?";
  static int validateOrderStatus(long orderId, Connection conn) throws SQLException {
    try (PreparedStatement pStmt = conn.prepareStatement(VALIDATE_ORDER_STATUS)) {
      pStmt.setLong(1, orderId);
      ResultSet rs = pStmt.executeQuery();
      Assert.assertTrue(rs.next());
      int status = rs.getInt(1);
      Assert.assertFalse(rs.next());
      return status;
    }
  }
}
