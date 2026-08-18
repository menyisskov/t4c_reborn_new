package com.perso.T4C.harvest.definition;

import com.perso.T4C.harvest.HerbDefinition;

public final class Emberbloom {
  private Emberbloom() {}

  public static HerbDefinition definition() {
    return new HerbDefinition("emberbloom", "item.herb_emberbloom", "64kHerbEmberbloom", 20);
  }
}
