package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAdamantitePlatemailBoots {
  private ItemItemAdamantitePlatemailBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.adamantite_platemail_boots",
        "${item.adamantite_platemail_boots}",
        BodyPart.FEET,
        "PupPlateFoot",
        null,
        null,
        "64kInvPlateArmorFeet",
        21551L,
        7L,
        10.665d,
        16L,
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
        40413,
        2,
        265,
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
