package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GmFrostbane {
  private GmFrostbane() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gm_frostbane",
        "${item.gm_frostbane}",
        BodyPart.WEAPON,
        "PupBattleSword",
        null,
        null,
        "64kInvBattleSword",
        19273L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        155L,
        0L,
        60L,
        60L,
        0.0d,
        false,
        false,
        false,
        40322,
        1,
        277,
        "0",
        "0",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10102, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
