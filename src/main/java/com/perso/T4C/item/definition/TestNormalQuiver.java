package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class TestNormalQuiver {
  private TestNormalQuiver() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.test_normal_quiver",
        "${item.test_normal_quiver}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kInvQuiver",
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
        0.0d,
        false,
        true,
        true,
        41370,
        8,
        422,
        "1d2",
        "0",
        0,
        0,
        false,
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
