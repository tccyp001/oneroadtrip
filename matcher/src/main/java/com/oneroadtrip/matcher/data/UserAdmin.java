package com.oneroadtrip.matcher.data;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonParser;
import com.oneroadtrip.matcher.common.OneRoadTripException;
import com.oneroadtrip.matcher.proto.LoginRequest;
import com.oneroadtrip.matcher.proto.LoginResponse;
import com.oneroadtrip.matcher.proto.SignupRequest;
import com.oneroadtrip.matcher.proto.SignupResponse;
import com.oneroadtrip.matcher.proto.SignupType;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.proto.UserInfo;
import com.oneroadtrip.matcher.util.HashUtil;
import com.oneroadtrip.matcher.util.HashUtil.Hasher;

public class UserAdmin {
  private static final Logger LOG = LogManager.getLogger();

  @Inject
  private DatabaseAccessor dbAccessor;
  
  @Inject
  private Curl curl;
  
  @Inject
  private Hasher hasher;

  // login仅提供给我们自己host的用户的。第三方登陆的用户，全部走sign-up流程。
  public LoginResponse login(LoginRequest request) {
    try {
      return internalLogin(request);
    } catch (OneRoadTripException e) {
      return LoginResponse.newBuilder().setStatus(e.getStatus()).build();
    }
  }
  
  private LoginResponse internalLogin(LoginRequest request) throws OneRoadTripException {
    try {
      // 1. Lookup user
      // 2. Validate the user password.
      // 3. Login and return token.
      UserInfo user = dbAccessor.lookupUser(request.getUsername());
      if (user == null) {
        // unknown user
        throw new OneRoadTripException(Status.UNKNOWN_USER, null);
      } else if (!user.getPassword().equals(HashUtil.getOneWayHash(request.getPassword()))) {
        throw new OneRoadTripException(Status.INCORRECT_PASSWORD, null);
      }
      return LoginResponse.newBuilder().setStatus(Status.SUCCESS).setToken(login(user)).build();
    } catch (NoSuchAlgorithmException e) {
      throw new OneRoadTripException(Status.SERVER_ERROR, e);
    }
  }

  public SignupResponse signUp(SignupRequest request) {
    try {
      switch (request.getType().getNumber()) {
      case SignupType.TRADITIONAL_VALUE:
        return traditionalSignup(request);
      case SignupType.QQ_OAUTH_VALUE:
      case SignupType.WEIBO_OAUTH_VALUE:
        return oauthSignup(request);
      }
    } catch (OneRoadTripException e) {
      // Should only catch OneRoadTripException
      LOG.info("Process error", e);
      return SignupResponse.newBuilder().setStatus(e.getStatus()).build();
    }
    // Should not reach here.
    return SignupResponse.newBuilder().setStatus(Status.SHOULD_NOT_REACH).build();
  }

  String getFormatByType(SignupType type) {
    if (type.getNumber() == SignupType.QQ_OAUTH_VALUE) {
      return "https://graph.qq.com/user/get_user_info?"
          + "access_token=%s&oauth_consumer_key=%s&openid=%s";
    } else if (type.getNumber() == SignupType.WEIBO_OAUTH_VALUE) {
      return "";
    }
    return "";
  }

  public SignupResponse oauthSignup(SignupRequest request) throws OneRoadTripException {
    // 1. Get user profile by oauth request to make sure it is accessible.
    // 2. Search OAuthUsers table by (client_id, openid), and get corresponding
    // user_id.
    // - If not found, created one for OAuthUsers, then create a user in Users.
    // - If found, go to next step.
    // 3. Go to login and return the token.
    try {
      String oauthResp = curl.curl(String.format(getFormatByType(request.getType()),
          request.getAccessToken(), request.getClientId(), request.getOpenId()));
      String nickname = getNicknameByOauthResponse(request.getType(), oauthResp);

      UserInfo user = dbAccessor.lookupOAuthUser(request.getClientId(), request.getOpenId());

      if (user == null) {
        // TODO(xfguo): 其实我们不需要保留accessToken，这里的保留只是第一次顺便做的。如果需要保留，
        // DB里面需要有个ts域来保留存入accessToken的时间。
        user = dbAccessor.addOAuthUser(nickname, request.getType(), request.getAccessToken(),
            request.getClientId(), request.getOpenId());
      }

      String token = login(user);
      return SignupResponse.newBuilder().setStatus(Status.SUCCESS).setToken(token).build();
    } catch (IOException e) {
      throw new OneRoadTripException(Status.ERROR_IN_OAUTH_CONFIRMATION, e);
    }
  }

  private String getNicknameByOauthResponse(SignupType type, String oauthResp)
      throws OneRoadTripException {
    if (type.equals(SignupType.QQ_OAUTH)) {
      return new JsonParser().parse(oauthResp).getAsJsonObject().get("nickname").getAsString();
    }
    // TODO(xfguo): Add other cases, for example weibo.
    throw new OneRoadTripException(Status.SHOULD_NOT_REACH, null);
  }

  private SignupResponse traditionalSignup(SignupRequest request) throws OneRoadTripException {
    try {
      // 1. Make sure the username isn't duplicated with other users.
      // - If duplicate, return error.
      // 2. Insert the user into Users
      // 3. Go to login and return the token.
      UserInfo user = dbAccessor.lookupUser(request.getUsername());
      if (user != null) {
        throw new OneRoadTripException(Status.INCORRECT_USER_NAME, null);
      }
      user = dbAccessor.addUser(request.getUsername(), request.getNickname(),
          HashUtil.getOneWayHash(request.getPassword()));
      return SignupResponse.newBuilder().setStatus(Status.SUCCESS).setToken(login(user)).build();
    } catch (NoSuchAlgorithmException e) {
      throw new OneRoadTripException(Status.SERVER_ERROR, e);
    }
  }

  /**
   * Login and return a new token for the user.
   * 
   * @return
   * @throws OneRoadTripException 
   */
  private String login(UserInfo user) throws OneRoadTripException {
    // 1. Set all current avail tokens expired
    // 2. Create one token and add it as a valid one.
    // 3. Returns it.
    dbAccessor.expireTokensOfUser(user);
    return dbAccessor.addOneValidToken(hasher, user);
  }

}