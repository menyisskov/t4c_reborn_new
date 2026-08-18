package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class HolyCutlass {
  private HolyCutlass() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.holy_cutlass",
        "${item.holy_cutlass}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvShortSword",
        0L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        120L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40857,
        1,
        1,
        "if(target.r_dark=5025?1d35+62:1d31+54)",
        "if(787-self.agi/250*787/2<600?600:787-self.agi/250*787/2)+1d394",
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
