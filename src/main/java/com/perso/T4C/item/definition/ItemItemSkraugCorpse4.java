package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemSkraugCorpse4 {
  private ItemItemSkraugCorpse4() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.skraug_corpse_4",
        "${item.skraug_corpse_4}",
        null,
        null,
        null,
        null,
        "64kSkavenWarriorC-s",
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
        41458,
        3,
        550,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        171,
        1000,
        2000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Raw crystal",
                    "Light healing potion",
                    "Healing potion",
                    "Serious healing potion",
                    "Critical healing potion",
                    "Potion of tranquility",
                    "Potion of clear thought",
                    "Potion of fury",
                    "Potion of fortitude",
                    "Potion of nimbleness",
                    "Torch"))),
        false);
  }
}
