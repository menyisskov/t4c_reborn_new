package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class PriestBoots {
  private PriestBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.priest_boots",
        "${item.priest_boots}",
        BodyPart.FEET,
        "V2_ManArmorBoots01",
        null,
        null,
        "Inv_ManArmor01Boots",
        2000L,
        0L,
        9.0d,
        0L,
        50L,
        0L,
        0L,
        0L,
        75L,
        300L,
        1.0d,
        false,
        false,
        true,
        3348,
        2,
        885,
        null,
        null,
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
