package me.horlick.db;

import com.google.common.base.Strings;
import com.google.protobuf.Timestamp;
import java.time.Instant;

public class TimestampUtils {

  private static Timestamp fromInstant(Instant instant) {
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }

  public static Timestamp fromString(String timestamp) {
    if (Strings.isNullOrEmpty(timestamp)) {
      return null;
    }
    return fromInstant(Instant.parse(timestamp));
  }

  static Instant toInstant(Timestamp timestamp) {
    return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
  }
}
