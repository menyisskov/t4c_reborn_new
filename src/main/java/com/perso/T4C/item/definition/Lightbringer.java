package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Lightbringer {
  private Lightbringer() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.lightbringer",
        "${item.lightbringer}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        1917L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        53L,
        0L,
        25L,
        25L,
        1.0d,
        false,
        false,
        false,
        40135,
        1,
        2,
        "1d11+15",
        "if(787-self.agi/250*787/2<600?600:787-self.agi/250*787/2)+1d394",
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
        List.of(new ItemDefinition.ItemBoost(91, 11, "100", 0, 0)),
        List.of(),
        false);
  }
}
