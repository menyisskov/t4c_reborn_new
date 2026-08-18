package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCloakOfRenewal {
  private ItemItemCloakOfRenewal() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cloak_of_renewal",
        "${item.cloak_of_renewal}",
        BodyPart.BODY,
        "PupMageRobe",
        null,
        null,
        "64kInvMageRobe",
        0L,
        5L,
        15.0d,
        25L,
        25L,
        0L,
        0L,
        0L,
        106L,
        112L,
        0.0d,
        false,
        false,
        false,
        41145,
        2,
        424,
        null,
        "0",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10303, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(602, 3, "10", 0, 0),
            new ItemDefinition.ItemBoost(603, 1, "25", 0, 0),
            new ItemDefinition.ItemBoost(604, 4, "25", 0, 0)),
        List.of(),
        false);
  }
}
