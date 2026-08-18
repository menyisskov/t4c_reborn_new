package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemWindslash {
  private ItemItemWindslash() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.windslash",
        "${item.windslash}",
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
        180L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40863,
        1,
        277,
        "1d53+97",
        "if(862-self.agi/250*862/2<600?600:862-self.agi/250*862/2)+1d431",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10231, 0, 100)),
        List.of(new ItemDefinition.ItemBoost(564, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
