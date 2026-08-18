package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class TestFancyShortBow {
  private TestFancyShortBow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.test_fancy_short_bow",
        "${item.test_fancy_short_bow}",
        BodyPart.WEAPON,
        "PupShortBow",
        null,
        null,
        "64kInvShortBowFancy",
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
        41373,
        9,
        449,
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
