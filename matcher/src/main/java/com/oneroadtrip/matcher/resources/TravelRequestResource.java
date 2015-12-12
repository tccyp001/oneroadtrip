package com.oneroadtrip.matcher.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.Status;
import com.oneroadtrip.matcher.TravelRequest;
import com.oneroadtrip.matcher.TravelResponse;

@Path("travelrequest")
public class TravelRequestResource {
  private static final Logger LOG = LogManager.getLogger();

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String post(String post) {
    LOG.info("xfguo: start parsing travelrequest: {}", post);
    
    // Got the request.
    TravelRequest.Builder builder = TravelRequest.newBuilder();
    TravelResponse.Builder respBuilder = TravelResponse.newBuilder().setStatus(Status.SUCCESS);
    try {
      JsonFormat.merge(post, builder);
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", e);
      return JsonFormat.printToString(respBuilder.setStatus(Status.INCORRECT_REQUEST).build()); 
    }

    // 这里我们不做user validatation，我们把validation留到出order的时候做。
    
    // 1. Choose guide candidates:
    //   - filters: destination / num_persons = adults + kids + seniors / level.
    //   - range: (startdate, enddate)
    //   - Unknown how to use: need_air_ticket / need_hotel / need_transportation / need_vip / interests
    
    // 2. Make sure all candidates are avail during (startdate..enddate).

    // 3. Provide a random order of accepted guides.
    
    return JsonFormat.printToString(respBuilder.build()); 
  }
}
