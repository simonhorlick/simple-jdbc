package me.horlick.db;

import java.util.Map;

// An SQL statement containing zero or more placeholders and a map of placeholder names to values that is used for the substitution. The format of a placeholder is a comma followed by alphanumeric characters or underscores, for example:
//   SELECT * FROM shapes WHERE sides = :sides
//   INSERT INTO shapes (sides,regular,convex) VALUES (:sides,:regular,:convex)
// The StatementParser does the actual binding of variables to placeholders in the SQL statement.
//
// SQL statements should always be compile-time constant to ensure no possibility of SQL injection.
class Statement {

  private final String sql;
  private final Map<String, Object> variables;

  /**
   * An SQL statement with placeholders.
   *
   * @param sql A compile-time constant that holds an SQL statement and zero or more placeholders.
   * @param variables The values of the placeholders to substitute into the SQL statement.
   */
  Statement(String sql, Map<String, Object> variables) {
    this.sql = sql;
    this.variables = variables;
  }

  String getSql() {
    return sql;
  }

  Map<String, Object> getVariables() {
    return variables;
  }
}
