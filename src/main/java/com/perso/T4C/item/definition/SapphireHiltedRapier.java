package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SapphireHiltedRapier {
  private SapphireHiltedRapier() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sapphire_hilted_rapier",
        "${item.sapphire_hilted_rapier}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvDarkSword",
        32522L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        198L,
        0L,
        40L,
        43L,
        1.0d,
        false,
        false,
        false,
        41473,
        1,
        271,
        "1d37+68",
        "if(787-self.agi/250*787/2<600?600:787-self.agi/250*787/2)+1d394",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10410, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(794, 6, "5", 0, 0),
            new ItemDefinition.ItemBoost(795, 3, "5", 0, 0),
            new ItemDefinition.ItemBoost(796, 4, "5", 0, 0),
            new ItemDefinition.ItemBoost(797, 1, "5", 0, 0),
            new ItemDefinition.ItemBoost(798, 2, "5", 0, 0)),
        List.of(),
        false);
  }
}
