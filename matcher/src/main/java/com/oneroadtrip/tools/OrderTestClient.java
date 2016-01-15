package com.oneroadtrip.tools;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.protobuf.TextFormat;
import com.googlecode.protobuf.format.JsonFormat;
import com.oneroadtrip.matcher.proto.BookingRequest;
import com.oneroadtrip.matcher.proto.BookingResponse;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.OrderRequest;
import com.oneroadtrip.matcher.proto.OrderResponse;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Token;
import com.stripe.net.RequestOptions;
import com.stripe.net.RequestOptions.RequestOptionsBuilder;

public class OrderTestClient {
  private static final Logger LOG = LogManager.getLogger();

  private static class Config {
    @Parameter(names = "--api_url", description = "API prefix", required = false)
    public String apiUrl = "http://127.0.0.1:8080/api";

    @Parameter(names = { "-h", "--help" }, description = "print help message", required = false)
    public boolean help = false;
  }

  static final String ITINERARY_TEXT = "" + "  city {" + "    num_days: 2"
      + "    start_date: 20151229" + "    guide {" + "      guide_id: 1" + "    }" + "  }"
      + "  city {" + "    num_days: 3" + "    start_date: 20151231" + "    guide {"
      + "      guide_id: 2" + "    }" + "  }" + "  city {" + "    num_days: 2"
      + "    start_date: 20160103" + "    guide {" + "      guide_id: 1" + "    }" + "  }"
      + "  choose_one_guide_solution: false" + "  quote_for_one_guide {" + "    cost_usd: 4900.0"
      + "    route_cost: 3000.0" + "    hotel_cost: 1400.0" + "    hotel_cost_for_guide: 500.0"
      + "  }" + "  quote_for_multiple_guides {" + "    cost_usd: 5000.0" + "    route_cost: 3000.0"
      + "    hotel_cost: 1700.0" + "    hotel_cost_for_guide: 300.0" + "  }";

  static String createStripeToken() throws AuthenticationException, InvalidRequestException,
      APIConnectionException, CardException, APIException {
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
    return token.getId();
  }

  public static void main(String[] args) throws Exception {
    for (String arg : args) {
      LOG.info("arg: {}", arg);
    }
    Config config = new Config();
    JCommander jc = new JCommander(config, args);
    if (config.help) {
      jc.usage();
      return;
    }

    Itinerary.Builder itinBuilder = Itinerary.newBuilder();
    TextFormat.merge(ITINERARY_TEXT, itinBuilder);
    BookingRequest request = BookingRequest.newBuilder().setItinerary(itinBuilder).build();

    try (CloseableHttpClient client = HttpClients.createDefault()) {
      BookingResponse.Builder respBuilder = BookingResponse.newBuilder();
      {
        HttpPost post = new HttpPost(String.format("%s/booking", config.apiUrl));
        post.setEntity(new StringEntity(JsonFormat.printToString(request),
            ContentType.APPLICATION_JSON));
        LOG.info("http post request: \n'{}'", post);
        HttpResponse response = client.execute(post);
        Preconditions.checkArgument(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        try (InputStream bodyStream = response.getEntity().getContent()) {
          JsonFormat.merge(new InputStreamReader(bodyStream, Charsets.UTF_8), respBuilder);
          LOG.info("booking response: \n'{}'", respBuilder.build());
        }
      }

      Itinerary.Builder orderItinBuilder = Itinerary.newBuilder(respBuilder.getItinerary());
      orderItinBuilder.getOrderBuilder().setDescription("testing")
          .setToken(createStripeToken());
      OrderResponse.Builder orderRespBuilder = OrderResponse.newBuilder();
      {
        HttpPost post = new HttpPost(String.format("%s/order", config.apiUrl));
        post.setEntity(new StringEntity(JsonFormat.printToString(OrderRequest.newBuilder()
            .setItinerary(orderItinBuilder).build()), ContentType.APPLICATION_JSON));
        HttpResponse response = client.execute(post);
        Preconditions.checkArgument(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        try (InputStream bodyStream = response.getEntity().getContent()) {
          JsonFormat.merge(new InputStreamReader(bodyStream, Charsets.UTF_8), orderRespBuilder);
          LOG.info("order response: \n'{}'", orderRespBuilder.build());
        }
      }

      Itinerary.Builder refundItinBuilder = Itinerary.newBuilder(orderRespBuilder.getItinerary());
      OrderResponse.Builder refundRespBuilder = OrderResponse.newBuilder();
      {
        HttpPost post = new HttpPost(String.format("%s/refund", config.apiUrl));
        post.setEntity(new StringEntity(JsonFormat.printToString(OrderRequest.newBuilder()
            .setItinerary(refundItinBuilder).build()), ContentType.APPLICATION_JSON));
        HttpResponse response = client.execute(post);
        Preconditions.checkArgument(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        try (InputStream bodyStream = response.getEntity().getContent()) {
          JsonFormat.merge(new InputStreamReader(bodyStream, Charsets.UTF_8), refundRespBuilder);
          LOG.info("refund response: \n'{}'", refundRespBuilder);
        }
      }
    }
  }
}
