package me.horlick.db;

import static java.util.stream.Collectors.joining;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// A StatementParser takes a Statement and replaces all placeholders with the correct values.
public class StatementParser {

  private enum ParseState {
    TEXT,
    PLACEHOLDER
  }

  interface Token {
    String get(Map<String, Object> variables, List<Object> values);
  }

  // A string of SQL.
  static class SqlText implements Token {
    private final String token;

    SqlText(String token) {
      this.token = token;
    }

    @Override
    public String toString() {
      return "SqlText{" + "token='" + token + '\'' + '}';
    }

    @Override
    public String get(Map<String, Object> variables, List<Object> values) {
      return token;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      SqlText sqlText = (SqlText) o;

      return token.equals(sqlText.token);
    }

    @Override
    public int hashCode() {
      return token.hashCode();
    }
  }

  // A variable that should be substituted.
  static class SqlVariable implements Token {
    private final String variableName;
    private final int offset;

    SqlVariable(String variableName, int offset) {
      this.variableName = variableName;
      this.offset = offset;
    }

    @Override
    public String toString() {
      return "SqlVariable{" + "variableName='" + variableName + '\'' + ", offset=" + offset + '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      SqlVariable that = (SqlVariable) o;

      return offset == that.offset && variableName.equals(that.variableName);
    }

    @Override
    public int hashCode() {
      int result = variableName.hashCode();
      result = 31 * result + offset;
      return result;
    }

    @Override
    public String get(Map<String, Object> variables, List<Object> values) {
      Object value = variables.get(variableName);
      if (value == null) {
        throw new RuntimeException(
            "Variable '" + variableName + "' not resolved at character " + offset);
      }

      values.add(value);

      // Substitute the named placeholder with the JDBC-style question mark placeholder.
      return "?";
    }

    String getVariableName() {
      return variableName;
    }
  }

  static class ParsedStatement {
    private final String sql;
    private final List<Object> values;

    @Override
    public String toString() {
      return "ParsedStatement{"
          + "sql='"
          + sql
          + '\''
          + ", values=["
          + values.stream().map(Object::toString).collect(joining(","))
          + "]"
          + '}';
    }

    ParsedStatement(String sql, List<Object> values) {
      this.sql = sql;
      this.values = values;
    }

    public String getSql() {
      return sql;
    }

    public List<Object> getValues() {
      return values;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ParsedStatement that = (ParsedStatement) o;

      return sql.equals(that.sql) && values.equals(that.values);
    }

    @Override
    public int hashCode() {
      int result = sql.hashCode();
      result = 31 * result + values.hashCode();
      return result;
    }
  }

  /**
   * Bind the values provided in the Statement to the placeholders in the SQL string.
   *
   * @param statement An SQL statement template and variables to bind.
   * @return The raw SQL string to be executed on a database.
   */
  ParsedStatement parse(final Statement statement) {
    List<Token> tokens = tokenise(statement.getSql());

    String result = "";
    List<Object> values = new ArrayList<>();
    for (Token token : tokens) {
      result += token.get(statement.getVariables(), values);
    }

    // Throw an exception if there are variables that have been provided, but not used.
    checkAllVariablesAreUsed(statement, tokens);

    return new ParsedStatement(result, values);
  }

  // Ensure all variables supplied in the Statement are used in the query.
  private void checkAllVariablesAreUsed(Statement statement, List<Token> tokens) {
    Set<String> usedVariables = new HashSet<>();
    for (Token token : tokens) {
      if (token instanceof SqlVariable) {
        usedVariables.add(((SqlVariable) token).getVariableName());
      }
    }

    Set<String> allVariables = statement.getVariables().keySet();

    Set<String> unusedVariables = Sets.difference(allVariables, usedVariables);
    if (!unusedVariables.isEmpty()) {
      throw new RuntimeException("Unused variables: " + join(unusedVariables));
    }
  }

  // Take a set of strings, comma separate them and join them.
  private String join(Set<String> strings) {
    String joined = "";
    boolean first = true;
    for (String string : strings) {
      if (first) {
        first = false;
      } else {
        joined += ", ";
      }
      joined += string;
    }
    return joined;
  }

  // Take an input SQL string and determine which parts are raw SQL and which parts are variable placeholders.
  @VisibleForTesting
  List<Token> tokenise(String input) {
    // First check there are no question mark placeholders in the SQL text. This could lead to injection attacks, and isn't correct anyway.
    if (input.contains("?")) {
      throw new IllegalArgumentException("SQL must not contain question marks");
    }

    ParseState state = ParseState.TEXT;

    // The current token that is being parsed.
    String token = "";

    List<Token> tokens = new ArrayList<>();

    for (int i = 0; i < input.length(); i++) {
      if (state == ParseState.TEXT) {
        // We can either continue skipping parts of the SQL statement, or begin a new placeholder.
        if (input.charAt(i) == ':') {
          if (!token.isEmpty()) {
            tokens.add(new SqlText(token));
          }
          state = ParseState.PLACEHOLDER;
          token = "";
        } else {
          token += input.charAt(i);

          // End of input.
          if (i == input.length() - 1) {
            tokens.add(new SqlText(token));
          }
        }
      } else if (state == ParseState.PLACEHOLDER) {
        token += input.charAt(i);

        // If we're mid way through a placeholder, then check the next character is still a valid identifier, if not, end the Placeholder and begin the next TEXT state on the following character.
        if (i >= input.length() - 1 || !isValidPlaceholderName(input.charAt(i + 1))) {
          tokens.add(new SqlVariable(token, i - token.length()));
          state = ParseState.TEXT;
          token = "";
        }
      }
    }

    return tokens;
  }

  private boolean isValidPlaceholderName(char c) {
    return Character.isAlphabetic(c) || Character.isDigit(c) || (c == '_');
  }
}
