package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMithrilBroadAxe {
  private ItemItemMithrilBroadAxe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_broad_axe",
        "${item.mithril_broad_axe}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvDouble Axe",
        0L,
        16L,
        0.0d,
        0L,
        0L,
        0L,
        416L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40554,
        1,
        122,
        "1d137+233",
        "if(1500-self.agi/250*1500/2<1500/2?1500/2:1500-self.agi/250*1500/2)+1d750",
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
