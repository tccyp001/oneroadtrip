package com.oneroadtrip.matcher.data;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;
import com.oneroadtrip.matcher.OneRoadTripConfig;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.Order;
import com.oneroadtrip.matcher.proto.Status;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.net.RequestOptions.RequestOptionsBuilder;

public class StripePayer implements Payer {
  private static final Logger LOG = LogManager.getLogger();
  
  @Inject
  OneRoadTripConfig config;
//
//  // TODO(xfguo): Inject api key.
//  private static final String apiKey = "sk_test_x7J2qxqTLBNo4WQoYkRNMEGx";

  public Order makePayment(Order order) throws OneRoadTripException {
    Order.Builder builder = Order.newBuilder(order);
    String idempotencyKey = order.getIdempotencyKey();
    if (idempotencyKey.isEmpty()) {
      idempotencyKey = UUID.randomUUID().toString();
      builder.setIdempotencyKey(idempotencyKey);
    }

    LOG.info("xfguo: config = {}", config);
    LOG.info("xfguo: idempotencyKey = {}, stripeSecureKey = {}", idempotencyKey, config.stripeSecureKey);
    RequestOptions options = new RequestOptionsBuilder().setIdempotencyKey(idempotencyKey)
        .setApiKey(config.stripeSecureKey).build();

    Map<String, Object> chargeParams = Maps.newHashMap();
    chargeParams.put("amount", (long) (order.getCostUsd() * 100));
    chargeParams.put("currency", "usd");
    chargeParams.put("source", order.getToken());
    if (!order.getDescription().isEmpty()) {
      chargeParams.put("description", order.getDescription());
    }

    try {
      Charge charge = Charge.create(chargeParams, options);
      builder.setStripeChargeId(charge.getId());
    } catch (AuthenticationException | InvalidRequestException | APIConnectionException
        | CardException | APIException e) {
      e.printStackTrace();

      // TODO(xfguo): Wrap the exception and throw an OneRoadTripException out.
      throw new OneRoadTripException(Status.ERROR_IN_STRIPE, e);
    }

    return builder.build();
  }

  @Override
  public void refundCharge(String chargeId, float refundAmount, String reason) throws OneRoadTripException {
    RequestOptions options = new RequestOptionsBuilder().setApiKey(config.stripeSecureKey).build();
    Map<String, Object> refundParams = Maps.newHashMap();
    refundParams.put("amount", (long) (refundAmount * 100));
    refundParams.put("charge", chargeId);
    if (reason != null && !reason.isEmpty()) {
      refundParams.put("reason", reason);
    }
    
    try {
      Refund refund = Refund.create(refundParams, options);
    } catch (AuthenticationException | InvalidRequestException | APIConnectionException
        | CardException | APIException e) {
      throw new OneRoadTripException(Status.ERROR_IN_STRIPE, e);
    }
  }
}
