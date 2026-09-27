package com.perso.T4C.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.SpriteBinIO;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/**
 * T4C-0077: every high-tier (250+) elemental impact has a complete, packed "-Ascended" frame set
 * with the same frame count and pixel dimensions as the base animation it was derived from (see
 * {@code tools.AscendedVfxGenerator}), so {@link com.perso.T4C.spell.HighTierSpellCurve} never
 * points at a partially-generated or missing sprite.
 */
class AscendedVfxAssetTest {
  private static final Map<String, String> BASE_TO_ASCENDED =
      Map.of(
          "GreatExplosion-", "GreatExplosion-Ascended-",
          "64kSpellFireCircle-", "64kSpellFireCircle-Ascended-",
          "64kSpellBoulders-", "64kSpellBoulders-Ascended-",
          "Flak1-", "Flak1-Ascended-",
          "GreatBolt-", "GreatBolt-Ascended-",
          "ElectricShield-", "ElectricShield-Ascended-",
          "64kSpellGlacier-", "64kSpellGlacier-Ascended-",
          "IceCloud-", "IceCloud-Ascended-",
          "HealingSpell-", "HealingSpell-Ascended-",
          "Curse-", "Curse-Ascended-");

  @Test
  void everyAscendedVariantMatchesItsBaseAnimationFrameForFrame() throws Exception {
    Map<String, Integer> baseCounts = new TreeMap<>();
    Map<String, Integer> ascendedCounts = new TreeMap<>();
    Map<String, int[]> baseDims = new HashMap<>();
    Map<String, int[]> ascendedDims = new HashMap<>();
    SpriteBinIO.readAll(
        Path.of("assets/sprites"),
        SpriteBinIO.DEFAULT_BASE_NAME,
        sprite -> {
          for (var entry : BASE_TO_ASCENDED.entrySet()) {
            if (startsWithIgnoreCase(sprite.name(), entry.getValue())) {
              ascendedCounts.merge(entry.getKey(), 1, Integer::sum);
              ascendedDims.putIfAbsent(sprite.name(), new int[] {sprite.width(), sprite.height()});
            } else if (startsWithIgnoreCase(sprite.name(), entry.getKey())) {
              baseCounts.merge(entry.getKey(), 1, Integer::sum);
              baseDims.putIfAbsent(sprite.name(), new int[] {sprite.width(), sprite.height()});
            }
          }
        });

    for (String base : BASE_TO_ASCENDED.keySet()) {
      int baseCount = baseCounts.getOrDefault(base, 0);
      int ascendedCount = ascendedCounts.getOrDefault(base, 0);
      assertTrue(baseCount > 0, "Base animation missing entirely: " + base);
      assertEquals(
          baseCount,
          ascendedCount,
          "Ascended variant of " + base + " has a different frame count than the base animation");
    }
  }

  private static boolean startsWithIgnoreCase(String name, String prefix) {
    return name.length() >= prefix.length()
        && name.regionMatches(true, 0, prefix, 0, prefix.length());
  }
}
