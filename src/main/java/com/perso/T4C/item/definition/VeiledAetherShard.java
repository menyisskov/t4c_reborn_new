package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

// T4C-0033: raw material for the Godsforged crafting chain's arcane component (see
// npc/WardenSeressa.java). Not equippable - see WyrmforgedEmber.java for why this is a legacy
// Java item rather than a JSON one. Rare drop from the world's most magically dangerous apex
// threats (see TheRootcrownWyrm.java, YsoldeTheVeiledMatriarch.java).
public final class VeiledAetherShard {
  private VeiledAetherShard() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.veiled_aether_shard",
        "${item.veiled_aether_shard}",
        null,
        null,
        null,
        null,
        "64kInvIceShard",
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
