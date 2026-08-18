package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SwordOfFury {
  private SwordOfFury() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sword_of_fury",
        "${item.sword_of_fury}",
        BodyPart.WEAPON,
        "PupBattleSword",
        null,
        null,
        "64kInvBattleSword",
        2521L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        59L,
        0L,
        21L,
        24L,
        1.0d,
        false,
        false,
        false,
        40253,
        1,
        277,
        "1d12+17",
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
        List.of(new ItemDefinition.ItemBoost(114, 8, "15", 0, 0)),
        List.of(),
        false);
  }
}
