package com.oneroadtrip.tools;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.protobuf.TextFormat;
import com.googlecode.protobuf.format.JsonFormat;
import com.oneroadtrip.matcher.proto.BookingRequest;
import com.oneroadtrip.matcher.proto.BookingResponse;
import com.oneroadtrip.matcher.proto.Itinerary;
import com.oneroadtrip.matcher.proto.OrderRequest;
import com.oneroadtrip.matcher.proto.OrderResponse;

public class OrderTestClient {
  private static final Logger LOG = LogManager.getLogger();

  static final String ITINERARY_TEXT = "itinerary {"
      + "  city {"
      + "    num_days: 2"
      + "    start_date: 20151229"
      + "    guide {"
      + "      guide_id: 1"
      + "    }"
      + "  }"
      + "  city {"
      + "    num_days: 3"
      + "    start_date: 20151231"
      + "    guide {"
      + "      guide_id: 2"
      + "    }"
      + "  }"
      + "  city {"
      + "    num_days: 2"
      + "    start_date: 20160103"
      + "    guide {"
      + "      guide_id: 1"
      + "    }"
      + "  }"
      + "}";
  public static void main(String[] args) throws Exception {
    // Get original request
    // send booking request / validate result
    // pack order request
    // send order reqeust / validate result
    // cleanup.
    
    String hostname = "127.0.0.1";
    String apiUrl = String.format("http://%s:8080/api", hostname);
    
    Itinerary.Builder itinBuilder = Itinerary.newBuilder();
    TextFormat.merge(ITINERARY_TEXT, itinBuilder);
    BookingRequest request = BookingRequest.newBuilder().setItinerary(itinBuilder).build();

    try (CloseableHttpClient client = HttpClients.createDefault()) {
      BookingResponse.Builder respBuilder = BookingResponse.newBuilder();
      {
        HttpPost post = new HttpPost(String.format("%s/booking", apiUrl));
        post.setEntity(new StringEntity(JsonFormat.printToString(request), ContentType.APPLICATION_JSON));
        LOG.info("http post request: \n'{}'", post);
        HttpResponse response = client.execute(post);
        Preconditions.checkArgument(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        try (InputStream bodyStream = response.getEntity().getContent()) {
          JsonFormat.merge(new InputStreamReader(bodyStream, Charsets.UTF_8), respBuilder);
          LOG.info("booking response: \n'{}'", respBuilder.build());
        }
      }
      
      Itinerary.Builder orderItinBuilder = Itinerary.newBuilder(respBuilder.getItinerary());
      orderItinBuilder.getOrderBuilder().setDescription("testing");
      OrderResponse.Builder orderRespBuilder = OrderResponse.newBuilder();
      {
        HttpPost post = new HttpPost(String.format("%s/order", apiUrl));
        post.setEntity(new StringEntity(JsonFormat.printToString(OrderRequest.newBuilder()
            .setItinerary(orderItinBuilder).build()), ContentType.APPLICATION_JSON));
        HttpResponse response = client.execute(post);
        Preconditions.checkArgument(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        try (InputStream bodyStream = response.getEntity().getContent()) {
          JsonFormat.merge(new InputStreamReader(bodyStream, Charsets.UTF_8), orderRespBuilder);
          LOG.info("booking response: \n'{}'", orderRespBuilder.build());
        }
      }
   }
  }
}
