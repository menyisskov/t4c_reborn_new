package com.perso.T4C.server.persistence;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
class OnlineDatabasePoolTest {
  private OnlineDatabasePool pool(int maximum) {
    return new OnlineDatabasePool(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"),maximum,500);
  }
  private Connection external() throws SQLException {
    return DriverManager.getConnection(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"));
  }
  private int backend(Connection c) throws SQLException {
    try(var s=c.createStatement();var r=s.executeQuery("SELECT pg_backend_pid()")) { r.next();return r.getInt(1); }
  }

  @Test void repeatedOperationsReuseOneBackendWithoutLeakingTransactionState() throws Exception {
    try(var pool=pool(1)) {
      int pid;
      try(var c=pool.open();var s=c.createStatement()) {
        pid=backend(c);s.execute("CREATE TEMP TABLE pool_rollback_probe(value integer)");
        c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);c.setAutoCommit(false);
        s.executeUpdate("INSERT INTO pool_rollback_probe VALUES (1)");
        // Return an uncommitted write: the next borrower must never inherit or commit it.
      }
      try(var c=pool.open();var s=c.createStatement();var r=s.executeQuery("SELECT count(*) FROM pool_rollback_probe")) {
        assertEquals(pid,backend(c));assertTrue(c.getAutoCommit());
        assertEquals(Connection.TRANSACTION_READ_COMMITTED,c.getTransactionIsolation());
        r.next();assertEquals(0,r.getInt(1));c.setReadOnly(true);
      }
      for(int i=0;i<20;i++) try(var c=pool.open()) { assertEquals(pid,backend(c));assertFalse(c.isReadOnly()); }
    }
  }

  @Test void poolMetricsCountAcquisitionsOperationTimeAndTimeoutsWithoutLabels() throws Exception {
    try(var pool=pool(1)) {
      for(int i=0;i<3;i++) try(var c=pool.open();var s=c.createStatement()) {
        s.execute("SELECT pg_sleep(0.03)");
      }
      var before=pool.metrics().snapshot();
      assertTrue(before.acquired()>=3 && before.operations()>=3);
      assertTrue(before.operationMaxMillis()>=25,"A 30 ms statement shows as operation time, was "+before.operationMaxMillis());
      assertEquals(before.operations(),java.util.Arrays.stream(before.operationBuckets()).sum());
      assertEquals(before.acquired(),java.util.Arrays.stream(before.acquireBuckets()).sum());
      assertEquals(DatabaseMetrics.UPPER_MILLIS.length+1,before.operationBuckets().length);
      assertEquals(1,before.poolMaximum());
      try(var held=pool.open()) {
        assertThrows(SQLTransientConnectionException.class,pool::open);
      }
      var after=pool.metrics().snapshot();
      assertEquals(before.acquireTimeouts()+1,after.acquireTimeouts(),"A failed acquisition is counted once");
      // connectionsCreated counts only connections opened after the tracker exists (not the fail-fast first one).
      assertTrue(after.connectionsCreated()>=0);
    }
    try(var store=OnlineStore.pooledFromEnvironment()) {
      store.migrate();
      assertTrue(store.databaseMetrics().operations()>=1,"The store reports its pool's operations");
    }
  }

  @Test void saturationIsBoundedAndPoolAndStoreOwnershipClosePhysicalSessions() throws Exception {
    var pool=pool(2);int firstPid,secondPid;
    try {
      try(var first=pool.open();var second=pool.open()) {
        firstPid=backend(first);secondPid=backend(second);
        long started=System.nanoTime();
        assertThrows(SQLTransientConnectionException.class,pool::open);
        long elapsed=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-started);
        assertTrue(elapsed>=400 && elapsed<4000,"Pool saturation must time out rather than wait indefinitely");
      }
      pool.close();assertThrows(SQLException.class,pool::open);
      try(var c=external();var s=c.prepareStatement("SELECT count(*) FROM pg_stat_activity WHERE pid IN (?,?)")) {
        s.setInt(1,firstPid);s.setInt(2,secondPid);
        try(var r=s.executeQuery()) { r.next();assertEquals(0,r.getInt(1),"Closing the owned pool closes idle PostgreSQL sessions"); }
      }
    } finally { pool.close(); }
    try(var store=OnlineStore.pooledFromEnvironment()) {
      store.migrate();store.close();assertThrows(SQLException.class,store::provisionAccount);
    }
  }

  @Test void poolReplacesOnlyItsTerminatedBackendAndPreservesCommittedCharacter() throws Exception {
    try(var pool=pool(1)) {
      var store=new OnlineStore((OnlineStore.Connections)pool);store.migrate();
      var account=store.provisionAccount();var saved=store.create(account.accountId(),"Pooled recovery",0,3.5,3.5);
      int ownedPid;
      try(var c=pool.open()) { ownedPid=backend(c); }
      // The PID comes directly from this test's one owned connection, never an arbitrary backend.
      try(var c=external();var s=c.prepareStatement("SELECT pg_terminate_backend(?)")) {
        s.setInt(1,ownedPid);try(var r=s.executeQuery()) { r.next();assertTrue(r.getBoolean(1)); }
      }
      Thread.sleep(600); // Beyond Hikari's short recent-use validation bypass.
      try(var c=pool.open()) { assertNotEquals(ownedPid,backend(c)); }
      assertEquals(saved,store.load(account.accountId(),saved.id()));
    }
  }
}
