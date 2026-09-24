package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

// T4C-0033: the arcane component of the Godsforged crafting chain - Warden Seressa's output (see
// npc/WardenSeressa.java), bound from 5 Veiled Aether Shards. Consumed alongside Tempered Godcore
// by Grandmaster Tholvenn to forge one of the five Godsforged items. Not equippable - see
// WyrmforgedEmber.java for why this is a legacy Java item.
public final class BoundGodsigil {
  private BoundGodsigil() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bound_godsigil",
        "${item.bound_godsigil}",
        null,
        null,
        null,
        null,
        "64kInvDestinyGem",
        0L,
        1L,
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
        true,
        0,
        2,
        0,
        null,
        null,
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
