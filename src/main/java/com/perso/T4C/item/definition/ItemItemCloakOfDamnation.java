package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCloakOfDamnation {
  private ItemItemCloakOfDamnation() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cloak_of_damnation",
        "${item.cloak_of_damnation}",
        BodyPart.BODY,
        "PupRedRobe",
        null,
        null,
        "64kInvRedRobe",
        0L,
        5L,
        21.0d,
        25L,
        31L,
        0L,
        0L,
        0L,
        277L,
        15L,
        0.0d,
        false,
        false,
        false,
        41154,
        2,
        423,
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
        List.of(new ItemDefinition.ItemSpell(10305, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(624, 1, "75", 0, 0),
            new ItemDefinition.ItemBoost(625, 17, "10", 0, 0)),
        List.of(),
        false);
  }
}
