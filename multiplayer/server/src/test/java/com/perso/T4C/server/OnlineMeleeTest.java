package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterExperience;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
class OnlineMeleeTest {
  @Test void twoPlayersCannotShareAKillAndCooldownSurvivesReconnect() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate();
    var source=MonsterCatalog.loadBundled().monsters().get("Olin Haad Guard 3");
    var gson=new Gson(); var json=gson.toJsonTree(source).getAsJsonObject();
    json.addProperty("health",1); json.addProperty("acMin",0); json.addProperty("acMax",0);
    json.addProperty("dodge",1); json.addProperty("agi",1); json.addProperty("respawnTime",300_000);
    var definition=gson.fromJson(json,MonsterDef.class);
    var nearby=new MonsterContent.Spawn("melee-"+UUID.randomUUID(),definition.getName(),3,3,0,true,false);
    var far=new MonsterContent.Spawn("melee-"+UUID.randomUUID(),definition.getName(),7,7,0,true,false);
    var content=new MonsterContent(MonsterContent.VERSION,Map.of(definition.getName(),definition),List.of(nearby,far));
    var map=new PreviewMap(new boolean[10][10],0,0,3,3); var world=new PreviewWorld(map,content);
    var sessions=new PersistentSessions(store,map,world);
    var key=store.provisionAccount(); var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1));
    var first=store.create(key.accountId(),"Melee one",0,3.5,3.5,prepared.progress(),prepared.gold());
    var second=store.create(key.accountId(),"Melee two",0,3.5,3.5,prepared.progress(),prepared.gold());
    long started=System.nanoTime(); var firstLease=store.acquire(key.secret(),first.id());
    String one=sessions.join(firstLease,started).token();
    started=System.nanoTime(); var secondLease=store.acquire(key.secret(),second.id());
    String two=sessions.join(secondLease,started).token();
    long now=System.currentTimeMillis();
    try(var creatures=new PersistentCreatures(store,world)) {
      // Max attack roll against the fixture's minimum dodge ensures this test exercises a hit.
      var rolls=new Random(1) { @Override public int nextInt(int bound) { return bound-1; } };
      var melee=new OnlineMelee(map,world,sessions,creatures,ItemCatalog.loadBundled(),rolls,() -> now);
      assertEquals("revision",assertThrows(PreviewWorld.Rejected.class,() -> melee.attack(one,99,1)).getMessage());
      assertEquals("out_of_range_or_blocked",assertThrows(PreviewWorld.Rejected.class,() -> melee.attack(one,0,2)).getMessage());
      assertEquals(first,store.load(key.accountId(),first.id()),"Rejected intent cannot mutate the character");
      var attack=melee.attack(one,0,1).toCompletableFuture();
      assertEquals("creature_busy",assertThrows(PreviewWorld.Rejected.class,() -> melee.attack(two,0,1)).getMessage());
      assertEquals(2,world.snapshot(one).creatures().size(),"No predicted death before the transaction completes");
      await(sessions,creatures,attack);
      var saved=attack.get().character();
      assertEquals(MonsterExperience.damageReward(definition.getXpPerHit(),1)+MonsterExperience.reward(definition.getXpOnDeath()),saved.experience());
      assertTrue(saved.gold()>first.gold());
      assertEquals(second,store.load(key.accountId(),second.id()));
      assertEquals(world.snapshot(one).creatures(),world.snapshot(two).creatures());
      assertEquals(1,world.snapshot(two).creatures().size());
      assertEquals("revision",assertThrows(PreviewWorld.Rejected.class,() -> melee.attack(one,0,1)).getMessage());
      assertEquals("cooldown",assertThrows(PreviewWorld.Rejected.class,() -> melee.attack(one,saved.revision(),2)).getMessage());
      var left=sessions.leave(one); await(sessions,creatures,left);
      started=System.nanoTime(); var rejoinedLease=store.acquire(key.secret(),first.id());
      String rejoined=sessions.join(rejoinedLease,started).token();
      assertEquals("cooldown",assertThrows(PreviewWorld.Rejected.class,
          () -> melee.attack(rejoined,rejoinedLease.character().revision(),2)).getMessage());
      assertEquals(saved.experience(),rejoinedLease.character().experience());
    } finally { sessions.shutdown(); }
  }

  private static void await(PersistentSessions sessions,PersistentCreatures creatures,CompletableFuture<?> result) throws Exception {
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
    while(!result.isDone() && System.nanoTime()<deadline) { sessions.step(); creatures.step(); Thread.sleep(5); }
    assertTrue(result.isDone()); result.get();
  }
}
