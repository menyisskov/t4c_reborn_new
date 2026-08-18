package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemWingsTest {
  private ItemItemWingsTest() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wings_test",
        "${item.wings_test}",
        BodyPart.BACK,
        "ArchWings",
        null,
        null,
        "Inv_ArchWingsW",
        200000L,
        2L,
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
        3963,
        2,
        672,
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
