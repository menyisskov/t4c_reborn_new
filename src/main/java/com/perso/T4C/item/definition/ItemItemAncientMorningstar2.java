package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAncientMorningstar2 {
  private ItemItemAncientMorningstar2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ancient_morningstar_2",
        "${item.ancient_morningstar_2}",
        BodyPart.WEAPON,
        "PupMorningStar",
        null,
        null,
        "64kInvMorningStar",
        0L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        260L,
        0L,
        0L,
        206L,
        1.0d,
        false,
        false,
        false,
        40699,
        1,
        4,
        "if(target.r_dark=5025?1d112+198:1d97+172)",
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
        List.of(new ItemDefinition.ItemBoost(437, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
