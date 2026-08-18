package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AdamantitePlatemailLeggings {
  private AdamantitePlatemailLeggings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.adamantite_platemail_leggings",
        "${item.adamantite_platemail_leggings}",
        BodyPart.LEGS,
        "PupPlateLegs",
        null,
        null,
        "64kInvPlateArmorLegs",
        25053L,
        11L,
        11.85d,
        17L,
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
        40496,
        2,
        266,
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
