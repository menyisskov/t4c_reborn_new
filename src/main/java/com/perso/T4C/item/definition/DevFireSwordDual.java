package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DevFireSwordDual {
  private DevFireSwordDual() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dev_fire_sword_dual",
        "${item.dev_fire_sword_dual}",
        BodyPart.WEAPON2,
        "NM_FireSword01Dual",
        null,
        null,
        "Inv_FireSword01",
        0L,
        0L,
        0.0d,
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
        3223,
        1,
        1088,
        "0",
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
