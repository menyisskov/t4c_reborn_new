package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class MordenthalDungeon3 {
  private MordenthalDungeon3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mordenthal_dungeon_3",
        "${item.mordenthal_dungeon_3}",
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
        41169,
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
        568,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Fine steel scimitar",
                    "Potion of tranquility",
                    "Elven chainmail gauntlets",
                    "Elven chainmail girdle",
                    "Potion of fury",
                    "Large shield",
                    "Potion of nimbleness",
                    "Plate protector",
                    "Elven chainmail gauntlets",
                    "Potion of cure disease",
                    "Light healing potion",
                    "Healing potion",
                    "Potion of mana",
                    "Mana elixir"))),
        false);
  }
}
