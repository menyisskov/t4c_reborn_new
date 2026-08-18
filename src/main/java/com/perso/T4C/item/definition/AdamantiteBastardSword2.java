package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AdamantiteBastardSword2 {
  private AdamantiteBastardSword2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.adamantite_bastard_sword_2",
        "${item.adamantite_bastard_sword_2}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        0L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        387L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40456,
        1,
        2,
        "1d105+182",
        "if(937-self.agi/250*937/2<600?600:937-self.agi/250*937/2)+1d469",
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
        List.of(new ItemDefinition.ItemBoost(330, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
