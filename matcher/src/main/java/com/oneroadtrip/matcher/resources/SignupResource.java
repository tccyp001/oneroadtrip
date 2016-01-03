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
import com.oneroadtrip.matcher.proto.SignupRequest;
import com.oneroadtrip.matcher.proto.SignupResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.util.HashUtil;
import com.oneroadtrip.matcher.util.SqlUtil;

@Path("signup")
public class SignupResource {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  @Nullable
  private Optional<Connection> connection;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String post(String post) throws SQLException {
    LOG.info("xfguo: start parsing login request: {}", post);

    // Got the request.
    SignupRequest.Builder builder = SignupRequest.newBuilder();
    SignupResponse.Builder respBuilder = SignupResponse.newBuilder().setStatus(Status.SUCCESS);
    List<ResultSet> resultSets = Lists.newArrayList();
    List<PreparedStatement> pStmts = Lists.newArrayList();
    try {
      JsonFormat.merge(post, builder);
      SignupRequest request = builder.build();

      LOG.info("xfguo: signup 1");
      if (!request.hasUsername() || request.getUsername().isEmpty()) {
        LOG.info("Incorrect request, no user_name, request: {}", request);
        return JsonFormat.printToString(respBuilder.setStatus(Status.INCORRECT_REQUEST).build());
      }

      LOG.info("xfguo: signup 2");
      {
        // Deduplicate
        String dedupSignup = "SELECT COUNT(user_id) AS rowcount FROM Users WHERE user_name = ?";
        PreparedStatement pStmt = connection.get().prepareStatement(dedupSignup);
        pStmts.add(pStmt);
        pStmt.setString(1, request.getUsername());
        ResultSet rs = pStmt.executeQuery();
        resultSets.add(rs);

        rs.next();
        if (rs.getInt("rowcount") > 0) {
          LOG.error("Duplicate user name: request: {}", request);
          return JsonFormat.printToString(respBuilder.setStatus(Status.INCORRECT_USER_NAME).build());
        }
      }

      LOG.info("xfguo: signup 3");
      String token = HashUtil.getRandomString();
      {
        // Add user
        String insertUser = "INSERT INTO Users(user_name, email, password) VALUES (?, ?, ?)";
        String insertToken = "INSERT INTO Tokens (token, user_id, expired_ts, is_expired) "
            + "VALUES (?, LAST_INSERT_ID(), CURRENT_TIMESTAMP(), false)";

        // Start the transaction.
        connection.get().setAutoCommit(false);
        PreparedStatement pStmt1 = SqlUtil.addPreparedStatement(connection.get(), insertUser, pStmts);
        pStmt1.setString(1, request.getUsername());
        pStmt1.setString(2, request.getEmail());
        pStmt1.setString(3, HashUtil.getOneWayHash(request.getPassword()));
        pStmt1.executeUpdate();
        PreparedStatement pStmt2 = SqlUtil.addPreparedStatement(connection.get(), insertToken, pStmts);
        pStmt2.setString(1, token);
        pStmt2.executeUpdate();
        connection.get().commit();
      }
      LOG.info("xfguo: signup 4");
      respBuilder.setToken(token);
      LOG.info("xfguo: signup 5");
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}", e);
      respBuilder.setStatus(Status.INCORRECT_REQUEST);
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
