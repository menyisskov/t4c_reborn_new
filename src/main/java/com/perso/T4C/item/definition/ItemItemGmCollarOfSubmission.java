package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGmCollarOfSubmission {
  private ItemItemGmCollarOfSubmission() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gm_collar_of_submission",
        "${item.gm_collar_of_submission}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 2",
        0L,
        0L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40702,
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
        List.of(),
        List.of(),
        false);
  }
}
