package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGreatAxeOfTheCrow {
  private ItemItemGreatAxeOfTheCrow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.great_axe_of_the_crow",
        "${item.great_axe_of_the_crow}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvDouble Axe",
        55412L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        242L,
        43L,
        25L,
        25L,
        1.0d,
        false,
        false,
        false,
        41384,
        1,
        122,
        "1d53+100",
        "if(937-self.agi/250*937/2<600?600:937-self.agi/250*937/2)+1d469",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10439, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(837, 10002, "10", 0, 0),
            new ItemDefinition.ItemBoost(838, 10001, "10", 0, 0),
            new ItemDefinition.ItemBoost(839, 10027, "10", 0, 0),
            new ItemDefinition.ItemBoost(840, 8, "25", 0, 0),
            new ItemDefinition.ItemBoost(841, 3, "10", 0, 0)),
        List.of(),
        false);
  }
}
