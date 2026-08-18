package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemGmMjolnir {
  private ItemItemGmMjolnir() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gm_mjolnir",
        "${item.gm_mjolnir}",
        null,
        null,
        null,
        null,
        "64kInvWarhammer",
        0L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        210L,
        0L,
        15L,
        169L,
        0.0d,
        false,
        false,
        false,
        40835,
        1,
        5,
        "if(target.r_dark=5025?1d67+123:1d58+110)",
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
        List.of(new ItemDefinition.ItemSpell(10225, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
