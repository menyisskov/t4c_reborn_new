package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class FlowingBlackRobe {
  private FlowingBlackRobe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.flowing_black_robe",
        "${item.flowing_black_robe}",
        BodyPart.BODY,
        "PupNecromanRobe",
        null,
        null,
        "64kInvNecromanRobe",
        449L,
        5L,
        2.0d,
        25L,
        12L,
        0L,
        0L,
        0L,
        40L,
        15L,
        0.0d,
        false,
        false,
        false,
        41146,
        2,
        278,
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
        List.of(new ItemDefinition.ItemBoost(605, 1, "10", 0, 0)),
        List.of(),
        false);
  }
}
