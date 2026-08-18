package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class LanceDuTriton {
  private LanceDuTriton() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.lance_du_triton",
        "${item.lance_du_triton}",
        BodyPart.WEAPON,
        "V2_Sceptre01",
        null,
        null,
        "Inv_V2_Sceptre01",
        50000L,
        0L,
        0.0d,
        0L,
        0L,
        150L,
        125L,
        0L,
        75L,
        0L,
        1.0d,
        false,
        false,
        false,
        3625,
        1,
        753,
        "1d50+63",
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
