package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.server.persistence.OnlineStore;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named="T4C_DB_URL", matches=".+")
class PersistentCreaturesTest {
  @Test void uncertainCommittedClanPairReloadsBothLivesWithoutReplayingTheStrike() throws Exception {
    var failAfterCommit=new AtomicBoolean();
    var store=new OnlineStore(() -> {
      var actual=DriverManager.getConnection(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),
          System.getenv("T4C_DB_PASSWORD"));
      return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},
          (proxy,method,args) -> {
            try {
              Object result=method.invoke(actual,args);
              if(method.getName().equals("close") && Thread.currentThread().getName().startsWith("creature-persistence-")
                  && failAfterCommit.compareAndSet(true,false)) throw new SQLException("Injected ambiguous pair commit");
              return result;
            } catch(InvocationTargetException error) { throw error.getCause(); }
          });
    });
    store.migrate();
    var definition=MonsterCatalog.loadBundled().monsters().get("Olin Haad Guard 3");
    var source=new MonsterContent.Spawn("pair-source-"+UUID.randomUUID(),definition.getName(),3,3,0,false,false);
    var target=new MonsterContent.Spawn("pair-target-"+UUID.randomUUID(),definition.getName(),4,3,0,false,false);
    var targetLife=store.loadOrCreateSpawn(target.persistenceKey());
    var sourceLife=store.loadOrCreateSpawn(source.persistenceKey());
    sourceLife=store.checkpointSpawn(new OnlineStore.SpawnMotion(sourceLife.key(),sourceLife.eventId(),
        sourceLife.stateRevision(),3,3,"CLAN",null,null,targetLife.key(),targetLife.eventId()),null);
    var content=new MonsterContent(MonsterContent.VERSION,Map.of(definition.getName(),definition),List.of(source,target));
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    var world=new PreviewWorld(map,content);
    try(var creatures=new PersistentCreatures(store,world,map,"start",null,ItemCatalog.loadBundled())) {
      var capturedSource=creatures.capture(1);var capturedTarget=creatures.capture(2);
      int maximum=definition.getHealth();
      long deadline=System.currentTimeMillis()+OnlineRetaliation.COOLDOWN_MILLIS;
      failAfterCommit.set(true);
      assertTrue(creatures.submitClanAttack(1,2,new OnlineStore.ClanAttack(capturedSource,capturedTarget,
          maximum,maximum,maximum-1,0,deadline)));
      long waitUntil=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
      OnlineStore.SpawnState publishedSource=null,publishedTarget=null;
      while(System.nanoTime()<waitUntil && (publishedSource==null || publishedTarget==null)) {
        creatures.step();
        try {publishedSource=creatures.life(1);publishedTarget=creatures.life(2);}
        catch(PreviewWorld.Rejected reloading) {publishedSource=null;publishedTarget=null;}
        Thread.sleep(5);
      }
      assertFalse(failAfterCommit.get(),"The worker must encounter the injected post-commit ambiguity");
      assertNotNull(publishedSource);assertNotNull(publishedTarget);
      assertEquals(sourceLife.eventId(),publishedSource.eventId());
      assertEquals(targetLife.eventId(),publishedTarget.eventId());
      assertEquals(deadline,publishedSource.nextAttackAt());
      assertEquals(maximum-1,publishedTarget.health());
      assertEquals(publishedSource,store.loadOrCreateSpawn(source.persistenceKey()));
      assertEquals(publishedTarget,store.loadOrCreateSpawn(target.persistenceKey()));
    }
  }

  @Test void gracefulCloseReloadsCommittedStateAfterOneUncertainReadWithoutReplayingIt() throws Exception {
    var failNext=new AtomicBoolean();var failureSeen=new CountDownLatch(1);
    var store=new OnlineStore(() -> {
      if(Thread.currentThread().getName().startsWith("creature-persistence-")
          && failNext.compareAndSet(true,false)) {
        failureSeen.countDown();throw new SQLException("Injected one-time read failure");
      }
      return DriverManager.getConnection(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),
          System.getenv("T4C_DB_PASSWORD"));
    });
    store.migrate();
    var definition=MonsterCatalog.loadBundled().monsters().get("Olin Haad Guard 3");
    var spawn=new MonsterContent.Spawn("close-recover-"+UUID.randomUUID(),definition.getName(),3,3,0,false,false);
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    var world=new PreviewWorld(map,new MonsterContent(MonsterContent.VERSION,
        Map.of(definition.getName(),definition),List.of(spawn)));
    try(var creatures=new PersistentCreatures(store,world)) {
      var life=creatures.life(1);
      failNext.set(true);creatures.damageCommitted(1);
      assertTrue(failureSeen.await(2,TimeUnit.SECONDS));
      for(int i=0;i<10;i++) { creatures.step();Thread.sleep(5); }
      assertEquals(life.eventId(),store.loadOrCreateSpawn(spawn.persistenceKey()).eventId());
      // close() must perform a read-only recovery despite the normal tick cadence stopping.
    }
  }

  @Test void hostedChasePublishesCurrentPoseAndGracefulRestartRecoversSameLife() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var definition=MonsterCatalog.loadBundled().monsters().get("Skeleton");
    var spawn=new MonsterContent.Spawn("pursuit-"+UUID.randomUUID(),definition.getName(),5,8,0,false,true);
    var content=new MonsterContent(MonsterContent.VERSION,Map.of(definition.getName(),definition),List.of(spawn));
    var map=new PreviewMap(new boolean[20][20],0,0,12,8);
    var plan=HostedRegionPlan.single(map);
    var key=store.provisionAccount();
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1));
    var character=store.create(key.accountId(),"Pursuit target",0,12.5,8.5,prepared.progress(),prepared.gold());
    UUID life;
    double beforeX, beforeY;
    try(var hosted=new HostedWorlds(plan,store,ItemCatalog.loadBundled(),content)) {
      var region=hosted.regions.getFirst();
      var lease=store.acquire(key.secret(),character.id());
      hosted.sessions.join(lease,System.nanoTime());
      var initial=region.world.creaturePosition(1);
      boolean advanced=false;
      for(int tick=0;tick<240;tick++) {
        hosted.step();
        var position=region.world.creaturePosition(1);
        if(position.x()>initial.x()+0.2) advanced=true;
        Thread.sleep(2);
      }
      assertTrue(advanced,"An eligible nearby player must pull the authored creature off its anchor");
      beforeX=region.world.creaturePosition(1).x();beforeY=region.world.creaturePosition(1).y();
      life=store.loadOrCreateSpawn(spawn.persistenceKey()).eventId();
    }
    var committed=store.loadOrCreateSpawn(spawn.persistenceKey());
    assertEquals(life,committed.eventId());
    assertNotNull(committed.currentX());assertNotNull(committed.currentY());
    assertEquals(beforeX,committed.currentX(),1e-6);assertEquals(beforeY,committed.currentY(),1e-6);
    try(var restarted=new HostedWorlds(plan,store,ItemCatalog.loadBundled(),content)) {
      var recovered=restarted.regions.getFirst().world.creaturePosition(1);
      assertEquals(committed.currentX(),recovered.x(),1e-6);
      assertEquals(committed.currentY(),recovered.y(),1e-6);
      assertEquals(life,restarted.regions.getFirst().creatures.life(1).eventId());
    }
  }

  @Test void deathFencesAnInFlightReadWhoseCachedTargetIsStillUnknown() throws Exception {
    var pause=new AtomicBoolean(); var captured=new CountDownLatch(1); var release=new CountDownLatch(1);
    var store=new OnlineStore(() -> {
      var actual=DriverManager.getConnection(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"));
      return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},
          (proxy,method,args) -> {
            try {
              Object result=method.invoke(actual,args);
              // loadOrCreateSpawn has materialized its return value before closing the connection.
              if(method.getName().equals("close") && Thread.currentThread().getName().startsWith("creature-persistence-")
                  && pause.compareAndSet(true,false)) {
                captured.countDown();
                if(!release.await(5,TimeUnit.SECONDS)) throw new SQLException("Test read gate timed out");
              }
              return result;
            } catch(InvocationTargetException failure) { throw failure.getCause(); }
          });
    });
    store.migrate();
    var definition=MonsterCatalog.loadBundled().monsters().get("Olin Haad Guard 3");
    var watched=new MonsterContent.Spawn("stale-read-"+UUID.randomUUID(),definition.getName(),3,3,0,false,false);
    var killer=new MonsterContent.Spawn("death-source-"+UUID.randomUUID(),definition.getName(),4,3,0,false,false);
    var content=new MonsterContent(MonsterContent.VERSION,Map.of(definition.getName(),definition),List.of(watched,killer));
    var world=new PreviewWorld(new PreviewMap(new boolean[8][8],0,0,3,3),content);
    var key=store.provisionAccount();
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var character=store.create(key.accountId(),"Death read fence",0,3.5,3.5,progress,0);
    var session=store.acquire(key.secret(),character.id());
    try(var creatures=new PersistentCreatures(store,world)) {
      try {
        var watchedLife=creatures.life(1); var killerLife=creatures.life(2);
        assertNull(watchedLife.targetCharacter());
        var provoked=store.commitGameplayEvent(session,character.revision(),UUID.randomUUID(),0,0,progress,
            new OnlineStore.CreatureProvocation(watchedLife.key(),watchedLife.eventId(),character.id(),0)).character();
        provoked=store.commitGameplayEvent(session,provoked.revision(),UUID.randomUUID(),0,0,progress,
            new OnlineStore.CreatureProvocation(killerLife.key(),killerLife.eventId(),character.id(),0)).character();
        pause.set(true); creatures.damageCommitted(1);
        assertTrue(captured.await(3,TimeUnit.SECONDS),"Worker captured new aggro while cached target remains null");
        var position=new com.perso.T4C.online.model.CharacterProgress.Respawn(0,3.5,3.5);
        var death=OnlineDeath.prepare(provoked,ItemCatalog.loadBundled(),position,position,new Random(1));
        store.commitMonsterAttack(session,provoked.revision(),UUID.randomUUID(),death.progress(),
            new OnlineStore.MonsterAttack(killerLife.key(),killerLife.eventId(),character.id(),0,System.currentTimeMillis()+1500),death.transition());
        creatures.characterDied(character.id());
        release.countDown();
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(4);
        OnlineStore.SpawnState published=null;
        while(published==null && System.nanoTime()<deadline) {
          creatures.step();
          try { published=creatures.life(1); } catch(PreviewWorld.Rejected loading) { Thread.sleep(5); }
        }
        assertNotNull(published);
        assertNull(published.targetCharacter(),"A pre-death read must not restore aggro cleared by the committed death");
        assertEquals(watchedLife.eventId(),published.eventId(),"Refreshing aggro cannot invent a new creature life");
      } finally { pause.set(false); release.countDown(); }
    } finally { store.release(session); }
  }

  @Test void recoveryHidesClaimedLifeAndWorkerPublishesOnlyCommittedRespawn() throws Exception {
    var block=new AtomicBoolean(); var entered=new CountDownLatch(1); var release=new CountDownLatch(1);
    var store=new OnlineStore(() -> {
      if (block.get()) {
        assertTrue(Thread.currentThread().getName().startsWith("creature-persistence-"));
        entered.countDown();
        try { if (!release.await(4,TimeUnit.SECONDS)) throw new java.sql.SQLException("Test gate timed out"); }
        catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new java.sql.SQLException(error); }
      }
      return DriverManager.getConnection(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"));
    });
    store.migrate();
    var gson=new Gson();
    var bundled=MonsterCatalog.loadBundled();
    var authored=bundled.region(0,2904,1029,80,60).getFirst();
    var source=bundled.monsters().get(authored.definition());
    assertNotNull(source);
    var json=gson.toJsonTree(source).getAsJsonObject(); json.addProperty("respawnTime",0);
    var definition=gson.fromJson(json,MonsterDef.class);
    var spawn=new MonsterContent.Spawn("qa-"+UUID.randomUUID(),definition.getName(),3,3,0,true,false);
    var content=new MonsterContent(MonsterContent.VERSION,Map.of(definition.getName(),definition),List.of(spawn));
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    var life=store.loadOrCreateSpawn(spawn.persistenceKey());
    var key=store.provisionAccount();
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1));
    var character=store.create(key.accountId(),"Creature recovery",0,3.5,3.5,prepared.progress(),prepared.gold());
    var session=store.acquire(key.secret(),character.id());
    store.commitWorldReward(session,character.revision(),life.eventId(),1,1,prepared.progress());
    var world=new PreviewWorld(map,content);
    var one=world.join(); var two=world.join();
    try (var creatures=new PersistentCreatures(store,world)) {
      assertTrue(world.snapshot(one.token()).creatures().isEmpty());
      assertTrue(world.snapshot(two.token()).creatures().isEmpty());
      block.set(true);
      for(int i=0;i<PreviewWorld.TICK_RATE;i++) creatures.step();
      assertTrue(entered.await(2,TimeUnit.SECONDS));
      world.input(one.token(),0,1,0); world.step();
      assertTrue(world.position(one.token()).x()>3.5,"Storage cannot block simulation movement");
      assertTrue(world.snapshot(one.token()).creatures().isEmpty(),"Do not predict a respawn before commit");
      block.set(false); release.countDown();
      long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(4);
      while(world.snapshot(one.token()).creatures().isEmpty() && System.nanoTime()<deadline) { creatures.step(); Thread.sleep(10); }
      assertEquals(1,world.snapshot(one.token()).creatures().size());
      assertEquals(world.snapshot(one.token()).creatures(),world.snapshot(two.token()).creatures());
      var next=creatures.life(1);
      assertEquals(life.generation()+1,next.generation());
      assertNotEquals(life.eventId(),next.eventId());
      var recoveredWorld=new PreviewWorld(map,content);
      try(var recovered=new PersistentCreatures(store,recoveredWorld)) { assertEquals(next,recovered.life(1)); }
      assertFalse(store.commitWorldReward(session,character.revision(),life.eventId(),1,1,prepared.progress()).applied());
      var current=store.load(key.accountId(),character.id());
      int maximum=definition.getHealth();
      assertTrue(maximum>1);
      var damaged=store.commitCreatureStrike(session,current.revision(),UUID.randomUUID(),0,1,prepared.progress(),
          new OnlineStore.CreatureStrike(next.key(),next.eventId(),maximum,maximum,maximum-1)).character();
      creatures.damageCommitted(1);
      assertEquals(1,world.snapshot(one.token()).creatures().size(),"Partial damage cannot make the actor disappear");
      deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(4);
      while(world.snapshot(one.token()).creatures().getFirst().health()==maximum && System.nanoTime()<deadline) {
        creatures.step(); Thread.sleep(10);
      }
      assertEquals(maximum-1,world.snapshot(one.token()).creatures().getFirst().health());
      assertEquals(world.snapshot(one.token()).creatures(),world.snapshot(two.token()).creatures());
      var damagedWorld=new PreviewWorld(map,content);
      try(var recovered=new PersistentCreatures(store,damagedWorld)) {
        assertEquals(maximum-1,recovered.life(1).health());
        assertEquals(maximum-1,damagedWorld.snapshot(damagedWorld.join().token()).creatures().getFirst().health());
      }
      store.commitCreatureStrike(session,damaged.revision(),next.eventId(),1,1,prepared.progress(),
          new OnlineStore.CreatureStrike(next.key(),next.eventId(),maximum,maximum-1,0));
      creatures.rewardCommitted(1);
      assertTrue(world.snapshot(one.token()).creatures().isEmpty(),"A live committed kill hides before another command can target it");
      assertThrows(PreviewWorld.Rejected.class,() -> creatures.life(1));
    } finally { block.set(false); release.countDown(); }
    assertEquals(spawn.persistenceKey(),new MonsterContent.Spawn(spawn.type(),null,3,3,0,false,true).persistenceKey(),
        "Behavior/lookup changes cannot silently reset a durable spawn life");
    assertThrows(IllegalArgumentException.class,() -> new PreviewWorld(map,
        new MonsterContent(MonsterContent.VERSION,content.monsters(),List.of(spawn,spawn))));
  }
}
