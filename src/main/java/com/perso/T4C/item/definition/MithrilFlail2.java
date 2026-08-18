package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilFlail2 {
  private MithrilFlail2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_flail_2",
        "${item.mithril_flail_2}",
        BodyPart.WEAPON,
        "PupFlail",
        null,
        null,
        "64kInvFlail",
        0L,
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
        40694,
        1,
        3,
        "if(target.r_dark=5025?1d76+144:1d66+125)",
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
        List.of(new ItemDefinition.ItemBoost(432, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
