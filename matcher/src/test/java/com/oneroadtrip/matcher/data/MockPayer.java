package com.oneroadtrip.matcher.data;

import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.Order;
import com.oneroadtrip.matcher.proto.Status;

public class MockPayer implements Payer {
  private boolean success;
  
  public MockPayer(boolean success) {
    this.success = success;
  }
  
  public void reset(boolean success) {
    this.success = success;
  }

  @Override
  public Order makePayment(Order order) throws OneRoadTripException {
    if (!success) {
      throw new OneRoadTripException(Status.ERROR_IN_STRIPE, null);
    }

    return Order.newBuilder(order).setStripeChargeId("abcdef").setIdempotencyKey("xfguoIdempotencyKey")
        .build();
  }

  @Override
  public void refundCharge(String chargeId, float refundAmount, String reason)
      throws OneRoadTripException {
    if (!success) {
      throw new OneRoadTripException(Status.ERROR_IN_STRIPE, null);
    }
  }
}
