package com.oneroadtrip.matcher.resources;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.LoginRequest;
import com.oneroadtrip.matcher.LoginResponse;
import com.oneroadtrip.matcher.Status;
import com.oneroadtrip.matcher.util.HashUtil;
import com.oneroadtrip.matcher.util.SqlUtil;

@Path("login")
public class LoginResource {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  @Nullable
  private Optional<Connection> connection;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String post(String post) throws SQLException {
    LoginResponse.Builder respBuilder = LoginResponse.newBuilder().setStatus(Status.SUCCESS);
    LOG.info("xfguo: start parsing login request: {}", post);

    // Got the request.
    LoginRequest.Builder builder = LoginRequest.newBuilder();
    List<ResultSet> resultSets = Lists.newArrayList();
    List<PreparedStatement> pStmts = Lists.newArrayList();
    try {
      JsonFormat.merge(post, builder);
      LoginRequest request = builder.build();
      
      String getToken = "SELECT token FROM Tokens "
          + "INNER JOIN Users USING(user_id) "
          + "WHERE user_name = ? AND password = ? AND is_expired = false";
      PreparedStatement pStmt = SqlUtil.addPreparedStatement(connection.get(), getToken, pStmts);
      pStmt.setString(1, request.getUsername());
      pStmt.setString(2, HashUtil.getOneWayHash(request.getPassword()));
      ResultSet rs = SqlUtil.addResultSet(pStmt, resultSets);
      
      if (!rs.next()) {
        // Should have a way to refresh it, not refresh in login.
        return JsonFormat.printToString(respBuilder.setStatus(Status.NO_TOKEN).build());
      }
      String token = rs.getString("token");
      if (rs.next()) {
        return JsonFormat.printToString(respBuilder.setStatus(Status.MULTIPLE_VALID_TOKEN).build());
      }
      respBuilder.setToken(token);
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", e);
      return JsonFormat.printToString(respBuilder.setStatus(Status.INCORRECT_REQUEST).build());
    } catch (NoSuchAlgorithmException e) {
      LOG.error("No SHA-256 algorithm", e);
      respBuilder.setStatus(Status.SERVER_ERROR);
    } catch (SQLException e) {
      LOG.error("Errors in running SQL", e);
      respBuilder.setStatus(Status.ERROR_IN_SQL);
    } catch (NoSuchElementException e) {
      LOG.error("No DB connection");
      respBuilder.setStatus(Status.NO_DB_CONNECTION);
    } finally {
      for (ResultSet rs : resultSets) {
        rs.close();
      }
      for (PreparedStatement pStmt : pStmts) {
        pStmt.close();
      }
      if (connection != null)
        connection.get().close();
    }

    return JsonFormat.printToString(respBuilder.build());
  }

}
