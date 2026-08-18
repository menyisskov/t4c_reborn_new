package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ChaosSword {
  private ChaosSword() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chaos_sword",
        "${item.chaos_sword}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        1755L,
        7L,
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
        40104,
        1,
        2,
        "1d41",
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
        List.of(),
        List.of(),
        false);
  }
}
