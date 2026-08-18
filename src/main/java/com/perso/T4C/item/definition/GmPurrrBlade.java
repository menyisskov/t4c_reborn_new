package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GmPurrrBlade {
  private GmPurrrBlade() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gm_purrr_blade",
        "${item.gm_purrr_blade}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        1L,
        3L,
        1000.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40236,
        1,
        274,
        "1",
        "0",
        100,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(
            new ItemDefinition.ItemSpell(10025, 0, 100),
            new ItemDefinition.ItemSpell(10028, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
