package com.oneroadtrip.matcher.data;

import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.Order;

public interface Payer {
  public Order makePayment(Order order) throws OneRoadTripException;

  public void refundCharge(String chargeId, float refundAmount, String reason) throws OneRoadTripException;
}
