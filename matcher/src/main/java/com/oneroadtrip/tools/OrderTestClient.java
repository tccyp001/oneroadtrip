package com.oneroadtrip.tools;

import java.io.File;
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
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import com.googlecode.protobuf.format.JsonFormat;
import com.oneroadtrip.matcher.proto.BookingRequest;
import com.oneroadtrip.matcher.proto.BookingResponse;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.OrderRequest;
import com.oneroadtrip.matcher.proto.OrderResponse;
import com.oneroadtrip.matcher.proto.SignupRequest;
import com.oneroadtrip.matcher.proto.SignupResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.proto.testing.TestingMessage;
import com.oneroadtrip.matcher.util.HashUtil.Hasher;
import com.oneroadtrip.matcher.util.HashUtil.HasherImpl;
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
    
    @Parameter(names = "--proto_path", description = "Protobuf text file path", required = false)
    public String protoPath = "src/test/resources/testclient/case1.data";

    @Parameter(names = { "-h", "--help" }, description = "print help message", required = false)
    public boolean help = false;
  }

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

    String itineraryContent = Files.toString(new File(config.protoPath), Charsets.UTF_8);
    TestingMessage.Builder msgBuilder = TestingMessage.newBuilder();
    TextFormat.merge(itineraryContent, msgBuilder);

    Hasher hasher = new HasherImpl();
    String userName =  String.format("test_%s", hasher.getRandomString(50));
    SignupRequest signup = SignupRequest.newBuilder(msgBuilder.getSignup())
        .setUsername(userName).build();
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      SignupResponse.Builder signupRespBuilder = SignupResponse.newBuilder();
      {
        HttpPost post = new HttpPost(String.format("%s/signup", config.apiUrl));
        post.setEntity(new StringEntity(JsonFormat.printToString(signup),
            ContentType.APPLICATION_JSON));
        LOG.info("http post request: \n'{}'", post);
        HttpResponse response = client.execute(post);
        Preconditions.checkArgument(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        try (InputStream bodyStream = response.getEntity().getContent()) {
          JsonFormat.merge(new InputStreamReader(bodyStream, Charsets.UTF_8), signupRespBuilder);
          Preconditions.checkArgument(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
          LOG.info("signup response: \n'{}'", signupRespBuilder.build());
        }
      }

      String userToken = signupRespBuilder.getToken();
      Itinerary itin = Itinerary.newBuilder(msgBuilder.getItinerary(0))
          .setUserToken(userToken).build();
      BookingRequest request = BookingRequest.newBuilder().setItinerary(itin).build();
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
          Preconditions.checkArgument(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
          LOG.info("booking response: \n'{}'", respBuilder.build());
        }
      }

      Preconditions.checkArgument(respBuilder.getStatus() == Status.SUCCESS);
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

      Preconditions.checkArgument(orderRespBuilder.getStatus() == Status.SUCCESS);
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
      Preconditions.checkArgument(refundRespBuilder.getStatus() == Status.SUCCESS);
    }
  }
}
