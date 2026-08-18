package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GoblinBlade {
  private GoblinBlade() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.goblin_blade",
        "${item.goblin_blade}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvShortSword",
        1755L,
        6L,
        0.0d,
        0L,
        0L,
        0L,
        53L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40075,
        1,
        1,
        "1d10+15",
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
        List.of(),
        List.of(),
        false);
  }
}
