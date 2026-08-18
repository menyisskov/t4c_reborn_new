package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RustedSwordOfSorrow {
  private RustedSwordOfSorrow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rusted_sword_of_sorrow",
        "${item.rusted_sword_of_sorrow}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvShortSword",
        4102L,
        6L,
        0.0d,
        0L,
        0L,
        0L,
        76L,
        0L,
        30L,
        20L,
        1.0d,
        false,
        false,
        false,
        40140,
        1,
        1,
        "1d24+16",
        "if(750-self.agi/250*750/2<600?600:750-self.agi/250*750/2)+1d375",
        0,
        0,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(),
        List.of(
            new ItemDefinition.ItemBoost(152, 4, "-10", 0, 0),
            new ItemDefinition.ItemBoost(153, 1, "-10", 0, 0),
            new ItemDefinition.ItemBoost(154, 3, "20", 0, 0),
            new ItemDefinition.ItemBoost(155, 8, "20", 0, 0)),
        List.of(),
        false);
  }
}
