package com.perso.T4C.harvest.definition;

import com.perso.T4C.harvest.HerbDefinition;

public final class Goldroot {
  private Goldroot() {}

  public static HerbDefinition definition() {
    return new HerbDefinition("goldroot", "item.herb_goldroot", "64kHerbGoldroot", 15);
  }
}
