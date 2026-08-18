package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DevNightmareWings {
  private DevNightmareWings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dev_nightmare_wings",
        "${item.dev_nightmare_wings}",
        BodyPart.BACK,
        "NightmareMedWings",
        null,
        null,
        "Inv_NightMareWings",
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
        3225,
        1,
        1097,
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
