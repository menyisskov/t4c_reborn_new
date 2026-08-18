package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBotteNoel {
  private ItemItemBotteNoel() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.botte_noel",
        "${item.botte_noel}",
        BodyPart.FEET,
        "NMS_LutinBoots",
        null,
        null,
        "Inv_LutinBoots",
        0L,
        0L,
        20.0d,
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
        4069,
        2,
        1150,
        null,
        null,
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
