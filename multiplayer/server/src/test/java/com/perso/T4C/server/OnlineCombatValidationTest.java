package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.spell.engine.SpellEngine;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * M4 "server validation" through the server's melee and spell paths with a real session and creature lives:
 * sanctuary (single-player's safe-haven tiles protect both sides, SanctuaryCombatRules.canFight), peaceful actors
 * (authored stationary creatures, BaseMonster.canBeAttackedByPlayer) and the stun that single-player's player gates
 * attacks, abilities and movement with (Player.isStunned). Every refusal must leave the character, the creature's
 * persisted life and the wallet exactly as they were.
 */
@EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
class OnlineCombatValidationTest {
  private static final int SAFE_HAVEN=6;
  private static final String SPELL="spell.fire_dart";

  private static final class Arena implements AutoCloseable {
    final OnlineStore store;
    final PreviewMap map;
    final PreviewWorld world;
    final PersistentSessions sessions;
    final PersistentCreatures creatures;
    final OnlineMelee melee;
    final OnlineSpellStrikes spells;
    final OnlineStore.AccountKey key;
    final AtomicLong clock=new AtomicLong(System.currentTimeMillis());
    final List<MonsterContent.Spawn> spawns;
    final Map<String,UUID> ids=new HashMap<>();

    /** Row 5: player lane. Tile (3,5) and tile (8,5) are safe-haven ground. Creature 1 (5,5) free, 2 (8,5) in sanctuary, 3 (11,5) stationary. */
    Arena() throws Exception {
      store=OnlineStore.fromEnvironment();store.migrate();
      var skeleton=MonsterCatalog.loadBundled().monsters().get("Skeleton");
      var gson=new com.google.gson.Gson();
      var json=gson.toJsonTree(skeleton).getAsJsonObject();
      json.addProperty("health",100_000);json.addProperty("respawnTime",300_000);
      json.add("resists",gson.toJsonTree(new int[12]));
      var tough=gson.fromJson(json,MonsterDef.class);
      spawns=List.of(
          new MonsterContent.Spawn("valid-"+UUID.randomUUID(),tough.getName(),5,5,0,false,false),
          new MonsterContent.Spawn("valid-"+UUID.randomUUID(),tough.getName(),8,5,0,false,false),
          new MonsterContent.Spawn("valid-"+UUID.randomUUID(),tough.getName(),11,5,0,true,false));
      var content=new MonsterContent(MonsterContent.VERSION,Map.of(tough.getName(),tough),spawns);
      byte[][] terrain=new byte[20][20];
      terrain[5][3]=SAFE_HAVEN;terrain[5][8]=SAFE_HAVEN;
      map=new PreviewMap(new boolean[20][20],terrain,0,0,2,2);
      world=new PreviewWorld(map,content);
      sessions=new PersistentSessions(store,map,world);
      creatures=new PersistentCreatures(store,world);
      melee=new OnlineMelee(map,world,sessions,creatures,ItemCatalog.loadBundled(),
          new Random(1){ @Override public int nextInt(int bound) { return bound-1; } },clock::get);
      spells=new OnlineSpellStrikes(map,world,sessions,creatures,ItemCatalog.loadBundled(),SpellCatalogV2.bundled(),
          new Random(4),clock::get);
      key=store.provisionAccount();
    }

    /** A warrior who knows Fire Dart, with the given cooldown entries, standing at the tile. */
    String join(String name,double x,double y,Map<String,Long> cooldowns) throws Exception {
      var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(3));
      var base=prepared.progress();
      var merged=new HashMap<>(base.cooldowns());merged.putAll(cooldowns);
      var progress=new CharacterProgress(base.version(),base.characterClass(),base.gender(),base.attributes(),
          new CharacterProgress.Vitals(base.vitals().hp(),base.vitals().maxHp(),20,20),base.advancement(),base.inventory(),
          base.equipment(),base.skills(),Set.of(SPELL),base.questFlags(),base.storage(),base.storageGold(),
          base.respawn(),base.buffs(),merged,0,base.companion());
      var created=store.create(key.accountId(),name,0,x,y,progress,prepared.gold());
      ids.put(name,created.id());
      return sessions.join(store.acquire(key.secret(),created.id()),System.nanoTime()).token();
    }

    long revision(String token) { return sessions.character(token).revision(); }

    void settle(CompletionStage<?> pending) throws Exception {
      var future=pending.toCompletableFuture();
      long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
      while(!future.isDone() && System.nanoTime()<deadline) { sessions.step();creatures.step();Thread.sleep(5); }
      future.get();
      // The creature is reloaded from the database after a committed hit; wait until every life is readable again.
      while(System.nanoTime()<deadline) {
        try { for(long id=1;id<=spawns.size();id++) creatures.life(id);return; }
        catch(PreviewWorld.Rejected reloading) { sessions.step();creatures.step();Thread.sleep(5); }
      }
      fail("creature lives did not become readable again");
    }

    @Override public void close() { sessions.shutdown(); }
  }

  /** The persisted facts that a refusal must not touch. */
  private record Snapshot(OnlineStore.CharacterState character,List<OnlineStore.SpawnState> lives) {}

  private static Snapshot snapshot(Arena a,String name) throws Exception {
    var lives=new ArrayList<OnlineStore.SpawnState>();
    for(var spawn:a.spawns) lives.add(a.store.loadOrCreateSpawn(spawn.persistenceKey()));
    return new Snapshot(a.store.load(a.key.accountId(),a.ids.get(name)),lives);
  }

  private static void assertRefused(Arena a,String name,String code,Runnable attempt) throws Exception {
    var before=snapshot(a,name);
    var refused=assertThrows(PreviewWorld.Rejected.class,attempt::run);
    assertEquals(code,refused.getMessage());
    assertEquals(before,snapshot(a,name),"a refusal mutates neither the character nor any creature life");
  }

  @Test void sanctuaryRefusesMeleeAndSpellsFromEitherSideAndOpensOnceBothAreOutsideIt() throws Exception {
    try(var a=new Arena()) {
      // The attacker stands on a safe-haven tile, one tile from creature 1.
      String inside=a.join("Sanctuary caster",3.5,5.5,Map.of());
      assertRefused(a,"Sanctuary caster","out_of_range_or_blocked",() -> a.melee.attack(inside,a.revision(inside),1));
      var spellRefusal=assertThrows(PreviewWorld.Rejected.class,
          () -> a.spells.cast(inside,a.revision(inside),SPELL,new SpellEngine.Target.Unit(1)));
      assertNotNull(spellRefusal.getMessage());
      assertEquals(spellRefusal.getMessage(),assertThrows(PreviewWorld.Rejected.class,
          () -> a.spells.cast(inside,a.revision(inside),SPELL,new SpellEngine.Target.Unit(1))).getMessage());
      assertEquals(20,a.sessions.character(inside).progress().vitals().mana(),"a refused cast spends no mana");
      assertTrue(a.sessions.character(inside).progress().cooldowns().keySet().stream().noneMatch(k -> k.endsWith("exhaustion")),
          "a refused cast starts no exhaustion");

      // The target stands on a safe-haven tile (creature 2 at tile 8,5); the attacker is outside it.
      String outside=a.join("Outside attacker",7.5,5.5,Map.of());
      assertRefused(a,"Outside attacker","out_of_range_or_blocked",() -> a.melee.attack(outside,a.revision(outside),2));
      var spellAtProtected=assertThrows(PreviewWorld.Rejected.class,
          () -> a.spells.cast(outside,a.revision(outside),SPELL,new SpellEngine.Target.Unit(2)));
      assertEquals(spellRefusal.getMessage(),spellAtProtected.getMessage(),"both sides of the fight are protected alike");
      assertEquals(20,a.sessions.character(outside).progress().vitals().mana());

      // Control: neither side in a sanctuary (creature 1 at 5,5, attacker at 4.5,5.5) fights and pays the spell cost.
      String open=a.join("Open fighter",4.5,5.5,Map.of());
      a.settle(a.melee.attack(open,a.revision(open),1));
      var cast=a.spells.cast(open,a.revision(open),SPELL,new SpellEngine.Target.Unit(1));
      a.settle(cast);
      assertEquals(19,a.sessions.character(open).progress().vitals().mana());
    }
  }

  @Test void peacefulStationaryActorsRefuseMeleeAndSpellsWithoutAnyChange() throws Exception {
    try(var a=new Arena()) {
      String token=a.join("Peaceful tester",10.5,5.5,Map.of());
      assertRefused(a,"Peaceful tester","protected_target",() -> a.melee.attack(token,a.revision(token),3));
      var spellRefusal=assertThrows(PreviewWorld.Rejected.class,
          () -> a.spells.cast(token,a.revision(token),SPELL,new SpellEngine.Target.Unit(3)));
      assertEquals("protected_target",spellRefusal.getMessage());
      assertEquals(20,a.sessions.character(token).progress().vitals().mana());
      assertNull(a.store.loadOrCreateSpawn(a.spawns.get(2).persistenceKey()).targetCharacter(),"a peaceful actor is never provoked");
    }
  }

  @Test void aStunnedPlayerCannotSwingOrCastUntilTheStunEndsAndRefusalsChangeNothing() throws Exception {
    try(var a=new Arena()) {
      long until=a.clock.get()+60_000;
      String stunned=a.join("Stunned fighter",4.5,5.5,Map.of("stun",until));
      assertRefused(a,"Stunned fighter","cooldown",() -> a.melee.attack(stunned,a.revision(stunned),1));
      var castRefusal=assertThrows(PreviewWorld.Rejected.class,
          () -> a.spells.cast(stunned,a.revision(stunned),SPELL,new SpellEngine.Target.Unit(1)));
      assertEquals("exhausted",castRefusal.getMessage(),"single-player gates abilities while stunned");
      assertEquals(20,a.sessions.character(stunned).progress().vitals().mana(),"a refused cast spends no mana");
      // The same stun, once it has run out, no longer gates either action.
      a.clock.set(until+1);
      a.settle(a.melee.attack(stunned,a.revision(stunned),1));
      a.clock.set(until+10_000);
      a.settle(a.spells.cast(stunned,a.revision(stunned),SPELL,new SpellEngine.Target.Unit(1)));
      assertEquals(19,a.sessions.character(stunned).progress().vitals().mana());
    }
  }
}
