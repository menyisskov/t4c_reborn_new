package com.perso.T4C.harvest.definition;

import com.perso.T4C.harvest.HerbDefinition;

public final class Silverleaf {
  private Silverleaf() {}

  public static HerbDefinition definition() {
    return new HerbDefinition("silverleaf", "item.herb_silverleaf", "64kHerbSilverleaf", 40);
  }
}
