package com.perso.T4C.harvest.definition;

import com.perso.T4C.harvest.HerbDefinition;

public final class Moonthistle {
  private Moonthistle() {}

  public static HerbDefinition definition() {
    return new HerbDefinition("moonthistle", "item.herb_moonthistle", "64kHerbMoonthistle", 25);
  }
}
