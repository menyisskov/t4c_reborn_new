package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilFlail {
  private MithrilFlail() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_flail",
        "${item.mithril_flail}",
        BodyPart.WEAPON,
        "PupFlail",
        null,
        null,
        "64kInvFlail",
        49245L,
        9L,
        0.0d,
        0L,
        0L,
        0L,
        170L,
        0L,
        0L,
        139L,
        1.0d,
        false,
        false,
        false,
        40531,
        1,
        3,
        "if(target.r_dark=5025?1d59+110:1d51+96)",
        "if(937-self.agi/250*937/2<600?600:937-self.agi/250*937/2)+1d469",
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
