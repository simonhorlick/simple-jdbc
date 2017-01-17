package me.horlick.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.horlick.db.StatementParser.ParsedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// A DatabaseClient handles communication with the database. It sends queries for execution and
// allows clients to begin transactions.
public class DatabaseClient implements AutoCloseable {

  private final Connection connection;
  private final StatementParser parser = new StatementParser();

  private static final Logger logger = LoggerFactory.getLogger(DatabaseClient.class);

  DatabaseClient(Connection connection) {
    this.connection = connection;
  }

  /**
   * Execute the SQL statement on the database.
   *
   * @param statement The SQL statement and any variables to be bound.
   * @return A Cursor to the result set if any data is returned, or an empty Cursor if no results.
   */
  public Cursor executeStatement(Statement statement) {
    try {
      ParsedStatement parsedStatement = parser.parse(statement);
      logger.info("Running \"" + parsedStatement + "\"");

      // Build the prepared statement and set all of the variables.
      PreparedStatement preparedStatement = connection.prepareStatement(parsedStatement.getSql());
      List<Object> values = parsedStatement.getValues();
      for (int i = 0; i < values.size(); i++) {
        Object value = values.get(i);
        preparedStatement.setObject(i + 1, value);
      }

      boolean hasResultSet = preparedStatement.execute();

      // If the execution produced a ResultSet then wrap it in a Cursor and return it.
      if (!hasResultSet) {
        return new EmptyCursor();
      } else {
        ResultSet rs = preparedStatement.getResultSet();
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

  public DatabaseTransaction begin() {
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
