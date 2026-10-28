package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** The pure spell draft: no database, a fake creature source, the real engine and kill machinery. */
class OnlineSpellStrikesPlanTest {
  private static final long NOW = 50_000L;

  private static MonsterDef skeleton(int health) throws Exception {
    var gson = new Gson();
    var json = gson.toJsonTree(MonsterCatalog.loadBundled().monsters().get("Skeleton")).getAsJsonObject();
    json.addProperty("health", health);
    json.add("resists", gson.toJsonTree(new int[12]));
    json.add("loot", gson.toJsonTree(List.of(new MonsterDef.LootDrop("Leather gloves", 1f))));
    return gson.fromJson(json, MonsterDef.class);
  }

  /** Creatures at given tiles; each has its own life. */
  private static final class Source implements OnlineSpellStrikes.CreatureSource {
    final List<PreviewWorld.CreatureSpawn> spawns = new ArrayList<>();
    final Map<Long, double[]> where = new java.util.HashMap<>();
    final Map<Long, UUID> lives = new java.util.HashMap<>();
    boolean busy;

    void add(long id, MonsterDef def, double x, double y) {
      spawns.add(new PreviewWorld.CreatureSpawn(id, new MonsterContent.Spawn("t-" + id, def.getName(), 5, 5, 0, false, false), def));
      where.put(id, new double[] {x, y});
      lives.put(id, UUID.randomUUID());
    }
    @Override public PreviewWorld.CreatureSpawn spawn(long id) { return spawns.stream().filter(s -> s.id() == id).findFirst().orElse(null); }
    @Override public List<PreviewWorld.CreatureSpawn> spawns() { return spawns; }
    @Override public PreviewWorld.CreaturePosition position(long id) { return new PreviewWorld.CreaturePosition(where.get(id)[0], where.get(id)[1]); }
    @Override public OnlineStore.SpawnState life(long id) {
      if (busy) throw new PreviewWorld.Rejected(409, "creature_unavailable");
      return new OnlineStore.SpawnState("t-" + id, 0, lives.get(id), null, null, null, null, 0, null, null, "IDLE", null, 0);
    }
    @Override public OnlineStore.SpawnMotion capture(long id) {
      return new OnlineStore.SpawnMotion("t-" + id, lives.get(id), 0, where.get(id)[0], where.get(id)[1], "IDLE", null, null);
    }
  }

  private static CharacterProgress progress(int mana, Set<String> spells, Map<String, Long> cooldowns) {
    var base = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(3)).progress();
    return new CharacterProgress(base.version(), base.characterClass(), base.gender(), base.attributes(),
        new CharacterProgress.Vitals(40, 100, mana, 150), base.advancement(), List.of(), Map.of(), base.skills(), spells,
        Map.of(), List.of(), 0, base.respawn(), List.of(), cooldowns, 0, base.companion());
  }

  private static OnlineStore.CharacterState state(CharacterProgress p) {
    return new OnlineStore.CharacterState(UUID.randomUUID(), UUID.randomUUID(), "Caster", 0, 3, 3, 0, 0, 0, p);
  }

  private static OnlineSpellStrikes.Plan plan(CharacterProgress p, String spell, Long target, Source source) throws Exception {
    var map = new PreviewMap(new boolean[30][60], 0, 0, 3, 3);
    return OnlineSpellStrikes.plan(state(p), spell, target == null ? new com.perso.T4C.spell.engine.SpellEngine.Target.Self()
        : new com.perso.T4C.spell.engine.SpellEngine.Target.Unit(target), NOW, new Random(11), ItemCatalog.loadBundled(),
        SpellCatalogV2.bundled(), map, 5.5, 5.5, 0, source);
  }

  private static String refusal(Runnable action) {
    return assertThrows(PreviewWorld.Rejected.class, action::run).getMessage();
  }

  @Test void aNukeSpendsManaStartsExhaustionAndKillsTheTargetWithRewardsAndSplash() throws Exception {
    var source = new Source();
    source.add(1, skeleton(1), 9.5, 5.5);
    source.add(2, skeleton(1_000_000), 10.5, 5.5);
    var p = progress(20, Set.of("spell.fire_dart"), Map.of());
    var plan = plan(p, "spell.fire_dart", 1L, source);
    assertEquals(1, plan.manaSpent());
    assertEquals(19, plan.progress().vitals().mana());
    assertTrue(plan.progress().cooldowns().get(OnlineCasting.MENTAL_EXHAUSTION) > NOW);
    // Both the primary (killed) and the adjacent neighbour (splashed) are struck in one draft.
    assertEquals(List.of(1L, 2L), plan.struck());
    var kill = plan.hits().get(0);
    assertTrue(kill.killed());
    assertEquals(0, kill.health());
    assertFalse(plan.hits().get(1).killed());
    assertTrue(plan.experience() > 0);
    assertEquals(1, plan.strikes().get(0).drops().size(), "the kill's one rolled drop commits with it");
    assertEquals(0, plan.strikes().get(0).strike().after());
    assertTrue(plan.strikes().get(1).strike().after() > 0);
  }

  @Test void aDrainHealsTheCasterAndAHealHealsOnSelfWithoutTouchingCreatures() throws Exception {
    var source = new Source();
    source.add(1, skeleton(1_000_000), 8.5, 5.5);
    var spells = Set.of("spell.drain_life", "spell.heal_serious");
    var drain = plan(progress(100, spells, Map.of()), "spell.drain_life", 1L, source);
    assertTrue(drain.healthGained() > 0 && drain.healthGained() <= 60, "gained " + drain.healthGained() + " hits " + drain.hits());
    assertEquals(1, drain.strikes().size());
    var heal = plan(progress(100, spells, Map.of()), "spell.heal_serious", null, source);
    assertTrue(heal.healthGained() > 0);
    assertTrue(heal.strikes().isEmpty() && heal.hits().isEmpty());
  }

  @Test void refusalsLeaveNoDraft() throws Exception {
    var source = new Source();
    source.add(1, skeleton(10), 8.5, 5.5);
    source.add(2, skeleton(10), 40, 5.5);
    var known = Set.of("spell.fire_dart");
    assertEquals("not_learned", refusal(() -> unchecked(progress(20, Set.of(), Map.of()), "spell.fire_dart", 1L, source)));
    assertEquals("not_enough_mana", refusal(() -> unchecked(progress(0, known, Map.of()), "spell.fire_dart", 1L, source)));
    assertEquals("exhausted", refusal(() -> unchecked(progress(20, known, Map.of(OnlineCasting.MENTAL_EXHAUSTION, NOW + 1)), "spell.fire_dart", 1L, source)));
    assertEquals("creature_unavailable", refusal(() -> unchecked(progress(20, known, Map.of()), "spell.fire_dart", 77L, source)));
    assertEquals("out_of_range", refusal(() -> unchecked(progress(20, known, Map.of()), "spell.fire_dart", 2L, source)));
    assertEquals("wrong_target", refusal(() -> unchecked(progress(20, known, Map.of()), "spell.fire_dart", null, source)));
    // Stage 2 and later spells stay refused exactly like an unknown key.
    assertEquals("unsupported_spell", refusal(() -> unchecked(progress(20, Set.of("spell.fireball"), Map.of()), "spell.fireball", 1L, source)));
    assertEquals("unsupported_spell", refusal(() -> unchecked(progress(20, Set.of("spell.barrier"), Map.of()), "spell.barrier", null, source)));
    source.busy = true;
    assertEquals("creature_unavailable", refusal(() -> unchecked(progress(20, known, Map.of()), "spell.fire_dart", 1L, source)));
  }

  private static void unchecked(CharacterProgress p, String spell, Long target, Source source) {
    try { plan(p, spell, target, source); } catch (PreviewWorld.Rejected e) { throw e; } catch (Exception e) { throw new RuntimeException(e); }
  }
}
