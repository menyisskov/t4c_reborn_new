package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.online.model.SpellContentV2;
import com.perso.T4C.spell.SpellFormulaContexts.Elements;
import com.perso.T4C.spell.engine.SpellEngine;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Online half of the stage-1 engine golden: the same fixture as the root module's {@code
 * SpellEngineStageOneGoldenTest}, resolved by {@link SpellEngine} from committed online characters,
 * must reproduce the text single-player's own code path produced. Also the home of the engine's edge cases.
 */
class SpellEngineGoldenTest {
  private static final Path GOLDEN = Path.of("../../src/test/resources/spell/engine-stage1-golden.txt");
  static final String[] CASTERS = {"plain", "skilled", "geared"};
  /** id, tileX, tileY, stationary, dead, resistance (fire, earth, air, water, light, dark; 0 = unset). */
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

  /** A fixed world: every unit visible, no safe havens, unless a test overrides it. */
  static class Fixture implements SpellEngine.WorldView {
    final List<SpellEngine.UnitView> units = new ArrayList<>();
    boolean sight = true;
    boolean safe = true;

    @Override public SpellEngine.UnitView unit(long id) {
      return units.stream().filter(u -> u.id() == id).findFirst().orElse(null);
    }
    @Override public List<SpellEngine.UnitView> units() { return units; }
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

  static SpellContentV2 content() throws Exception {
    try (var input = SpellCatalog.class.getResourceAsStream("/content/spells-v2.json")) {
      return new Gson().fromJson(new String(input.readAllBytes(), StandardCharsets.UTF_8), SpellContentV2.class);
    }
  }

  static String scenario(SpellContentV2.Spell spell, int keyIndex, String name, int casterIndex) throws Exception {
    var items = ItemCatalog.loadBundled();
    var progress = OnlineSpellContextParityTest.character(name, items);
    var caster = OnlineSpellContexts.caster(progress, items);
    boolean heal = spell.kind() == SpellContentV2.Kind.HEAL;
    var request = new SpellEngine.CastRequest(spell, caster, 7, 10, heal ? new SpellEngine.Target.Self() : new SpellEngine.Target.Unit(1),
        true, 10_000, false, false, false);
    var resolved = SpellEngine.cast(request, Fixture.standard(), new Random(1_000L + keyIndex * 7L + casterIndex));
    var out = new StringBuilder(spell.key() + " " + name + " verdict=" + resolved.verdict().name());
    out.append(" mana=").append(resolved.costs() == null ? 0 : resolved.costs().manaCost());
    for (var event : resolved.events()) {
      switch (event) {
        case SpellEngine.Event.SelfHeal h -> out.append(" heal=").append(h.amount());
        case SpellEngine.Event.DrainHeal d -> out.append(" drain=").append(d.amount());
        case SpellEngine.Event.Damage d -> out.append(d.kind() == SpellEngine.DamageKind.DIRECT ? " primary=" : " res" + d.target() + "=")
            .append(d.amount());
      }
    }
    return out.toString();
  }

  @Test void engineReproducesTheSinglePlayerGoldenForAllFortyEightSpells() throws Exception {
    var keys = new ArrayList<String>();
    content().spells().values().stream().filter(s -> s.stage() == 1).forEach(s -> keys.add(s.key()));
    java.util.Collections.sort(keys);
    assertEquals(48, keys.size());
    var text = new StringBuilder("# Stage-1 spell outcomes from the single-player code path; the engine must equal them.\n");
    for (int k = 0; k < keys.size(); k++)
      for (int c = 0; c < CASTERS.length; c++) text.append(scenario(content().spells().get(keys.get(k)), k, CASTERS[c], c)).append('\n');
    assertEquals(Files.readString(GOLDEN).replace("\r\n", "\n"), text.toString());
  }

  // ---- edge cases ----

  private static SpellEngine.CastRequest fireDart(SpellEngine.Target target, int mana, boolean cooldown, boolean exhausted,
      double casterX) throws Exception {
    var items = ItemCatalog.loadBundled();
    var caster = OnlineSpellContexts.caster(OnlineSpellContextParityTest.character("plain", items), items);
    return new SpellEngine.CastRequest(content().spells().get("spell.fire_dart"), caster, casterX, 10, target, true, mana,
        false, cooldown, exhausted);
  }

  @Test void outOfRangeNoSightNoManaExhaustedAndUnknownTargetsAreRefusedWithoutEvents() throws Exception {
    var world = Fixture.standard();
    var unit = new SpellEngine.Target.Unit(1);
    var far = SpellEngine.cast(fireDart(unit, 100, false, false, -20), world, new Random(1));
    assertEquals("OUT_OF_RANGE", far.verdict().name());
    world.sight = false;
    assertEquals("NO_LINE_OF_SIGHT", SpellEngine.cast(fireDart(unit, 100, false, false, 7), world, new Random(1)).verdict().name());
    world.sight = true;
    assertEquals("NOT_ENOUGH_MANA", SpellEngine.cast(fireDart(unit, 0, false, false, 7), world, new Random(1)).verdict().name());
    assertEquals("EXHAUSTED", SpellEngine.cast(fireDart(unit, 100, false, true, 7), world, new Random(1)).verdict().name());
    assertEquals("COOLDOWN", SpellEngine.cast(fireDart(unit, 100, true, false, 7), world, new Random(1)).verdict().name());
    world.safe = false;
    assertEquals("SAFE_HAVEN", SpellEngine.cast(fireDart(unit, 100, false, false, 7), world, new Random(1)).verdict().name());
    world.safe = true;
    assertEquals("creature_unavailable", SpellEngine.cast(fireDart(new SpellEngine.Target.Unit(6), 100, false, false, 7), world, new Random(1)).refusal(),
        "a dead target");
    assertEquals("creature_unavailable", SpellEngine.cast(fireDart(new SpellEngine.Target.Unit(99), 100, false, false, 7), world, new Random(1)).refusal());
    assertEquals("protected_target", SpellEngine.cast(fireDart(new SpellEngine.Target.Unit(5), 100, false, false, 7), world, new Random(1)).refusal());
    assertEquals("WRONG_TARGET", SpellEngine.cast(fireDart(new SpellEngine.Target.Self(), 100, false, false, 7), world, new Random(1)).verdict().name());
    assertEquals("WRONG_TARGET", SpellEngine.cast(fireDart(new SpellEngine.Target.Position(3, 3), 100, false, false, 7), world, new Random(1)).verdict().name());
  }

  @Test void splashNeverReachesBeyondSevenNeighboursOrThroughWalls() throws Exception {
    var world = Fixture.standard();
    // Put eight eligible neighbours in the ring: the cap leaves seven splash slots.
    world.units.clear();
    world.units.add(new SpellEngine.UnitView(1, 10, 10, 1000, true, Elements.uniform(100)));
    long id = 2;
    for (int dx = -1; dx <= 1; dx++)
      for (int dy = -1; dy <= 1; dy++)
        if (dx != 0 || dy != 0) world.units.add(new SpellEngine.UnitView(id++, 10 + dx, 10 + dy, 1000, true, Elements.uniform(100)));
    var cast = SpellEngine.cast(fireDart(new SpellEngine.Target.Unit(1), 100, false, false, 7), world, new Random(5));
    long splash = cast.events().stream().filter(e -> e instanceof SpellEngine.Event.Damage d && d.kind() == SpellEngine.DamageKind.RESIDUAL).count();
    assertEquals(7, splash);
    assertTrue(cast.events().stream().allMatch(e -> !(e instanceof SpellEngine.Event.Damage d) || d.amount() >= 1));
  }
}
