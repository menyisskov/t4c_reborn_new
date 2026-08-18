package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class IncandescentBlade {
  private IncandescentBlade() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.incandescent_blade",
        "${item.incandescent_blade}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        38747L,
        5L,
        0.0d,
        0L,
        0L,
        0L,
        155L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        3281,
        1,
        274,
        "1d50+63",
        "if(750-self.agi/250*750/2<600?600:750-self.agi/250*750/2)+1d375",
        0,
        -1,
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
