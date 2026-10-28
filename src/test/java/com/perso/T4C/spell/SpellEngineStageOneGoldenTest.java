package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.RandomSource;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.online.model.SpellContentV2;
import com.perso.T4C.player.Player;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Single-player half of the stage-1 engine golden. Every one of the 48 stage-1 spells is cast by
 * three fixture characters through the <em>single-player</em> code path (the success roll in {@link
 * SpellCastingService#begin}, the damage and heal rolls in {@link SpellEffectManager}, the splash
 * selection through {@link SpellAreaTargeting}) with a seeded {@link RandomSource}, and the outcome
 * is compared with the committed golden text. The server module's {@code SpellEngineGoldenTest}
 * reproduces the same text from {@code SpellEngine}, so the engine equals single-player value for
 * value, in the same draw order. Regenerate after an intentional content change with {@code
 * -Dspell.golden.update=true}.
 *
 * <p>The splash selection mirrors {@code MainGameScreen.applySpellImpact}: adjacent ring, alive,
 * attackable, nearest first, seven of the eight slots (the primary holds one). The fixture ring has
 * a stationary and a dead neighbour that must never be hit.
 */
@ResourceLock("ItemRegistry")
class SpellEngineStageOneGoldenTest {
  static final Path GOLDEN = Path.of("src/test/resources/spell/engine-stage1-golden.txt");
  static final Path CONTENT = Path.of("multiplayer/server/src/main/resources/content/spells-v2.json");
  static final float CASTER_TILE_X = 7;
  static final float CASTER_TILE_Y = 10;
  /** (id, tileX, tileY, stationary, dead, resistance in fire,earth,air,water,light,dark order). */
  static final Object[][] CREATURES = {
    {1L, 10, 10, false, false, new int[] {100, 250, 7, 100, 100, 3}},
    {2L, 9, 9, false, false, new int[] {100, 100, 100, 100, 100, 100}},
    {3L, 10, 9, false, false, new int[] {40, 0, 300, 100, 60, 100}},
    {4L, 11, 9, false, false, new int[] {100, 100, 100, 100, 100, 100}},
    {5L, 9, 10, true, false, new int[] {100, 100, 100, 100, 100, 100}},
    {6L, 11, 10, false, true, new int[] {100, 100, 100, 100, 100, 100}},
    {7L, 9, 11, false, false, new int[] {250, 100, 100, 100, 100, 100}},
    {8L, 10, 11, false, false, new int[] {100, 100, 100, 100, 100, 5}},
    {9L, 11, 11, false, false, new int[] {100, 100, 100, 100, 100, 100}},
    {10L, 13, 10, false, false, new int[] {100, 100, 100, 100, 100, 100}},
  };

  @BeforeAll
  static void loadItems() {
    ItemRegistry.resetAdditionalDefinitions();
    ItemJsonLoader.loadAndRegister();
  }

  /** A real monster whose only used facts are its element resistances. */
  private static final class FixtureMonster extends BaseMonster {
    FixtureMonster(int[] resistanceInElementOrder) throws GameException {
      super("Test monster", 0f, 0f, 10, 0, 0, 0, 1, 1, 1_000L, null, null, null, null, null);
      // BaseMonster.getElementResistance(1..6) = fire, earth, air, water, light, dark.
      int[] slots = {3, 1, 0, 2, 5, 4};
      for (int element = 0; element < 6; element++) combatResists[slots[element]] = resistanceInElementOrder[element];
    }
  }

  static List<String> stageOneKeys() throws Exception {
    var content = new Gson().fromJson(Files.readString(CONTENT), SpellContentV2.class);
    var keys = new ArrayList<String>();
    content.spells().values().stream().filter(spell -> spell.stage() == 1).forEach(spell -> keys.add(spell.key()));
    java.util.Collections.sort(keys);
    return keys;
  }

  static final String[] CASTERS = {"plain", "skilled", "geared"};

  static String scenario(String key, int keyIndex, String casterName, int casterIndex) throws Exception {
    SpellData spell = SpellRegistry.findByName(key);
    assertNotNull(spell, key);
    Player player = SpellContextParityTest.player(casterName);
    player.setMana(10_000);
    player.setSpells(new ArrayList<>(List.of(spell.getKey())));
    long seed = 1_000L + keyIndex * 7L + casterIndex;
    boolean heal = !spell.isAttack();
    var fixtures = new ArrayList<Object[]>();
    var monsters = new java.util.HashMap<Long, FixtureMonster>();
    for (Object[] row : CREATURES) {
      fixtures.add(row);
      monsters.put((Long) row[0], new FixtureMonster((int[]) row[5]));
    }
    var out = new StringBuilder(key + " " + casterName);
    try (RandomSource.Scope ignored = RandomSource.use(new Random(seed))) {
      float distance = 3f;
      SpellCastingService.Result cast =
          SpellCastingService.begin(
              new SpellCastingService.Request(
                  spell,
                  player,
                  heal ? SpellCastingService.TargetKind.SELF : SpellCastingService.TargetKind.HOSTILE_UNIT,
                  heal ? 0f : distance,
                  true,
                  false,
                  true));
      out.append(" verdict=").append(cast.failure().name()).append(" mana=").append(cast.manaSpent());
      if (!cast.success()) return out.toString();
      var manager = new SpellEffectManager();
      if (heal) {
        int delta = manager.resolvePlayerHealthDelta(spell, player);
        if (delta > 0) out.append(" heal=").append(delta);
        return out.toString();
      }
      Object[] primaryRow = fixtures.get(0);
      var primary = monsters.get((Long) primaryRow[0]);
      float cx = ((Integer) primaryRow[1]) * GameConstants.GRID_W;
      float cy = ((Integer) primaryRow[2]) * GameConstants.GRID_H;
      // Selected before damage, as applySpellImpact does.
      List<Object[]> secondary =
          SpellAreaTargeting.nearest(
              fixtures,
              row ->
                  (Long) row[0] != (Long) primaryRow[0]
                      && !(Boolean) row[3]
                      && !(Boolean) row[4]
                      && SpellAreaTargeting.isAdjacent(
                          cx,
                          cy,
                          ((Integer) row[1]) * GameConstants.GRID_W,
                          ((Integer) row[2]) * GameConstants.GRID_H),
              row -> {
                double dx = (((Integer) row[1]) * GameConstants.GRID_W) - cx;
                double dy = (((Integer) row[2]) * GameConstants.GRID_H) - cy;
                return dx * dx + dy * dy;
              },
              1);
      SpellEffectManager.Impact impact = manager.resolve(spell, player, primary, 0d);
      if (impact.healthDelta() < 0) out.append(" primary=").append(-impact.healthDelta());
      if (impact.drainedHealth() > 0) out.append(" drain=").append(impact.drainedHealth());
      for (Object[] row : secondary) {
        int damage = manager.resolveResidualDamage(spell, player, monsters.get((Long) row[0]));
        if (damage > 0) out.append(" res").append(row[0]).append('=').append(damage);
      }
    }
    return out.toString();
  }

  static String produce() throws Exception {
    var text = new StringBuilder("# Stage-1 spell outcomes from the single-player code path; the engine must equal them.\n");
    var keys = stageOneKeys();
    for (int k = 0; k < keys.size(); k++)
      for (int c = 0; c < CASTERS.length; c++) text.append(scenario(keys.get(k), k, CASTERS[c], c)).append('\n');
    return text.toString();
  }

  @Test
  void fortyEightSpellsMatchTheCommittedGolden() throws Exception {
    assertEquals(48, stageOneKeys().size());
    String text = produce();
    if (Boolean.getBoolean("spell.golden.update")) {
      Files.createDirectories(GOLDEN.getParent());
      Files.writeString(GOLDEN, text);
    }
    assertEquals(Files.readString(GOLDEN).replace("\r\n", "\n"), text,
        "Regenerate with -Dspell.golden.update=true and review the diff");
  }

  @Test
  void theFixtureReallyExercisesSplashDrainsHealsAndTheIgnoredNeighbours() throws Exception {
    String text = produce();
    assertTrue(text.contains(" drain="), "drains heal the caster");
    assertTrue(text.contains(" heal="), "single heals restore health");
    assertTrue(text.contains(" res2="), "an adjacent neighbour takes splash");
    // Stationary (5), dead (6) and distant (10) creatures are never splashed.
    assertFalse(text.contains(" res5="));
    assertFalse(text.contains(" res6="));
    assertFalse(text.contains(" res10="));
    // The fixture ring holds six eligible neighbours, all within the seven splash slots.
    assertTrue(text.lines().anyMatch(line -> line.contains(" res9=") && line.contains(" res8=")));
    assertEquals(48 * CASTERS.length, text.lines().filter(line -> line.startsWith("spell.")).count());
  }
}
