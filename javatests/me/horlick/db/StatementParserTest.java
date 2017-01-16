package me.horlick.db;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.horlick.db.StatementParser.SqlText;
import me.horlick.db.StatementParser.SqlVariable;
import me.horlick.db.StatementParser.Token;
import org.junit.Test;

public class StatementParserTest {

  StatementParser parser = new StatementParser();

  @Test
  public void shouldReturnOriginalSqlIfNoVariables() {
    Statement statement = new Statement("SELECT * FROM shapes", Collections.emptyMap());
    assertEquals(statement.getSql(), parser.parse(statement));
  }

  @Test
  public void shouldTokeniseStatementWithSingleVariable() {
    List<Token> tokens = parser.tokenise("INSERT INTO shapes (sides) VALUES (:sides)");

    assertEquals(
        Arrays.asList(
            new SqlText("INSERT INTO shapes (sides) VALUES ("),
            new SqlVariable("sides", 35),
            new SqlText(")")),
        tokens);
  }

  @Test
  public void shouldTokeniseStatementWithMultipleVariables() {
    List<Token> tokens =
        parser.tokenise("INSERT INTO shapes (sides,regular) VALUES (:sides,:regular)");

    assertEquals(
        Arrays.asList(
            new SqlText("INSERT INTO shapes (sides,regular) VALUES ("),
            new SqlVariable("sides", 43),
            new SqlText(","),
            new SqlVariable("regular", 50),
            new SqlText(")")),
        tokens);
  }

  @Test
  public void shouldHandlePlaceholdersThatAreNextToEachOther() {
    List<Token> tokens = parser.tokenise("sql:sides:regular");

    assertEquals(
        Arrays.asList(
            new SqlText("sql"), new SqlVariable("sides", 3), new SqlVariable("regular", 9)),
        tokens);
  }

  @Test
  public void shouldAllowPlaceholdersWithNumbersAndUnderscores() {
    List<Token> tokens = parser.tokenise(":sides_are_great123");

    assertEquals(Arrays.asList(new SqlVariable("sides_are_great123", 0)), tokens);
  }

  @Test
  public void shouldSubstituteSingleNumericPlaceholder() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("sides", 3);

    Statement statement = new Statement("INSERT INTO shapes (sides) VALUES (:sides)", variables);

    assertEquals("INSERT INTO shapes (sides) VALUES (3)", parser.parse(statement));
  }

  @Test
  public void shouldSubstituteSingleStringPlaceholder() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("sides", "3");

    Statement statement = new Statement("INSERT INTO shapes (sides) VALUES (:sides)", variables);

    assertEquals("INSERT INTO shapes (sides) VALUES ('3')", parser.parse(statement));
  }

  @Test
  public void shouldSubstituteMultiplePlaceholders() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("sides", "3");
    variables.put("regular", false);
    variables.put("convex", true);

    Statement statement =
        new Statement(
            "INSERT INTO shapes (sides,regular,convex) VALUES (:sides,:regular,:convex)",
            variables);

    assertEquals(
        "INSERT INTO shapes (sides,regular,convex) VALUES ('3',FALSE,TRUE)",
        parser.parse(statement));
  }

  @Test
  public void shouldThrowIfPlaceholderIsNotBound() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("sides", "3");
    variables.put("convex", true);

    Statement statement =
        new Statement(
            "INSERT INTO shapes (sides,regular,convex) VALUES (:sides,:regular,:convex)",
            variables);
    try {
      parser.parse(statement);
      fail("Expecting exception");
    } catch (Exception ignored) {
    }
  }

  @Test
  public void shouldThrowIfUnboundVariables() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("sides", "3");
    variables.put("convex", true);
    variables.put("regular", false);
    variables.put("not_used_in_statement", "not_used");

    Statement statement =
        new Statement(
            "INSERT INTO shapes (sides,regular,convex) VALUES (:sides,:regular,:convex)",
            variables);
    try {
      parser.parse(statement);
      fail("Expecting exception");
    } catch (Exception ignored) {
    }
  }
}
