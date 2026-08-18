package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GmDrussBriberOfSouls {
  private GmDrussBriberOfSouls() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gm_druss_briber_of_souls",
        "${item.gm_druss_briber_of_souls}",
        BodyPart.WEAPON,
        "PupBattleAxe",
        null,
        null,
        "64kInvDouble Axe",
        13612L,
        13L,
        1.0d,
        0L,
        0L,
        0L,
        130L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40707,
        1,
        122,
        "1d36+63",
        "if(1125-self.agi/250*1125/2<600?600:1125-self.agi/250*1125/2)+1d563",
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
        List.of(
            new ItemDefinition.ItemBoost(394, 3, "10", 0, 0),
            new ItemDefinition.ItemBoost(395, 9, "25", 0, 0)),
        List.of(),
        false);
  }
}
