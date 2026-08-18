package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilFlail1 {
  private MithrilFlail1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_flail_1",
        "${item.mithril_flail_1}",
        BodyPart.WEAPON,
        "PupFlail",
        null,
        null,
        "64kInvFlail",
        98491L,
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
        40693,
        1,
        3,
        "if(target.r_dark=5025?1d68+127:1d59+110)",
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
        List.of(new ItemDefinition.ItemBoost(431, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
