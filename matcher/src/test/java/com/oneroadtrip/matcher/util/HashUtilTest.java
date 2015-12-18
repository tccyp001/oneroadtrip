package com.oneroadtrip.matcher.util;

import java.security.NoSuchAlgorithmException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HashUtilTest {
  @Test
  public void testGetRandomString() {
    for (int i = 0; i < 10; ++i) {
      Assert.assertTrue(HashUtil.getRandomString().length() <= 40);
    }
  }

  @Test
  public void testOneWayHash() throws NoSuchAlgorithmException {
    Assert.assertEquals("2cf24dba5fb0a3e26e83b2ac5b9e29e1b161e5c1fa7425e7343362938b9824",
        HashUtil.getOneWayHash("hello"));
    Assert.assertEquals("ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c3265534f75ae",
        HashUtil.getOneWayHash("test123"));
    Assert.assertEquals("d1c6f4f7488817e832444213e817b7961fdbaa39f721529a8920e67660fbf7",
        HashUtil.getOneWayHash("89qhjkfasui234rkj"));
  }
}
