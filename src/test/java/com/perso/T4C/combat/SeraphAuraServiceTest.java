package com.perso.T4C.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SeraphAuraServiceTest {
  private static final SeraphAuraService.EffectiveStats STATS =
      new SeraphAuraService.EffectiveStats(10, 20, 30, 40, 50);

  @Test
  void recognizesOnlyOriginalWhiteAndBlackWingAppearancePrefixes() {
    assertTrue(SeraphAuraService.isSeraphWingAppearance("PupSeraphWhiteWings"));
    assertTrue(SeraphAuraService.isSeraphWingAppearance("PupSeraphWhiteWingsVariant"));
    assertTrue(SeraphAuraService.isSeraphWingAppearance("PupSeraphBlackWings"));
    assertTrue(SeraphAuraService.isSeraphWingAppearance("pupseraphwhitewingsLegacy"));
    assertFalse(SeraphAuraService.isSeraphWingAppearance("PupSeraphDarkWings"));
    assertFalse(SeraphAuraService.isSeraphWingAppearance(null));
  }

  @Test
  void synchronizesLegacyWingSavesOnlyOnce() throws Exception {
    Player player = new Player();
    player.getEquippedItems().put(BodyPart.BACK, "PupSeraphWhiteWings");
    assertTrue(SeraphAuraService.synchronize(player));
    assertEquals(1, player.getRebirthCount());
    assertTrue(player.hasBuff(SeraphAuraService.AURA_NAME));
    assertFalse(SeraphAuraService.synchronize(player));
  }

  @Test
  void removesAnOrphanAuraFromAMortalSave() throws Exception {
    Player player = new Player();
    player.applyBuff(
        SeraphAuraService.AURA_NAME,
        SeraphAuraService.AURA_DESCRIPTION,
        SeraphAuraService.AURA_ICON,
        null,
        true);
    assertTrue(SeraphAuraService.synchronize(player));
    assertFalse(player.hasBuff(SeraphAuraService.AURA_NAME));
    assertFalse(SeraphAuraService.synchronize(player));
  }

  @Test
  void onHitKeepsIndependentProcsAndTheOriginalDoubleHealing() {
    SeraphAuraService.OnHitResult result =
        SeraphAuraService.onHit(1, STATS, 0, new SequenceRandom(5, 0, 4, 6));
    assertTrue(result.healingTriggered());
    assertEquals(31, result.centralHealing());
    assertEquals(35, result.radialHealing());
    assertEquals(66, result.totalHealing());
    assertFalse(result.retaliationTriggered());
    assertEquals(0, result.retaliationDamage());
  }

  @Test
  void retaliationFormulaAndResistanceThresholdMatchWda() {
    SeraphAuraService.OnHitResult vulnerable =
        SeraphAuraService.onHit(1, STATS, 4_999, new SequenceRandom(6, 5, 2));
    assertFalse(vulnerable.healingTriggered());
    assertTrue(vulnerable.retaliationTriggered());
    assertEquals(18, vulnerable.retaliationDamage());
    SeraphAuraService.OnHitResult immune =
        SeraphAuraService.onHit(1, STATS, 5_000, new SequenceRandom(6, 5));
    assertTrue(immune.retaliationTriggered());
    assertEquals(0, immune.retaliationDamage());
    SeraphAuraService.OnHitResult invulnerable =
        SeraphAuraService.onHit(1, STATS, 0, 65_000d, new SequenceRandom(6, 5, 2));
    assertTrue(invulnerable.retaliationTriggered());
    assertEquals(0, invulnerable.retaliationDamage());
  }

  @Test
  void chanceRollIsInclusiveButZeroChanceCanNeverProc() {
    assertTrue(SeraphAuraService.onAttackHit(1, new SequenceRandom(1)).triggered());
    assertFalse(SeraphAuraService.onAttackHit(1, new SequenceRandom(2)).triggered());
    assertTrue(SeraphAuraService.onAttackHit(100, new SequenceRandom(100)).triggered());
    assertFalse(SeraphAuraService.onAttackHit(0, new SequenceRandom()).triggered());
  }

  @Test
  void mortalPlayerCannotProcOnHitDespiteTheHealFormulaPlusFour() {
    SeraphAuraService.OnHitResult result =
        SeraphAuraService.onHit(0, STATS, 0, new SequenceRandom());
    assertFalse(result.healingTriggered());
    assertEquals(0, result.totalHealing());
    assertFalse(result.retaliationTriggered());
    assertEquals(0, result.retaliationDamage());
  }

  @Test
  void areaProcIsGlobalButDamageAndDiceAreResolvedPerTarget() {
    SeraphAuraService.OnAttackHitResult proc =
        SeraphAuraService.onAttackHit(3, new SequenceRandom(3));
    assertTrue(proc.triggered());
    assertEquals(6, proc.radius());
    assertEquals(29, SeraphAuraService.rollAreaDamage(STATS, 4_999, new SequenceRandom(0)));
    assertEquals(25, SeraphAuraService.rollAreaDamage(STATS, 4_999, new SequenceRandom(4)));
    assertEquals(29, SeraphAuraService.rollAreaDamage(STATS, 0, 64_999d, new SequenceRandom(0)));
    assertEquals(0, SeraphAuraService.rollAreaDamage(STATS, 5_000, new SequenceRandom()));
    assertEquals(0, SeraphAuraService.rollAreaDamage(STATS, 0, 65_000d, new SequenceRandom(2)));
  }

  private static final class SequenceRandom extends Random {
    private final int[] values;
    private int index;

    private SequenceRandom(int... values) {
      this.values = values;
    }

    @Override
    public int nextInt(int bound) {
      if (index >= values.length) {
        throw new AssertionError("Unexpected random roll with bound " + bound);
      }
      int value = values[index++];
      if (value < 0 || value >= bound) {
        throw new AssertionError("Random value " + value + " outside [0," + bound + ")");
      }
      return value;
    }
  }
}
