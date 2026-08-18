package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemStaffOfHope {
  private ItemItemStaffOfHope() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.staff_of_hope",
        "${item.staff_of_hope}",
        BodyPart.WEAPON,
        "V2_Sceptre01",
        null,
        null,
        "Inv_V2_Sceptre01",
        59113L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        110L,
        0L,
        23L,
        131L,
        1.0d,
        false,
        false,
        false,
        40908,
        1,
        295,
        "if(target.r_dark=5025?1d46+82:1d40+71)",
        "if(862-self.agi/250*862/2<600?600:862-self.agi/250*862/2)+1d431",
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
            new ItemDefinition.ItemBoost(572, 23, "self.true_light *20/100", 0, 0),
            new ItemDefinition.ItemBoost(573, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
