package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class LostBladeOfTheDragon {
  private LostBladeOfTheDragon() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.lost_blade_of_the_dragon",
        "${item.lost_blade_of_the_dragon}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        0L,
        5L,
        0.0d,
        0L,
        0L,
        0L,
        242L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        41387,
        1,
        274,
        "1d54+101",
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
        List.of(new ItemDefinition.ItemSpell(12694, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
