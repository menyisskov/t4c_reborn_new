package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemPrismaticBlade {
  private ItemItemPrismaticBlade() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.prismatic_blade",
        "${item.prismatic_blade}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        12351L,
        4L,
        0.0d,
        0L,
        0L,
        0L,
        126L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        41474,
        1,
        274,
        "1d23+40",
        "if(675-self.agi/250*675/2<600?600:675-self.agi/250*675/2)+1d338",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10413, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
