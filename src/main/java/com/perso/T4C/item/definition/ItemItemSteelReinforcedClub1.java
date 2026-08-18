package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemSteelReinforcedClub1 {
  private ItemItemSteelReinforcedClub1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.steel_reinforced_club_1",
        "${item.steel_reinforced_club_1}",
        BodyPart.WEAPON,
        "PupOgreClub",
        null,
        null,
        "64kInvOgreClub",
        1574L,
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
        40678,
        1,
        447,
        "if(target.r_dark=5025?1d10+13:1d9+11)",
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
        List.of(new ItemDefinition.ItemBoost(416, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
