package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AxeOfDestruction {
  private AxeOfDestruction() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.axe_of_destruction",
        "${item.axe_of_destruction}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvSingle Axe",
        5407L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        82L,
        0L,
        25L,
        30L,
        1.0d,
        false,
        false,
        false,
        40068,
        1,
        123,
        "1d19+31",
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
        List.of(new ItemDefinition.ItemBoost(458, 3, "10", 0, 0)),
        List.of(),
        false);
  }
}
