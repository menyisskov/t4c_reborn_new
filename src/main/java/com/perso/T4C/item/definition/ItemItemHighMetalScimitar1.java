package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHighMetalScimitar1 {
  private ItemItemHighMetalScimitar1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_scimitar_1",
        "${item.high_metal_scimitar_1}",
        BodyPart.WEAPON,
        "PupBattleSword",
        null,
        null,
        "64kInvBattleSword",
        86582L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        227L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40516,
        1,
        277,
        "1d53+99",
        "if(862-self.agi/250*862/2<600?600:862-self.agi/250*862/2)+1d431",
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
        List.of(new ItemDefinition.ItemBoost(309, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
