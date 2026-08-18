package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ArcTest {
  private ArcTest() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.arc_test",
        "${item.arc_test}",
        BodyPart.WEAPON,
        "V2_Special01",
        null,
        null,
        "Inv_V2_Sp01",
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
        1.4285714285714286d,
        false,
        false,
        true,
        4050,
        9,
        756,
        "1",
        "700",
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
