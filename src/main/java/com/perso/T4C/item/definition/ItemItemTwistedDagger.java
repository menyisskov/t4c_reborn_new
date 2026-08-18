package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTwistedDagger {
  private ItemItemTwistedDagger() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.twisted_dagger",
        "${item.twisted_dagger}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        3217L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        30L,
        0L,
        30L,
        50L,
        1.0d,
        false,
        false,
        false,
        40752,
        1,
        274,
        "if(target.r_dark=5025?1d12+17:1d10+15)",
        "if(675-self.agi/250*675/2<600?600:675-self.agi/250*675/2)+1d338",
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
        List.of(new ItemDefinition.ItemBoost(515, 22, "15", 0, 0)),
        List.of(),
        false);
  }
}
