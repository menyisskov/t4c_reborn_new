package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class BladeOfHeroism {
  private BladeOfHeroism() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.blade_of_heroism",
        "${item.blade_of_heroism}",
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
        40904,
        1,
        274,
        "1d35+63",
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
        List.of(new ItemDefinition.ItemBoost(569, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
