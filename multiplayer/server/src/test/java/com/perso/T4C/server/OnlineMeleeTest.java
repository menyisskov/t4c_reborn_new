package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.item.ItemKeys;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterExperience;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.quest.QuestRules;
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
    var nearby=new MonsterContent.Spawn("melee-"+UUID.randomUUID(),definition.getName(),3,3,0,false,false);
    var far=new MonsterContent.Spawn("melee-"+UUID.randomUUID(),definition.getName(),7,7,0,false,false);
    var protectedActor=new MonsterContent.Spawn("protected-"+UUID.randomUUID(),definition.getName(),3,4,0,true,false);
    var content=new MonsterContent(MonsterContent.VERSION,Map.of(definition.getName(),definition),List.of(nearby,far,protectedActor));
    var map=new PreviewMap(new boolean[10][10],0,0,3,3); var world=new PreviewWorld(map,content);
    var sessions=new PersistentSessions(store,map,world);
    var key=store.provisionAccount(); var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1));
    var story=gson.toJsonTree(prepared.progress()).getAsJsonObject();
    story.getAsJsonObject("questFlags").addProperty(com.perso.T4C.quest.CrimsonscaleLetterRules.LETTER_FLAG,1);
    story.getAsJsonObject("questFlags").addProperty(QuestRules.killLogFlag(definition.getName()),Integer.MAX_VALUE);
    var firstProgress=gson.fromJson(story,com.perso.T4C.online.model.CharacterProgress.class);
    var first=store.create(key.accountId(),"Melee one",0,3.5,3.5,firstProgress,prepared.gold());
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
      var protectedLife=creatures.life(3);
      assertEquals("protected_target",assertThrows(PreviewWorld.Rejected.class,() -> melee.attack(one,0,3)).getMessage());
      assertEquals(protectedLife,creatures.life(3),"Protected targets cannot consume a creature life or change health");
      assertEquals(first,store.load(key.accountId(),first.id()),"Rejected intent cannot mutate the character");
      var attack=melee.attack(one,0,1).toCompletableFuture();
      assertEquals("creature_busy",assertThrows(PreviewWorld.Rejected.class,() -> melee.attack(two,0,1)).getMessage());
      assertEquals(3,world.snapshot(one).creatures().size(),"No predicted death before the transaction completes");
      await(sessions,creatures,attack);
      var saved=attack.get().character();
      assertEquals(MonsterExperience.damageReward(definition.getXpPerHit(),1)+MonsterExperience.reward(definition.getXpOnDeath()),saved.experience());
      assertTrue(saved.gold()>first.gold());
      assertEquals(Integer.MAX_VALUE,saved.progress().questFlags().get(QuestRules.killLogFlag(definition.getName())),
          "An online kill log at its integer limit saturates without blocking the kill");
      assertFalse(saved.progress().questFlags().containsKey("quest.renewed_wards.kills"),
          "A non-objective guard kill grants no quest credit");
      assertEquals(second,store.load(key.accountId(),second.id()));
      assertEquals(world.snapshot(one).creatures(),world.snapshot(two).creatures());
      assertEquals(2,world.snapshot(two).creatures().size());
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

  @Test void lootableSkeletonIsAttackableItsKillCreatesTheRolledDropAndScriptedDefinitionsStayRejected() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate();
    var gson=new Gson();
    var skeleton=MonsterCatalog.loadBundled().monsters().get("Skeleton");
    var json=gson.toJsonTree(skeleton).getAsJsonObject();
    json.addProperty("health",1); json.addProperty("acMin",0); json.addProperty("acMax",0);
    json.addProperty("dodge",1); json.addProperty("agi",1); json.addProperty("respawnTime",300_000);
    // A certain drop keeps the kill deterministic; the authored skeleton table is otherwise unchanged.
    json.add("loot",gson.toJsonTree(List.of(new MonsterDef.LootDrop("Leather gloves",1f))));
    var definition=gson.fromJson(json,MonsterDef.class);
    var scripted=gson.toJsonTree(definition).getAsJsonObject();
    scripted.addProperty("name","Scripted skeleton");
    scripted.add("sourceEvents",gson.toJsonTree(Map.of("kill","quest")));
    var scriptedDefinition=gson.fromJson(scripted,MonsterDef.class);
    // The authored anchor is one tile outside the quest circle; the current death tile is on its edge.
    var lootable=new MonsterContent.Spawn("skeleton-"+UUID.randomUUID(),definition.getName(),2836,1095,0,false,false);
    var authored=new MonsterContent.Spawn("scripted-"+UUID.randomUUID(),scriptedDefinition.getName(),2836,1096,0,false,false);
    var content=new MonsterContent(MonsterContent.VERSION,
        Map.of(definition.getName(),definition,scriptedDefinition.getName(),scriptedDefinition),List.of(lootable,authored));
    var map=new PreviewMap(new boolean[10][10],2831,1090,4,5); var world=new PreviewWorld(map,content);
    var sessions=new PersistentSessions(store,map,world);
    var key=store.provisionAccount(); var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(3));
    var quest=QuestCatalog.bundled().definition("renewed_wards");
    var flags=new HashMap<>(prepared.progress().questFlags());
    flags.putAll(QuestRules.accept(k->flags.getOrDefault(k,0),quest));
    var active=ProgressEdits.withFlags(prepared.progress(),flags);
    var killer=store.create(key.accountId(),"Skeleton killer",0,2835.5,1095.5,active,prepared.gold());
    String token=sessions.join(store.acquire(key.secret(),killer.id()),System.nanoTime()).token();
    var now=new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis());
    try(var creatures=new PersistentCreatures(store,world)) {
      world.creaturePosition(1,4,5); // Current global tile (2835,1095), distinct from the anchor.
      var rolls=new Random(1) { @Override public int nextInt(int bound) { return bound-1; } };
      var melee=new OnlineMelee(map,world,sessions,creatures,ItemCatalog.loadBundled(),rolls,now::get);
      // Scripted definitions are still refused before any damage is dealt, even with an ordinary loot table.
      assertEquals("unsupported_combat",assertThrows(PreviewWorld.Rejected.class,() -> melee.attack(token,0,2)).getMessage());
      var attack=melee.attack(token,0,1).toCompletableFuture();
      await(sessions,creatures,attack);
      var saved=attack.get().character();
      assertEquals(1,saved.progress().questFlags().get(QuestRules.killsFlag(quest)),
          "The first real lethal strike credits the current death tile, not the authored anchor");
      assertEquals(1,saved.progress().questFlags().get(QuestRules.killLogFlag(definition.getName())));
      assertEquals(1,store.load(key.accountId(),killer.id()).progress().questFlags().get(QuestRules.killsFlag(quest)));
      assertTrue(saved.gold()-killer.gold()>=definition.getGoldMin() && saved.gold()-killer.gold()<=definition.getGoldMax(),
          "The single roll still supplies the authored gold");
      var drops=store.listGroundLoot(0,new OnlineStore.TileRect(2831,1090,2841,1100)).stream()
          .filter(loot -> killer.id().equals(loot.ownerCharacter())).toList();
      assertEquals(1,drops.size(),"A lethal strike commits its rolled drop with the kill");
      assertEquals(ItemKeys.normalize("Leather gloves"),drops.getFirst().key());
      assertEquals(2835.0,drops.getFirst().x()); assertEquals(1095.0,drops.getFirst().y());
      now.set(saved.progress().cooldowns().get("attack")+1); // Pass only this hit's cooldown.
      assertEquals("creature_unavailable",assertThrows(PreviewWorld.Rejected.class,
          () -> melee.attack(token,saved.revision(),1)).getMessage());
      assertEquals(1,store.load(key.accountId(),killer.id()).progress().questFlags().get(QuestRules.killsFlag(quest)),
          "A terminal creature life cannot grant a second objective credit");
    } finally { sessions.shutdown(); }
  }

  @Test void paladinStarterMaceAndShieldCanLandAnAuthoritativePhysicalHit() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var json=new Gson().toJsonTree(MonsterCatalog.loadBundled().monsters().get("Olin Haad Guard 3")).getAsJsonObject();
    json.addProperty("health",1);json.addProperty("acMin",0);json.addProperty("acMax",0);
    json.addProperty("dodge",1);json.addProperty("agi",1);json.addProperty("respawnTime",300_000);
    var definition=new Gson().fromJson(json,MonsterDef.class);
    var spawn=new MonsterContent.Spawn("paladin-hit-"+UUID.randomUUID(),definition.getName(),3,3,0,false,false);
    var world=new PreviewWorld(new PreviewMap(new boolean[8][8],0,0,3,3),
        new MonsterContent(MonsterContent.VERSION,Map.of(definition.getName(),definition),List.of(spawn)));
    var sessions=new PersistentSessions(store,world.map(),world);
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.PALADIN,"male",false,new Random(2));
    assertTrue(prepared.progress().inventory().stream().anyMatch(i->i.key().equals("item.recruits_mace")));
    assertTrue(prepared.progress().inventory().stream().anyMatch(i->i.key().equals("item.newbi_set_shield")));
    var key=store.provisionAccount();
    var player=store.create(key.accountId(),"Paladin starter hit",0,3.5,3.5,prepared.progress(),prepared.gold());
    var token=sessions.join(store.acquire(key.secret(),player.id()),System.nanoTime()).token();
    try(var creatures=new PersistentCreatures(store,world)) {
      var rolls=new Random(1) { @Override public int nextInt(int bound) { return bound-1; } };
      var melee=new OnlineMelee(world.map(),world,sessions,creatures,ItemCatalog.loadBundled(),rolls,System::currentTimeMillis);
      var result=melee.attack(token,player.revision(),1).toCompletableFuture();
      await(sessions,creatures,result);
      assertTrue(result.get().character().experience()>player.experience());
      assertEquals("PLAYER",store.loadOrCreateSpawn(spawn.persistenceKey()).deathKind());
    } finally { sessions.shutdown(); }
  }

  @Test void learnedStunBlowAppliesOnlyAfterTheHitCommitsAndExpires() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var gson=new Gson();
    var json=gson.toJsonTree(MonsterCatalog.loadBundled().monsters().get("Olin Haad Guard 3")).getAsJsonObject();
    json.addProperty("health",10000);json.addProperty("end",1);json.addProperty("acMin",0);json.addProperty("acMax",0);
    json.addProperty("dodge",1);json.addProperty("agi",1);
    var definition=gson.fromJson(json,MonsterDef.class);
    var spawn=new MonsterContent.Spawn("stun-blow-"+UUID.randomUUID(),definition.getName(),3,3,0,false,false);
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    var world=new PreviewWorld(map,new MonsterContent(MonsterContent.VERSION,
        Map.of(definition.getName(),definition),List.of(spawn)));
    var sessions=new PersistentSessions(store,map,world);
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(11));
    var progressJson=gson.toJsonTree(prepared.progress()).getAsJsonObject();
    progressJson.getAsJsonObject("skills").addProperty("stun_blow",200);
    var learned=gson.fromJson(progressJson,com.perso.T4C.online.model.CharacterProgress.class);
    var key=store.provisionAccount();
    var player=store.create(key.accountId(),"Stun blow fighter",0,3.5,3.5,learned,prepared.gold());
    var token=sessions.join(store.acquire(key.secret(),player.id()),System.nanoTime()).token();
    var now=new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis());
    try(var creatures=new PersistentCreatures(store,world)) {
      var rolls=new Random(1) { @Override public int nextInt(int bound) { return bound==100 ? 0 : bound-1; } };
      var melee=new OnlineMelee(map,world,sessions,creatures,ItemCatalog.loadBundled(),rolls,now::get);
      var attack=melee.attack(token,player.revision(),1).toCompletableFuture();
      assertFalse(creatures.stunned(1,now.get()),"An uncommitted attack cannot apply its stun effect");
      await(sessions,creatures,attack);
      assertTrue(attack.get().character().revision()>player.revision(),
          "The learned stun skill must be accepted by the authoritative attack path");
      assertTrue(creatures.stunned(1,now.get()),"A successful committed hit applies its resolved stun duration");
      now.addAndGet(3000);
      assertFalse(creatures.stunned(1,now.get()),"Creature stun is transient and expires without persistence");
    } finally { sessions.shutdown(); }
  }

  @Test void archerBowAttackUsesRangedReachAndCommitsTheHitWithoutConsumingArrows() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var gson=new Gson();
    var json=gson.toJsonTree(MonsterCatalog.loadBundled().monsters().get("Olin Haad Guard 3")).getAsJsonObject();
    json.addProperty("health",1000);json.addProperty("acMin",0);json.addProperty("acMax",0);
    json.addProperty("dodge",1);json.addProperty("agi",1);
    var definition=gson.fromJson(json,MonsterDef.class);
    var near=new MonsterContent.Spawn("archer-near-"+UUID.randomUUID(),definition.getName(),8,3,0,false,false);
    var far=new MonsterContent.Spawn("archer-far-"+UUID.randomUUID(),definition.getName(),19,3,0,false,false);
    var map=new PreviewMap(new boolean[10][22],0,0,3,3);
    var world=new PreviewWorld(map,new MonsterContent(MonsterContent.VERSION,
        Map.of(definition.getName(),definition),List.of(near,far)));
    long nearId=world.creatureSpawns().stream().filter(spawn->spawn.spawn().persistenceKey().equals(near.persistenceKey()))
        .findFirst().orElseThrow().id();
    long farId=world.creatureSpawns().stream().filter(spawn->spawn.spawn().persistenceKey().equals(far.persistenceKey()))
        .findFirst().orElseThrow().id();
    var sessions=new PersistentSessions(store,map,world);
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.ARCHER,"male",false,new Random(7));
    var key=store.provisionAccount();
    var player=store.create(key.accountId(),"Ranged Archer",0,3.5,3.5,prepared.progress(),prepared.gold());
    String token=sessions.join(store.acquire(key.secret(),player.id()),System.nanoTime()).token();
    long now=System.currentTimeMillis();
    try(var creatures=new PersistentCreatures(store,world)) {
      var rolls=new Random(1) { @Override public int nextInt(int bound) { return bound-1; } };
      var melee=new OnlineMelee(map,world,sessions,creatures,ItemCatalog.loadBundled(),rolls,()->now);
      assertEquals("out_of_range_or_blocked",assertThrows(PreviewWorld.Rejected.class,
          ()->melee.attack(token,player.revision(),farId)).getMessage(),"Bow range is longer than melee but still bounded");
      assertEquals(player,store.load(key.accountId(),player.id()),"Rejected ranged intent cannot mutate progress");
      var result=melee.attack(token,player.revision(),nearId).toCompletableFuture();
      await(sessions,creatures,result);
      var saved=result.get().character();
      assertTrue(saved.experience()>player.experience());
      assertEquals(player.progress().inventory(),saved.progress().inventory(),
          "Single-player bow attacks do not consume arrows; online attacks preserve that rule");
      assertEquals(player.progress().equipment(),saved.progress().equipment());
      OnlineStore.SpawnState updated=null;
      long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
      while(updated==null && System.nanoTime()<deadline) {
        sessions.step();creatures.step();
        try { updated=creatures.life(nearId); }
        catch(PreviewWorld.Rejected recovering) { Thread.sleep(5); }
      }
      assertNotNull(updated,"Creature state reload completes after the committed strike");
      assertTrue(updated.health()<definition.getHealth(),"The ranged strike damages its authoritative creature");
      assertTrue(saved.progress().cooldowns().get("attack")>now);
    } finally { sessions.shutdown(); }
  }

  @Test void damagingBowHitAppliesNearestQuiverSplashInSameCommitWithoutConsumingArrows() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var gson=new Gson();
    var json=gson.toJsonTree(MonsterCatalog.loadBundled().monsters().get("Olin Haad Guard 3")).getAsJsonObject();
    json.addProperty("health",1000);json.addProperty("acMin",0);json.addProperty("acMax",0);
    json.addProperty("dodge",1);json.addProperty("agi",1);
    var definition=gson.fromJson(json,MonsterDef.class);
    var invulnerableJson=gson.toJsonTree(definition).getAsJsonObject();
    invulnerableJson.addProperty("name","Invulnerable splash guard");
    invulnerableJson.addProperty("acMin",Float.floatToIntBits(100000f));
    invulnerableJson.addProperty("acMax",Float.floatToIntBits(100000f));
    var invulnerable=gson.fromJson(invulnerableJson,MonsterDef.class);
    var primary=new MonsterContent.Spawn("splash-primary-"+UUID.randomUUID(),definition.getName(),8,3,0,false,false);
    var nearest=new MonsterContent.Spawn("splash-nearest-"+UUID.randomUUID(),invulnerable.getName(),8,4,0,false,false);
    var farther=new MonsterContent.Spawn("splash-farther-"+UUID.randomUUID(),definition.getName(),9,3,0,false,false);
    var map=new PreviewMap(new boolean[12][12],0,0,3,3);
    var world=new PreviewWorld(map,new MonsterContent(MonsterContent.VERSION,
        Map.of(definition.getName(),definition,invulnerable.getName(),invulnerable),List.of(primary,nearest,farther)));
    long primaryId=id(world,primary),nearestId=id(world,nearest),fartherId=id(world,farther);
    var sessions=new PersistentSessions(store,map,world);
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.ARCHER,"male",false,new Random(17));
    var key=store.provisionAccount();
    var player=store.create(key.accountId(),"Splash Archer",0,3.5,3.5,prepared.progress(),prepared.gold());
    String token=sessions.join(store.acquire(key.secret(),player.id()),System.nanoTime()).token();
    long now=System.currentTimeMillis();
    try(var creatures=new PersistentCreatures(store,world)) {
      var bundled=ItemCatalog.loadBundled(); var defs=new HashMap<>(bundled.items());
      var quiver=defs.get("item.recruits_quiver");
      defs.put(quiver.key(),new com.perso.T4C.online.model.ItemContent.Definition(quiver.key(),quiver.name(),quiver.bodyPart(),
          quiver.secondaryBodyPart(),quiver.appearanceEquippedPrimary(),quiver.appearanceEquippedSecondary(),quiver.appearanceInventory(),
          quiver.price(),quiver.weight(),quiver.armorClass(),quiver.dodgeLost(),quiver.minEnd(),quiver.reqAttack(),quiver.reqStr(),
          quiver.reqAgi(),quiver.minInt(),quiver.minWis(),quiver.attackSpeed(),quiver.unique(),quiver.isBow(),quiver.unlimitedUse(),
          quiver.numId(),quiver.structure(),quiver.dmgFormula(),quiver.atkDelay(),quiver.nbCharges(),quiver.undroppable(),quiver.boosts(),
          quiver.spells(),quiver.rarityPriced(),new com.perso.T4C.online.model.ItemContent.Splash(1.5,2,50)));
      var items=new com.perso.T4C.online.model.ItemContent(com.perso.T4C.online.model.ItemContent.VERSION,defs);
      var rolls=new Random(1) { @Override public int nextInt(int bound) { return bound-1; } };
      var melee=new OnlineMelee(map,world,sessions,creatures,items,rolls,()->now);
      var result=melee.attack(token,player.revision(),primaryId).toCompletableFuture();
      await(sessions,creatures,result);
      var saved=result.get().character();
      assertTrue(saved.experience()>player.experience());
      assertEquals(player.progress().inventory(),saved.progress().inventory());
      assertNull(store.loadOrCreateSpawn(nearest.persistenceKey()).health(),
          "Invulnerable armor excludes a secondary target");
      assertTrue(store.loadOrCreateSpawn(farther.persistenceKey()).health()<definition.getHealth(),
          "The nearest eligible neighbour receives splash damage in the primary strike transaction");
    } finally { sessions.shutdown(); }
  }

  private static long id(PreviewWorld world,MonsterContent.Spawn wanted) {
    return world.creatureSpawns().stream().filter(candidate->candidate.spawn().persistenceKey().equals(wanted.persistenceKey()))
        .findFirst().orElseThrow().id();
  }

  private static void await(PersistentSessions sessions,PersistentCreatures creatures,CompletableFuture<?> result) throws Exception {
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
    while(!result.isDone() && System.nanoTime()<deadline) { sessions.step(); creatures.step(); Thread.sleep(5); }
    assertTrue(result.isDone()); result.get();
  }
}
