package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SoulOfFlare {
  private SoulOfFlare() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.soul_of_flare",
        "${item.soul_of_flare}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvGlinting Sword",
        0L,
        12L,
        0.0d,
        0L,
        0L,
        0L,
        350L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40864,
        1,
        202,
        "1d122+215",
        "if(1125-self.agi/250*1125/2<600?600:1125-self.agi/250*1125/2)+1d563",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10232, 0, 100)),
        List.of(new ItemDefinition.ItemBoost(565, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
