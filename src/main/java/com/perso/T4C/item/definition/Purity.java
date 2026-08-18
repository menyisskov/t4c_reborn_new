package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Purity {
  private Purity() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.purity",
        "${item.purity}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvShortSword",
        0L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        150L,
        0L,
        0L,
        30L,
        1.0d,
        false,
        false,
        false,
        40848,
        1,
        1,
        "1d42+78",
        "if(750-self.agi/250*750/2<600?600:750-self.agi/250*750/2)+1d375",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10173, 0, 100)),
        List.of(new ItemDefinition.ItemBoost(550, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
