package com.oneroadtrip.matcher.data;

import java.io.File;

import org.javatuples.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import com.oneroadtrip.matcher.handlers.DbTest;
import com.oneroadtrip.matcher.proto.LoginRequest;
import com.oneroadtrip.matcher.proto.LoginResponse;
import com.oneroadtrip.matcher.proto.SignupRequest;
import com.oneroadtrip.matcher.proto.SignupResponse;
import com.oneroadtrip.matcher.testutil.TestingDataProcessor;

public class UserAdminTest {
  public static class SignUp extends DbTest {
    @Test
    public void testSignUp() throws Exception {
      TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
          Files.toString(new File(TESTDATA_PATH + "signup.data"), Charsets.UTF_8));

      UserAdmin userAdmin = injector.getInstance(UserAdmin.class);
      for (Pair<String, String> entry : processor.getCases()) {
        SignupRequest.Builder reqBuilder = SignupRequest.newBuilder();
        TextFormat.merge(entry.getValue0(), reqBuilder);
        SignupResponse.Builder respBuilder = SignupResponse.newBuilder();
        TextFormat.merge(entry.getValue1(), respBuilder);
        SignupResponse actual = userAdmin.signUp(reqBuilder.build());
        Assert.assertEquals(respBuilder.build(), actual);
      }
    }
  }
  
  public static class Login extends DbTest {
    @Test
    public void testLogin() throws Exception {
      TestingDataProcessor processor = TestingDataProcessor.loadData(h2Info.connection.get(),
          Files.toString(new File(TESTDATA_PATH + "login.data"), Charsets.UTF_8));

      UserAdmin userAdmin = injector.getInstance(UserAdmin.class);
      for (Pair<String, String> entry : processor.getCases()) {
        LoginRequest.Builder reqBuilder = LoginRequest.newBuilder();
        TextFormat.merge(entry.getValue0(), reqBuilder);
        LoginResponse.Builder respBuilder = LoginResponse.newBuilder();
        TextFormat.merge(entry.getValue1(), respBuilder);
        LoginResponse actual = userAdmin.login(reqBuilder.build());
        Assert.assertEquals(respBuilder.build(), actual);
      }
    }
  }
}
