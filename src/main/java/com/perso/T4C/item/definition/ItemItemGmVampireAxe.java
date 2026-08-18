package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGmVampireAxe {
  private ItemItemGmVampireAxe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gm_vampire_axe",
        "${item.gm_vampire_axe}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvBattleAxe",
        6752L,
        11L,
        0.0d,
        0L,
        0L,
        0L,
        85L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40248,
        1,
        7,
        "(1d1-(self.str-20)/5)",
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
        List.of(new ItemDefinition.ItemSpell(10116, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
