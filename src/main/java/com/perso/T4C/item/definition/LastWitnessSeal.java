package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

/** The Dusk Regent's remembered oath, consumed when Vael records his final warning. */
public final class LastWitnessSeal {
  private LastWitnessSeal() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.last_witness_seal", "${item.last_witness_seal}",
        null, null, null, null, "64kInvDestinyGem",
        0L, 1L, 0.0d, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1.0d,
        false, false, true, 0, 2, 0, null, null, 0, 0, false,
        null, 0, null, 0, 0, 0, List.of(), List.of(), List.of(), false);
  }
}
