package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAdamantiteBlade1 {
  private ItemItemAdamantiteBlade1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.adamantite_blade_1",
        "${item.adamantite_blade_1}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        98491L,
        5L,
        0.0d,
        0L,
        0L,
        0L,
        242L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40441,
        1,
        274,
        "1d54+101",
        "if(750-self.agi/250*750/2<600?600:750-self.agi/250*750/2)+1d375",
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
        List.of(new ItemDefinition.ItemBoost(362, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
