package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemNewbiSetHelm {
  private ItemItemNewbiSetHelm() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.newbi_set_helm",
        "${item.newbi_set_helm}",
        BodyPart.HEAD,
        "PupElvenHat",
        null,
        null,
        "64kInvElvenHat",
        2000L,
        0L,
        5.0d,
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
        3249,
        2,
        281,
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
