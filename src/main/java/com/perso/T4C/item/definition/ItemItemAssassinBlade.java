package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAssassinBlade {
  private ItemItemAssassinBlade() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.assassin_blade",
        "${item.assassin_blade}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        19873L,
        4L,
        0.0d,
        0L,
        0L,
        0L,
        110L,
        65L,
        15L,
        0L,
        1.0d,
        false,
        false,
        false,
        41389,
        1,
        274,
        "1d28+50",
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
        List.of(new ItemDefinition.ItemSpell(10407, 0, 100)),
        List.of(new ItemDefinition.ItemBoost(784, 10027, "10", 0, 0)),
        List.of(),
        false);
  }
}
