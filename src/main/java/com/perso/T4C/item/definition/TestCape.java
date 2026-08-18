package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class TestCape {
  private TestCape() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.test_cape",
        "${item.test_cape}",
        BodyPart.BACK,
        "NMS_NewCapeGarde01__pal3",
        null,
        null,
        "Inv_NMS_NewCapeGarde01__pal3",
        0L,
        0L,
        0.0d,
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
        3334,
        2,
        942,
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
