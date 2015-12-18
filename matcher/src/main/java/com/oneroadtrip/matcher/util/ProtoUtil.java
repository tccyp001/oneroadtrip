package com.oneroadtrip.matcher.util;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;

public class ProtoUtil {
  @SuppressWarnings("unchecked")
  public static <T extends Message> T GetRequest(String post, T.Builder builder)
      throws ParseException {
    JsonFormat.merge(post, builder);
    return (T) builder.build();
  }
}
