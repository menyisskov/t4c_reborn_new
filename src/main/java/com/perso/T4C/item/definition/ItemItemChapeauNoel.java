package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemChapeauNoel {
  private ItemItemChapeauNoel() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chapeau_noel",
        "${item.chapeau_noel}",
        BodyPart.HEAD,
        "NoelHat",
        null,
        null,
        "inv_NoelHat",
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
        4072,
        2,
        675,
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
