package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AdamantiteBroadsword1 {
  private AdamantiteBroadsword1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.adamantite_broadsword_1",
        "${item.adamantite_broadsword_1}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        0L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        358L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40502,
        1,
        2,
        "1d77+136",
        "if(825-self.agi/250*825/2<600?600:825-self.agi/250*825/2)+1d413",
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
        List.of(new ItemDefinition.ItemBoost(325, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
