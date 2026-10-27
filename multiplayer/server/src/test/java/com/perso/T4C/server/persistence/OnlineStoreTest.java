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
  @Test void movingCreatureCheckpointCannotRewindCommittedDamageOrTargetEpoch() throws Exception {
    var life=store.loadOrCreateSpawn("motion:"+UUID.randomUUID());
    assertNull(life.currentX());assertNull(life.currentY());
    var key=store.provisionAccount();
    var character=store.create(key.accountId(),"Motion fence",0,10,20,CharacterProgressTest.sample(),0);
    var session=store.acquire(key.secret(),character.id());
    var initial=new OnlineStore.SpawnMotion(life.key(),life.eventId(),life.stateRevision(),10.5,20.5,
        "PATROL",null,null);
    var checkpoint=store.checkpointSpawn(initial,null);
    assertEquals(10.5,checkpoint.currentX());assertEquals(20.5,checkpoint.currentY());
    assertEquals("PATROL",checkpoint.mode());
    assertEquals(life.eventId(),checkpoint.eventId());
    var impact=new OnlineStore.SpawnMotion(life.key(),life.eventId(),checkpoint.stateRevision(),10.75,20.5,
        "RETALIATION",character.id(),character.locationEpoch());
    var progress=CharacterProgressTest.sample();
    var strike=new OnlineStore.CreatureStrike(life.key(),life.eventId(),20,20,12,impact);
    var provoke=new OnlineStore.CreatureProvocation(life.key(),life.eventId(),character.id(),0,impact);
    var event=UUID.randomUUID();
    assertTrue(store.commitCreatureStrike(session,character.revision(),event,0,8,progress,strike,provoke).applied());
    var committed=store.loadOrCreateSpawn(life.key());
    assertEquals(12,committed.health());assertEquals(10.75,committed.currentX());
    assertEquals(character.id(),committed.targetCharacter());
    assertEquals(character.locationEpoch(),committed.targetEpoch());
    assertEquals("RETALIATION",committed.mode());
    var changedPose=new OnlineStore.SpawnMotion(life.key(),life.eventId(),checkpoint.stateRevision(),
        10.6,20.5,"RETALIATION",character.id(),character.locationEpoch());
    assertThrows(OnlineStore.Conflict.class,()->store.commitCreatureStrike(session,character.revision(),event,
        0,8,progress,new OnlineStore.CreatureStrike(life.key(),life.eventId(),20,20,12,changedPose),
        new OnlineStore.CreatureProvocation(life.key(),life.eventId(),character.id(),0,changedPose)),
        "A replay with a different impact position must conflict");
    assertThrows(OnlineStore.Conflict.class,()->store.checkpointSpawn(initial,null),
        "A delayed pose cannot undo a committed hit and target");
    var stale=new OnlineStore.CreatureStrike(life.key(),life.eventId(),20,12,9,
        new OnlineStore.SpawnMotion(life.key(),life.eventId(),checkpoint.stateRevision(),10.5,20.5,
            "PATROL",null,null));
    assertThrows(OnlineStore.Conflict.class,()->store.commitCreatureStrike(session,1,UUID.randomUUID(),0,3,progress,stale));
    assertEquals(committed,store.loadOrCreateSpawn(life.key()));
    assertEquals(8,store.load(key.accountId(),character.id()).experience());
    var lethalMotion=new OnlineStore.SpawnMotion(life.key(),life.eventId(),committed.stateRevision(),
        committed.currentX(),committed.currentY(),"RETALIATION",character.id(),character.locationEpoch());
    assertTrue(store.commitCreatureStrike(session,1,life.eventId(),0,0,progress,
        new OnlineStore.CreatureStrike(life.key(),life.eventId(),20,12,0,lethalMotion),null).applied());
    var killed=store.loadOrCreateSpawn(life.key());
    assertEquals(0,killed.health());assertEquals(character.id(),killed.recipient());
    assertThrows(OnlineStore.Conflict.class,()->store.checkpointSpawn(impact,session),
        "A delayed moving pose cannot revive a rewarded life");
    var next=store.respawn(life.key(),life.eventId(),0);
    assertNotEquals(life.eventId(),next.eventId());assertNull(next.health());
    assertNull(next.currentX());assertNull(next.currentY());assertNull(next.targetCharacter());
    store.release(session);
  }

  @Test void schemaTenTargetMigratesWithoutHealingOrInventingACharacterEpoch() throws Exception {
    String schema="pursuit_"+UUID.randomUUID().toString().replace("-","");
    UUID event=UUID.randomUUID(),target=UUID.randomUUID();
    try(var c=testConnection();var s=c.createStatement()) {
      s.execute("CREATE SCHEMA "+schema);
      s.execute("CREATE TABLE "+schema+".online_schema(version integer PRIMARY KEY)");
      s.execute("INSERT INTO "+schema+".online_schema(version) SELECT generate_series(1,10)");
      s.execute("CREATE TABLE "+schema+".online_spawns(spawn_key varchar(256) PRIMARY KEY,generation bigint NOT NULL DEFAULT 0,event_id uuid NOT NULL,health integer,target_character uuid,next_attack_at bigint NOT NULL DEFAULT 0)");
      s.execute("CREATE TABLE "+schema+".online_world_rewards(event_id uuid,character_id uuid,claimed_at timestamptz)");
      try(var insert=c.prepareStatement("INSERT INTO "+schema+".online_spawns VALUES (?,?,?,?,?,?)")) {
        insert.setString(1,"legacy:"+event);insert.setLong(2,4);insert.setObject(3,event);
        insert.setInt(4,7);insert.setObject(5,target);insert.setLong(6,1234);insert.executeUpdate();
      }
    }
    String url=System.getenv("T4C_DB_URL")+(System.getenv("T4C_DB_URL").contains("?") ? "&" : "?")+"currentSchema="+schema;
    var legacy=new OnlineStore(()->DriverManager.getConnection(url,System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD")));
    legacy.migrate();
    var migrated=legacy.loadOrCreateSpawn("legacy:"+event);
    assertEquals(event,migrated.eventId());assertEquals(4,migrated.generation());
    assertEquals(7,migrated.health());assertEquals(target,migrated.targetCharacter());
    assertEquals(1234,migrated.nextAttackAt());assertEquals("RETALIATION",migrated.mode());
    assertNull(migrated.currentX());assertNull(migrated.currentY());assertNull(migrated.targetEpoch());
    assertEquals(0,migrated.stateRevision());
    assertNull(migrated.diedAt());assertNull(migrated.deathKind());
    try(var c=testConnection();var s=c.createStatement()) {
      assertThrows(SQLException.class,()->s.execute("UPDATE "+schema+".online_spawns SET health=0"),
          "Zero health without terminal metadata must never be stored as a living life");
      assertThrows(SQLException.class,()->s.execute("UPDATE "+schema+".online_spawns SET health=0,died_at=clock_timestamp()"),
          "A nullable death kind cannot pass PostgreSQL CHECK's three-valued logic");
    }
  }

  @Test void schemaElevenClaimedLifeKeepsItsDamagePoseAndRewardWhileClearingOldAggro() throws Exception {
    String schema="clanlegacy_"+UUID.randomUUID().toString().replace("-","");
    UUID event=UUID.randomUUID(),target=UUID.randomUUID(),recipient=UUID.randomUUID();
    try(var c=testConnection();var s=c.createStatement()) {
      s.execute("CREATE SCHEMA "+schema);
      s.execute("CREATE TABLE "+schema+".online_schema(version integer PRIMARY KEY)");
      s.execute("INSERT INTO "+schema+".online_schema(version) SELECT generate_series(1,11)");
      s.execute("""
          CREATE TABLE %s.online_spawns (
            spawn_key varchar(256) PRIMARY KEY,generation bigint NOT NULL DEFAULT 0,event_id uuid NOT NULL,
            health integer,target_character uuid,next_attack_at bigint NOT NULL DEFAULT 0,
            current_x double precision,current_y double precision,mode varchar(16) NOT NULL DEFAULT 'IDLE',
            target_epoch bigint,state_revision bigint NOT NULL DEFAULT 0,
            CONSTRAINT online_spawn_mode CHECK (mode IN ('IDLE','PATROL','NATURAL','RETALIATION','RETURN')))
          """.formatted(schema));
      s.execute("CREATE TABLE "+schema+".online_world_rewards(event_id uuid,character_id uuid,claimed_at timestamptz)");
      try(var insert=c.prepareStatement("INSERT INTO "+schema+".online_spawns VALUES (?,?,?,?,?,?,?,?,?,?,?)")) {
        insert.setString(1,"legacy-dead:"+event);insert.setLong(2,3);insert.setObject(3,event);
        insert.setNull(4,java.sql.Types.INTEGER);insert.setObject(5,target);insert.setLong(6,2345);
        insert.setDouble(7,10.25);insert.setDouble(8,20.75);insert.setString(9,"RETALIATION");
        insert.setLong(10,8);insert.setLong(11,9);insert.executeUpdate();
      }
      try(var insert=c.prepareStatement("INSERT INTO "+schema+".online_world_rewards VALUES (?,?,clock_timestamp()-interval '10 minutes')")) {
        insert.setObject(1,event);insert.setObject(2,recipient);insert.executeUpdate();
      }
    }
    String url=System.getenv("T4C_DB_URL")+(System.getenv("T4C_DB_URL").contains("?") ? "&" : "?")+"currentSchema="+schema;
    var legacy=new OnlineStore(()->DriverManager.getConnection(url,System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD")));
    legacy.migrate();
    var migrated=legacy.loadOrCreateSpawn("legacy-dead:"+event);
    assertEquals(event,migrated.eventId());assertEquals(3,migrated.generation());
    assertEquals(0,migrated.health());assertEquals(recipient,migrated.recipient());
    assertEquals(migrated.claimedAt(),migrated.diedAt());assertEquals("PLAYER",migrated.deathKind());
    assertEquals(10.25,migrated.currentX());assertEquals(20.75,migrated.currentY());
    assertEquals(9,migrated.stateRevision());
    assertEquals("IDLE",migrated.mode());assertNull(migrated.targetCharacter());
    assertNull(migrated.targetEpoch());assertEquals(0,migrated.nextAttackAt());
    assertThrows(OnlineStore.Conflict.class,()->legacy.respawn(migrated.key(),event,migrated.stateRevision(),86_400_000));
  }

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
    var strike=new OnlineStore.CreatureStrike(spawn,life.eventId(),10,10,0);
    store.commitCreatureStrike(session,0,life.eventId(),7,11,progress,strike);
    var restarted=OnlineStore.fromEnvironment(); restarted.migrate();
    var dead=restarted.loadOrCreateSpawn(spawn);
    assertEquals(life.eventId(),dead.eventId());
    assertEquals(character.id(),dead.recipient());
    assertNotNull(dead.claimedAt());
    assertEquals(0,dead.health());
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
    assertNull(next.health(),"Only a new generation restores authored health");
    assertThrows(OnlineStore.Conflict.class,() -> restarted.respawn(spawn,life.eventId(),0));
    assertFalse(restarted.commitCreatureStrike(session,0,life.eventId(),7,11,progress,strike).applied(),
        "Respawning must retain the old life's receipt for delayed retries");
    assertEquals(next,restarted.loadOrCreateSpawn(spawn));
  }

  @Test void playerAndClanLethalRaceClaimsOneLifeWithOnlyPlayerRewards() throws Exception {
    var source=store.loadOrCreateSpawn("clan-source:"+UUID.randomUUID());
    var target=store.loadOrCreateSpawn("clan-target:"+UUID.randomUUID());
    var sourceReady=store.checkpointSpawn(new OnlineStore.SpawnMotion(source.key(),source.eventId(),
        source.stateRevision(),3.5,3.5,"CLAN",null,null,target.key(),target.eventId()),null);
    var targetReady=store.checkpointSpawn(new OnlineStore.SpawnMotion(target.key(),target.eventId(),
        target.stateRevision(),4.5,3.5,"PATROL",null,null),null);
    var sourceMotion=new OnlineStore.SpawnMotion(source.key(),source.eventId(),sourceReady.stateRevision(),
        3.5,3.5,"CLAN",null,null,target.key(),target.eventId());
    var targetMotion=new OnlineStore.SpawnMotion(target.key(),target.eventId(),targetReady.stateRevision(),
        4.5,3.5,"IDLE",null,null);
    var clan=new OnlineStore.ClanAttack(sourceMotion,targetMotion,20,20,0,0,System.currentTimeMillis()+1500);
    var key=store.provisionAccount();
    var progress=CharacterProgressTest.sample();
    var player=store.create(key.accountId(),"Clan race",0,4.5,3.5,progress,0);
    var session=store.acquire(key.secret(),player.id());
    var strike=new OnlineStore.CreatureStrike(target.key(),target.eventId(),20,20,0,targetMotion);
    var start=new java.util.concurrent.CountDownLatch(1);
    boolean clanWon,playerWon;
    try(var executor=Executors.newFixedThreadPool(2)) {
      var clanResult=executor.submit(() -> {start.await();try {store.commitClanAttack(clan);return true;}
        catch(SQLException lost) {return false;}});
      var playerResult=executor.submit(() -> {start.await();try {return store.commitCreatureStrike(session,player.revision(),
          target.eventId(),0,7,progress,strike).applied();}catch(SQLException lost) {return false;}});
      start.countDown();clanWon=clanResult.get();playerWon=playerResult.get();
    }
    assertNotEquals(clanWon,playerWon,"The two terminal transactions cannot both claim one life");
    var dead=store.loadOrCreateSpawn(target.key());
    assertEquals(0,dead.health());assertNotNull(dead.diedAt());
    assertEquals(clanWon ? "CLAN" : "PLAYER",dead.deathKind());
    assertEquals(playerWon ? player.id() : null,dead.recipient());
    assertEquals(playerWon ? 7 : 0,store.load(key.accountId(),player.id()).experience());
    var sourceAfter=store.loadOrCreateSpawn(source.key());
    assertEquals(clanWon ? sourceReady.stateRevision()+1 : sourceReady.stateRevision(),sourceAfter.stateRevision(),
        "A failed pair transaction cannot consume its attack cooldown");
    if(clanWon) assertThrows(OnlineStore.Conflict.class,()->store.commitWorldReward(session,player.revision(),
        target.eventId(),0,7,progress),"A clan terminal life cannot receive a later player reward");
    assertThrows(OnlineStore.Conflict.class,()->store.checkpointSpawn(sourceMotion,null),
        "A queued stale checkpoint cannot restore an old target life");
    var next=store.respawn(target.key(),target.eventId(),dead.stateRevision(),0);
    assertNotEquals(target.eventId(),next.eventId());assertNull(next.health());
    assertNull(next.deathKind());assertNull(next.diedAt());
    assertThrows(OnlineStore.Conflict.class,()->store.commitClanAttack(clan),
        "The old target life cannot be struck after respawn");
    store.release(session);
  }

  @Test void damageAndRewardCommitTogetherAndCompetingStaleDamageCannotGrantXp() throws Exception {
    var life=store.loadOrCreateSpawn("damage:"+UUID.randomUUID());
    var key=store.provisionAccount();
    var one=store.acquire(key.secret(),store.create(key.accountId(),"Damage one",0,10,20).id());
    var two=store.acquire(key.secret(),store.create(key.accountId(),"Damage two",0,10,20).id());
    var progress=CharacterProgressTest.sample();
    var strike=new OnlineStore.CreatureStrike(life.key(),life.eventId(),20,20,12);
    UUID event=UUID.randomUUID();
    var start=new java.util.concurrent.CountDownLatch(1);
    boolean firstWon;
    try (var executor=Executors.newFixedThreadPool(2)) {
      var actions=new ArrayList<java.util.concurrent.Future<Boolean>>();
      for (var session:java.util.List.of(one,two)) actions.add(executor.submit(() -> {
        start.await();
        try { return store.commitCreatureStrike(session,0,event,0,8,progress,strike).applied(); }
        catch (OnlineStore.Conflict stale) { return false; }
      }));
      start.countDown(); firstWon=actions.get(0).get();
      assertNotEquals(firstWon,actions.get(1).get());
    }
    var winner=firstWon ? one : two; var loser=firstWon ? two : one;
    var restarted=OnlineStore.fromEnvironment();
    assertEquals(12,restarted.loadOrCreateSpawn(life.key()).health());
    assertEquals(loser.character(),restarted.load(key.accountId(),loser.characterId()));
    assertEquals(8,restarted.load(key.accountId(),winner.characterId()).experience());
    assertFalse(restarted.commitCreatureStrike(winner,0,event,0,8,progress,strike).applied());
    assertThrows(OnlineStore.Conflict.class,() -> restarted.commitCreatureStrike(winner,0,event,0,8,progress,
        new OnlineStore.CreatureStrike(life.key(),life.eventId(),20,20,11)));
    assertThrows(OnlineStore.Conflict.class,() -> restarted.commitGameplayEvent(winner,0,event,0,8,progress));
    assertThrows(OnlineStore.Conflict.class,() -> restarted.respawn(life.key(),life.eventId(),0));

    var before=restarted.reward(winner,UUID.randomUUID(),Long.MAX_VALUE,0).character();
    var lethal=new OnlineStore.CreatureStrike(life.key(),life.eventId(),20,12,0);
    assertThrows(SQLException.class,() -> restarted.commitCreatureStrike(winner,before.revision(),life.eventId(),1,12,progress,lethal));
    assertEquals(before,restarted.load(key.accountId(),winner.characterId()));
    assertEquals(12,restarted.loadOrCreateSpawn(life.key()).health(),"A failed reward must roll back damage too");
    assertNull(restarted.loadOrCreateSpawn(life.key()).recipient());
    assertTrue(restarted.commitCreatureStrike(winner,before.revision(),life.eventId(),0,12,progress,lethal).applied());
    assertEquals(0,restarted.loadOrCreateSpawn(life.key()).health());
    assertEquals(winner.characterId(),restarted.loadOrCreateSpawn(life.key()).recipient());
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
