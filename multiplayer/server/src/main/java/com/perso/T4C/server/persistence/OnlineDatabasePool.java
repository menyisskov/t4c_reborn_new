package com.perso.T4C.server.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/** One owned pool per persistent server. Acquisition and validation belong on blocking workers. */
public final class OnlineDatabasePool implements OnlineStore.Connections,AutoCloseable {
  public static final int MAXIMUM_CONNECTIONS=8;
  public static final long ACQUIRE_TIMEOUT_MILLIS=1000;
  private final HikariDataSource source;

  public OnlineDatabasePool(String url,String user,String password) {
    this(url,user,password,MAXIMUM_CONNECTIONS,ACQUIRE_TIMEOUT_MILLIS);
  }

  OnlineDatabasePool(String url,String user,String password,int maximum,long acquireMillis) {
    if(url==null || !url.startsWith("jdbc:postgresql://") || user==null || user.isBlank() || password==null || password.isBlank())
      throw new IllegalArgumentException("PostgreSQL connection settings required");
    if(maximum<1 || maximum>MAXIMUM_CONNECTIONS || acquireMillis<500 || acquireMillis>ACQUIRE_TIMEOUT_MILLIS)
      throw new IllegalArgumentException("Invalid bounded pool limits");
    var database=new PGSimpleDataSource();
    try { database.setUrl(url); }
    catch(RuntimeException invalid) { throw new IllegalArgumentException("Invalid PostgreSQL URL"); }
    // Apply these after parsing the URL so query parameters cannot disable the safety limits.
    database.setUser(user);database.setPassword(password);
    database.setConnectTimeout(5);database.setLoginTimeout(5);database.setSocketTimeout(10);
    database.setTcpKeepAlive(true);
    var config=new HikariConfig();
    config.setDataSource(database);config.setPoolName("online-realm");
    config.setMaximumPoolSize(maximum);config.setMinimumIdle(Math.min(2,maximum));
    config.setConnectionTimeout(acquireMillis);config.setValidationTimeout(250);
    config.setInitializationFailTimeout(1000);config.setIdleTimeout(60_000);
    config.setMaxLifetime(30*60_000);config.setKeepaliveTime(120_000);
    config.setAutoCommit(true);config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
    try { source=new HikariDataSource(config); }
    catch(RuntimeException unavailable) {
      // Do not attach the raw cause: CLI startup stack traces can expose driver connection values.
      throw new IllegalStateException("Database pool unavailable ("+PersistenceFailure.describe(unavailable)+")");
    }
  }

  @Override public Connection open() throws SQLException { return source.getConnection(); }
  @Override public void close() { source.close(); }
}
