package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class TensionPrimordialeUltimate {
  private TensionPrimordialeUltimate() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.tension_primordiale_ultimate",
        "${item.tension_primordiale_ultimate}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow2",
        0L,
        2L,
        0.0d,
        0L,
        0L,
        0L,
        165L,
        690L,
        0L,
        0L,
        1.3333333333333333d,
        false,
        true,
        false,
        4008,
        9,
        448,
        "1d1560+2960+3*arrow_dmg",
        "750",
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
