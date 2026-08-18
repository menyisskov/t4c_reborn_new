package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GuardianRingOfVitality {
  private GuardianRingOfVitality() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.guardian_ring_of_vitality",
        "${item.guardian_ring_of_vitality}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 5",
        1L,
        0L,
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
        41862,
        2,
        180,
        null,
        "0",
        0,
        -1,
        true,
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
