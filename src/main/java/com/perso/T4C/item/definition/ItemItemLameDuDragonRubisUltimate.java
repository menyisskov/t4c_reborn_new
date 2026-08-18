package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemLameDuDragonRubisUltimate {
  private ItemItemLameDuDragonRubisUltimate() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.lame_du_dragon_rubis_ultimate",
        "${item.lame_du_dragon_rubis_ultimate}",
        BodyPart.WEAPON,
        "NM_SabreLaser__pal2",
        null,
        null,
        "Inv_SabreLaser__pal2",
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
        4013,
        1,
        1087,
        "1d1200+3000",
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
