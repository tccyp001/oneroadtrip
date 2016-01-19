package com.oneroadtrip.tools;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.oneroadtrip.matcher.data.Payer;
import com.oneroadtrip.matcher.data.StripePayer;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.Order;
import com.stripe.model.Token;
import com.stripe.net.RequestOptions;
import com.stripe.net.RequestOptions.RequestOptionsBuilder;

public class ExperimentStripePayment {
  private static final Logger LOG = LogManager.getLogger();

  public static void main(String[] args) throws Exception {
    RequestOptions options = new RequestOptionsBuilder().setApiKey(
        "sk_test_x7J2qxqTLBNo4WQoYkRNMEGx").build();

    Map<String, Object> defaultCardParams = Maps.newTreeMap();
    defaultCardParams.put("number", "4242424242424242");
    defaultCardParams.put("exp_month", 12);
    defaultCardParams.put("exp_year", 2017);
    defaultCardParams.put("cvc", "123");
    defaultCardParams.put("name", "J Bindings Cardholder");
    defaultCardParams.put("address_line1", "140 2nd Street");
    defaultCardParams.put("address_line2", "4th Floor");
    defaultCardParams.put("address_city", "San Francisco");
    defaultCardParams.put("address_zip", "94105");
    defaultCardParams.put("address_state", "CA");
    defaultCardParams.put("address_country", "USA");
    Map<String, Object> defaultTokenParams = ImmutableMap.of("card", defaultCardParams);

    Token token = Token.create(defaultTokenParams, options);
    Order order = Order.newBuilder().setCostUsd(3.00f).setToken(token.getId())
        .setDescription("test xfguo").build();
    Itinerary itin = Itinerary.newBuilder().setOrder(order).build();
    Payer payer = new StripePayer();
    Order newOrder = payer.makePayment(order);
    LOG.info("xfguo: origin order: \n{}", order);
    LOG.info("xfguo: returned order:\n{}", newOrder);
  }
}
