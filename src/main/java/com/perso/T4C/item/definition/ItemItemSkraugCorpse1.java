package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemSkraugCorpse1 {
  private ItemItemSkraugCorpse1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.skraug_corpse_1",
        "${item.skraug_corpse_1}",
        null,
        null,
        null,
        null,
        "64kSkavenPeonC-t",
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
        41455,
        3,
        543,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        344,
        1000,
        2000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Raw crystal",
                    "Moon Tug scalp",
                    "Potion of nimbleness",
                    "Bone tipped arrow",
                    "Wooden arrow",
                    "Elm reflex bow",
                    "Elm recurve bow",
                    "Potion of cure poison",
                    "Potion of cure disease",
                    "Healing potion",
                    "Crude skraug bow",
                    "Potion of cure poison",
                    "Potion of cure disease",
                    "Healing potion",
                    "Potion of nimbleness"))),
        false);
  }
}
