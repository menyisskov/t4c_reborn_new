package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RedempteurDuJuste1Ultimate {
  private RedempteurDuJuste1Ultimate() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.redempteur_du_juste_1_ultimate",
        "${item.redempteur_du_juste_1_ultimate}",
        BodyPart.WEAPON,
        "NMS_NMDeathAxe__pal5",
        null,
        null,
        "NMS_NMDeathAxe_INV__pal5",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        500L,
        475L,
        0L,
        0L,
        300L,
        1.0d,
        false,
        false,
        false,
        4011,
        1,
        1603,
        "1d910+1100",
        "if(750-self.agi/250*750/2<600?600:750-self.agi/250*750/2)+1d469",
        0,
        -1,
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
