package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

// T4C-0033: the physical component of the Godsforged crafting chain - Ember-Smith Corvain's
// output (see npc/EmberSmithCorvain.java), forged from 5 Wyrmforged Embers. Consumed alongside
// Bound Godsigil by Grandmaster Tholvenn to forge one of the five Godsforged items. Not
// equippable - see WyrmforgedEmber.java for why this is a legacy Java item.
public final class TemperedGodcore {
  private TemperedGodcore() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.tempered_godcore",
        "${item.tempered_godcore}",
        null,
        null,
        null,
        null,
        "V3_Orbe_Gold",
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
