package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemFineSteelDagger1 {
  private ItemItemFineSteelDagger1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fine_steel_dagger_1",
        "${item.fine_steel_dagger_1}",
        BodyPart.WEAPON,
        "V2_Dague01",
        null,
        null,
        "Inv_V2_dague01",
        13924L,
        4L,
        0.0d,
        0L,
        0L,
        0L,
        97L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40402,
        1,
        687,
        "1d21+34",
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
        List.of(new ItemDefinition.ItemBoost(351, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
