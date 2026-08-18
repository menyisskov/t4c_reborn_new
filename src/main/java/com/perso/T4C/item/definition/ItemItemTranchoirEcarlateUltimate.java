package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTranchoirEcarlateUltimate {
  private ItemItemTranchoirEcarlateUltimate() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.tranchoir_ecarlate_ultimate",
        "${item.tranchoir_ecarlate_ultimate}",
        BodyPart.WEAPON,
        "NMS_NMDeathAxe__pal4",
        null,
        null,
        "NMS_NMDeathAxe_INV__pal4",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        500L,
        775L,
        75L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        4009,
        1,
        1602,
        "1d2160+6960",
        "if(750-self.agi/250*750/2<600?600:750-self.agi/250*750/2)+1d469",
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
