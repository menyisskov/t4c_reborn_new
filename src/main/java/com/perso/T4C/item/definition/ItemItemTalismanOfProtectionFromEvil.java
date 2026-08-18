package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTalismanOfProtectionFromEvil {
  private ItemItemTalismanOfProtectionFromEvil() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.talisman_of_protection_from_evil",
        "${item.talisman_of_protection_from_evil}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 2",
        2415L,
        2L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        40L,
        53L,
        0.0d,
        false,
        false,
        false,
        40726,
        2,
        173,
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
        List.of(new ItemDefinition.ItemBoost(451, 22, "25", 0, 0)),
        List.of(),
        false);
  }
}
