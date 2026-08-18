package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class V2DagueViperine {
  private V2DagueViperine() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.v2_dague_viperine",
        "${item.v2_dague_viperine}",
        BodyPart.WEAPON,
        "V2_Viperine",
        null,
        null,
        "Inv_V2_Viperine",
        9L,
        3L,
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
        false,
        3041,
        1,
        686,
        "1000",
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
