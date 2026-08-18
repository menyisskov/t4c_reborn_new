package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Axe {
  private Axe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.axe",
        "${item.axe}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvSingle Axe",
        787L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        39L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40061,
        1,
        123,
        "1d10+12",
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
