package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SkraugbashorMace {
  private SkraugbashorMace() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.skraugbashor_mace",
        "${item.skraugbashor_mace}",
        BodyPart.WEAPON,
        "PupOgreClub",
        null,
        null,
        "64kInvOgreClub",
        10284L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        280L,
        25L,
        20L,
        25L,
        1.0d,
        false,
        false,
        false,
        41378,
        1,
        447,
        "1d53+100",
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
