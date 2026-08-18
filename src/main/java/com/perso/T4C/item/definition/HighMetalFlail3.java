package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class HighMetalFlail3 {
  private HighMetalFlail3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_flail_3",
        "${item.high_metal_flail_3}",
        BodyPart.WEAPON,
        "PupFlail",
        null,
        null,
        "64kInvFlail",
        97601L,
        9L,
        0.0d,
        0L,
        0L,
        0L,
        140L,
        0L,
        0L,
        116L,
        1.0d,
        false,
        false,
        false,
        40692,
        1,
        3,
        "if(target.r_dark=5025?1d73+135:1d63+117)",
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
        List.of(new ItemDefinition.ItemBoost(430, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
