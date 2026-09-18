package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class AvalonGateway {
  private AvalonGateway() {}

  // Destination tile (worldZ 0 / WORLDMAP) for "Avalon Sanctuary" is PROVISIONAL, pending the
  // terrain agent's final placement of the settlement. If the terrain pass shifts the
  // settlement's location, update the EffectParam coordinates below to match.
  public static SpellData definition() {
    return new SpellData(
        "${spell.avalon_gateway}",
        "${spell.description.avalon_gateway}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "0",
        "0",
        0,
        null,
        99915,
        0,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30012,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                7,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "1280"),
                    new SpellData.T4cEffect.EffectParam(2, "1480"),
                    new SpellData.T4cEffect.EffectParam(3, "0")))));
  }
}
