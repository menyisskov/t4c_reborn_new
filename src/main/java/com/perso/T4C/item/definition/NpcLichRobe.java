package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class NpcLichRobe {
  private NpcLichRobe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.npc_lich_robe",
        "${item.npc_lich_robe}",
        BodyPart.BODY,
        "ManLichRobeBlanc",
        null,
        null,
        "Inv_LichRobeWhite",
        0L,
        0L,
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
        3458,
        2,
        925,
        null,
        null,
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
