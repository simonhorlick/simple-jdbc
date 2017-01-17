package me.horlick.db;

// A single transaction. Multiple statements can be executed atomically by calling execute.
public class DatabaseTransaction implements AutoCloseable {

  private final DatabaseClient client;

  private boolean committed = false;

  DatabaseTransaction(DatabaseClient client) {
    this.client = client;
  }

  /**
   * Add a statement to the transaction to be executed atomically when commit is called.
   *
   * @param statement
   */
  public void execute(Statement statement) {
    client.executeStatement(statement);
  }

  // Atomically execute all statements in this transaction. This method should only be called once.
  public synchronized void commit() {
    committed = true;
    client.commit();
  }

  @Override
  public synchronized void close() throws Exception {
    if (!committed) {
      commit();
    }
  }
}
