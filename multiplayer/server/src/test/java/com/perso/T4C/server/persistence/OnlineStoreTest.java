package com.perso.T4C.server.persistence;

import com.perso.T4C.online.model.CharacterProgress;

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
  @Test void durableSpawnLifeSurvivesRecoveryAndRespawnsOnceOnlyAfterItsReward() throws Exception {
    String spawn="test:"+UUID.randomUUID();
    OnlineStore.SpawnState life;
    try (var executor=Executors.newFixedThreadPool(2)) {
      var a=executor.submit(() -> store.loadOrCreateSpawn(spawn));
      var b=executor.submit(() -> store.loadOrCreateSpawn(spawn));
      life=a.get(); assertEquals(life,b.get(),"Concurrent startup must recover one life identity");
    }
    assertNull(life.recipient());
    assertThrows(OnlineStore.Conflict.class,() -> store.respawn(spawn,life.eventId(),0));
    var key=store.provisionAccount();
    var character=store.create(key.accountId(),"Spawn reward",0,10,20);
    var session=store.acquire(key.secret(),character.id());
    var progress=CharacterProgressTest.sample();
    store.commitWorldReward(session,0,life.eventId(),7,11,progress);
    var restarted=OnlineStore.fromEnvironment(); restarted.migrate();
    var dead=restarted.loadOrCreateSpawn(spawn);
    assertEquals(life.eventId(),dead.eventId());
    assertEquals(character.id(),dead.recipient());
    assertNotNull(dead.claimedAt());
    assertThrows(OnlineStore.Conflict.class,() -> restarted.respawn(spawn,life.eventId(),60_000));
    try (var executor=Executors.newFixedThreadPool(2)) {
      var results=new ArrayList<java.util.concurrent.Future<Boolean>>();
      for(int i=0;i<2;i++) results.add(executor.submit(() -> {
        try { restarted.respawn(spawn,life.eventId(),0); return true; }
        catch(OnlineStore.Conflict stale) { return false; }
      }));
      assertNotEquals(results.get(0).get(),results.get(1).get());
    }
    var next=restarted.loadOrCreateSpawn(spawn);
    assertEquals(life.generation()+1,next.generation());
    assertNotEquals(life.eventId(),next.eventId());
    assertNull(next.recipient());
    assertNull(next.claimedAt());
    assertThrows(OnlineStore.Conflict.class,() -> restarted.respawn(spawn,life.eventId(),0));
    assertFalse(restarted.commitWorldReward(session,0,life.eventId(),7,11,progress).applied(),
        "Respawning must retain the old life's receipt for delayed retries");
    assertEquals(next,restarted.loadOrCreateSpawn(spawn));
  }

  @Test void sharedWorldEventHasOneRecipientAndSurvivesReconnectWithoutAnotherAward() throws Exception {
    var key=store.provisionAccount();
    var first=store.create(key.accountId(),"World contender one",0,10,20);
    var second=store.create(key.accountId(),"World contender two",0,10,20);
    var one=store.acquire(key.secret(),first.id());
    var two=store.acquire(key.secret(),second.id());
    var progress=CharacterProgressTest.sample();
    UUID event=UUID.randomUUID();
    var start=new java.util.concurrent.CountDownLatch(1);
    try (var executor=Executors.newFixedThreadPool(2)) {
      var actions=new ArrayList<java.util.concurrent.Future<Boolean>>();
      for (var session:java.util.List.of(one,two)) actions.add(executor.submit(() -> {
        start.await();
        try { return store.commitWorldReward(session,0,event,7,11,progress).applied(); }
        catch (OnlineStore.Conflict expectedLoser) { return false; }
      }));
      start.countDown();
      assertNotEquals(actions.get(0).get(),actions.get(1).get(),"Exactly one contender must commit");
    }
    var firstSaved=store.load(key.accountId(),first.id());
    var secondSaved=store.load(key.accountId(),second.id());
    assertEquals(7,firstSaved.gold()+secondSaved.gold());
    assertEquals(11,firstSaved.experience()+secondSaved.experience());
    try (var c=testConnection(); var s=c.prepareStatement("SELECT count(*) FROM online_rewards WHERE event_id=?")) {
      s.setObject(1,event);
      try (var rows=s.executeQuery()) { assertTrue(rows.next()); assertEquals(1,rows.getInt(1)); }
    }
    var winner=firstSaved.gold()==7 ? one : two;
    var loser=winner==one ? two : one;
    assertEquals(loser.character(),store.load(key.accountId(),loser.characterId()),"Losing claim rolls back payload and receipt");
    store.release(winner);
    var restarted=OnlineStore.fromEnvironment(); restarted.migrate();
    var replacement=restarted.acquire(key.secret(),winner.characterId());
    assertFalse(restarted.commitWorldReward(replacement,0,event,7,11,progress).applied());
    assertThrows(OnlineStore.Conflict.class,() -> restarted.commitWorldReward(loser,0,event,7,11,progress));
    assertThrows(OnlineStore.Conflict.class,() -> restarted.commitWorldReward(replacement,0,event,8,11,progress));
  }

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
    var progress=CharacterProgressTest.sample();
    try (var executor = Executors.newFixedThreadPool(8)) {
      var actions = new ArrayList<Callable<OnlineStore.RewardResult>>();
      for (int i=0; i<8; i++) actions.add(() -> store.commitGameplayEvent(session,created.revision(),event,7,11,progress));
      int applied=0;
      for (var result : executor.invokeAll(actions)) if (result.get().applied()) applied++;
      assertEquals(1, applied);
    }
    var state=store.load(account, created.id());
    assertEquals(7, state.gold()); assertEquals(11, state.experience());
    assertEquals(progress,state.progress());
    assertThrows(OnlineStore.Conflict.class, () -> store.commitGameplayEvent(session,state.revision(),event,8,11,progress));
    assertThrows(OnlineStore.Conflict.class, () -> store.commitGameplayEvent(session,state.revision(),event,7,11,CharacterProgressTest.sample()));
    assertThrows(OnlineStore.Conflict.class, () -> store.reward(session,event,7,11));
    assertEquals(state, store.load(account, created.id()));
    store.release(session);
    var restarted=OnlineStore.fromEnvironment();
    var replacement=restarted.acquire(key.secret(),created.id());
    var moved=restarted.checkpoint(replacement,state.revision(),0,11,20);
    // A lost success response can be retried after reload and later movement without regranting.
    var retried=restarted.commitGameplayEvent(replacement,created.revision(),event,7,11,moved.progress());
    assertFalse(retried.applied()); assertEquals(moved,retried.character());
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
    var before=store.reward(session, UUID.randomUUID(), Long.MAX_VALUE, 0).character();
    var progress=CharacterProgressTest.sample();
    assertThrows(SQLException.class, () -> store.commitWorldReward(session,before.revision(),failedEvent,1,0,progress));
    assertEquals(before,store.load(account,created.id()),"Failed balance update must not publish items or quest progress");
    // If the failed transaction left its receipt behind, this retry would conflict or be ignored.
    assertTrue(store.commitWorldReward(session,before.revision(),failedEvent,0,3,progress).applied());
    var state=store.load(account, created.id());
    assertEquals(Long.MAX_VALUE, state.gold()); assertEquals(3, state.experience());
    assertEquals(progress,state.progress());
  }

  @Test void staleGameplayEventCannotOverwriteProgressOrConsumeItsReceipt() throws Exception {
    var key=store.provisionAccount(); var created=store.create(key.accountId(),"Stale event",0,10,20);
    var session=store.acquire(key.secret(),created.id()); var progress=CharacterProgressTest.sample();
    var moved=store.checkpoint(session,created.revision(),0,11,20); UUID event=UUID.randomUUID();
    assertThrows(OnlineStore.Conflict.class,() -> store.commitGameplayEvent(session,created.revision(),event,2,3,progress));
    assertEquals(moved,store.load(key.accountId(),created.id()));
    assertTrue(store.commitGameplayEvent(session,moved.revision(),event,2,3,progress).applied());
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
