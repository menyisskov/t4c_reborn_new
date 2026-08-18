package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class SkraugCorpse3 {
  private SkraugCorpse3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.skraug_corpse_3",
        "${item.skraug_corpse_3}",
        null,
        null,
        null,
        null,
        "64kSkavenShamanC-s",
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
        41457,
        3,
        548,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        234,
        1000,
        2000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Raw crystal",
                    "Moon Tug scalp",
                    "Potion of cure rabies",
                    "Potion of cure disease",
                    "Potion of cure poison",
                    "Potion of tranquility",
                    "Potion of clear thought",
                    "Potion of mana",
                    "Mana elixir",
                    "Manastone",
                    "Shaman mantle",
                    "Light healing potion",
                    "Torch",
                    "Scroll of recall"))),
        false);
  }
}
