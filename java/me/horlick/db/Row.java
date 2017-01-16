package me.horlick.db;

import java.util.Map;
import java.util.stream.Collectors;

// A single row of data from a table, treated as a map from column names to values.
public class Row {

  private final Map<String, Integer> columnIndices;
  private final Object values[];

  public Row(Map<String, Integer> columnIndices, Object[] values) {
    this.columnIndices = columnIndices;
    this.values = values;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String columnName) {
    Integer index = columnIndices.get(columnName);
    if (index == null) {
      throw new RuntimeException(
          "Column \""
              + columnName
              + "\" not found. Available columns are: "
              + columnIndices
                  .keySet()
                  .stream()
                  .map(name -> "\"" + name + "\"")
                  .collect(Collectors.joining(", ")));
    }
    return (T) values[index];
  }
}
