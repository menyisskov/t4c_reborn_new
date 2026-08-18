package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRangKwan1 {
  private ItemItemRangKwan1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rang_kwan_1",
        "${item.rang_kwan_1}",
        BodyPart.WEAPON,
        "PupWoodenStaff",
        null,
        null,
        "64kInvWoodenStaff",
        3990L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        16L,
        0L,
        56L,
        0L,
        1.0d,
        false,
        false,
        false,
        40670,
        1,
        118,
        "1d13+18",
        "if(1650-self.agi/250*1650/2<1650/2?1650/2:1650-self.agi/250*1650/2)+1d825",
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
        List.of(new ItemDefinition.ItemBoost(409, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
