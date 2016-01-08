package com.oneroadtrip.matcher.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

public class LogUtil {
  private static final Logger LOG = LogManager.getLogger();
  
  static String getMessageUnicodeString(Message msg) {
    if (msg == null) {
      return "<null>";
    }
    return TextFormat.printToUnicodeString(msg);
  }

  public static <T extends Message> T logAndReturnResponse(String path, Message request,
      T response) {
    LOG.info("request path: '{}' ,\nrequest :\n{}\nresponse:\n{}", path,
        getMessageUnicodeString(request), getMessageUnicodeString(response));
    return response;
  }
}
