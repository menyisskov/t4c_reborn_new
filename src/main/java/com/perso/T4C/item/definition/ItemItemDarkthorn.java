package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDarkthorn {
  private ItemItemDarkthorn() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.darkthorn",
        "${item.darkthorn}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        1128L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        44L,
        0L,
        15L,
        15L,
        1.0d,
        false,
        false,
        false,
        40132,
        1,
        274,
        "1d13+9",
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
        List.of(new ItemDefinition.ItemBoost(88, 8, "10", 0, 0)),
        List.of(),
        false);
  }
}
