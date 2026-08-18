package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AdamantitePlatemailHelmet {
  private AdamantitePlatemailHelmet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.adamantite_platemail_helmet",
        "${item.adamantite_platemail_helmet}",
        BodyPart.HEAD,
        "PupPlateHelm",
        null,
        null,
        "64kInvPlateArmorHelm",
        23886L,
        7L,
        10.27d,
        15L,
        230L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40497,
        2,
        267,
        null,
        "0",
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
