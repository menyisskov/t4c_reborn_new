package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class BloodstainedBroadsword {
  private BloodstainedBroadsword() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bloodstained_broadsword",
        "${item.bloodstained_broadsword}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        5982L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        82L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        41615,
        1,
        2,
        "1d17+27",
        "if(825-self.agi/250*825/2<600?600:825-self.agi/250*825/2)+1d413",
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
            new ItemDefinition.ItemBoost(932, 3, "5", 0, 0),
            new ItemDefinition.ItemBoost(933, 6, "5", 0, 0),
            new ItemDefinition.ItemBoost(934, 2, "5", 0, 0),
            new ItemDefinition.ItemBoost(935, 4, "-5", 0, 0),
            new ItemDefinition.ItemBoost(936, 1, "-5", 0, 0),
            new ItemDefinition.ItemBoost(937, 10027, "5", 0, 0),
            new ItemDefinition.ItemBoost(938, 10002, "5", 0, 0),
            new ItemDefinition.ItemBoost(939, 10001, "5", 0, 0)),
        List.of(),
        false);
  }
}
