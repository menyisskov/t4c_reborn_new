package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemEclipseBoots {
  private ItemItemEclipseBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.eclipse_boots",
        "${item.eclipse_boots}",
        BodyPart.FEET,
        "V2_ManArmorBoots01__pal2",
        null,
        null,
        "Inv_ManArmor01Boots__pal2",
        40000L,
        2L,
        25.0d,
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
        3317,
        2,
        886,
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
