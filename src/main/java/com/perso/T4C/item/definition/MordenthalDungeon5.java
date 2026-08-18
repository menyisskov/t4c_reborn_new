package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class MordenthalDungeon5 {
  private MordenthalDungeon5() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mordenthal_dungeon_5",
        "${item.mordenthal_dungeon_5}",
        null,
        null,
        null,
        null,
        "64kInvChest",
        0L,
        10000L,
        0.0d,
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
        41272,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        652,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Mithril chainmail helmet",
                    "Healing potion",
                    "Dwarven platemail gauntlets",
                    "Fine steel scimitar",
                    "Potion of nimbleness",
                    "Hickory compound bow",
                    "Plate protector",
                    "Fine steel hand axe",
                    "Hickory recurve bow",
                    "Critical healing potion",
                    "Fine steel hand axe",
                    "Elven chainmail gauntlets"))),
        false);
  }
}
