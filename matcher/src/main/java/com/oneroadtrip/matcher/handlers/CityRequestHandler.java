package com.oneroadtrip.matcher.handlers;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.proto.CityRequest;
import com.oneroadtrip.matcher.proto.CityResponse;
import com.oneroadtrip.matcher.proto.CityResponse.City;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.util.ProtoUtil;

public class CityRequestHandler implements RequestHandler {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  private ImmutableMap<Long, City> cityIdToInfo;

  // TODO(xfguo): (P4) in case we use grpc in the future.
  @Override
  public String process(String post) {
    try {
      // TODO(xfguo): (P1) Remove the parse for CityRequest, it by-default is empty.
      CityRequest request = ProtoUtil.GetRequest(post, CityRequest.newBuilder());
      return JsonFormat.printToString(process(request));
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", e);
      return JsonFormat.printToString(
          CityResponse.newBuilder().setStatus(Status.INCORRECT_REQUEST).build());
    }
  }

  public CityResponse process(CityRequest request) {
    CityResponse.Builder respBuilder = CityResponse.newBuilder().setStatus(Status.SUCCESS);
    try {
      mutateCityContent(respBuilder);
    } catch (SQLException e) {
      LOG.error("Errors in running SQL", e);
      respBuilder.setStatus(Status.ERROR_IN_SQL);
    } catch (NoSuchElementException e) {
      LOG.error("No DB connection");
      respBuilder.setStatus(Status.NO_DB_CONNECTION);
    }
    return respBuilder.build();
  }

  private void mutateCityContent(CityResponse.Builder builder) throws SQLException {
    LOG.info("xfguo: cityIdToInfo: {}", cityIdToInfo);
    for (City city : cityIdToInfo.values()) {
      builder.addCity(city);
    }
  }

}
