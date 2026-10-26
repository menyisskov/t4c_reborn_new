package com.perso.T4C.server.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Runs against PostgreSQL, including in CI; no in-memory substitute for transaction semantics. */
@EnabledIfEnvironmentVariable(named="T4C_DB_URL", matches=".+")
class OnlineStoreTest {
  private OnlineStore store;
  @BeforeEach void setup() throws Exception { store = OnlineStore.fromEnvironment(); store.migrate(); }

  @Test void reloadsAcrossStoreRestartAndRejectsWrongOwnerOrStaleCheckpoint() throws Exception {
    var key = store.provisionAccount();
    UUID account = key.accountId();
    var created = store.create(account, "Checkpoint test", 0, 10, 20);
    var session = store.acquire(key.secret(), created.id());
    var progress=CharacterProgressTest.sample();
    var saved = store.checkpointCharacter(session, created.revision(), 1, 30, 40, progress);
    var restarted = OnlineStore.fromEnvironment();
    restarted.migrate();
    assertEquals(saved, restarted.load(account, created.id()));
    assertEquals(progress,restarted.load(account,created.id()).progress());
    assertThrows(OnlineStore.Conflict.class, () -> restarted.load(UUID.randomUUID(), created.id()));
    assertThrows(OnlineStore.Conflict.class, () -> restarted.checkpoint(session, created.revision(), 0, 1, 2));
    assertEquals(saved, restarted.load(account, created.id()));
    var reward=restarted.reward(session,UUID.randomUUID(),7,11).character();
    var moved=restarted.checkpoint(session,reward.revision(),1,31,40);
    assertEquals(progress,moved.progress(),"Movement and reward writes must preserve gameplay state");
    assertEquals(7,moved.gold()); assertEquals(11,moved.experience());
    assertThrows(OnlineStore.Conflict.class,() -> restarted.checkpointCharacter(session,saved.revision(),1,0,0,progress));
  }

  @Test void concurrentRetriesGrantOneRewardAndRejectChangedPayload() throws Exception {
    var key = store.provisionAccount();
    UUID account = key.accountId(), event = UUID.randomUUID();
    var created = store.create(account, "Reward test", 0, 10, 20);
    var session = store.acquire(key.secret(), created.id());
    try (var executor = Executors.newFixedThreadPool(8)) {
      var actions = new ArrayList<Callable<OnlineStore.RewardResult>>();
      for (int i=0; i<8; i++) actions.add(() -> store.reward(session, event, 7, 11));
      int applied=0;
      for (var result : executor.invokeAll(actions)) if (result.get().applied()) applied++;
      assertEquals(1, applied);
    }
    var state=store.load(account, created.id());
    assertEquals(7, state.gold()); assertEquals(11, state.experience());
    assertThrows(OnlineStore.Conflict.class, () -> store.reward(session, event, 8, 11));
    assertEquals(state, store.load(account, created.id()));
  }

  @Test void unknownGameplayVersionFailsClosedWithoutReplacingSavedState() throws Exception {
    var key=store.provisionAccount();
    var created=store.create(key.accountId(),"Future state",0,10,20);
    var session=store.acquire(key.secret(),created.id());
    store.checkpointCharacter(session,created.revision(),0,10,20,CharacterProgressTest.sample());
    try (var c=testConnection(); var s=c.prepareStatement(
        "UPDATE online_characters SET progress=jsonb_set(progress,'{version}','999') WHERE id=?")) {
      s.setObject(1,created.id()); s.executeUpdate();
    }
    assertThrows(SQLException.class,() -> store.load(key.accountId(),created.id()));
    try (var c=testConnection(); var s=c.prepareStatement("SELECT progress->>'version' FROM online_characters WHERE id=?")) {
      s.setObject(1,created.id());
      try (var rows=s.executeQuery()) { assertTrue(rows.next()); assertEquals("999",rows.getString(1)); }
    }
  }

  @Test void failedBalanceUpdateRollsBackRewardReceiptSoRetryCanSucceed() throws Exception {
    var key = store.provisionAccount();
    UUID account=key.accountId(), failedEvent=UUID.randomUUID();
    var created=store.create(account, "Rollback test", 0, 10, 20);
    var session = store.acquire(key.secret(), created.id());
    store.reward(session, UUID.randomUUID(), Long.MAX_VALUE, 0);
    assertThrows(SQLException.class, () -> store.reward(session, failedEvent, 1, 0));
    // If the failed transaction left its receipt behind, this retry would conflict or be ignored.
    assertTrue(store.reward(session, failedEvent, 0, 3).applied());
    var state=store.load(account, created.id());
    assertEquals(Long.MAX_VALUE, state.gold()); assertEquals(3, state.experience());
  }

  @Test void concurrentLoginsAcquireOnlyOneWriterAndReleaseAllowsReconnect() throws Exception {
    var key=store.provisionAccount();
    var created=store.create(key.accountId(), "Lease test", 0, 10, 20);
    OnlineStore.Session winner=null;
    try (var executor=Executors.newFixedThreadPool(8)) {
      var actions=new ArrayList<Callable<OnlineStore.Session>>();
      for (int i=0; i<8; i++) actions.add(() -> {
        try { return store.acquire(key.secret(), created.id()); }
        catch (OnlineStore.Conflict expected) { return null; }
      });
      int acquired=0;
      for (var result:executor.invokeAll(actions)) {
        var session=result.get();
        if (session!=null) { acquired++; winner=session; }
      }
      assertEquals(1, acquired);
    }
    store.renew(winner); store.release(winner);
    var reconnect=store.acquire(key.secret(),created.id());
    assertNotEquals(winner.leaseId(), reconnect.leaseId());
    store.release(winner); // A delayed disconnect cannot evict the replacement session.
    store.renew(reconnect);
  }

  @Test void expiredWriterCannotRenewSaveOrRewardAfterTakeover() throws Exception {
    var key=store.provisionAccount();
    var created=store.create(key.accountId(), "Expired writer", 0, 10, 20);
    var old=store.acquire(key.secret(),created.id());
    // Expire using database time instead of a slow, timing-sensitive sleep.
    try (var c=testConnection(); var s=c.prepareStatement("UPDATE online_characters SET session_until=clock_timestamp()-interval '1 second' WHERE id=?")) {
      s.setObject(1,created.id()); s.executeUpdate();
    }
    assertThrows(OnlineStore.Conflict.class, () -> store.renew(old));
    var fresh=store.acquire(key.secret(),created.id());
    assertThrows(OnlineStore.Conflict.class, () -> store.checkpoint(old,created.revision(),0,99,99));
    assertThrows(OnlineStore.Conflict.class, () -> store.reward(old,UUID.randomUUID(),100,100));
    var saved=store.checkpoint(fresh,created.revision(),0,30,40);
    assertEquals(saved,store.load(key.accountId(),created.id()));
  }

  @Test void credentialsBindOwnershipAndRotationRevokesOldKeyAndSession() throws Exception {
    var key=store.provisionAccount(); var other=store.provisionAccount();
    var created=store.create(key.accountId(),"Key rotation",0,10,20);
    assertThrows(OnlineStore.Conflict.class, () -> store.acquire(other.secret(),created.id()));
    var old=store.acquire(key.secret(),created.id());
    var replacement=store.rotateAccountKey(key.accountId());
    assertThrows(OnlineStore.Conflict.class, () -> store.acquire(key.secret(),created.id()));
    assertThrows(OnlineStore.Conflict.class, () -> store.renew(old));
    assertThrows(OnlineStore.Conflict.class, () -> store.reward(old,UUID.randomUUID(),1,1));
    var fresh=store.acquire(replacement.secret(),created.id());
    assertEquals(created,fresh.character());
    try (var c=testConnection(); var s=c.prepareStatement("SELECT access_hash FROM online_accounts WHERE id=?")) {
      s.setObject(1,key.accountId());
      try (var rows=s.executeQuery()) { assertTrue(rows.next()); assertNotEquals(replacement.secret(),rows.getString(1)); }
    }
  }

  private java.sql.Connection testConnection() throws SQLException {
    return DriverManager.getConnection(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"));
  }

  @Test void upgradesVersionOneWithoutLosingCharactersOrReplayingRewards() throws Exception {
    String schema="migration_"+UUID.randomUUID().toString().replace("-","");
    UUID account=UUID.randomUUID(), character=UUID.randomUUID(), event=UUID.randomUUID();
    try (var c=testConnection(); var s=c.createStatement()) {
      s.execute("CREATE SCHEMA "+schema); c.setSchema(schema);
      try (var input=getClass().getResourceAsStream("/schema-v1.sql")) {
        s.execute(new String(java.util.Objects.requireNonNull(input).readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));
      }
      try (var insert=c.prepareStatement("INSERT INTO online_characters(id,account_id,name,world,x,y,gold,experience,revision) VALUES (?,?,'Existing',0,10,20,7,11,1)")) {
        insert.setObject(1,character); insert.setObject(2,account); insert.executeUpdate();
      }
      try (var insert=c.prepareStatement("INSERT INTO online_rewards(character_id,event_id,gold,experience) VALUES (?,?,7,11)")) {
        insert.setObject(1,character); insert.setObject(2,event); insert.executeUpdate();
      }
    }
    var upgraded=new OnlineStore(() -> { var c=testConnection(); c.setSchema(schema); return c; });
    upgraded.migrate(); upgraded.migrate();
    var expected=new OnlineStore.CharacterState(character,account,"Existing",0,10,20,7,11,1,null);
    assertEquals(expected,upgraded.load(account,character));
    var key=upgraded.rotateAccountKey(account);
    var session=upgraded.acquire(key.secret(),character);
    assertFalse(upgraded.reward(session,event,7,11).applied());
    assertEquals(expected,upgraded.load(account,character));
  }
}
