package com.oneroadtrip.matcher.data;

import java.sql.Connection;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.internal.Lists;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.Order;
import com.oneroadtrip.matcher.proto.OrderRequest;
import com.oneroadtrip.matcher.proto.OrderResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.util.SqlUtil;

public class OrderProcessor {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  DataSource dataSource;
  
  @Inject
  DatabaseAccessor dbAccessor;

  @Inject
  Payer payer;

  /**
   * 关于Stripe charge一些要注意的点： - 保留charge_id在数据库中，以后可能会有refund之类的事情。（partially
   * refund需要figure out） - 如果charge成功了，但是数据在本地数据库更新失败，是否有rollback机制？ -
   * 保证只charge一次，特别是在charge失败的时候。（可以使用Idempotency-Key） - OrderId可以放在stripe
   * charge的metadata中，这样当一个charge成功了之后，以后可以作为一个备份的查询点。 -
   * 设置Stripe-Version以免stripe系统升级导致的不必要的问题。
   */
  public OrderResponse process(OrderRequest request) {
    final List<Long> guideReservationIds = Lists.newArrayList();
    Order order = null;
    try {
      Itinerary itin = request.getItinerary();
      guideReservationIds.addAll(SqlUtil.executeTransaction(dataSource,
          (Connection c) -> DatabaseAccessor.reserveGuides(itin, c)));
      order = payer.makePayment(itin.getOrder());
      dbAccessor.updateOrder(order.getOrderId(), order.getStripeChargeId());
    } catch (OneRoadTripException e) {
      try {
        int revertRows = SqlUtil.executeTransaction(dataSource,
            (Connection c) -> DatabaseAccessor.revertReservedGuides(guideReservationIds, c));
        LOG.info("Revert rows: {} for maybe payment error", revertRows);
      } catch (OneRoadTripException e1) {
        // TODO(xfguo): Serious exception, needs to take care ASAP.
        LOG.error("Failed to revert reservation of guides, please take a look ASAP: ", e1);
        return OrderResponse.newBuilder().setStatus(e1.getStatus()).build();
      }
      return OrderResponse.newBuilder().setStatus(e.getStatus()).build();
    }

    Itinerary newItin = Itinerary.newBuilder(request.getItinerary())
        .addAllReservationId(guideReservationIds).setOrder(order).build();
    return OrderResponse.newBuilder().setStatus(Status.SUCCESS).setItinerary(newItin).build();
  }
  
  public OrderResponse refund(OrderRequest request) {
    Itinerary itin = request.getItinerary();
    Order order = itin.getOrder();
    try {
      String chargeId = dbAccessor.getChargeId(order.getOrderId());
      payer.refundCharge(chargeId, order.hasRefundUsd() ? order.getRefundUsd()
          : order.getCostUsd(), order.getRefundReason());
      dbAccessor.cancelOrder(order);
      return OrderResponse.newBuilder().setStatus(Status.SUCCESS).build();
    } catch (OneRoadTripException e) {
      return OrderResponse.newBuilder().setStatus(e.getStatus()).build();
    }
  }
}
