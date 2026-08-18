package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemEclipseBack {
  private ItemItemEclipseBack() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.eclipse_back",
        "${item.eclipse_back}",
        BodyPart.BACK,
        "PupRedCape__pal3",
        null,
        null,
        "64kInvRedCape__pal3",
        40000L,
        2L,
        18.0d,
        0L,
        350L,
        0L,
        100L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3319,
        2,
        652,
        null,
        null,
        0,
        -1,
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
