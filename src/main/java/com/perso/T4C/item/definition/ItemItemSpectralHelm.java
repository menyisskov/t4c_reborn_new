package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemSpectralHelm {
  private ItemItemSpectralHelm() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.spectral_helm",
        "${item.spectral_helm}",
        BodyPart.HEAD,
        "PupHornedHelmet",
        null,
        null,
        "64kInvHornedHelmet",
        18867L,
        8L,
        3.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        91L,
        19L,
        0.0d,
        false,
        false,
        false,
        41406,
        2,
        276,
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
        List.of(
            new ItemDefinition.ItemBoost(688, 1, "20", 0, 0),
            new ItemDefinition.ItemBoost(689, 24, "10", 0, 0)),
        List.of(),
        false);
  }
}
