package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GantsDuBoxeur7 {
  private GantsDuBoxeur7() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gants_du_boxeur_7",
        "${item.gants_du_boxeur_7}",
        BodyPart.WEAPON,
        null,
        null,
        null,
        "Inv_ManArmor01Glove__pal7",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        650L,
        650L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        3883,
        1,
        1128,
        "1d1880+8540",
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
