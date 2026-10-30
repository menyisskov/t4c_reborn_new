package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
class OnlineRetaliationTest {
  @Test void authoredHealSeriousHealsMonsterToMaximumAndPersistsItsCooldown() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate();
    var gson=new Gson();
    var json=gson.toJsonTree(MonsterCatalog.loadBundled().monsters().get("Olin Haad Guard 3")).getAsJsonObject();
    json.addProperty("health",2000);
    json.addProperty("level",1000);
    json.add("attacks",JsonParser.parseString("[{\"name\":\"\",\"value1\":1000000,\"value2\":100,\"value3\":10616,\"value4\":0,\"value5\":100}]") );
    var definition=gson.fromJson(json,MonsterDef.class);
    assertTrue(OnlineRetaliation.supported(definition));
    var spawn=new MonsterContent.Spawn("heal-retaliation-"+UUID.randomUUID(),definition.getName(),3,3,0,false,false);
    var map=new PreviewMap(new boolean[10][10],0,0,1,1);
    var world=new PreviewWorld(map,new MonsterContent(MonsterContent.VERSION,Map.of(definition.getName(),definition),List.of(spawn)));
    var sessions=new PersistentSessions(store,map,world);
    var account=store.provisionAccount();
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var target=store.create(account.accountId(),"Heal retaliation target",0,3.5,3.5,progress,0);
    long started=System.nanoTime();var lease=store.acquire(account.secret(),target.id());
    var token=sessions.join(lease,started).token();
    var rolls=new Random(1) { @Override public int nextInt(int bound) { return bound-1; } };
    var items=ItemCatalog.loadBundled();
    try {
      OnlineStore.SpawnState injured;
      try(var creatures=new PersistentCreatures(store,world)) {
        var melee=new OnlineMelee(map,world,sessions,creatures,items,rolls,System::currentTimeMillis);
        var hit=melee.attack(token,0,1).toCompletableFuture();
        await(sessions,creatures,hit::isDone);hit.get();
        await(sessions,creatures,()->available(creatures));
        injured=creatures.life(1);
        assertNotNull(injured.health());
        assertTrue(injured.health()>0 && injured.health()<definition.getHealth(),"A real player hit damages the monster before healing");
        int playerHp=store.load(account.accountId(),target.id()).progress().vitals().hp();
        var retaliation=new OnlineRetaliation(map,world,sessions,creatures,items,rolls,System::currentTimeMillis);
        Thread.sleep(Math.max(0,injured.nextAttackAt()-System.currentTimeMillis())+30);
        retaliation.step();
        await(sessions,creatures,()->available(creatures));
        var healed=creatures.life(1);
        assertEquals(definition.getHealth(),healed.health(),"Heal Serious caps at authored maximum HP");
        assertTrue(healed.nextAttackAt()>injured.nextAttackAt(),"The heal commits the next attack deadline");
        assertTrue(healed.stateRevision()>injured.stateRevision());
        assertEquals(playerHp,store.load(account.accountId(),target.id()).progress().vitals().hp(),"A self-heal never damages its target player");
      }
      try(var reloaded=new PersistentCreatures(store,world)) {
        var afterRestart=reloaded.life(1);
        assertEquals(definition.getHealth(),afterRestart.health());
        assertEquals("RETALIATION",afterRestart.mode());
        assertTrue(afterRestart.nextAttackAt()>injured.nextAttackAt());
      }
    } finally { sessions.shutdown(); }
  }

  @Test void authoredElementalSpellRetaliationCommitsBalancedResistedHealth() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate();
    var gson=new Gson();
    var json=gson.toJsonTree(MonsterCatalog.loadBundled().monsters().get("Olin Haad Guard 3")).getAsJsonObject();
    json.addProperty("health",100000);
    json.add("attacks",JsonParser.parseString("[{\"name\":\"\",\"value1\":1000000,\"value2\":100,\"value3\":10086,\"value4\":0,\"value5\":1}]"));
    var definition=gson.fromJson(json,MonsterDef.class);
    assertTrue(OnlineRetaliation.supported(definition));
    var spawn=new MonsterContent.Spawn("spell-retaliation-"+UUID.randomUUID(),definition.getName(),3,3,0,false,false);
    var map=new PreviewMap(new boolean[10][10],0,0,1,1);
    var world=new PreviewWorld(map,new MonsterContent(MonsterContent.VERSION,Map.of(definition.getName(),definition),List.of(spawn)));
    var sessions=new PersistentSessions(store,map,world);
    var account=store.provisionAccount();
    var original=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var progressJson=gson.toJsonTree(original).getAsJsonObject();
    progressJson.getAsJsonObject("vitals").addProperty("hp",1500);
    progressJson.getAsJsonObject("vitals").addProperty("maxHp",1500);
    progressJson.add("respawn",JsonParser.parseString("{\"world\":0,\"x\":1.5,\"y\":1.5}"));
    progressJson.add("buffs",gson.toJsonTree(List.of(new CharacterProgress.Buff(
        "spell.test_water_resistance",Long.MAX_VALUE,0,0,Map.of("resist:water",100),0))));
    var progress=gson.fromJson(progressJson,CharacterProgress.class);
    var target=store.create(account.accountId(),"Spell retaliation target",0,3.5,3.5,progress,0);
    long started=System.nanoTime(); var lease=store.acquire(account.secret(),target.id());
    var token=sessions.join(lease,started).token();
    try(var creatures=new PersistentCreatures(store,world)) {
      var rolls=new Random(1) { @Override public int nextInt(int bound) { return bound-1; } };
      var melee=new OnlineMelee(map,world,sessions,creatures,ItemCatalog.loadBundled(),rolls,System::currentTimeMillis);
      var provoke=melee.attack(token,0,1).toCompletableFuture();
      await(sessions,creatures,provoke::isDone); provoke.get();
      await(sessions,creatures,() -> available(creatures));
      assertEquals(target.id(),creatures.life(1).targetCharacter());

      var retaliation=new OnlineRetaliation(map,world,sessions,creatures,ItemCatalog.loadBundled(),rolls,System::currentTimeMillis);
      Thread.sleep(OnlineRetaliation.COOLDOWN_MILLIS+30);
      retaliation.step(); retaliation.step();
      assertEquals(1500,world.position(token).hp(),"Predicted health is not published before commit completion");
      await(sessions,creatures,() -> sessions.character(token).progress().vitals().hp()<1500);

      int raw=OnlineMonsterSpellAttacks.rawDamage(definition,10086,new Random(1) {
        @Override public int nextInt(int bound) { return bound-1; }
      });
      int expected=OnlineMonsterSpellAttacks.elementalDamage(
          OnlineMonsterSpellAttacks.supportedSpell(10086),definition,raw,
          sessions.character(token).progress(),ItemCatalog.loadBundled(),System.currentTimeMillis());
      assertTrue(expected>0);
      assertEquals(1500-expected,world.position(token).hp());
      assertEquals(world.position(token).hp(),store.load(account.accountId(),target.id()).progress().vitals().hp());
    } finally { sessions.shutdown(); }
  }

  @Test void retaliationPublishesCommittedHealthOnlyAndDeathRecoversAcrossLogout() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate();
    var gson=new Gson();
    var json=gson.toJsonTree(MonsterCatalog.loadBundled().monsters().get("Olin Haad Guard 3")).getAsJsonObject();
    json.addProperty("health",100000);
    json.add("attacks",JsonParser.parseString("[{\"name\":\"1d1+1000\",\"value1\":1000000,\"value2\":100}]"));
    var definition=gson.fromJson(json,MonsterDef.class);
    var spawn=new MonsterContent.Spawn("retaliation-"+UUID.randomUUID(),definition.getName(),3,3,0,false,false);
    var map=new PreviewMap(new boolean[10][10],0,0,1,1);
    var world=new PreviewWorld(map,new MonsterContent(MonsterContent.VERSION,Map.of(definition.getName(),definition),List.of(spawn)));
    var sessions=new PersistentSessions(store,map,world);
    var account=store.provisionAccount();
    var original=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var progressJson=gson.toJsonTree(original).getAsJsonObject();
    progressJson.getAsJsonObject("vitals").addProperty("hp",1500);
    progressJson.getAsJsonObject("vitals").addProperty("maxHp",1500);
    progressJson.add("respawn",JsonParser.parseString("{\"world\":0,\"x\":1.5,\"y\":1.5}"));
    var progress=gson.fromJson(progressJson,CharacterProgress.class);
    var first=store.create(account.accountId(),"Retaliation target",0,3.5,3.5,progress,0);
    var second=store.create(account.accountId(),"Retaliation observer",0,3.5,3.5,progress,0);
    long started=System.nanoTime(); var lease=store.acquire(account.secret(),first.id());
    var one=sessions.join(lease,started).token();
    started=System.nanoTime(); var observerLease=store.acquire(account.secret(),second.id());
    var two=sessions.join(observerLease,started).token();
    try(var creatures=new PersistentCreatures(store,world)) {
      var rolls=new Random(1) { @Override public int nextInt(int bound) { return bound-1; } };
      var melee=new OnlineMelee(map,world,sessions,creatures,ItemCatalog.loadBundled(),rolls,System::currentTimeMillis);
      var provoke=melee.attack(one,0,1).toCompletableFuture();
      await(sessions,creatures,provoke::isDone); provoke.get();
      await(sessions,creatures,() -> available(creatures));
      assertEquals(first.id(),creatures.life(1).targetCharacter(),"A real player attack persists provocation");
      var retaliation=new OnlineRetaliation(map,world,sessions,creatures,ItemCatalog.loadBundled(),rolls,System::currentTimeMillis);
      retaliation.step(); sessions.step();
      assertEquals(1,sessions.character(one).revision(),"Provocation does not grant an immediate attack");
      Thread.sleep(OnlineRetaliation.COOLDOWN_MILLIS+30);
      retaliation.step(); retaliation.step();
      assertEquals(1500,world.position(one).hp(),"No predicted HP before persistence completion is drained");
      await(sessions,creatures,() -> sessions.character(one).revision()>1);
      int injured=world.position(one).hp();
      assertTrue(injured>0 && injured<1500);
      assertEquals(1500,world.position(two).hp(),"Only the persisted aggro target takes damage");
      assertEquals(injured,store.load(account.accountId(),first.id()).progress().vitals().hp());
      await(sessions,creatures,() -> available(creatures));
      long revision=sessions.character(one).revision();
      retaliation.step(); sessions.step();
      assertEquals(revision,sessions.character(one).revision(),"A second tick cannot bypass the persisted attack deadline");
      // Reconnecting must retain the creature's aggro and deadline.
      var left=sessions.leave(one); await(sessions,creatures,left::isDone); left.get();
      started=System.nanoTime(); var rejoin=store.acquire(account.secret(),first.id());
      var returning=sessions.join(rejoin,started).token();
      world.relocate(returning,7,7);
      Thread.sleep(OnlineRetaliation.COOLDOWN_MILLIS+30);
      retaliation.step(); sessions.step();
      assertEquals(injured,world.position(returning).hp(),"Out-of-range targets are not hit");
      world.relocate(returning,3.5,3.5);
      retaliation.step();
      world.input(returning,1,1,0); world.step();
      assertEquals(3.5,world.position(returning).x(),"Fatal transaction freezes movement but still accepts input");
      // Logout while the fatal transaction is in flight must checkpoint the committed respawn location.
      var deathLogout=sessions.leave(returning);
      await(sessions,creatures,deathLogout::isDone); deathLogout.get();
      var deadSaved=store.load(account.accountId(),first.id());
      assertEquals(1.5,deadSaved.x()); assertEquals(1.5,deadSaved.y());
      assertTrue(deadSaved.progress().vitals().hp()>0);
      assertEquals(second,store.load(account.accountId(),second.id()));
      await(sessions,creatures,() -> available(creatures));
      assertNull(creatures.life(1).targetCharacter(),"Death clears persisted aggro");
    } finally { sessions.shutdown(); }
  }

  @Test void applicationClockAheadOfDatabaseDeadlineDefersAttackWithoutDisconnect() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate();
    var gson=new Gson();
    var json=gson.toJsonTree(MonsterCatalog.loadBundled().monsters().get("Olin Haad Guard 3")).getAsJsonObject();
    json.addProperty("health",100000);
    json.add("attacks",JsonParser.parseString("[{\"name\":\"1d1+1000\",\"value1\":1000000,\"value2\":100}]"));
    var definition=gson.fromJson(json,MonsterDef.class);
    var spawn=new MonsterContent.Spawn("clock-skew-retaliation-"+UUID.randomUUID(),definition.getName(),3,3,0,false,false);
    var map=new PreviewMap(new boolean[10][10],0,0,1,1);
    var world=new PreviewWorld(map,new MonsterContent(MonsterContent.VERSION,Map.of(definition.getName(),definition),List.of(spawn)));
    var sessions=new PersistentSessions(store,map,world);
    var account=store.provisionAccount();
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var progressJson=gson.toJsonTree(progress).getAsJsonObject();
    progressJson.getAsJsonObject("vitals").addProperty("hp",1500);
    progressJson.getAsJsonObject("vitals").addProperty("maxHp",1500);
    progressJson.add("respawn",JsonParser.parseString("{\"world\":0,\"x\":1.5,\"y\":1.5}"));
    progress=gson.fromJson(progressJson,CharacterProgress.class);
    var character=store.create(account.accountId(),"Clock skew target",0,3.5,3.5,progress,0);
    long started=System.nanoTime(); var lease=store.acquire(account.secret(),character.id());
    var token=sessions.join(lease,started).token();
    try(var creatures=new PersistentCreatures(store,world)) {
      var rolls=new Random(1) { @Override public int nextInt(int bound) { return bound-1; } };
      var melee=new OnlineMelee(map,world,sessions,creatures,ItemCatalog.loadBundled(),rolls,System::currentTimeMillis);
      var provoke=melee.attack(token,0,1).toCompletableFuture();
      await(sessions,creatures,provoke::isDone); provoke.get();
      await(sessions,creatures,() -> available(creatures));
      var before=creatures.life(1);
      long persistedDeadline=before.nextAttackAt();
      assertTrue(persistedDeadline>System.currentTimeMillis());

      var retaliation=new OnlineRetaliation(map,world,sessions,creatures,ItemCatalog.loadBundled(),rolls,
          () -> persistedDeadline+60_000);
      retaliation.step();
      await(sessions,creatures,() -> available(creatures));

      var after=store.load(account.accountId(),character.id());
      var afterSpawn=creatures.life(1);
      assertTrue(sessions.belongs(token,world),"A not-yet-due attack is deferred without disconnecting the target");
      assertEquals(1,after.revision(),"A deferred attack does not revise the character");
      assertEquals(1500,after.progress().vitals().hp(),"A deferred attack does not alter health");
      assertEquals(persistedDeadline,afterSpawn.nextAttackAt(),"A deferred attack preserves its persisted deadline");
      assertEquals(before.stateRevision(),afterSpawn.stateRevision(),"A deferred attack preserves the spawn CAS revision");
      assertEquals(character.id(),afterSpawn.targetCharacter(),"A deferred attack preserves aggro");
    } finally { sessions.shutdown(); }
  }

  private static boolean available(PersistentCreatures creatures) {
    try { creatures.life(1); return true; } catch(PreviewWorld.Rejected busy) { return false; }
  }
  private static void await(PersistentSessions sessions,PersistentCreatures creatures,BooleanSupplier done) throws Exception {
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
    while(!done.getAsBoolean() && System.nanoTime()<deadline) { sessions.step(); creatures.step(); Thread.sleep(5); }
    assertTrue(done.getAsBoolean());
  }
}
