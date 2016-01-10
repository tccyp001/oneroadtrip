package com.oneroadtrip.matcher.data;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.Order;
import com.oneroadtrip.matcher.proto.Status;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;
import com.stripe.net.RequestOptions.RequestOptionsBuilder;

public class StripePayer implements Payer {
  // TODO(xfguo): Inject api key.
  private static final String apiKey = "sk_test_x7J2qxqTLBNo4WQoYkRNMEGx";

  public Order makePayment(Order order) throws OneRoadTripException {
    Order.Builder builder = Order.newBuilder(order);
    String idempotencyKey = order.getIdempotencyKey();
    if (idempotencyKey.isEmpty()) {
      idempotencyKey = UUID.randomUUID().toString();
      builder.setIdempotencyKey(idempotencyKey);
    }

    RequestOptions options = new RequestOptionsBuilder().setIdempotencyKey(idempotencyKey)
        .setApiKey(apiKey).build();

    Map<String, Object> chargeParams = Maps.newHashMap();
    chargeParams.put("amount", (long) (order.getCostUsd() * 100));
    chargeParams.put("currency", "usd");
    chargeParams.put("source", order.getToken());
    chargeParams.put("description", order.getDescription());

    try {
      Charge charge = Charge.create(chargeParams, options);
      builder.setStripeChargeId(charge.getId());
    } catch (AuthenticationException | InvalidRequestException | APIConnectionException
        | CardException | APIException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();

      // TODO(xfguo): Wrap the exception and throw an OneRoadTripException out.
      throw new OneRoadTripException(Status.ERROR_IN_STRIPE, e);
    }

    return builder.build();
  }
}
