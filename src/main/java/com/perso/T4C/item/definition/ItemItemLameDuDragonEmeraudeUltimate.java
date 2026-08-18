package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemLameDuDragonEmeraudeUltimate {
  private ItemItemLameDuDragonEmeraudeUltimate() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.lame_du_dragon_emeraude_ultimate",
        "${item.lame_du_dragon_emeraude_ultimate}",
        BodyPart.WEAPON,
        "NM_SabreLaser",
        null,
        null,
        "Inv_SabreLaser",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        500L,
        775L,
        75L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        4012,
        1,
        1085,
        "1d1560+3360",
        "if(750-self.agi/250*750/2<600?600:750-self.agi/250*750/2)+1d469",
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
