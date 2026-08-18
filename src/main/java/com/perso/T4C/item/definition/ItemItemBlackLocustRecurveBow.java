package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBlackLocustRecurveBow {
  private ItemItemBlackLocustRecurveBow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.black_locust_recurve_bow",
        "${item.black_locust_recurve_bow}",
        BodyPart.WEAPON,
        "PupLongBow",
        null,
        null,
        "64kInvLongBow",
        0L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        90L,
        503L,
        0L,
        0L,
        900.0d,
        false,
        true,
        false,
        41261,
        9,
        421,
        "1d71+139+2*arrow_dmg",
        "900",
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
