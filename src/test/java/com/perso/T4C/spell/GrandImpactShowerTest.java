package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * T4C-0064: an area attack spell's impact showers into more bursts the higher its tier, so a
 * level-400 ultra nuke reads as visibly grander than the low-tier spell it shares an impact sprite
 * with, while everything below the grand-tier threshold keeps the original single centered burst.
 */
class GrandImpactShowerTest {
  @Test
  void burstCountEscalatesWithEveryHighTier() {
    int previousCount = 0;
    for (int tier : HighTierSpellCurve.TIERS) {
      SpellData spell =
          HighTierSpellCurve.attack(
              "test_area_" + tier, 90000 + tier, HighTierSpellCurve.FIRE, tier,
              HighTierSpellCurve.Shape.AREA);
      int count = GrandImpactShower.bursts(spell, new Random(1)).size();
      assertTrue(
          count > previousCount, "tier " + tier + " (" + count + " bursts) should exceed tier below it");
      previousCount = count;
    }
  }

  @Test
  void firstBurstIsAlwaysCenteredWithNoDelay() {
    SpellData spell =
        HighTierSpellCurve.attack(
            "test_area_400", 99999, HighTierSpellCurve.FIRE, 400, HighTierSpellCurve.Shape.AREA);
    GrandImpactShower.Burst first = GrandImpactShower.bursts(spell, new Random(7)).get(0);
    assertEquals(0f, first.radiusFraction());
    assertEquals(0f, first.angleRadians());
    assertEquals(0f, first.delaySeconds());
  }

  @Test
  void belowGrandTierStaysASingleBurst() {
    SpellData spell =
        HighTierSpellCurve.attack(
            "fireball_like", 90001, HighTierSpellCurve.FIRE, 27, HighTierSpellCurve.Shape.AREA);
    List<GrandImpactShower.Burst> bursts = GrandImpactShower.bursts(spell, new Random(1));
    assertEquals(1, bursts.size());
  }

  @Test
  void boltShapeNeverShowersEvenAtHighTier() {
    SpellData spell =
        HighTierSpellCurve.attack(
            "test_bolt_400", 99998, HighTierSpellCurve.FIRE, 400, HighTierSpellCurve.Shape.BOLT);
    List<GrandImpactShower.Burst> bursts = GrandImpactShower.bursts(spell, new Random(1));
    assertEquals(1, bursts.size());
  }

  @Test
  void nonAttackSpellNeverShowers() {
    SpellData healSpell =
        new SpellData(
            "${spell.test_heal}",
            "${spell.description.test_heal}",
            "10",
            4,
            10,
            10,
            400,
            false,
            false,
            "icon",
            "proj",
            "impact",
            0,
            0,
            "sound.wav",
            "sound.wav",
            0,
            "0",
            "0",
            100,
            null,
            88888,
            5,
            15,
            SpellData.ATTACK_MENTAL,
            "100",
            "1000",
            "750",
            "750",
            0,
            0,
            true,
            List.of());
    assertEquals(1, GrandImpactShower.bursts(healSpell, new Random(1)).size());
  }
}
