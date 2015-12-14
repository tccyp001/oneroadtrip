package com.oneroadtrip.matcher.resources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import jersey.repackaged.com.google.common.collect.Maps;
import jersey.repackaged.com.google.common.collect.Sets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.ChosenGuide;
import com.oneroadtrip.matcher.Status;
import com.oneroadtrip.matcher.TravelRequest;
import com.oneroadtrip.matcher.TravelResponse;
import com.oneroadtrip.matcher.internal.GuideCandidate;

@Path("travelrequest")
public class TravelRequestResource {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  @Nullable
  private Optional<Connection> connection;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String post(String post) {
    LOG.info("xfguo: start parsing travel request: {}", post);
    TravelResponse.Builder respBuilder = TravelResponse.newBuilder().setStatus(Status.SUCCESS);
    if (connection == null) {
      LOG.error("No DB connection");
      return JsonFormat.printToString(respBuilder.setStatus(Status.NO_DB_CONNECTION).build());
    }
    
    // Got the request.
    TravelRequest.Builder reqBuilder = TravelRequest.newBuilder();
    try {
      JsonFormat.merge(post, reqBuilder);
    } catch (ParseException e) {
      LOG.error("failed to parse the json: {}, exception {}", post, e);
      return JsonFormat.printToString(respBuilder.setStatus(Status.INCORRECT_REQUEST).build()); 
    }

    // 这里我们不做user validatation，我们把validation留到出order的时候做。
    
    // 1. Choose guide candidates:
    //   - filters: destination / num_persons = adults + kids + seniors / level.
    //   - range: (startdate, enddate)
    //   - Unknown how to use: need_air_ticket / need_hotel / need_transportation / need_vip / interests
    
    // 2. Make sure all candidates are avail during (startdate..enddate).

    // 3. Provide a random order of accepted guides.

    TravelRequest request = reqBuilder.build();
    LOG.info("request: {}", request);
    String queryGuides =
        "SELECT"
        + "  Guides.guide_id, "
        + "  Users.user_name, "
        + "  Guides.description, "
        + "  Guides.level, "
        + "  Guides.max_persons, "
        + "  Guides.citizenship, "
        + "  Guides.has_car, "
        + "  Guides.score, "
        + "  GuideLocations.price_usd, "
        + "  GuideLocations.price_cny "
        + "FROM "
        + "  Guides "
        + "  INNER JOIN Users USING(user_id) "
        + "  INNER JOIN GuideLocations USING(guide_id) "
        + "  INNER JOIN Locations USING(location_id) "
        + "WHERE "
        + "  Guides.max_persons > ? "
        + "  AND Locations.city = ? "
        + "  AND GuideLocations.is_effective = true";
    String queryReservations =
        "SELECT "
        + "  distinct Guides.guide_id "
        + "FROM "
        + "  Guides "
        + "  INNER JOIN GuideLocations USING(guide_id) "
        + "  INNER JOIN Locations USING(location_id) "
        + "  INNER JOIN GuideReservations USING(guide_id) "
        + "WHERE "
        + "  Guides.max_persons > ? "
        + "  AND Locations.city = ? "
        + "  AND GuideLocations.is_effective = true "
        + "  AND GuideReservations.reserved_date >= ? AND GuideReservations.reserved_date < ?";
    Map<Long, GuideCandidate> guides = Maps.newHashMap();
    Set<Long> reservedGuideIds = Sets.newHashSet();

    try (PreparedStatement pStmtQueryGuides = connection.get().prepareStatement(queryGuides);
        PreparedStatement pStmtQueryReservations = connection.get().prepareStatement(queryReservations)) {
      int numPeople = request.getAdults() + request.getKids() + request.getSeniors();
      pStmtQueryGuides.setInt(1, numPeople);
      pStmtQueryGuides.setString(2, request.getDestination());
      pStmtQueryReservations.setInt(1, numPeople);
      pStmtQueryReservations.setString(2, request.getDestination());
//      pStmtQueryReservations.setString(2, "纽约");
      pStmtQueryReservations.setInt(3, request.getStartdate());
      pStmtQueryReservations.setInt(4, request.getEnddate());
      
      LOG.info("queryGuide = {}", pStmtQueryGuides);
      try (ResultSet rsGuides = pStmtQueryGuides.executeQuery();
          ResultSet rsReservations = pStmtQueryReservations.executeQuery()) {
        while (rsGuides.next()) {
          LOG.info("xfguo: here");
          // TODO(xiaofengguo): Make the result retrieving more generic.
          GuideCandidate.Builder builder = GuideCandidate.newBuilder();
          builder.setGuideId(rsGuides.getLong("guide_id"));
          builder.setUserName(rsGuides.getString("user_name"));
          builder.setDescription(rsGuides.getString("description"));
          builder.setLevel(rsGuides.getInt("level"));
          builder.setMaxPersons(rsGuides.getInt("max_persons"));
          builder.setCitizenship(rsGuides.getString("citizenship"));
          builder.setHasCar(rsGuides.getBoolean("has_car"));
          builder.setScore(rsGuides.getFloat("score"));
          builder.setPriceUsd(rsGuides.getFloat("price_usd"));
          builder.setPriceCny(rsGuides.getFloat("price_cny"));
          GuideCandidate candidate = builder.build();
          if (guides.containsKey(candidate.getGuideId())) {
            LOG.info("Duplicate guide {} and existed {}", candidate, guides.get(candidate.getGuideId()));
            continue;
          }
          guides.put(candidate.getGuideId(), candidate);
        }
        
        while (rsReservations.next()) {
          LOG.info("xfguo: here 2");
          Long guide_id = rsReservations.getLong("guide_id");
          reservedGuideIds.add(guide_id);
        }
      }

      // Filter unaccepted candidates
      for (Map.Entry<Long, GuideCandidate> e : guides.entrySet()) {
        if (reservedGuideIds.contains(e.getKey())) {
          continue;
        }
        GuideCandidate candiadate = e.getValue();
        ChosenGuide.Builder builder = ChosenGuide.newBuilder();
        builder.setName(candiadate.getUserName());
        builder.setScore(candiadate.getScore());
        builder.setDescription(candiadate.getDescription());
        builder.setExperience(-1);  // TODO(lamuguo): add this.
        builder.setHasCar(candiadate.getHasCar());
        builder.setMaxPeople(candiadate.getMaxPersons());
        builder.addLanguage("unknown");  // TODO(lamuguo): add this.
        builder.setCitizenship(candiadate.getCitizenship());
        builder.setPriceUsd(candiadate.getPriceUsd());
        builder.setPriceCny(candiadate.getPriceCny());
        respBuilder.addGuide(builder);
      }
    } catch (SQLException e) {
      LOG.error("Errors in running SQL", e);
      respBuilder.setStatus(Status.ERROR_IN_SQL);
    } catch (NoSuchElementException e) {
      LOG.error("No DB connection");
      respBuilder.setStatus(Status.NO_DB_CONNECTION);
    }
    LOG.info("guides = {}, reservations = {}", guides, reservedGuideIds);
    
    return JsonFormat.printToString(respBuilder.build()); 
  }
}
