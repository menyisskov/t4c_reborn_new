package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RuneEtchedRobeOfProtection {
  private RuneEtchedRobeOfProtection() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rune_etched_robe_of_protection",
        "${item.rune_etched_robe_of_protection}",
        BodyPart.BODY,
        "PupMageRobe",
        null,
        null,
        "64kInvMageRobe",
        5332L,
        5L,
        11.0d,
        25L,
        16L,
        0L,
        0L,
        0L,
        43L,
        50L,
        0.0d,
        false,
        false,
        false,
        41144,
        2,
        424,
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
            new ItemDefinition.ItemBoost(599, 3, "5", 0, 0),
            new ItemDefinition.ItemBoost(600, 1, "5", 0, 0),
            new ItemDefinition.ItemBoost(601, 4, "5", 0, 0)),
        List.of(),
        false);
  }
}
