package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTranchoirEcarlate {
  private ItemItemTranchoirEcarlate() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.tranchoir_ecarlate",
        "${item.tranchoir_ecarlate}",
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
        3578,
        1,
        1602,
        "1d1800+6600",
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
