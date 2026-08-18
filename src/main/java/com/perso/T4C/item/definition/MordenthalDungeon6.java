package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class MordenthalDungeon6 {
  private MordenthalDungeon6() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mordenthal_dungeon_6",
        "${item.mordenthal_dungeon_6}",
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
        41273,
        3,
        238,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        671,
        1800,
        2400,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Scroll of recall",
                    "Hyper potion of fury",
                    "Hyper potion of tranquility",
                    "Hyper potion of clear thought",
                    "Hyper potion of fortitude",
                    "Hyper potion of nimbleness",
                    "Potion of regeneration",
                    "Potion of regeneration",
                    "Scroll of recall",
                    "Scroll of recall",
                    "Potion of regeneration",
                    "Potion of regeneration",
                    "Scroll of recall",
                    "Scroll of recall",
                    "Potion of regeneration")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Potion of clear thought",
                    "Potion of tranquility",
                    "Potion of nimbleness",
                    "Plate protector",
                    "Potion of fortitude",
                    "Mithril chainmail leggings",
                    "Rough amethyst",
                    "Potion of cure rabies",
                    "Scroll of recall",
                    "Potion of cure poison",
                    "Healing potion",
                    "Hickory recurve bow",
                    "Shield of the Damned",
                    "Healing potion",
                    "Healing potion"))),
        false);
  }
}
