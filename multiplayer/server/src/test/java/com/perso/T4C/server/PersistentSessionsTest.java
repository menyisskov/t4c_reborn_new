package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.LootDropDraft;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.server.persistence.OnlineStore;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Consequential join between the tick-owned state and the real PostgreSQL transaction queue. */
@EnabledIfEnvironmentVariable(named="T4C_DB_URL", matches=".+")
class PersistentSessionsTest {
  @Test void blockedCorpseLookupsCannotStarveTwentyCharacterWrites() throws Exception {
    var pause=new AtomicBoolean();var entered=new CountDownLatch(2);var release=new CountDownLatch(1);
    var store=new OnlineStore(() -> {
      if(pause.get()) {
        entered.countDown();
        try { if(!release.await(5,TimeUnit.SECONDS)) throw new SQLException("Test lookup gate timed out"); }
        catch(InterruptedException interrupted) { Thread.currentThread().interrupt();throw new SQLException(interrupted); }
      }
      return connection();
    });
    store.migrate();var key=store.provisionAccount();
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var world=new PreviewWorld(new PreviewMap(new boolean[8][8],0,0,3,3));
    var sessions=new PersistentSessions(store,new PreviewMap(new boolean[8][8],0,0,3,3),world);
    var tokens=new java.util.ArrayList<String>();
    try {
      for(int i=0;i<PreviewWorld.MAX_PLAYERS;i++) {
        var character=store.create(key.accountId(),"Lookup contention "+i,0,3.5,3.5,progress,0);
        long started=System.nanoTime();var lease=store.acquire(key.secret(),character.id());
        tokens.add(sessions.join(lease,started).token());
      }
      pause.set(true);
      var lookups=new java.util.ArrayList<CompletableFuture<?>>();
      for(var token:tokens) lookups.add(sessions.corpses(token).toCompletableFuture());
      assertTrue(entered.await(2,TimeUnit.SECONDS));
      var writes=new java.util.ArrayList<CompletableFuture<OnlineStore.RewardResult>>();
      for(var token:tokens) writes.add(sessions.commitGameplayEvent(token,0,UUID.randomUUID(),1,0,progress).toCompletableFuture());
      sessions.step();
      for(var token:tokens) assertNotNull(world.position(token),"Lookup backpressure cannot disconnect an authoritative writer");
      // Repeated polls share the same pending lookup, rather than filling another bounded queue.
      for(var token:tokens) lookups.add(sessions.corpses(token).toCompletableFuture());
      pause.set(false);release.countDown();
      var all=CompletableFuture.allOf(java.util.stream.Stream.concat(writes.stream(),lookups.stream()).toArray(CompletableFuture[]::new));
      await(sessions,all);all.get();
      for(var token:tokens) assertEquals(1,sessions.character(token).gold());
      for(var write:writes) assertTrue(write.get().applied());
      var retiringRead=sessions.corpses(tokens.getFirst()).toCompletableFuture();
      var closed=sessions.leave(tokens.getFirst());
      assertTrue(retiringRead.isCompletedExceptionally(),"Logout completes pending reads even before their worker returns");
      await(sessions,closed);closed.get();
    } finally { pause.set(false);release.countDown();sessions.shutdown(); }
  }

  @Test void incompleteFlushWaitsForAcceptedWorkerExitAndReportsFailure() throws Exception {
    var pause=new AtomicBoolean();var entered=new CountDownLatch(1);
    var interrupted=new CountDownLatch(1);var release=new CountDownLatch(1);
    var store=new OnlineStore(() -> {
      if(pause.get()) {
        entered.countDown();
        for(;;) {
          try { if(release.await(5,TimeUnit.SECONDS)) break;throw new SQLException("Test gate timeout"); }
          catch(InterruptedException signal) { interrupted.countDown(); } // JDBC may not stop on thread interruption.
        }
      }
      return connection();
    });
    store.migrate();var key=store.provisionAccount();
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(4)).progress();
    var saved=store.create(key.accountId(),"Shutdown gate",0,3.5,3.5,progress,0);
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    var sessions=new PersistentSessions(store,map,new PreviewWorld(map));
    long started=System.nanoTime();var lease=store.acquire(key.secret(),saved.id());
    var token=sessions.join(lease,started).token();
    try(var executor=Executors.newSingleThreadExecutor()) {
      pause.set(true);
      var accepted=sessions.commitGameplayEvent(token,0,UUID.randomUUID(),1,0,progress).toCompletableFuture();
      assertTrue(entered.await(2,TimeUnit.SECONDS));
      var stopped=executor.submit(()->sessions.shutdown(TimeUnit.MILLISECONDS.toNanos(1),TimeUnit.SECONDS.toNanos(3)));
      try {
        assertTrue(interrupted.await(2,TimeUnit.SECONDS));
        assertFalse(stopped.isDone(),"Interrupting a JDBC worker is not proof that it stopped");
      } finally { pause.set(false);release.countDown(); }
      var failure=assertThrows(ExecutionException.class,()->stopped.get(4,TimeUnit.SECONDS));
      assertTrue(failure.getCause().getMessage().contains("flush incomplete"));
      assertTrue(accepted.isCompletedExceptionally());
      assertEquals(1,store.load(key.accountId(),saved.id()).gold(),"Unacknowledged commit must be recovered from storage, never guessed or replayed");
    } finally { pause.set(false);release.countDown();sessions.shutdown(); }
  }

  @Test void gameplayWritesDoNotPostponeTheMovementCheckpointDeadline() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate(); var key=store.provisionAccount();
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(3));
    var created=store.create(key.accountId(),"Checkpoint deadline",0,3.5,3.5,prepared.progress(),prepared.gold());
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    var world=new PreviewWorld(map); var sessions=new PersistentSessions(store,map,world);
    long started=System.nanoTime(); var lease=store.acquire(key.secret(),created.id());
    String token=sessions.join(lease,started).token();
    try {
      for (int i=0;i<PreviewWorld.TICK_RATE*5-1;i++) sessions.step();
      var result=sessions.commitWorldReward(token,created.revision(),UUID.randomUUID(),1,0,prepared.progress()).toCompletableFuture();
      world.input(token,0,1,0); world.step();
      double movedX=world.position(token).x();
      // Reach the original deadline while the reward is in flight, then drain at the same tick
      // cadence. Resetting the deadline for the reward would strand this movement until much later.
      long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(4);
      for (int i=0;i<30 && sessions.character(token).x()!=movedX && System.nanoTime()<deadline;i++) {
        sessions.step(); Thread.sleep(100);
      }
      assertEquals(movedX,sessions.character(token).x());
      assertTrue(result.get(1,TimeUnit.SECONDS).applied());
      assertEquals(created.gold()+1,sessions.character(token).gold());
    } finally { sessions.shutdown(); }
  }

  @Test void gameplayWaitsForCheckpointAndLogoutWaitsForGameplayBeforeSavingMovement() throws Exception {
    var pause=new AtomicBoolean();
    var entered=new CountDownLatch(1);
    var release=new CountDownLatch(1);
    var store=new OnlineStore(() -> {
      if (pause.get()) {
        entered.countDown();
        try { if (!release.await(5,TimeUnit.SECONDS)) throw new SQLException("Test gate timeout"); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new SQLException(e); }
      }
      return connection();
    });
    store.migrate(); var key=store.provisionAccount();
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1));
    var created=store.create(key.accountId(),"Serialized gameplay",0,103.5,203.5,prepared.progress(),prepared.gold());
    var map=new PreviewMap(new boolean[8][8],100,200,3,3);
    var world=new PreviewWorld(map); var sessions=new PersistentSessions(store,map,world);
    long started=System.nanoTime(); var lease=store.acquire(key.secret(),created.id());
    String token=sessions.join(lease,started).token();
    var progress=questProgress(prepared.progress());
    try {
      pause.set(true);
      for (int i=0;i<PreviewWorld.TICK_RATE*5;i++) sessions.step();
      assertTrue(entered.await(3,TimeUnit.SECONDS));
      var result=sessions.commitGameplayEvent(token,created.revision(),UUID.randomUUID(),7,11,progress).toCompletableFuture();
      assertFalse(result.isDone());
      assertEquals(created,sessions.character(token),"Uncommitted progress must not be published");
      var busy=assertThrows(PreviewWorld.Rejected.class,() ->
          sessions.commitGameplayEvent(token,created.revision(),UUID.randomUUID(),1,1,progress));
      assertEquals("busy",busy.getMessage());
      world.input(token,0,1,0); world.step(); sessions.step();
      double movedX=map.originX+world.position(token).x();
      assertTrue(movedX>created.x(),"Database stalls must not stop the actor");
      var closed=sessions.leave(token);
      assertFalse(closed.isDone()); assertFalse(result.isDone());
      pause.set(false); release.countDown();
      await(sessions,closed); closed.get();
      assertTrue(result.get().applied());
      var restored=store.load(key.accountId(),created.id());
      assertEquals(progress,restored.progress());
      assertEquals(created.gold()+7,restored.gold()); assertEquals(11,restored.experience());
      assertEquals(movedX,restored.x());
      var replacement=store.acquire(key.secret(),created.id()); // Final save released the lease.
      store.release(replacement);
      assertThrows(PreviewWorld.Rejected.class,() -> sessions.character(token));
    } finally { pause.set(false); release.countDown(); sessions.shutdown(); }
  }

  @Test void lostCommitAcknowledgementDisconnectsAndRecoveryRetriesTheSameEventOnce() throws Exception {
    var loseAck=new AtomicBoolean();
    var store=new OnlineStore(() -> {
      var actual=connection();
      return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},
          (proxy,method,args) -> {
            try {
              Object value=method.invoke(actual,args);
              if (method.getName().equals("commit") && loseAck.compareAndSet(true,false))
                throw new SQLException("Injected lost commit acknowledgement");
              return value;
            } catch (InvocationTargetException e) { throw e.getCause(); }
          });
    });
    store.migrate(); var key=store.provisionAccount();
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(2));
    var created=store.create(key.accountId(),"Uncertain gameplay",0,3.5,3.5,prepared.progress(),prepared.gold());
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    var world=new PreviewWorld(map); var sessions=new PersistentSessions(store,map,world);
    long started=System.nanoTime(); var lease=store.acquire(key.secret(),created.id());
    String token=sessions.join(lease,started).token(); UUID event=UUID.randomUUID();
    var life=store.loadOrCreateSpawn("uncertain-strike:"+UUID.randomUUID());
    var strike=new OnlineStore.CreatureStrike(life.key(),life.eventId(),20,20,12);
    var progress=questProgress(prepared.progress());
    try {
      var stale=assertThrows(PreviewWorld.Rejected.class,() ->
          sessions.commitGameplayEvent(token,created.revision()+1,event,7,11,progress));
      assertEquals("revision",stale.getMessage());
      loseAck.set(true);
      var result=sessions.commitCreatureStrike(token,created.revision(),event,7,11,progress,strike).toCompletableFuture();
      await(sessions,result);
      assertThrows(ExecutionException.class,result::get);
      assertNull(world.position(token));
      assertThrows(PreviewWorld.Rejected.class,() -> sessions.character(token));
      var recovered=store.load(key.accountId(),created.id());
      assertEquals(progress,recovered.progress()); assertEquals(created.gold()+7,recovered.gold());
      assertEquals(12,store.loadOrCreateSpawn(life.key()).health(),"Lost acknowledgement must not undo the rewarded damage");
      // Advance only this failed writer's lease; real lease timing is covered separately.
      try (var c=connection(); var s=c.prepareStatement(
          "UPDATE online_characters SET session_until=clock_timestamp()-interval '1 second' WHERE id=?")) {
        s.setObject(1,created.id()); s.executeUpdate();
      }
      started=System.nanoTime(); var replacement=store.acquire(key.secret(),created.id());
      String rejoined=sessions.join(replacement,started).token();
      var retry=sessions.commitCreatureStrike(rejoined,recovered.revision(),event,7,11,progress,strike).toCompletableFuture();
      await(sessions,retry);
      assertFalse(retry.get().applied());
      assertEquals(recovered,sessions.character(rejoined));
    } finally { sessions.shutdown(); }
  }

  @Test void dropsPublishOnlyAfterTheirCommitAndAClaimTheCacheMissesIsRefusedWithoutDisconnecting() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate(); var key=store.provisionAccount();
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    try(var hosted=hostedFor(store,map)) {
      var sessions=hosted.sessions; var region=hosted.regions.getFirst();
      var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(5));
      var killer=store.create(key.accountId(),"Loot killer",0,3.5,3.5,prepared.progress(),0);
      var looter=store.create(key.accountId(),"Loot picker",0,3.5,3.5,prepared.progress(),0);
      var thief=store.create(key.accountId(),"Loot thief",0,3.5,3.5,prepared.progress(),0);
      String killerToken=sessions.join(store.acquire(key.secret(),killer.id()),System.nanoTime()).token();
      String looterToken=sessions.join(store.acquire(key.secret(),looter.id()),System.nanoTime()).token();
      var life=store.loadOrCreateSpawn("loot-kill:"+UUID.randomUUID());
      var strike=new OnlineStore.CreatureStrike(life.key(),life.eventId(),10,10,0);
      var first=new LootDropDraft(UUID.randomUUID(),0,"item.leather_gloves",1,0,0,3.5,3.5);
      var second=new LootDropDraft(UUID.randomUUID(),1,"item.leather_gloves",1,0,0,3.5,3.5);
      var kill=sessions.commitCreatureStrike(killerToken,killer.revision(),life.eventId(),0,0,prepared.progress(),
          strike,null,List.of(first,second)).toCompletableFuture();
      assertFalse(sessions.groundLootPublished(killerToken,first.id()),"A drop is not published before its kill commits");
      await(sessions,kill);
      assertTrue(kill.get().applied());
      assertTrue(sessions.groundLootPublished(killerToken,first.id()));
      assertTrue(sessions.groundLootPublished(killerToken,second.id()));
      closeOwnerWindow(first.id(),second.id()); // The 60-second owner window is closed in the database for this test.

      // A claim that happens outside this cache is refused by the database, and the actor stays connected.
      var thiefSession=store.acquire(key.secret(),thief.id());
      store.claimGroundLoot(thiefSession,0,UUID.randomUUID(),second.id(),0,3.5,3.5,ItemCatalog.loadBundled());
      store.release(thiefSession);
      var refused=sessions.pickup(looterToken,sessions.character(looterToken).revision(),
          sessions.character(looterToken).locationEpoch(),second.id()).toCompletableFuture();
      assertTrue(sessions.groundLootPublished(looterToken,second.id()),"The stale cache entry is still there until the database answers");
      await(sessions,refused);
      var failure=assertThrows(ExecutionException.class,refused::get);
      assertEquals("loot_unavailable",((PreviewWorld.Rejected)failure.getCause()).getMessage());
      assertNotNull(region.world.position(looterToken),"A definite refusal never disconnects the actor");
      assertFalse(sessions.groundLootPublished(looterToken,second.id()),"A refusal that proves the cache stale removes the drop");

      var claimed=sessions.pickup(looterToken,sessions.character(looterToken).revision(),
          sessions.character(looterToken).locationEpoch(),first.id()).toCompletableFuture();
      assertTrue(sessions.groundLootPublished(looterToken,first.id()),"A claim leaves the cache only after its commit");
      await(sessions,claimed);
      assertTrue(claimed.get().applied());
      assertFalse(sessions.groundLootPublished(looterToken,first.id()));
      assertTrue(claimed.get().character().progress().inventory().stream().anyMatch(item -> item.id().equals(first.id())));
      assertTrue(store.load(key.accountId(),looter.id()).progress().inventory().stream().anyMatch(item -> item.id().equals(first.id())));
    }
  }

  @Test void ambiguousPickupDisconnectsAndReloadsTheRegionWithoutReplay() throws Exception {
    var loseAck=new AtomicBoolean();
    var store=new OnlineStore(() -> {
      var actual=connection();
      return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},
          (proxy,method,args) -> {
            try {
              Object value=method.invoke(actual,args);
              if (method.getName().equals("commit") && loseAck.compareAndSet(true,false))
                throw new SQLException("Injected lost commit acknowledgement");
              return value;
            } catch (InvocationTargetException e) { throw e.getCause(); }
          });
    });
    store.migrate(); var key=store.provisionAccount();
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    try(var hosted=hostedFor(store,map)) {
      var sessions=hosted.sessions; var region=hosted.regions.getFirst();
      var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(6));
      var killer=store.create(key.accountId(),"Ambiguous killer",0,3.5,3.5,prepared.progress(),0);
      var looter=store.create(key.accountId(),"Ambiguous picker",0,3.5,3.5,prepared.progress(),0);
      String killerToken=sessions.join(store.acquire(key.secret(),killer.id()),System.nanoTime()).token();
      var life=store.loadOrCreateSpawn("ambiguous-kill:"+UUID.randomUUID());
      var drop=new LootDropDraft(UUID.randomUUID(),0,"item.leather_gloves",1,0,0,3.5,3.5);
      var kill=sessions.commitCreatureStrike(killerToken,killer.revision(),life.eventId(),0,0,prepared.progress(),
          new OnlineStore.CreatureStrike(life.key(),life.eventId(),10,10,0),null,List.of(drop)).toCompletableFuture();
      await(sessions,kill);
      kill.get();
      closeOwnerWindow(drop.id());
      String looterToken=sessions.join(store.acquire(key.secret(),looter.id()),System.nanoTime()).token();

      loseAck.set(true);
      var pick=sessions.pickup(looterToken,looter.revision(),0,drop.id()).toCompletableFuture();
      await(sessions,pick);
      assertThrows(ExecutionException.class,pick::get);
      assertNull(region.world.position(looterToken),"An uncertain pickup disconnects the actor");
      assertThrows(PreviewWorld.Rejected.class,() -> sessions.character(looterToken));

      // The database holds exactly one committed claim; the disconnected actor did not replay it.
      var committed=store.load(key.accountId(),looter.id());
      assertEquals(1,committed.progress().inventory().stream().filter(item -> item.id().equals(drop.id())).count());
      long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(8);
      while((region.loot.reading() || region.loot.needsRead()) && System.nanoTime()<deadline) { sessions.step(); Thread.sleep(5); }
      assertFalse(region.loot.contains(drop.id()),"The region reloads from the database, so the claimed drop is gone");

      // Rejoining shows the one committed claim; a retry is refused by the database, never applied twice.
      expireLease(looter.id()); // Only the failed writer's lease is advanced; real lease timing is covered separately.
      String rejoined=sessions.join(store.acquire(key.secret(),looter.id()),System.nanoTime()).token();
      assertEquals(1,store.load(key.accountId(),looter.id()).progress().inventory().stream()
          .filter(item -> item.id().equals(drop.id())).count());
      assertThrows(PreviewWorld.Rejected.class,() -> sessions.pickup(rejoined,sessions.character(rejoined).revision(),0,drop.id()));
    }
  }

  @Test void restartLoadsLiveLootBeforeAdmissionAndClaimsItOnce() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate(); var key=store.provisionAccount();
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    var drop=new LootDropDraft(UUID.randomUUID(),0,"item.leather_gloves",1,0,0,3.5,3.5);
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(7));
    var killer=store.create(key.accountId(),"Restart killer",0,3.5,3.5,prepared.progress(),0);
    var looter=store.create(key.accountId(),"Restart picker",0,3.5,3.5,prepared.progress(),0);
    try(var before=hostedFor(store,map)) {
      String killerToken=before.sessions.join(store.acquire(key.secret(),killer.id()),System.nanoTime()).token();
      var life=store.loadOrCreateSpawn("restart-kill:"+UUID.randomUUID());
      var kill=before.sessions.commitCreatureStrike(killerToken,killer.revision(),life.eventId(),0,0,prepared.progress(),
          new OnlineStore.CreatureStrike(life.key(),life.eventId(),10,10,0),null,List.of(drop)).toCompletableFuture();
      await(before.sessions,kill);
      kill.get();
    }
    try(var restarted=hostedFor(store,map)) {
      // The region cache is filled in the constructor, before any session can be admitted.
      assertTrue(restarted.regions.getFirst().loot.loaded());
      assertTrue(restarted.regions.getFirst().loot.contains(drop.id()),"Committed live loot survives a restart");
      closeOwnerWindow(drop.id());
      String token=restarted.sessions.join(store.acquire(key.secret(),looter.id()),System.nanoTime()).token();
      var pick=restarted.sessions.pickup(token,looter.revision(),0,drop.id()).toCompletableFuture();
      await(restarted.sessions,pick);
      assertTrue(pick.get().applied());
    }
    try(var again=hostedFor(store,map)) {
      assertFalse(again.regions.getFirst().loot.contains(drop.id()),"A claimed drop never returns after restart");
      assertEquals(1,store.load(key.accountId(),looter.id()).progress().inventory().stream()
          .filter(item -> item.id().equals(drop.id())).count());
    }
  }

  @Test void wardenTurnInCommitsGoldExperienceSpellAndFlagsOnceAcrossRepeatAndReconnect() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate(); var key=store.provisionAccount();
    var aelric=NpcInteractionCatalog.loadBundled().require("WardenAelric");
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(8));
    var created=store.create(key.accountId(),"Warden turn-in",aelric.world(),aelric.x()+0.5,aelric.y()+0.5,
        wardenReady(prepared.progress()),prepared.gold());
    try(var hosted=startingSlice(store)) {
      var sessions=hosted.sessions;
      var lease=store.acquire(key.secret(),created.id());
      String token=sessions.join(lease,System.nanoTime()).token();
      sessions.regionReady(token,lease.character().locationEpoch());
      var reply=sessions.interact(token,created.revision(),"WardenAelric").toCompletableFuture();
      // Single flight: a second request while the first draft is uncommitted is refused, not queued.
      assertEquals("busy",assertThrows(PreviewWorld.Rejected.class,()->sessions.interact(token,created.revision(),"WardenAelric")).getMessage());
      await(sessions,reply);
      var first=reply.get();
      assertTrue(first.changed());
      assertEquals("quest.renewed_wards.completion",first.messageKey());
      var saved=store.load(key.accountId(),created.id());
      assertEquals(created.gold()+500,saved.gold());
      assertEquals(created.experience()+400,saved.experience());
      assertEquals(2,saved.progress().questFlags().get("quest.renewed_wards.status"));
      assertTrue(saved.progress().spells().contains("spell.renew_armor"));
      assertEquals(first.revision(),saved.revision());

      // A repeat in the same session is a no-op: no second reward and no new revision.
      var repeat=sessions.interact(token,first.revision(),"WardenAelric").toCompletableFuture().get();
      assertFalse(repeat.changed());
      assertEquals(first.revision(),repeat.revision());
      assertEquals(saved.progress(),store.load(key.accountId(),created.id()).progress());

      // Reconnect: the committed completion is what the rejoined actor sees, and a repeat still pays nothing.
      var closed=sessions.leave(token); await(sessions,closed); closed.get();
      String rejoined=sessions.join(store.acquire(key.secret(),created.id()),System.nanoTime()).token();
      sessions.regionReady(rejoined,sessions.character(rejoined).locationEpoch());
      var afterReconnect=sessions.interact(rejoined,first.revision(),"WardenAelric").toCompletableFuture().get();
      assertFalse(afterReconnect.changed());
      var finalState=store.load(key.accountId(),created.id());
      assertEquals(created.gold()+500,finalState.gold(),"The reward is added exactly once across repeat and reconnect");
      assertEquals(created.experience()+400,finalState.experience());
    }
  }

  @Test void ambiguousWardenTurnInDisconnectsWithoutReplayAndRejoinSeesTheOneReward() throws Exception {
    var loseAck=new AtomicBoolean();
    var store=new OnlineStore(() -> {
      var actual=connection();
      return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},
          (proxy,method,args) -> {
            try {
              Object value=method.invoke(actual,args);
              if (method.getName().equals("commit") && loseAck.compareAndSet(true,false))
                throw new SQLException("Injected lost commit acknowledgement");
              return value;
            } catch (InvocationTargetException e) { throw e.getCause(); }
          });
    });
    store.migrate(); var key=store.provisionAccount();
    var aelric=NpcInteractionCatalog.loadBundled().require("WardenAelric");
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(9));
    var created=store.create(key.accountId(),"Warden ambiguous",aelric.world(),aelric.x()+0.5,aelric.y()+0.5,
        wardenReady(prepared.progress()),prepared.gold());
    try(var hosted=startingSlice(store)) {
      var sessions=hosted.sessions; var region=hosted.regions.getFirst();
      var lease=store.acquire(key.secret(),created.id());
      String token=sessions.join(lease,System.nanoTime()).token();
      sessions.regionReady(token,lease.character().locationEpoch());
      loseAck.set(true);
      var reply=sessions.interact(token,created.revision(),"WardenAelric").toCompletableFuture();
      await(sessions,reply);
      assertThrows(ExecutionException.class,reply::get);
      assertNull(region.world.position(token),"An uncertain turn-in disconnects the actor");
      assertThrows(PreviewWorld.Rejected.class,() -> sessions.character(token));

      // The lost acknowledgement hid a commit that did land: exactly one reward, never a replayed second one.
      var committed=store.load(key.accountId(),created.id());
      assertEquals(created.gold()+500,committed.gold());
      assertEquals(2,committed.progress().questFlags().get("quest.renewed_wards.status"));

      expireLease(created.id()); // Only the failed writer's lease is advanced; real lease timing is covered separately.
      String rejoined=sessions.join(store.acquire(key.secret(),created.id()),System.nanoTime()).token();
      sessions.regionReady(rejoined,sessions.character(rejoined).locationEpoch());
      var retry=sessions.interact(rejoined,committed.revision(),"WardenAelric").toCompletableFuture();
      await(sessions,retry);
      assertFalse(retry.get().changed(),"The completed quest is a no-op, so nothing is replayed");
      assertEquals(created.gold()+500,store.load(key.accountId(),created.id()).gold());
    }
  }

  /** Ready Warden Aelric quest flags (status active, twelve kills) on top of a valid warrior. */
  private static CharacterProgress wardenReady(CharacterProgress p) {
    return new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),p.vitals(),p.advancement(),
        p.inventory(),p.equipment(),p.skills(),p.spells(),Map.of("quest.renewed_wards.status",1,"quest.renewed_wards.kills",12),
        p.storage(),p.storageGold(),p.respawn(),p.buffs(),p.cooldowns(),p.hiddenUntil(),p.companion());
  }

  /** The bundled starting slice, so Warden Aelric's authored temple region is hosted with its NPC catalog. */
  private static HostedWorlds startingSlice(OnlineStore store) throws Exception {
    var guard=MonsterCatalog.loadBundled().monsters().get("Olin Haad Guard 3");
    return new HostedWorlds(HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin")),
        store,ItemCatalog.loadBundled(),new MonsterContent(MonsterContent.VERSION,Map.of(guard.getName(),guard),List.of()));
  }

  /** The 60-second owner window is production policy; these tests close it directly in the database. */
  private static void closeOwnerWindow(UUID... loot) throws SQLException {
    try (var c=connection(); var s=c.prepareStatement(
        "UPDATE online_ground_loot SET owner_until=clock_timestamp()-interval '1 second' WHERE id=?")) {
      for (var id:loot) { s.setObject(1,id); s.executeUpdate(); }
    }
  }

  private static void expireLease(UUID character) throws SQLException {
    try (var c=connection(); var s=c.prepareStatement(
        "UPDATE online_characters SET session_until=clock_timestamp()-interval '1 second' WHERE id=?")) {
      s.setObject(1,character); s.executeUpdate();
    }
  }

  private static HostedWorlds hostedFor(OnlineStore store,PreviewMap map) throws Exception {
    var source=MonsterCatalog.loadBundled().monsters();
    var guard=source.get("Olin Haad Guard 3");
    return new HostedWorlds(HostedRegionPlan.single(map),store,ItemCatalog.loadBundled(),
        new MonsterContent(MonsterContent.VERSION,Map.of(guard.getName(),guard),List.of()));
  }

  private static void await(PersistentSessions sessions,CompletableFuture<?> result) throws Exception {
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(8);
    while (!result.isDone() && System.nanoTime()<deadline) { sessions.step(); Thread.sleep(5); }
    assertTrue(result.isDone(),"Persistence completion timed out");
  }

  private static CharacterProgress questProgress(CharacterProgress p) {
    return new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),p.vitals(),p.advancement(),
        p.inventory(),p.equipment(),p.skills(),p.spells(),Map.of("test.quest",1),p.storage(),p.storageGold(),
        p.respawn(),p.buffs(),p.cooldowns(),p.hiddenUntil(),p.companion());
  }

  private static Connection connection() throws SQLException {
    return DriverManager.getConnection(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"));
  }
}
