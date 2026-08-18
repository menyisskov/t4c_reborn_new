package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCloakOfTheSkull {
  private ItemItemCloakOfTheSkull() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cloak_of_the_skull",
        "${item.cloak_of_the_skull}",
        BodyPart.BODY,
        "PupNecromanRobe",
        null,
        null,
        "64kInvNecromanRobe",
        0L,
        2L,
        30.0d,
        30L,
        30L,
        0L,
        0L,
        0L,
        100L,
        50L,
        0.0d,
        false,
        false,
        false,
        41669,
        2,
        278,
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
