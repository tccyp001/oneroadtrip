package com.oneroadtrip.matcher.common;

import javax.annotation.Nullable;

import com.oneroadtrip.matcher.Status;

// TODO(xfguo): Clean up exception
public class OneRoadTripException extends Exception {
  private static final long serialVersionUID = 3578125858814955719L;

  Status status;
  Exception originException;

  public OneRoadTripException(Status status, @Nullable Exception e) {
    this.status = status;
    this.originException = e;
  }

  public Status getStatus() {
    return status;
  }

  public String toString() {
    return String.format("OneRoadTripException(%s): original exception: (%s)", status,
        originException);
  }
}
