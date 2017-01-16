# db

A simple wrapper around JDBC.

## Examples

```java
class Example {
  public static insertAndSelect() {
    HostAndPort hostAndPort = HostAndPort.fromString("localhost:32769");

    try (DatabaseClient client = new DatabaseClientFactory().create(hostAndPort)) {

      Map<String, Object> triangle =
          new ImmutableMap.Builder<String, Object>()
              .put("sides", 3)
              .put("regular", true)
              .put("convex", true)
              .build();

      Map<String, Object> square =
          new ImmutableMap.Builder<String, Object>()
              .put("sides", 4)
              .put("regular", true)
              .put("convex", true)
              .build();

      DatabaseTransaction tx = client.begin();
      tx.execute(new Statement("INSERT INTO shapes (sides,regular,convex) VALUES (:sides,:regular,:convex)", triangle));
      tx.execute(new Statement("INSERT INTO shapes (sides,regular,convex) VALUES (:sides,:regular,:convex)", square));
      tx.commit();

      try (Cursor cursor =
          client.executeStatement(new Statement("SELECT sides, regular, convex FROM shapes",
              Collections.emptyMap()))) {
        Row row;
        while ((row = cursor.next()) != null) {
          Integer sides = row.get("sides");
          Boolean regular = row.get("regular");
          Boolean convex = row.get("convex");
          System.out.format("(%d, %b, %b)\n", id, asset_url, labels);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
```
