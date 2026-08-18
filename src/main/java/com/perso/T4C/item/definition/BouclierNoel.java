package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class BouclierNoel {
  private BouclierNoel() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bouclier_noel",
        "${item.bouclier_noel}",
        BodyPart.SHIELD,
        "V2_IceShield01",
        null,
        null,
        "Inv_V2_iceShield01",
        0L,
        0L,
        20.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        4071,
        2,
        895,
        null,
        null,
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
