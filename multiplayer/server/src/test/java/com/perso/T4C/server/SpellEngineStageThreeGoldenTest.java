package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.SpellContentV2;
import com.perso.T4C.spell.SpellFormulaContexts.Elements;
import com.perso.T4C.spell.engine.SpellEngine;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Online half of the stage-3 engine golden: the same crowd as the root module's {@code
 * SpellEngineStageThreeGoldenTest}, resolved by {@link SpellEngine} from committed online characters,
 * must reproduce the text single-player's own code path produced - 25 area attacks, 4 area heals, the
 * three one-hit damage-over-time spells, the 3 shields and 7 gateways, for three casters. Also holds the
 * engine's stage-3 edge cases (range, sight, haven, the boundary of the radius, the cap).
 */
class SpellEngineStageThreeGoldenTest {
  private static final Path GOLDEN = Path.of("../../src/test/resources/spell/engine-stage3-golden.txt");
  static final String[] CASTERS = {"plain", "skilled", "geared"};
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
  static final int[] GROUND = {11, 10};

  static class Fixture implements SpellEngine.WorldView {
    final List<SpellEngine.UnitView> units = new ArrayList<>();
    boolean sight = true;
    boolean safe = true;
    /** Boxes the engine asked for, to prove an area spell reads only what it needs. */
    final List<double[]> asked = new ArrayList<>();

    @Override public SpellEngine.UnitView unit(long id) {
      return units.stream().filter(u -> u.id() == id).findFirst().orElse(null);
    }
    @Override public List<SpellEngine.UnitView> units() { return units; }
    @Override public List<SpellEngine.UnitView> unitsNear(double x, double y, double tx, double ty) {
      asked.add(new double[] {x, y, tx, ty});
      return units.stream().filter(u -> Math.abs(u.x() - x) <= tx && Math.abs(u.y() - y) <= ty).toList();
    }
    @Override public boolean combatSafe(double a, double b, double c, double d) { return safe; }
    @Override public boolean lineOfSight(double a, double b, double c, double d) { return sight; }

    static Fixture standard() {
      var fixture = new Fixture();
      for (Object[] row : CREATURES) {
        int[] r = (int[]) row[5];
        fixture.units.add(new SpellEngine.UnitView((Long) row[0], (Integer) row[1], (Integer) row[2],
            (Boolean) row[4] ? 0 : 1_000_000, !(Boolean) row[3],
            new Elements(norm(r[0]), norm(r[1]), norm(r[2]), norm(r[3]), norm(r[4]), norm(r[5]))));
      }
      return fixture;
    }
    private static int norm(int value) { return value <= 0 ? 100 : value; }
  }

  static SpellContentV2 content() throws Exception { return SpellEngineGoldenTest.content(); }

  static List<SpellContentV2.Spell> stageThree() throws Exception {
    var spells = new ArrayList<SpellContentV2.Spell>();
    content().spells().values().stream().filter(s -> s.stage() == 3).forEach(spells::add);
    spells.sort(java.util.Comparator.comparing(SpellContentV2.Spell::key));
    return spells;
  }

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

  static SpellEngine.Target target(String scenario) {
    return switch (scenario) {
      case "unit" -> new SpellEngine.Target.Unit(1);
      case "pos" -> new SpellEngine.Target.Position(GROUND[0], GROUND[1]);
      default -> new SpellEngine.Target.Self();
    };
  }

  static String scenario(SpellContentV2.Spell spell, int keyIndex, String name, int casterIndex, int scenarioIndex)
      throws Exception {
    var items = ItemCatalog.loadBundled();
    var progress = OnlineSpellContextParityTest.character(name, items);
    var caster = OnlineSpellContexts.caster(progress, items);
    String scenario = scenarios(spell).get(scenarioIndex);
    var request = new SpellEngine.CastRequest(spell, caster, 7, 10, target(scenario), true, 10_000, false, false, false);
    var resolved = SpellEngine.cast(request, Fixture.standard(), new Random(3_000L + keyIndex * 13L + casterIndex * 3L + scenarioIndex));
    var out = new StringBuilder(spell.key() + " " + name + " " + scenario + " verdict=" + resolved.verdict().name());
    out.append(" mana=").append(resolved.costs() == null ? 0 : resolved.costs().manaCost());
    for (var event : resolved.events()) {
      switch (event) {
        case SpellEngine.Event.SelfHeal h -> out.append(" heal=").append(h.amount());
        case SpellEngine.Event.Damage d -> {
          switch (d.kind()) {
            case DIRECT -> out.append(" primary=");
            case RESIDUAL -> out.append(" res").append(d.target()).append('=');
            case AREA -> out.append(" a").append(d.target()).append('=');
          }
          out.append(d.amount());
        }
        default -> throw new AssertionError("Stage 3 spells emit no " + event);
      }
    }
    return out.toString();
  }

  @Test void engineReproducesTheSinglePlayerGoldenForAllFortyTwoSpells() throws Exception {
    var spells = stageThree();
    assertEquals(42, spells.size());
    var text = new StringBuilder("# Stage-3 spell outcomes from the single-player code path; the engine must equal them.\n");
    for (int k = 0; k < spells.size(); k++)
      for (int c = 0; c < CASTERS.length; c++)
        for (int s = 0; s < scenarios(spells.get(k)).size(); s++)
          text.append(scenario(spells.get(k), k, CASTERS[c], c, s)).append('\n');
    assertEquals(Files.readString(GOLDEN).replace("\r\n", "\n"), text.toString());
  }

  // ---- edge cases ----

  private static SpellEngine.CastRequest cast(String key, SpellEngine.Target target, double x, double y, int mana)
      throws Exception {
    var items = ItemCatalog.loadBundled();
    var caster = OnlineSpellContexts.caster(OnlineSpellContextParityTest.character("plain", items), items);
    return new SpellEngine.CastRequest(content().spells().get(key), caster, x, y, target, true, mana, false, false, false);
  }

  private static long hits(SpellEngine.CastResolution resolution) {
    return resolution.events().stream().filter(e -> e instanceof SpellEngine.Event.Damage).count();
  }

  @Test void shapesTheRulesRefuseAreWrongTargetsAndNothingIsRolledForThem() throws Exception {
    var world = Fixture.standard();
    // Fireball is aimed at a creature: not self, not the ground. Earthquake is self-centred only.
    assertEquals("WRONG_TARGET", SpellEngine.cast(cast("spell.fireball", new SpellEngine.Target.Self(), 7, 10, 500), world, new Random(1)).verdict().name());
    assertEquals("WRONG_TARGET", SpellEngine.cast(cast("spell.fireball", new SpellEngine.Target.Position(10, 10), 7, 10, 500), world, new Random(1)).verdict().name());
    assertEquals("WRONG_TARGET", SpellEngine.cast(cast("spell.earthquake", new SpellEngine.Target.Unit(1), 7, 10, 500), world, new Random(1)).verdict().name());
    assertEquals("WRONG_TARGET", SpellEngine.cast(cast("spell.earthquake", new SpellEngine.Target.Position(10, 10), 7, 10, 500), world, new Random(1)).verdict().name());
    assertEquals("WRONG_TARGET", SpellEngine.cast(cast("spell.hurricane", new SpellEngine.Target.Self(), 7, 10, 500), world, new Random(1)).verdict().name());
    // An area heal is a self cast; the ground is not a heal target.
    assertEquals("WRONG_TARGET", SpellEngine.cast(cast("spell.mass_healing", new SpellEngine.Target.Position(10, 10), 7, 10, 500), world, new Random(1)).verdict().name());
  }

  @Test void groundCastsCheckRangeSightAndHavenBeforeTheyHurtAnyone() throws Exception {
    var world = Fixture.standard();
    var ground = new SpellEngine.Target.Position(11, 10);
    assertEquals("OUT_OF_RANGE", SpellEngine.cast(cast("spell.hurricane", new SpellEngine.Target.Position(40, 10), 7, 10, 500), world, new Random(1)).verdict().name());
    world.sight = false;
    assertEquals("NO_LINE_OF_SIGHT", SpellEngine.cast(cast("spell.hurricane", ground, 7, 10, 500), world, new Random(1)).verdict().name());
    world.sight = true;
    world.safe = false;
    var haven = SpellEngine.cast(cast("spell.hurricane", ground, 7, 10, 500), world, new Random(1));
    assertEquals("SAFE_HAVEN", haven.verdict().name());
    assertTrue(haven.events().isEmpty());
    // A self-centred cast made from inside a safe haven is refused the same way.
    assertEquals("SAFE_HAVEN", SpellEngine.cast(cast("spell.earthquake", new SpellEngine.Target.Self(), 7, 10, 500), world, new Random(1)).verdict().name());
    world.safe = true;
    assertEquals("NOT_ENOUGH_MANA", SpellEngine.cast(cast("spell.hurricane", ground, 7, 10, 0), world, new Random(1)).verdict().name());
  }

  @Test void aSelfCentredBlastIncludesTheRadiusEdgeButAGroundBlastStopsShortOfIt() throws Exception {
    // Flame Wave: radius 5, self-centred, inclusive. A creature exactly 5 tiles east is hit; 6 is not.
    var world = new Fixture();
    world.units.add(new SpellEngine.UnitView(1, 12, 10, 1_000_000, true, Elements.uniform(100)));
    world.units.add(new SpellEngine.UnitView(2, 13, 10, 1_000_000, true, Elements.uniform(100)));
    var wave = SpellEngine.cast(cast("spell.flame_wave", new SpellEngine.Target.Self(), 7, 10, 500), world, new Random(2));
    assertEquals(1, hits(wave));
    assertEquals(1L, ((SpellEngine.Event.Damage) wave.events().get(0)).target());
    // Avalanche: radius 6 around the tile, strict. A creature exactly 6 tiles east of the tile is not hit.
    var ground = new Fixture();
    ground.units.add(new SpellEngine.UnitView(1, 15, 10, 1_000_000, true, Elements.uniform(100)));
    ground.units.add(new SpellEngine.UnitView(2, 14, 10, 1_000_000, true, Elements.uniform(100)));
    var avalanche = SpellEngine.cast(cast("spell.avalanche", new SpellEngine.Target.Position(9, 10), 7, 10, 500), ground, new Random(2));
    assertEquals(1, hits(avalanche));
    assertEquals(2L, ((SpellEngine.Event.Damage) avalanche.events().get(0)).target());
    // The ground rule measures y in half-size tiles over the larger side, so 11 tiles north is only 5.5 away.
    var tall = new Fixture();
    tall.units.add(new SpellEngine.UnitView(1, 9, 21, 1_000_000, true, Elements.uniform(100)));
    assertEquals(1, hits(SpellEngine.cast(cast("spell.avalanche", new SpellEngine.Target.Position(9, 10), 7, 10, 500), tall, new Random(2))));
  }

  @Test void aSpellStaysAwayFromWhatItMayNotHurt() throws Exception {
    var world = new Fixture();
    world.units.add(new SpellEngine.UnitView(1, 10, 10, 1_000_000, true, Elements.uniform(100)));
    world.units.add(new SpellEngine.UnitView(2, 10, 11, 1_000_000, false, Elements.uniform(100)));
    world.units.add(new SpellEngine.UnitView(3, 11, 10, 0, true, Elements.uniform(100)));
    var cast = SpellEngine.cast(cast("spell.fireball", new SpellEngine.Target.Unit(1), 7, 10, 500), world, new Random(3));
    assertEquals(1, hits(cast), "a stationary and a dead creature are left out");
    // The creature aimed at must be available and attackable, like any attack.
    assertEquals("creature_unavailable", SpellEngine.cast(cast("spell.fireball", new SpellEngine.Target.Unit(3), 7, 10, 500), world, new Random(3)).refusal());
    assertEquals("protected_target", SpellEngine.cast(cast("spell.fireball", new SpellEngine.Target.Unit(2), 7, 10, 500), world, new Random(3)).refusal());
  }

  @Test void anAreaOnlyReadsTheBoxItCanReach() throws Exception {
    var world = Fixture.standard();
    SpellEngine.cast(cast("spell.boulders", new SpellEngine.Target.Unit(1), 7, 10, 500), world, new Random(4));
    assertEquals(1, world.asked.size());
    var box = world.asked.get(0);
    assertArrayEquals(new double[] {10, 10, 3, 5}, box, 1e-9, "radius 2: x within 3, y within 5 tiles");
  }

  @Test void aOneHitDamageOverTimeCastSpawnsNoSplashUnlessItIsAnAttack() throws Exception {
    var world = Fixture.standard();
    var poison = SpellEngine.cast(cast("spell.poison", new SpellEngine.Target.Unit(1), 7, 10, 500), world, new Random(5));
    assertTrue(poison.events().stream().noneMatch(e -> e instanceof SpellEngine.Event.Damage d && d.kind() == SpellEngine.DamageKind.RESIDUAL));
    var arrow = SpellEngine.cast(cast("spell.poison_arrow", new SpellEngine.Target.Unit(1), 7, 10, 500), world, new Random(5));
    assertTrue(arrow.events().stream().anyMatch(e -> e instanceof SpellEngine.Event.Damage d && d.kind() == SpellEngine.DamageKind.RESIDUAL));
  }

  @Test void entangleAndThePortalsStayBeyondStageThree() throws Exception {
    assertEquals(4, content().spells().get("spell.entangle").stage());
    assertThrows(IllegalArgumentException.class, () -> SpellEngine.cast(cast("spell.entangle", new SpellEngine.Target.Unit(1), 7, 10, 500),
        Fixture.standard(), new Random(1)));
  }
}
