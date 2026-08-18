package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class WoodenClub1 {
  private WoodenClub1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wooden_club_1",
        "${item.wooden_club_1}",
        BodyPart.WEAPON,
        "PupOgreClub",
        null,
        null,
        "64kInvOgreClub",
        19L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40675,
        1,
        447,
        "1d4+1",
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
        List.of(new ItemDefinition.ItemBoost(413, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
