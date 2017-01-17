package me.horlick.db;

import com.google.common.net.HostAndPort;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseClientFactory {

  private final HostAndPort hostAndPort;

  public DatabaseClientFactory(HostAndPort hostAndPort) {
    this.hostAndPort = hostAndPort;
  }

  public DatabaseClient create() {
    String url = "jdbc:postgresql://" + hostAndPort.getHost() + ":" + hostAndPort.getPort() + "/";

    Properties props = new Properties();
    props.setProperty("user", "postgres");
    props.setProperty("password", "supersecret");

    // TODO(simon): Fix this. On a default install I get "PSQLException: The server does not support SSL"
    //props.setProperty("ssl", "true");

    try {
      Connection conn = DriverManager.getConnection(url, props);

      // We force everything to be wrapped in a DatabaseTransaction, so this committing happens there.
      conn.setAutoCommit(false);

      return new DatabaseClient(conn);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
