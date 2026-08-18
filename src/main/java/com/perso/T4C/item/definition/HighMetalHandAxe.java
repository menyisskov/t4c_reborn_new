package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class HighMetalHandAxe {
  private HighMetalHandAxe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_metal_hand_axe",
        "${item.high_metal_hand_axe}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvSingle Axe",
        49245L,
        10L,
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
        40391,
        1,
        123,
        "1d53+100",
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
