package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GrimSwordOfWar {
  private GrimSwordOfWar() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.grim_sword_of_war",
        "${item.grim_sword_of_war}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvGlinting Sword",
        0L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        227L,
        83L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        41666,
        1,
        202,
        "1d60+113",
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
        List.of(
            new ItemDefinition.ItemBoost(956, 8, "100", 0, 0),
            new ItemDefinition.ItemBoost(957, 10001, "15", 0, 0),
            new ItemDefinition.ItemBoost(958, 10002, "25", 0, 0)),
        List.of(),
        false);
  }
}
