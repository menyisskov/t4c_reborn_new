package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTidalBlade {
  private ItemItemTidalBlade() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.tidal_blade",
        "${item.tidal_blade}",
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
        110L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40861,
        1,
        274,
        "1d33+57",
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
        List.of(new ItemDefinition.ItemSpell(10228, 0, 100)),
        List.of(new ItemDefinition.ItemBoost(561, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
