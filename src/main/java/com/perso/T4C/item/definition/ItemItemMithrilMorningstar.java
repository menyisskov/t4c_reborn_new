package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMithrilMorningstar {
  private ItemItemMithrilMorningstar() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_morningstar",
        "${item.mithril_morningstar}",
        BodyPart.WEAPON,
        "PupMorningStar",
        null,
        null,
        "64kInvMorningStar",
        76897L,
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
        40479,
        1,
        4,
        "if(target.r_dark=5025?1d73+138:1d63+120)",
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
        List.of(),
        List.of(),
        false);
  }
}
