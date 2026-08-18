package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class FleauDesEnfersUltimate {
  private FleauDesEnfersUltimate() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fleau_des_enfers_ultimate",
        "${item.fleau_des_enfers_ultimate}",
        BodyPart.WEAPON,
        "V2_Special03",
        null,
        null,
        "Inv_V2_Sp03",
        100000L,
        0L,
        0.0d,
        0L,
        0L,
        500L,
        475L,
        0L,
        300L,
        0L,
        1.0d,
        false,
        false,
        false,
        4086,
        1,
        758,
        "1d550+850",
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
