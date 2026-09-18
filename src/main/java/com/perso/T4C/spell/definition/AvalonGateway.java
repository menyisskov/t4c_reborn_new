package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class AvalonGateway {
  private AvalonGateway() {}

  // Destination tile (worldZ 0 / WORLDMAP) is the confirmed walkable floor tile inside the
  // Avalon Sanctuary temple, per the terrain pass's final building placement.
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
                    new SpellData.T4cEffect.EffectParam(1, "1340"),
                    new SpellData.T4cEffect.EffectParam(2, "1477"),
                    new SpellData.T4cEffect.EffectParam(3, "0")))));
  }
}
