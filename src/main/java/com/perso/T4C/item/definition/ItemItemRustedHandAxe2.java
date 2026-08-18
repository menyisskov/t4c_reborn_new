package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRustedHandAxe2 {
  private ItemItemRustedHandAxe2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rusted_hand_axe_2",
        "${item.rusted_hand_axe_2}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvSingle Axe",
        2361L,
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
        40455,
        1,
        123,
        "1d13+16",
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
        List.of(new ItemDefinition.ItemBoost(369, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
