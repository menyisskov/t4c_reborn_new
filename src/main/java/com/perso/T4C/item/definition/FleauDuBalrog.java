package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class FleauDuBalrog {
  private FleauDuBalrog() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.fleau_du_balrog",
        "${item.fleau_du_balrog}",
        BodyPart.WEAPON,
        "PupFireFlail",
        null,
        null,
        "InvFireFlail",
        100000L,
        0L,
        0.0d,
        0L,
        0L,
        500L,
        200L,
        0L,
        175L,
        0L,
        1.0d,
        false,
        false,
        false,
        3622,
        1,
        576,
        "1d220+450",
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
