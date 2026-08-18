package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemSkraugCorpse2 {
  private ItemItemSkraugCorpse2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.skraug_corpse_2",
        "${item.skraug_corpse_2}",
        null,
        null,
        null,
        null,
        "64kSkavenSkavengerC-s",
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
        41456,
        3,
        545,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        126,
        1000,
        2000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Bracers of battle",
                    "Potion of fury",
                    "Light healing potion",
                    "Healing potion",
                    "Serious healing potion",
                    "Potion of regeneration",
                    "Fine steel short sword",
                    "Fine steel long sword",
                    "Potion of fortitude",
                    "Healing potion",
                    "Potion of fortitude",
                    "Healing potion",
                    "Light healing potion",
                    "Potion of fury"))),
        false);
  }
}
