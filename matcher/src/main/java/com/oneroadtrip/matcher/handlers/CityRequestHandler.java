package com.oneroadtrip.matcher.handlers;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.googlecode.protobuf.format.JsonFormat;
import com.oneroadtrip.matcher.proto.CityInfo;
import com.oneroadtrip.matcher.proto.CityResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.util.EmailUtil;
import com.oneroadtrip.matcher.util.LogUtil;

public class CityRequestHandler implements RequestHandler {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  private ImmutableMap<Long, CityInfo> cityIdToInfo;

  // TODO(xfguo): (P4) in case we use grpc in the future.
  public String handleGet() {
	return JsonFormat.printToString(process());
  }

  public CityResponse process() {
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
    return LogUtil.logAndReturnResponse("/api/city", null, respBuilder.build());
  }

  private void mutateCityContent(CityResponse.Builder builder) throws SQLException {
    for (CityInfo city : cityIdToInfo.values()) {
      builder.addCity(city);
    }
  }

  // Never call so far.
  @Override
  public String process(String post) {
    // TODO Auto-generated method stub
    return null;
  }

}
