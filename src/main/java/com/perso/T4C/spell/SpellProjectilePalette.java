package com.perso.T4C.spell;

import java.util.Set;

public final class SpellProjectilePalette {
  private SpellProjectilePalette() {}

  private static final Set<Integer> BLUE =
      Set.of(
          30044, 30052, 30058, 30064, 30080, 30085, 30086, 30091, 30095, 30097, 30099, 30108, 30151,
          30159, 30167, 30175, 30183, 30191, 30199, 30207, 30215, 30223, 30231, 30235, 30243, 30251,
          30259, 30267, 30275, 30283, 30291, 30299);
  private static final Set<Integer> YELLOW =
      Set.of(
          30045, 30050, 30053, 30059, 30065, 30081, 30083, 30087, 30100, 30154, 30162, 30170, 30178,
          30186, 30194, 30202, 30210, 30218, 30226, 30234, 30238, 30246, 30254, 30262, 30270, 30278,
          30286, 30294, 30302);
  private static final Set<Integer> BLACK =
      Set.of(
          30048, 30062, 30069, 30071, 30089, 30093, 30103, 30112, 30115, 30118, 30122, 30149, 30157,
          30165, 30173, 30181, 30189, 30197, 30205, 30213, 30221, 30229, 30241, 30249, 30257, 30265,
          30273, 30281, 30289, 30297);
  private static final Set<Integer> PURPLE =
      Set.of(
          30049, 30055, 30063, 30067, 30075, 30079, 30084, 30090, 30094, 30104, 30107, 30110, 30116,
          30119, 30123, 30150, 30158, 30166, 30174, 30182, 30190, 30198, 30206, 30214, 30222, 30230,
          30242, 30250, 30258, 30266, 30274, 30282, 30290, 30298);

  public static String projectileFor(int visualEffect, String importedProjectile) {
    if (BLUE.contains(visualEffect)) return "64kSpellEnergyBallBlue-";
    if (YELLOW.contains(visualEffect)) return "64kSpellEnergyBallYellow-";
    if (BLACK.contains(visualEffect)) return "64kSpellEnergyBallBlack-";
    if (PURPLE.contains(visualEffect)) return "64kSpellEnergyBallPurple-";
    return importedProjectile;
  }
}
