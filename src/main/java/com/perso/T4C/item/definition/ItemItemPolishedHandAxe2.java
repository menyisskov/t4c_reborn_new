package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemPolishedHandAxe2 {
  private ItemItemPolishedHandAxe2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.polished_hand_axe_2",
        "${item.polished_hand_axe_2}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvSingle Axe",
        20887L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        97L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40419,
        1,
        123,
        "1d29+49",
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
        List.of(new ItemDefinition.ItemBoost(372, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
