package me.horlick.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

// A DatabaseClient handles communication with the database. It sends queries for execution and
// allows clients to begin transactions.
public class DatabaseClient implements AutoCloseable {

  private final Connection connection;
  private final StatementParser parser = new StatementParser();

  DatabaseClient(Connection connection) {
    this.connection = connection;
  }

  /**
   * Execute the SQL statement on the database.
   *
   * @param statement The SQL statement and any variables to be bound.
   * @return A Cursor to the result set if any data is returned, or an empty Cursor if no results.
   */
  Cursor executeStatement(Statement statement) {
    try {
      String sql = parser.parse(statement);
      System.out.println("Running \"" + sql + "\"");

      java.sql.Statement stmt = connection.createStatement();
      boolean hasResultSet = stmt.execute(sql);

      // If the execution produced a ResultSet then wrap it in a Cursor and return it.
      if (!hasResultSet) {
        return new EmptyCursor();
      } else {
        ResultSet rs = stmt.getResultSet();
        ResultSetMetaData md = rs.getMetaData();

        // Extract the names of all columns.
        int numColumns = md.getColumnCount();
        Map<String, Integer> columns = new HashMap<>();

        for (int i = 0; i < numColumns; i++) {
          // Note here that getColumnName uses 1-based indexing!
          columns.put(md.getColumnName(i + 1), i);
        }

        return new ResultSetCursor(rs, columns);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  DatabaseTransaction begin() {
    return new DatabaseTransaction(this);
  }

  @Override
  public void close() throws Exception {
    connection.close();
  }

  void commit() {
    try {
      connection.commit();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
