package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBackbreaker {
  private ItemItemBackbreaker() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.backbreaker",
        "${item.backbreaker}",
        BodyPart.WEAPON,
        "PupOgreClub",
        null,
        null,
        "64kInvOgreClub",
        4843L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        60L,
        0L,
        0L,
        56L,
        1.0d,
        false,
        false,
        false,
        41619,
        1,
        447,
        "1d20+31",
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
        List.of(new ItemDefinition.ItemSpell(10693, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(967, 4, "-5", 0, 0),
            new ItemDefinition.ItemBoost(968, 1, "-5", 0, 0),
            new ItemDefinition.ItemBoost(969, 3, "10", 0, 0)),
        List.of(),
        false);
  }
}
