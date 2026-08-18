package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMinerPickaxe {
  private ItemItemMinerPickaxe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.miner_pickaxe",
        "${item.miner_pickaxe}",
        BodyPart.WEAPON,
        "Pickaxe",
        null,
        null,
        "Inv_Pickaxe",
        25L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        0,
        1,
        0,
        "1d6",
        "1200+1d600",
        0,
        0,
        false,
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
