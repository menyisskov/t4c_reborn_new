package com.perso.T4C.monster.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.json.MonsterJsonLoader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * T4C-0071: several monsters (mostly newer fork zones - the Sandglass Sentinel ladder, Tideworn
 * Reaver, a handful of Skraug variants) had drifted well under the roster's own established gold
 * curve, to the point a level 300 monster was paying less gold than a level 30 one nearby - the
 * owner's own complaint that started this pass. This pins the floor so new content can't silently
 * repeat it; see {@link MonsterGoldCurve} for the formula and where it came from.
 *
 * <p>A monster with {@code goldMax <= 1} is exempt - that is this roster's existing convention for
 * "not meant to pay real gold" (arena/training dummies, a few verbatim ported original-game
 * oddities), not a broken value.
 */
class MonsterGoldCurveTest {
  private static final double FLOOR_FRACTION_OF_CURVE = 0.6d;

  @AfterEach
  void resetRegistry() {
    MonsterRegistry.resetToGeneratedDefinitions();
  }

  @Test
  void noMonstersGoldFallsWellBelowTheLevelCurve() {
    MonsterJsonLoader.loadAndRegister();
    List<String> tooLow = new ArrayList<>();
    for (MonsterDef def : MonsterRegistry.load()) {
      if (def.getLevel() <= 0 || def.getGoldMax() <= 1) continue;
      int floor = (int) Math.floor(MonsterGoldCurve.goldMax(def.getLevel()) * FLOOR_FRACTION_OF_CURVE);
      if (def.getGoldMax() < floor) {
        tooLow.add(
            def.getName()
                + " (level "
                + def.getLevel()
                + "): goldMax="
                + def.getGoldMax()
                + ", expected at least "
                + floor);
      }
    }
    assertTrue(
        tooLow.isEmpty(),
        "these monsters pay well below the roster's established gold-per-level curve: " + tooLow);
  }
}
