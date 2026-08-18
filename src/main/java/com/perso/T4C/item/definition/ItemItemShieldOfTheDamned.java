package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemShieldOfTheDamned {
  private ItemItemShieldOfTheDamned() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shield_of_the_damned",
        "${item.shield_of_the_damned}",
        BodyPart.SHIELD,
        "PupOrcShield",
        null,
        null,
        "64kInvOrcShield",
        7923L,
        12L,
        15.0d,
        30L,
        175L,
        0L,
        0L,
        0L,
        60L,
        60L,
        0.0d,
        false,
        false,
        false,
        41459,
        2,
        282,
        null,
        "0",
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
            new ItemDefinition.ItemBoost(803, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(804, 22, "10", 0, 0),
            new ItemDefinition.ItemBoost(805, 14, "10", 0, 0),
            new ItemDefinition.ItemBoost(806, 15, "10", 0, 0),
            new ItemDefinition.ItemBoost(807, 12, "10", 0, 0),
            new ItemDefinition.ItemBoost(808, 23, "-25", 0, 0)),
        List.of(),
        false);
  }
}
