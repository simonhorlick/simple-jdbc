package me.horlick.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class ResultSetCursor implements Cursor {

  private final ResultSet rs;
  private final Map<String, Integer> columnIndices;

  ResultSetCursor(ResultSet rs, Map<String, Integer> columnIndices) {
    this.rs = rs;
    this.columnIndices = columnIndices;
  }

  @Override
  public Row next() {
    try {
      if (!rs.next()) {
        return null;
      }

      int numColumns = columnIndices.size();

      Object values[] = new Object[numColumns];
      for (int i = 0; i < numColumns; i++) {
        // getObject uses 1-based indexes.
        values[i] = rs.getObject(i + 1);
      }

      return new Row(columnIndices, values);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() throws Exception {
    rs.close();
  }
}
