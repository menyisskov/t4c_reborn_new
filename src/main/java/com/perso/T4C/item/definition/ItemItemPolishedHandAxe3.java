package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemPolishedHandAxe3 {
  private ItemItemPolishedHandAxe3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.polished_hand_axe_3",
        "${item.polished_hand_axe_3}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvSingle Axe",
        27849L,
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
        40428,
        1,
        123,
        "1d33+57",
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
        List.of(new ItemDefinition.ItemBoost(373, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
