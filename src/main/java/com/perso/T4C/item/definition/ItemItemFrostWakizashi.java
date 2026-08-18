package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemFrostWakizashi {
  private ItemItemFrostWakizashi() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.frost_wakizashi",
        "${item.frost_wakizashi}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvShortSword",
        0L,
        6L,
        0.0d,
        0L,
        0L,
        0L,
        240L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40866,
        1,
        1,
        "1d59+110",
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
        List.of(new ItemDefinition.ItemSpell(10234, 0, 100)),
        List.of(new ItemDefinition.ItemBoost(567, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
