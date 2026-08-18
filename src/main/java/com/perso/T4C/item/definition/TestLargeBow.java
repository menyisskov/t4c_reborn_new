package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class TestLargeBow {
  private TestLargeBow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.test_large_bow",
        "${item.test_large_bow}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
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
        false,
        41371,
        9,
        421,
        "1d3+arrow_dmg",
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
