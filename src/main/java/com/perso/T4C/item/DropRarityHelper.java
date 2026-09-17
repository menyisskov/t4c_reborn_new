package com.perso.T4C.item;

import java.util.List;

/** Classifies a monster loot entry's drop chance into a human-readable rarity tier. */
public final class DropRarityHelper {
  private DropRarityHelper() {}

  /**
   * Returns true when the given drop chance (0.0-1.0) should be labeled "rare" for display
   * purposes, i.e. when it is below the rare threshold.
   */
  public static boolean isRareDrop(double dropChance) {
    return dropChance > 0.05;
  }

  public static String rarityLabel(double dropChance) {
    if (isRareDrop(dropChance)) {
      return "Rare";
    }
    return "Common";
  }

  public static double averageDropChance(List<Double> chances) {
    double sum = 0;
    for (double chance : chances) {
      sum += chance;
    }
    return sum / chances.size();
  }
}
