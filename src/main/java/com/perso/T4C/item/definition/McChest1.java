package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class McChest1 {
  private McChest1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mc_chest_1",
        "${item.mc_chest_1}",
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
        41274,
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
        534,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Potion of clear thought",
                    "Elm recurve bow",
                    "Bone tipped arrow",
                    "Potion of fortitude",
                    "Mithril chainmail leggings",
                    "Potion of fortitude",
                    "Elven leather belt",
                    "Healing potion",
                    "Fine steel scimitar",
                    "Elven leather boots",
                    "Hickory recurve bow",
                    "Elven chainmail boots",
                    "Light healing potion",
                    "Healing potion",
                    "Serious healing potion"))),
        false);
  }
}
