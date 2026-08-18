package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CapeOfTheAncientAssassin {
  private CapeOfTheAncientAssassin() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cape_of_the_ancient_assassin",
        "${item.cape_of_the_ancient_assassin}",
        BodyPart.BACK,
        "PupRedCape",
        null,
        null,
        "64kInvRedCape",
        0L,
        2L,
        3.0d,
        0L,
        13L,
        0L,
        0L,
        48L,
        18L,
        35L,
        0.0d,
        false,
        false,
        false,
        41668,
        2,
        287,
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
            new ItemDefinition.ItemBoost(959, 22, "10", 0, 0),
            new ItemDefinition.ItemBoost(960, 10014, "15", 0, 0),
            new ItemDefinition.ItemBoost(961, 10028, "15", 0, 0)),
        List.of(),
        false);
  }
}
