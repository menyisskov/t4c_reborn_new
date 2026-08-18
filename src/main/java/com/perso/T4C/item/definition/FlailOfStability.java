package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class FlailOfStability {
  private FlailOfStability() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.flail_of_stability",
        "${item.flail_of_stability}",
        BodyPart.WEAPON,
        "PupFlail",
        null,
        null,
        "64kInvFlail",
        1755L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        40L,
        0L,
        0L,
        41L,
        1.0d,
        false,
        false,
        false,
        40072,
        1,
        3,
        "1d2+23",
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
        List.of(new ItemDefinition.ItemBoost(148, 8, "10", 0, 0)),
        List.of(),
        false);
  }
}
