package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Stinger {
  private Stinger() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.stinger",
        "${item.stinger}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        3755L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        73L,
        0L,
        15L,
        15L,
        1.0d,
        false,
        false,
        false,
        40546,
        1,
        274,
        "1d22+16",
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
        List.of(new ItemDefinition.ItemSpell(10163, 0, 100)),
        List.of(new ItemDefinition.ItemBoost(132, 6, "10", 0, 0)),
        List.of(),
        false);
  }
}
