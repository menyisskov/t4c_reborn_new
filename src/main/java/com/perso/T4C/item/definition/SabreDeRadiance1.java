package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SabreDeRadiance1 {
  private SabreDeRadiance1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sabre_de_radiance_1",
        "${item.sabre_de_radiance_1}",
        BodyPart.WEAPON,
        "NM_SabreLaser__pal2",
        null,
        null,
        "Inv_SabreLaser__pal2",
        25664L,
        10L,
        0.0d,
        0L,
        300L,
        0L,
        20L,
        0L,
        75L,
        400L,
        1.0d,
        false,
        false,
        false,
        4018,
        1,
        1087,
        "1d50+64",
        "if(1125-self.agi/250*1125/2<600?600:1125-self.agi/250*1125/2)+1d563",
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
