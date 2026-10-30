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
  private static class Source implements OnlineSpellStrikes.CreatureSource {
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
    // Stage 4 remains closed except for the separately supported authored recall.
    assertEquals("unsupported_spell", refusal(() -> unchecked(progress(20, Set.of("spell.entangle"), Map.of()), "spell.entangle", 1L, source)));
    var recall=plan(progress(150,Set.of("spell.word_of_recall"),Map.of()),"spell.word_of_recall",null,source);
    assertEquals(100,recall.manaSpent());
    assertEquals(4,recall.events().stream().filter(com.perso.T4C.spell.engine.SpellEngine.Event.Dispel.class::isInstance).count());
    assertTrue(recall.events().getLast() instanceof com.perso.T4C.spell.engine.SpellEngine.Event.Recall);
    assertEquals("unsupported_spell", refusal(() -> unchecked(progress(20, Set.of("spell.tame_beast"), Map.of()), "spell.tame_beast", null, source)));
    source.busy = true;
    assertEquals("creature_unavailable", refusal(() -> unchecked(progress(20, known, Map.of()), "spell.fire_dart", 1L, source)));
  }

  private static OnlineSpellStrikes.Plan aimed(CharacterProgress p, String spell, com.perso.T4C.spell.engine.SpellEngine.Target target,
      Source source) throws Exception {
    var map = new PreviewMap(new boolean[30][60], 0, 0, 3, 3);
    return OnlineSpellStrikes.plan(state(p), spell, target, NOW, new Random(11), ItemCatalog.loadBundled(),
        SpellCatalogV2.bundled(), map, 5.5, 5.5, 0, source);
  }

  /** A fireball takes the aimed creature and the others inside its radius in one draft; the far and the stationary are left. */
  @Test void anAreaSpellStrikesEveryCreatureInItsRadiusInOneDraftWithEachKillRewarded() throws Exception {
    var source = new Source();
    source.add(1, skeleton(1), 9.5, 5.5);
    source.add(2, skeleton(1_000_000), 10.5, 5.5);
    source.add(3, skeleton(1), 9.5, 6.5);
    source.add(4, skeleton(1_000_000), 20.5, 5.5);
    source.add(5, skeleton(1_000_000), 8.5, 5.5);
    source.spawns.set(4, new PreviewWorld.CreatureSpawn(5, new MonsterContent.Spawn("t-5", "Skeleton", 5, 5, 0, true, false), skeleton(1_000_000)));
    var plan = aimed(progress(100, Set.of("spell.fireball"), Map.of()), "spell.fireball",
        new com.perso.T4C.spell.engine.SpellEngine.Target.Unit(1), source);
    assertEquals(3, plan.struck().size(), "aimed creature plus two inside the radius: " + plan.struck());
    assertTrue(plan.struck().containsAll(List.of(1L, 2L, 3L)));
    assertFalse(plan.struck().contains(4L), "out of the radius");
    assertFalse(plan.struck().contains(5L), "stationary creatures cannot be attacked");
    assertEquals(2, plan.hits().stream().filter(OnlineSpellStrikes.Hit::killed).count());
    assertEquals(2, plan.strikes().stream().filter(s -> s.strike().after() == 0).mapToInt(s -> s.drops().size()).sum(),
        "each kill commits its own rolled drop");
    assertEquals(plan.struck().size(), plan.strikes().size());
    assertTrue(plan.experience() > 0);
  }

  @Test void aGroundAimedAreaAndASelfCentredAreaNeedNoAimedCreature() throws Exception {
    var source = new Source();
    source.add(1, skeleton(1), 10.5, 5.5);
    source.add(2, skeleton(1_000_000), 11.5, 5.5);
    var ground = aimed(progress(150, Set.of("spell.hurricane"), Map.of()), "spell.hurricane",
        new com.perso.T4C.spell.engine.SpellEngine.Target.Position(10, 5), source);
    assertEquals(2, ground.struck().size());
    var quake = aimed(progress(150, Set.of("spell.earthquake"), Map.of()), "spell.earthquake",
        new com.perso.T4C.spell.engine.SpellEngine.Target.Self(), source);
    assertEquals(2, quake.struck().size(), "self-centred, 5 and 6 tiles away, radius 7");
    // Nothing to hit is still a cast: mana is spent and the draft carries no strike.
    var empty = aimed(progress(150, Set.of("spell.earthquake"), Map.of()), "spell.earthquake",
        new com.perso.T4C.spell.engine.SpellEngine.Target.Self(), new Source());
    assertTrue(empty.strikes().isEmpty());
    assertTrue(empty.manaSpent() > 0);
  }

  @Test void anAreaHealHealsOnlyTheCasterAndAShieldOrGatewayOnlyCosts() throws Exception {
    var source = new Source();
    source.add(1, skeleton(1_000_000), 6.5, 5.5);
    var p = progress(150, Set.of("spell.mass_healing", "spell.fire_shield", "spell.lighthaven_gateway"), Map.of());
    var heal = plan(p, "spell.mass_healing", null, source);
    assertTrue(heal.healthGained() > 0 && heal.strikes().isEmpty());
    for (String key : List.of("spell.fire_shield", "spell.lighthaven_gateway")) {
      var inert = plan(p, key, null, source);
      assertTrue(inert.strikes().isEmpty() && inert.hits().isEmpty() && inert.buffs().isEmpty(), key);
      assertEquals(0, inert.healthGained());
      assertTrue(inert.manaSpent() >= 0 && inert.progress().vitals().mana() == 150 - inert.manaSpent());
    }
  }

  /** Poison is one hit offline (its tick spell does not exist), so it is one hit here, with no splash. */
  @Test void poisonIsASingleHitWithoutSplash() throws Exception {
    var source = new Source();
    source.add(1, skeleton(1_000_000), 8.5, 5.5);
    source.add(2, skeleton(1_000_000), 9.5, 5.5);
    var plan = plan(progress(100, Set.of("spell.poison"), Map.of()), "spell.poison", 1L, source);
    assertFalse(plan.struck().contains(2L), "poison is not an attack: no splash on the neighbour");
    assertTrue(plan.struck().size() <= 1);
  }

  /** A creature mid-commit near the blast is left alone; one that would be hit is not skipped silently when aimed at. */
  @Test void aBusyBystanderDoesNotRefuseAnAreaButABusyAimedTargetDoes() throws Exception {
    var source = new Source() {
      @Override public OnlineStore.SpawnState life(long id) {
        if (id == 2) throw new PreviewWorld.Rejected(409, "creature_unavailable");
        return super.life(id);
      }
    };
    source.add(1, skeleton(1_000_000), 9.5, 5.5);
    source.add(2, skeleton(1_000_000), 10.5, 5.5);
    var plan = aimed(progress(100, Set.of("spell.fireball"), Map.of()), "spell.fireball",
        new com.perso.T4C.spell.engine.SpellEngine.Target.Unit(1), source);
    assertEquals(List.of(1L), plan.struck());
    assertEquals("creature_unavailable", refusal(() -> {
      try { aimed(progress(100, Set.of("spell.fireball"), Map.of()), "spell.fireball",
          new com.perso.T4C.spell.engine.SpellEngine.Target.Unit(2), source); }
      catch (PreviewWorld.Rejected e) { throw e; } catch (Exception e) { throw new RuntimeException(e); }
    }));
  }

  private static void unchecked(CharacterProgress p, String spell, Long target, Source source) {
    try { plan(p, spell, target, source); } catch (PreviewWorld.Rejected e) { throw e; } catch (Exception e) { throw new RuntimeException(e); }
  }
}
