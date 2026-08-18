package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMithrilMorningstar3 {
  private ItemItemMithrilMorningstar3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_morningstar_3",
        "${item.mithril_morningstar_3}",
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
        210L,
        0L,
        0L,
        169L,
        1.0d,
        false,
        false,
        false,
        40697,
        1,
        4,
        "if(target.r_dark=5025?1d108+208:1d95+180)",
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
        List.of(new ItemDefinition.ItemBoost(433, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
