package com.oneroadtrip.matcher.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class HashUtil {
  public static String getOneWayHash(String text) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
    StringBuilder builder = new StringBuilder();
    for (byte number : hash) {
      builder.append(Integer.toHexString(0xFF & number));
    }
    return builder.toString();
  }
  
  static Random random = new SecureRandom();
  public static String getRandomString() {
    return new BigInteger(200, random).toString(32);
  }
}
