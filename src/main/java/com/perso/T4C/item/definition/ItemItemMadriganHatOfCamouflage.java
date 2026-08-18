package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMadriganHatOfCamouflage {
  private ItemItemMadriganHatOfCamouflage() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.madrigan_hat_of_camouflage",
        "${item.madrigan_hat_of_camouflage}",
        BodyPart.HEAD,
        "PupElvenHat",
        null,
        null,
        "64kInvElvenHat",
        7999L,
        3L,
        10.0d,
        2L,
        75L,
        0L,
        0L,
        50L,
        16L,
        12L,
        0.0d,
        false,
        false,
        false,
        41622,
        2,
        281,
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
        List.of(
            new ItemDefinition.ItemBoost(899, 10014, "10", 0, 0),
            new ItemDefinition.ItemBoost(900, 10016, "10", 0, 0)),
        List.of(),
        false);
  }
}
