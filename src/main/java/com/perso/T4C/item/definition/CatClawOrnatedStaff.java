package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CatClawOrnatedStaff {
  private CatClawOrnatedStaff() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cat_claw_ornated_staff",
        "${item.cat_claw_ornated_staff}",
        BodyPart.WEAPON,
        "PupLichStaff",
        null,
        null,
        "64kInvLichStaff",
        25664L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        20L,
        0L,
        73L,
        73L,
        1.0d,
        false,
        false,
        false,
        41477,
        1,
        294,
        "1d21+34",
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
            new ItemDefinition.ItemBoost(822, 18, "10", 0, 0),
            new ItemDefinition.ItemBoost(823, 19, "10", 0, 0),
            new ItemDefinition.ItemBoost(824, 16, "10", 0, 0),
            new ItemDefinition.ItemBoost(825, 1, "10", 0, 0),
            new ItemDefinition.ItemBoost(826, 4, "10", 0, 0)),
        List.of(),
        false);
  }
}
