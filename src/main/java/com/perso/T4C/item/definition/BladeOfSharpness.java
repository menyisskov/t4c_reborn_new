package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class BladeOfSharpness {
  private BladeOfSharpness() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.blade_of_sharpness",
        "${item.blade_of_sharpness}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        5057L,
        5L,
        0.0d,
        0L,
        0L,
        0L,
        82L,
        0L,
        20L,
        23L,
        1.0d,
        false,
        false,
        false,
        40272,
        1,
        274,
        "1d17+27",
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
        List.of(new ItemDefinition.ItemBoost(143, 3, "5", 0, 0)),
        List.of(),
        false);
  }
}
