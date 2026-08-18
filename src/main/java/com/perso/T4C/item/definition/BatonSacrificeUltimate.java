package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class BatonSacrificeUltimate {
  private BatonSacrificeUltimate() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.baton_sacrifice_ultimate",
        "${item.baton_sacrifice_ultimate}",
        BodyPart.WEAPON,
        "PupGemStaff",
        null,
        null,
        "64kInvGemStaff",
        25664L,
        10L,
        0.0d,
        0L,
        150L,
        0L,
        0L,
        310L,
        310L,
        0L,
        1.0d,
        false,
        false,
        false,
        4004,
        1,
        295,
        "1d50+64",
        "if(1125-self.agi/250*1125/2<600?600:1125-self.agi/250*1125/2)+1d563",
        0,
        -1,
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
