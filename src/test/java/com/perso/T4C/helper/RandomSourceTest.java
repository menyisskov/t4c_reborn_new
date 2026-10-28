package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellEffectManager;
import com.perso.T4C.spell.SpellHealRules;
import com.perso.T4C.spell.SpellRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

/**
 * Pins the spell random seam. Equivalence evidence: the expected values below are computed by
 * re-implementing the pre-change algorithm (one {@code nextInt(faces) + 1} per die, left to right;
 * {@code nextInt(low, high + 1)} for the min/max fallback; {@code nextInt(101) <= chance}) over a
 * second generator with the same seed, because the pre-change code could only draw from the
 * unseedable thread-local source.
 */
class RandomSourceTest {
  private static final long SEED = 20261028L;

  /** Counts every draw so tests can pin the number of draws, not just the values. */
  private static final class CountingRandom extends Random {
    int draws;

    CountingRandom(long seed) {
      super(seed);
    }

    @Override
    public int nextInt(int bound) {
      draws++;
      return super.nextInt(bound);
    }
  }

  @Test
  void defaultSourceIsTheThreadLocalRandom() {
    assertSame(ThreadLocalRandom.current(), RandomSource.current());
  }

  @Test
  void defaultDiceStillComeFromTheThreadLocalSourceAndStayInRange() {
    DiceFormula formula = DiceFormula.of("3d6+2");
    for (int i = 0; i < 2000; i++) {
      int value = formula.evaluate(DiceFormula.Context.ZERO);
      assertTrue(value >= 5 && value <= 20, "value " + value);
    }
  }

  @Test
  void seededDiceMatchTheOldAlgorithmValueForValue() {
    String[] formulas = {"1d20", "3d6+2", "2d8+1d4", "(1d6)d4", "round(2d10/3)", "4d6*2-1"};
    try (RandomSource.Scope ignored = RandomSource.use(new Random(SEED))) {
      Random old = new Random(SEED);
      for (int round = 0; round < 50; round++) {
        assertEquals(
            oldDice(old, 1, 20), DiceFormula.of(formulas[0]).evaluate(DiceFormula.Context.ZERO));
        assertEquals(
            oldDice(old, 3, 6) + 2, DiceFormula.of(formulas[1]).evaluate(DiceFormula.Context.ZERO));
        int a = oldDice(old, 2, 8);
        int b = oldDice(old, 1, 4);
        assertEquals(a + b, DiceFormula.of(formulas[2]).evaluate(DiceFormula.Context.ZERO));
        int count = oldDice(old, 1, 6);
        assertEquals(
            oldDice(old, count, 4), DiceFormula.of(formulas[3]).evaluate(DiceFormula.Context.ZERO));
        assertEquals(
            (int) Math.round(oldDice(old, 2, 10) / 3.0),
            DiceFormula.of(formulas[4]).evaluate(DiceFormula.Context.ZERO));
        assertEquals(
            oldDice(old, 4, 6) * 2 - 1,
            DiceFormula.of(formulas[5]).evaluate(DiceFormula.Context.ZERO));
      }
    }
  }

  @Test
  void drawCountIsOnePerDieAndMinMaxDrawNothing() {
    CountingRandom counting = new CountingRandom(SEED);
    try (RandomSource.Scope ignored = RandomSource.use(counting)) {
      DiceFormula.of("3d6+2d4").evaluate(DiceFormula.Context.ZERO);
      assertEquals(5, counting.draws);
      DiceFormula.of("3d6+2d4").min();
      DiceFormula.of("3d6+2d4").max();
      assertEquals(5, counting.draws);
    }
  }

  @Test
  void healFallbackMatchesTheOldRange() {
    try (RandomSource.Scope ignored = RandomSource.use(new Random(SEED))) {
      Random old = new Random(SEED);
      for (int i = 0; i < 100; i++) {
        int expected = old.nextInt(10, 31);
        assertEquals(
            expected,
            SpellHealRules.healthDelta(
                List.of(), 10, 30, DiceFormula.Context.ZERO, RandomSource.current()));
      }
    }
  }

  @Test
  void spellEffectManagerPlayerHealUsesTheScopedSource() {
    SpellEffectManager manager = new SpellEffectManager();
    int compared = 0;
    for (SpellData spell : SpellRegistry.load()) {
      List<Integer> first = new ArrayList<>();
      List<Integer> second = new ArrayList<>();
      for (List<Integer> sink : List.of(first, second)) {
        try (RandomSource.Scope ignored = RandomSource.use(new Random(SEED))) {
          for (int i = 0; i < 5; i++)
            sink.add(manager.resolvePlayerHealthDelta(spell, new Player()));
        }
      }
      assertEquals(first, second, spell.getName());
      compared++;
    }
    assertTrue(compared > 100);
  }

  @Test
  void playerApplyHealDrawsOneValueFromTheScopedSource() {
    Player player = new Player();
    player.setMaxHp(10000);
    player.setCurrentHp(1);
    CountingRandom counting = new CountingRandom(SEED);
    int expected = new Random(SEED).nextInt(5, 51);
    try (RandomSource.Scope ignored = RandomSource.use(counting)) {
      player.applyHeal(5, 50);
    }
    // nextInt(low, high) on java.util.Random delegates to nextInt() bounded form, not
    // nextInt(bound)
    assertEquals(1 + expected, player.getCurrentHp());
  }

  @Test
  void overrideDoesNotLeakAcrossThreadsAndRestoresOnClose() throws Exception {
    RandomGenerator seeded = new Random(SEED);
    AtomicReference<RandomGenerator> seenOnOtherThread = new AtomicReference<>();
    try (RandomSource.Scope ignored = RandomSource.use(seeded)) {
      assertSame(seeded, RandomSource.current());
      Thread other = new Thread(() -> seenOnOtherThread.set(RandomSource.current()));
      other.start();
      other.join();
      assertNotSame(seeded, seenOnOtherThread.get());
      RandomGenerator inner = new Random(1);
      try (RandomSource.Scope nested = RandomSource.use(inner)) {
        assertSame(inner, RandomSource.current());
      }
      assertSame(seeded, RandomSource.current());
    }
    assertSame(ThreadLocalRandom.current(), RandomSource.current());
  }

  private static int oldDice(Random random, int count, int faces) {
    if (count <= 0 || faces <= 0) return 0;
    int total = 0;
    for (int i = 0; i < count; i++) total += random.nextInt(faces) + 1;
    return total;
  }
}
