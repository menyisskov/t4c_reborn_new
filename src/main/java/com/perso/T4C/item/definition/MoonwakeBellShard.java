package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

/** Guaranteed proof carried by the Pale Cantor, consumed when Maelin repairs the first account. */
public final class MoonwakeBellShard {
  private MoonwakeBellShard() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.moonwake_bell_shard", "${item.moonwake_bell_shard}",
        null, null, null, null, "64kInvIceShard",
        0L, 1L, 0.0d, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1.0d,
        false, false, true, 0, 2, 0, null, null, 0, 0, false,
        null, 0, null, 0, 0, 0, List.of(), List.of(), List.of(), false);
  }
}
