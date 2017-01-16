package me.horlick.db;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

// An SqlTypeAdapter converts java types into their equivalent SQL types for use as variables in a query.
public class SqlTypeAdapter implements Adapter<Object, String> {

  private final DateTimeFormatter timestampFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(ZoneId.of("UTC"));

  @Override
  public String adapt(Object o) {
    if (o == null) {
      return "NULL";
    } else if (o instanceof Instant) {
      return timestampFormatter.format((Instant) o);
    } else if (o instanceof Timestamp) {
      return timestampFormatter.format(TimestampUtils.toInstant((Timestamp) o));
    } else if (o instanceof Double) {
      if (Double.isNaN((Double) o)) {
        return "NULL";
      }
      return Double.toString((Double) o);
    } else if (o instanceof String) {
      return String.format("'%s'", (String) o);
    } else if (o instanceof Long) {
      return Long.toString((Long) o);
    } else if (o instanceof Integer) {
      return Integer.toString((Integer) o);
    } else if (o instanceof Boolean) {
      return (Boolean) o ? "TRUE" : "FALSE";
    } else {
      throw new RuntimeException("Failed to serialise \"" + o + "\" of type " + o.getClass());
    }
  }
}
