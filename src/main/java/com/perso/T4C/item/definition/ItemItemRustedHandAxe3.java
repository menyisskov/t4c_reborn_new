package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRustedHandAxe3 {
  private ItemItemRustedHandAxe3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rusted_hand_axe_3",
        "${item.rusted_hand_axe_3}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvSingle Axe",
        3148L,
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
        40410,
        1,
        123,
        "1d14+19",
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
        List.of(new ItemDefinition.ItemBoost(370, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
