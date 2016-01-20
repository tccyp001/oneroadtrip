package com.oneroadtrip.matcher.handlers;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.common.Constants;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.data.DatabaseAccessor;
import com.oneroadtrip.matcher.proto.GuidePlanRequest;
import com.oneroadtrip.matcher.proto.GuidePlanResponse;
import com.oneroadtrip.matcher.proto.ResetPwdRequest;
import com.oneroadtrip.matcher.proto.ResetPwdResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.proto.UserInfo;
import com.oneroadtrip.matcher.util.EmailUtil;
import com.oneroadtrip.matcher.util.HashUtil;
import com.oneroadtrip.matcher.util.ItineraryUtil;
import com.oneroadtrip.matcher.util.LogUtil;
import com.oneroadtrip.matcher.util.ProtoUtil;
import com.oneroadtrip.matcher.util.SqlUtil;

public class ResetPwdRequestHandler  implements RequestHandler {
	private static final Logger LOG = LogManager.getLogger();

	@Inject
	DataSource dataSource;
	 
	@Inject
	private DatabaseAccessor dbAccessor;
	  
	@Override
	public String process(String post) {
		 try {
			ResetPwdRequest request = ProtoUtil.GetRequest(post, ResetPwdRequest.newBuilder());
			 return JsonFormat.printToString(process(request));
		} catch (ParseException e) {
		     LOG.error("failed to parse the json: {}", post, e);
		     return JsonFormat.printToString(GuidePlanResponse.newBuilder()
		          .setStatus(Status.INCORRECT_REQUEST).build());
		} catch (OneRoadTripException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    return JsonFormat.printToString(GuidePlanResponse.newBuilder()
			          .setStatus(Status.INCORRECT_REQUEST).build());
		}
	}
	ResetPwdResponse process(ResetPwdRequest request) throws OneRoadTripException {
		if(request.getStep() == 1) {
			processStepOne(request.getUsermail());
		}
		else {
			processStepTwo(request.getToken(), request.getPassword());
		}
		
		
		ResetPwdResponse.Builder builder = ResetPwdResponse.newBuilder().setStatus(Status.SUCCESS);
		return LogUtil.logAndReturnResponse("/api/resetpwd", request, builder.build());
	}
	
	private void processStepOne(String useremail) throws OneRoadTripException {
		UserInfo userInfo = dbAccessor.lookupUserByEmail(useremail);
		HashUtil.HasherImpl hasher = new HashUtil.HasherImpl();
		String token = dbAccessor.addOneValidToken(hasher, userInfo, Constants.TOKEN_TYPE_RESET);
		EmailUtil.sendPwdResetEmail(token);
		
		
	}
	private void processStepTwo(String token, String password) throws OneRoadTripException {
		long userId = 0;
		try(Connection conn = dataSource.getConnection()) {
			userId = SqlUtil.executeTransaction(dataSource,
			        (Connection c) -> DatabaseAccessor.getUserIdResetPwd(c, token));
			UserInfo userInfo = dbAccessor.lookupUser(conn, userId);
			userInfo = UserInfo.newBuilder(userInfo).setPassword(HashUtil.getOneWayHash(password)).build();
			dbAccessor.updateUser(userInfo);
		} 
		catch (SQLException e) {
		      throw new OneRoadTripException(Status.NO_DB_CONNECTION, e);
		 } catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	  
}
