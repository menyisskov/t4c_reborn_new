package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Defender {
  private Defender() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.defender",
        "${item.defender}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvGlinting Sword",
        5189L,
        12L,
        5.0d,
        0L,
        0L,
        0L,
        68L,
        0L,
        25L,
        30L,
        1.0d,
        false,
        false,
        false,
        40139,
        1,
        202,
        "1d19+30",
        "if(1125-self.agi/250*1125/2<600?600:1125-self.agi/250*1125/2)+1d563",
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
        List.of(),
        List.of(),
        false);
  }
}
