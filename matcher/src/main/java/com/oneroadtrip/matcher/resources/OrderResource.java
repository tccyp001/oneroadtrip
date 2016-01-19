package com.oneroadtrip.matcher.resources;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.data.OrderProcessor;
import com.oneroadtrip.matcher.proto.OrderRequest;
import com.oneroadtrip.matcher.proto.OrderResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.util.ProtoUtil;

@Path("order")
public class OrderResource {
  private static final Logger LOG = LogManager.getLogger();
  
  @Inject
  OrderProcessor stripeProcessor;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String post(String post) {
    try {
      LOG.info("/api/order: '{}'", post);
      OrderRequest request = ProtoUtil.GetRequest(post, OrderRequest.newBuilder());
      return JsonFormat.printToString(stripeProcessor.process(request));
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", e);
      return JsonFormat.printToString(OrderResponse.newBuilder()
          .setStatus(Status.INCORRECT_REQUEST).build());
    }
  }
}
