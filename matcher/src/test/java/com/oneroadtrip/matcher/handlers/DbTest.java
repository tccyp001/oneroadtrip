package com.oneroadtrip.matcher.handlers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.googlecode.protobuf.format.JsonFormat;
import com.oneroadtrip.matcher.OneRoadTripConfig;
import com.oneroadtrip.matcher.data.Curl;
import com.oneroadtrip.matcher.data.MockPayer;
import com.oneroadtrip.matcher.data.Payer;
import com.oneroadtrip.matcher.data.PreloadedDataModule;
import com.oneroadtrip.matcher.data.TestingDataAccessor;
import com.oneroadtrip.matcher.handlers.DbTestingModule.H2Info;
import com.oneroadtrip.matcher.module.DbModule;
import com.oneroadtrip.matcher.proto.SignupType;
import com.oneroadtrip.matcher.proto.testing.CurlResponse;
import com.oneroadtrip.matcher.util.HashUtil.Hasher;
import com.oneroadtrip.matcher.util.ScriptRunner;

public abstract class DbTest {
  protected final String TESTDATA_PATH = "src/test/resources/testdata/";

  protected AbstractModule testModule;
  protected Injector injector;
  protected H2Info h2Info;

  @BeforeClass
  protected void setUp() throws IOException, SQLException, ClassNotFoundException {
    testModule = new AbstractModule() {
      // Test config module
      @Override
      protected void configure() {
        OneRoadTripConfig config = new OneRoadTripConfig();
        // Same as default, can adjust is necessary.
        config.preload_period_in_seconds = TimeUnit.MINUTES.toSeconds(5);
        bind(OneRoadTripConfig.class).toInstance(config);

        install(new DbTestingModule());
        install(new DbModule());
        install(new PreloadedDataModule());

        // Testing only
        bind(TestingDataAccessor.class);
      }

      // TODO(xfguo): Move this into getPayer().
      private final Payer payer = new MockPayer(true);

      @Provides
      @Singleton
      Payer getPayer() {
        return payer;
      }

      @Provides
      @Singleton
      Curl getCurl() {
        return new Curl() {
          private int qqCount = 0;
          private int weiboCount = 0;

          private String makeCurlResponse(SignupType type, String... args) {
            CurlResponse.Builder builder = CurlResponse.newBuilder();
            switch (type.getNumber()) {
            case SignupType.QQ_OAUTH_VALUE:
              Preconditions.checkArgument(args.length == 2);
              builder.setNickname(args[0]);
              builder.setFigureurlQq1(args[1]);
              break;
            case SignupType.WEIBO_OAUTH_VALUE:
              Preconditions.checkArgument(args.length == 2);
              builder.setName(args[0]);
              builder.setProfileImageUrl(args[1]);
              break;
            }
            return JsonFormat.printToString(builder.build());
          }

          @Override
          public String curl(String url) throws IOException {
            if (url.contains("CF1C38F60AFF1F9183C7466EF8C7917D")) {
              return makeCurlResponse(SignupType.QQ_OAUTH, "Victor",
                  qqCount++ % 3 == 0 ? "http://qzapp.qlogo.cn/" : "http://qzapp.qlogo.cn/abc");
            } else if (url.contains("2.00hk2b9Cd9bLZD58a010acc0eT9aeD")) {
              return makeCurlResponse(SignupType.WEIBO_OAUTH, "Victor-MSE",
                  weiboCount++ % 3 == 0 ? "http://tp4.sinaimg.cn/2254904827/50/5694495059/0"
                      : "http://tp4.sinaimg.cn/2254904827/50/5694495059/1");
            }
            throw new IOException("No match");
          }
        };
      }

      @Provides
      @Singleton
      Hasher getHasher() {
        return new Hasher() {
          @Override
          public String getRandomString() {
            return "ABCDEFGHIJKLMNOPG";
          }

          @Override
          public String getRandomString(int length) {
            return null;
          }
        };
      }
    };

    injector = Guice.createInjector(testModule);
    h2Info = injector.getInstance(H2Info.class);

    // Create tables in the database.
    Connection conn = h2Info.connection.get();
    ScriptRunner runner = new ScriptRunner(conn, true, false);
    runner.runScript(new BufferedReader(new FileReader("src/main/resources/create_tables.sql")));
  }

  @AfterClass
  protected void tearDown() throws IOException {
    FileUtils.deleteDirectory(h2Info.testingDir);
  }
}
