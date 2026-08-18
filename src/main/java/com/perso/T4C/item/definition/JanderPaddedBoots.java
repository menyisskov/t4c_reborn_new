package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class JanderPaddedBoots {
  private JanderPaddedBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.jander_padded_boots",
        "${item.jander_padded_boots}",
        BodyPart.FEET,
        "PupBlackLeatherBoots",
        null,
        null,
        "64kInvBlackLeatherBoots",
        4245L,
        3L,
        2.97d,
        0L,
        110L,
        0L,
        15L,
        55L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41453,
        2,
        288,
        null,
        "0",
        0,
        0,
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
