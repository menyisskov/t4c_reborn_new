package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AdamantiteBastardSword {
  private AdamantiteBastardSword() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.adamantite_bastard_sword",
        "${item.adamantite_bastard_sword}",
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
        40424,
        1,
        2,
        "1d81+140",
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
        List.of(),
        List.of(),
        false);
  }
}
