package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CarquoisDesVaguesSilencieuses {
  private CarquoisDesVaguesSilencieuses() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.carquois_des_vagues_silencieuses",
        "${item.carquois_des_vagues_silencieuses}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kIconQuiver",
        52000L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        165L,
        690L,
        0L,
        0L,
        1.0d,
        false,
        true,
        true,
        3582,
        8,
        451,
        "1d550+1500",
        "0",
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
