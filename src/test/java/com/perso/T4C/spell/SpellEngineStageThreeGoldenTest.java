package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.Vector2;
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
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Single-player half of the stage-3 engine golden: the 25 area attacks, the 4 area heals, the three
 * damage-over-time spells that single-player resolves as one hit, and the 10 shields and gateways
 * (which do nothing but cost on a self cast). Each is cast by three fixture characters through the
 * single-player code path with a seeded {@link RandomSource}: the success roll in {@link
 * SpellCastingService#begin}, the damage rolls in {@link SpellEffectManager}, the target selection
 * through {@link SpellAreaTargeting} on real {@link Vector2} positions exactly as {@code
 * MainGameScreen} does it. The server module's {@code SpellEngineStageThreeGoldenTest} reproduces the
 * same text from {@code SpellEngine}. Regenerate with {@code -Dspell.golden.update=true}.
 */
@ResourceLock("ItemRegistry")
class SpellEngineStageThreeGoldenTest {
  static final Path GOLDEN = Path.of("src/test/resources/spell/engine-stage3-golden.txt");
  static final Path CONTENT = Path.of("multiplayer/server/src/main/resources/content/spells-v2.json");
  static final int CASTER_TILE_X = 7;
  static final int CASTER_TILE_Y = 10;
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
    {11L, 12, 10, false, false, new int[] {100, 100, 100, 100, 100, 100}},
    {12L, 10, 12, false, false, new int[] {60, 100, 100, 100, 100, 100}},
    {13L, 8, 8, false, false, new int[] {100, 100, 100, 100, 100, 100}},
    {14L, 12, 12, false, false, new int[] {100, 100, 100, 100, 100, 100}},
    {15L, 20, 10, false, false, new int[] {100, 100, 100, 100, 100, 100}},
  };
  /** The tile an aimed-at-the-ground cast targets. */
  static final int[] GROUND = {11, 10};

  @BeforeAll
  static void loadItems() {
    ItemRegistry.resetAdditionalDefinitions();
    ItemJsonLoader.loadAndRegister();
  }

  private static final class FixtureMonster extends BaseMonster {
    FixtureMonster(int[] resistanceInElementOrder) throws GameException {
      super("Test monster", 0f, 0f, 10, 0, 0, 0, 1, 1, 1_000L, null, null, null, null, null);
      int[] slots = {3, 1, 0, 2, 5, 4};
      for (int element = 0; element < 6; element++) combatResists[slots[element]] = resistanceInElementOrder[element];
    }
  }

  static SpellContentV2 content() throws Exception {
    return new Gson().fromJson(Files.readString(CONTENT), SpellContentV2.class);
  }

  static List<SpellContentV2.Spell> stageThree() throws Exception {
    var spells = new ArrayList<SpellContentV2.Spell>();
    content().spells().values().stream().filter(spell -> spell.stage() == 3).forEach(spells::add);
    spells.sort(java.util.Comparator.comparing(SpellContentV2.Spell::key));
    return spells;
  }

  /** The casts a spell is exercised with: aimed at a creature, at the ground, or self-centred. */
  static List<String> scenarios(SpellContentV2.Spell spell) {
    return switch (spell.kind()) {
      case AREA_DAMAGE -> switch (spell.targetType()) {
        case 18 -> List.of("self");
        case 19 -> List.of("unit", "pos");
        default -> List.of("unit");
      };
      case DOT -> List.of("unit");
      default -> List.of("self");
    };
  }

  static final String[] CASTERS = {"plain", "skilled", "geared"};

  private static Vector2 at(Object[] row) {
    return new Vector2(((Integer) row[1]) * GameConstants.GRID_W, ((Integer) row[2]) * GameConstants.GRID_H);
  }

  static String scenario(SpellContentV2.Spell data, int keyIndex, String casterName, int casterIndex, int scenarioIndex)
      throws Exception {
    String scenario = scenarios(data).get(scenarioIndex);
    SpellData spell = SpellRegistry.findByName(data.key());
    assertNotNull(spell, data.key());
    Player player = SpellContextParityTest.player(casterName);
    player.setMana(10_000);
    player.setSpells(new ArrayList<>(List.of(spell.getKey())));
    long seed = 3_000L + keyIndex * 13L + casterIndex * 3L + scenarioIndex;
    var monsters = new HashMap<Long, FixtureMonster>();
    for (Object[] row : CREATURES) monsters.put((Long) row[0], new FixtureMonster((int[]) row[5]));
    var out = new StringBuilder(data.key() + " " + casterName + " " + scenario);
    try (RandomSource.Scope ignored = RandomSource.use(new Random(seed))) {
      Vector2 casterAt = new Vector2(CASTER_TILE_X * GameConstants.GRID_W, CASTER_TILE_Y * GameConstants.GRID_H);
      SpellCastingService.TargetKind kind;
      float distance = 0f;
      Object[] primaryRow = CREATURES[0];
      Vector2 ground = new Vector2(GROUND[0] * GameConstants.GRID_W, GROUND[1] * GameConstants.GRID_H);
      switch (scenario) {
        case "unit" -> {
          kind = SpellCastingService.TargetKind.HOSTILE_UNIT;
          distance = casterAt.dst(at(primaryRow)) / Math.max(GameConstants.GRID_W, GameConstants.GRID_H);
        }
        case "pos" -> {
          kind = SpellCastingService.TargetKind.POSITION;
          distance = casterAt.dst(ground) / Math.max(GameConstants.GRID_W, GameConstants.GRID_H);
        }
        default -> kind = SpellCastingService.TargetKind.SELF;
      }
      SpellCastingService.Result cast =
          SpellCastingService.begin(new SpellCastingService.Request(spell, player, kind, distance, true, false, true));
      out.append(" verdict=").append(cast.failure().name()).append(" mana=").append(cast.manaSpent());
      if (!cast.success()) return out.toString();
      var manager = new SpellEffectManager();
      switch (data.kind()) {
        case AREA_HEAL, SHIELD, GATEWAY -> {
          // MainGameScreen.castDefensiveSpell: the heal roll, then nothing else for these spells.
          int delta = manager.resolvePlayerHealthDelta(spell, player);
          if (delta > 0) out.append(" heal=").append(delta);
        }
        case DOT -> {
          var primary = monsters.get((Long) primaryRow[0]);
          Vector2 center = at(primaryRow);
          boolean residual = spell.isAttack() && spell.getRadius() <= 0;
          List<Object[]> secondary = residual
              ? SpellAreaTargeting.nearest(
                  List.of(CREATURES),
                  row -> row != primaryRow && eligible(row)
                      && SpellAreaTargeting.isAdjacent(center.x, center.y, at(row).x, at(row).y),
                  row -> center.dst2(at(row)), 1)
              : List.of();
          var impact = manager.resolve(spell, player, primary, 0d);
          if (impact.healthDelta() < 0) out.append(" primary=").append(-impact.healthDelta());
          for (Object[] row : secondary) {
            int damage = manager.resolveResidualDamage(spell, player, monsters.get((Long) row[0]));
            if (damage > 0) out.append(" res").append(row[0]).append('=').append(damage);
          }
          manager.installTimedHooks(spell, player, primary);
        }
        case AREA_DAMAGE -> {
          boolean selfCentred = scenario.equals("self");
          Vector2 center = selfCentred ? casterAt : scenario.equals("unit") ? at(primaryRow) : ground;
          Object[] primary = selfCentred || scenario.equals("pos") ? null : primaryRow;
          List<Object[]> targets = SpellAreaTargeting.nearest(
              List.of(CREATURES),
              row -> {
                if (row == primary || !eligible(row)) return false;
                Vector2 pos = at(row);
                return selfCentred
                    ? SpellAreaTargeting.contains(spell, center.x, center.y, pos.x, pos.y)
                    : center.dst(pos) / Math.max(GameConstants.GRID_W, GameConstants.GRID_H) < spell.getRadius();
              },
              row -> center.dst2(at(row)), primary == null ? 0 : 1);
          if (primary != null) {
            var impact = manager.resolve(spell, player, monsters.get((Long) primary[0]), 0d);
            if (impact.healthDelta() < 0) out.append(" primary=").append(-impact.healthDelta());
          }
          for (Object[] row : targets) {
            Vector2 pos = at(row);
            double range = selfCentred
                ? SpellAreaTargeting.distanceInTiles(center.x, center.y, pos.x, pos.y)
                : center.dst(pos) / Math.max(GameConstants.GRID_W, GameConstants.GRID_H);
            var impact = manager.resolve(spell, player, monsters.get((Long) row[0]), range);
            if (impact.healthDelta() < 0) out.append(" a").append(row[0]).append('=').append(-impact.healthDelta());
          }
        }
        default -> throw new AssertionError("Not a stage-3 kind " + data.kind());
      }
    }
    return out.toString();
  }

  private static boolean eligible(Object[] row) {
    return !(Boolean) row[3] && !(Boolean) row[4];
  }

  static String produce() throws Exception {
    var text = new StringBuilder("# Stage-3 spell outcomes from the single-player code path; the engine must equal them.\n");
    var spells = stageThree();
    for (int k = 0; k < spells.size(); k++)
      for (int c = 0; c < CASTERS.length; c++)
        for (int s = 0; s < scenarios(spells.get(k)).size(); s++)
          text.append(scenario(spells.get(k), k, CASTERS[c], c, s)).append('\n');
    return text.toString();
  }

  @Test
  void fortyTwoSpellsMatchTheCommittedGolden() throws Exception {
    assertEquals(42, stageThree().size());
    String text = produce();
    if (Boolean.getBoolean("spell.golden.update")) {
      Files.createDirectories(GOLDEN.getParent());
      Files.writeString(GOLDEN, text);
    }
    assertEquals(Files.readString(GOLDEN).replace("\r\n", "\n"), text,
        "Regenerate with -Dspell.golden.update=true and review the diff");
  }

  /**
   * Why Poison, Poison Arrow and Plague are one hit online: the tick spells they link do not exist in
   * single-player, so no timer is ever installed for them. If someone defines one, these spells start to
   * tick offline and this test fails: the server then needs a periodic effect before the stage tag may
   * stay at 3 (and the exporter will tag the spell stage 4 by itself).
   */
  @Test
  void theTickSpellsThePoisonsLinkAreNotDefinedSoNothingTicks() throws Exception {
    for (int id : new int[] {10021, 10024, 10105}) assertNull(SpellRegistry.findById(id), "spell id " + id);
    assertNotNull(SpellRegistry.findById(10151), "Entangle's linked effect is real, which is why it stays beyond stage 3");
    for (var spell : stageThree()) {
      if (spell.kind() != SpellContentV2.Kind.DOT) continue;
      for (var effect : spell.effects()) {
        if (effect.type() != 9) continue;
        int linked = Integer.parseInt(effect.params().stream().filter(p -> p.id() == 1).findFirst().orElseThrow().expression());
        assertNull(SpellRegistry.findById(linked), spell.key() + " links a defined spell " + linked);
      }
    }
    assertEquals(3, stageThree().stream().filter(spell -> spell.kind() == SpellContentV2.Kind.DOT).count());
    assertEquals(4, content().spells().get("spell.entangle").stage());
  }

  @Test
  void theFixtureReallyExercisesEveryAreaShapeTheCapAndTheIgnoredCreatures() throws Exception {
    String text = produce();
    assertTrue(text.contains(" pos verdict=NONE"), "ground-aimed casts");
    assertTrue(text.contains(" self verdict=NONE"), "self-centred casts");
    assertTrue(text.contains(" heal="), "area heals restore health");
    // Stationary (5), dead (6) and far-away (15) creatures are never hit.
    assertFalse(text.contains(" a5="));
    assertFalse(text.contains(" a6="));
    assertFalse(text.contains(" a15="));
    // The cap: some self-centred cast reaches exactly eight creatures although more are in range.
    assertTrue(text.lines().anyMatch(line -> line.startsWith("spell.earthquake")
        && line.split(" a[0-9]+=").length - 1 == 8), "an eight-target area");
    // A unit-centred cast holds one slot for the creature aimed at: primary plus seven.
    assertTrue(text.lines().anyMatch(line -> line.contains(" unit ") && line.contains(" primary=")
        && line.split(" a[0-9]+=").length - 1 == 7), "primary plus seven");
    // A ranged formula is used away from the centre: earthquake has no centre damage at all.
    assertTrue(text.lines().anyMatch(line -> line.startsWith("spell.earthquake") && line.contains(" a")));
    long expected = 0;
    for (var spell : stageThree()) expected += (long) CASTERS.length * scenarios(spell).size();
    assertEquals(expected, text.lines().filter(line -> line.startsWith("spell.")).count());
  }
}
