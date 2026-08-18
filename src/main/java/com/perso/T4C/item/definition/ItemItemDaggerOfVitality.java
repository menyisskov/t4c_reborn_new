package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDaggerOfVitality {
  private ItemItemDaggerOfVitality() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dagger_of_vitality",
        "${item.dagger_of_vitality}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        1514L,
        4L,
        0.0d,
        0L,
        0L,
        0L,
        42L,
        0L,
        25L,
        30L,
        1.0d,
        false,
        false,
        false,
        40071,
        1,
        274,
        "1d8+11",
        "if(675-self.agi/250*675/2<600?600:675-self.agi/250*675/2)+1d338",
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
        List.of(new ItemDefinition.ItemBoost(67, 2, "10", 0, 0)),
        List.of(),
        false);
  }
}
