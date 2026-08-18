package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SkullDagger2 {
  private SkullDagger2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.skull_dagger_2",
        "${item.skull_dagger_2}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        415L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        12L,
        0L,
        43L,
        15L,
        1.0d,
        false,
        false,
        false,
        40751,
        1,
        274,
        "1d5+4",
        "600+1d300",
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
        List.of(new ItemDefinition.ItemBoost(511, 1, "5", 0, 0)),
        List.of(),
        false);
  }
}
