package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilScimitar2 {
  private MithrilScimitar2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_scimitar_2",
        "${item.mithril_scimitar_2}",
        BodyPart.WEAPON,
        "PupBattleSword",
        null,
        null,
        "64kInvBattleSword",
        0L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        300L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40379,
        1,
        277,
        "1d79+149",
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
        List.of(new ItemDefinition.ItemBoost(319, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
