package com.oneroadtrip.matcher.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.CityRequest;
import com.oneroadtrip.matcher.CityResponse;
import com.oneroadtrip.matcher.Status;
import com.oneroadtrip.matcher.CityResponse.City;
import com.oneroadtrip.matcher.util.ProtoUtil;

public class CityRequestHandler implements RequestHandler {
  private static final Logger LOG = LogManager.getLogger();

  private static final String QUERY_CITIES = "SELECT city_id, city_name, suggest, min, max FROM Cities";

  @Inject
  private Optional<Connection> connection;

  // TODO(xfguo): (P4) in case we use grpc in the future.
  @Override
  public String process(String post) {
    CityResponse.Builder respBuilder = CityResponse.newBuilder();
    try {
      // TODO(xfguo): (P1) Remove the parse for CityRequest, it by-default is empty.
      CityRequest request = ProtoUtil.GetRequest(post, CityRequest.newBuilder());
      respBuilder = process(request);
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", e);
      respBuilder.setStatus(Status.INCORRECT_REQUEST);
    }
    return JsonFormat.printToString(respBuilder.build());
  }

  public CityResponse.Builder process(CityRequest request) {
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
    return respBuilder;
  }

  private void mutateCityContent(CityResponse.Builder builder) throws SQLException {
    try (Connection conn = connection.get();
        PreparedStatement pStmt = connection.get().prepareStatement(QUERY_CITIES)) {
      try (ResultSet rs = pStmt.executeQuery()) {
        while (rs.next()) {
          City.Builder cityBuilder = City.newBuilder();
          cityBuilder.setCityId(rs.getLong(1));
          cityBuilder.setName(rs.getString(2));
          cityBuilder.setSuggest(rs.getInt(3));
          cityBuilder.setMin(rs.getInt(4));
          cityBuilder.setMax(rs.getInt(5));
          builder.addCity(cityBuilder);
        }
      }
    }
  }

}
