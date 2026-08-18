package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGantsDuBoxeur5 {
  private ItemItemGantsDuBoxeur5() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gants_du_boxeur_5",
        "${item.gants_du_boxeur_5}",
        BodyPart.WEAPON,
        null,
        null,
        null,
        "Inv_ManArmor01Glove",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        350L,
        350L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        3881,
        1,
        883,
        "1d680+1440",
        "(if(750-self.agi/250*750/2<600?600:750-self.agi/250*750/2)) * (50-self.viewflag(10097)) / 50 + self.viewflag(10097) * 5 + 1d469",
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
