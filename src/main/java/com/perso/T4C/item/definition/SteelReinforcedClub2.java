package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SteelReinforcedClub2 {
  private SteelReinforcedClub2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.steel_reinforced_club_2",
        "${item.steel_reinforced_club_2}",
        BodyPart.WEAPON,
        "PupOgreClub",
        null,
        null,
        "64kInvOgreClub",
        2361L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        30L,
        0L,
        0L,
        34L,
        1.0d,
        false,
        false,
        false,
        40679,
        1,
        447,
        "if(target.r_dark=5025?1d11+15:1d10+13)",
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
        List.of(new ItemDefinition.ItemBoost(417, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
