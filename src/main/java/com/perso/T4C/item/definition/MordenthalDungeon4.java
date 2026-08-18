package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class MordenthalDungeon4 {
  private MordenthalDungeon4() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mordenthal_dungeon_4",
        "${item.mordenthal_dungeon_4}",
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
        41271,
        3,
        238,
        null,
        "0",
        0,
        0,
        true,
        "Twisted key",
        75,
        null,
        603,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Fine steel hand axe",
                    "Potion of fury",
                    "Fine steel mace",
                    "Elven chainmail helmet",
                    "Elven leather belt",
                    "Large shield",
                    "Rough limestone",
                    "Fine steel scimitar",
                    "Light healing potion",
                    "Fine steel scimitar",
                    "Potion of fortitude",
                    "Potion of tranquility",
                    "Hickory longbow",
                    "Elven leather gloves",
                    "High metal broadsword"))),
        false);
  }
}
