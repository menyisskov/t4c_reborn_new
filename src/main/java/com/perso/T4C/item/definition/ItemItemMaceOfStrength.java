package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMaceOfStrength {
  private ItemItemMaceOfStrength() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mace_of_strength",
        "${item.mace_of_strength}",
        BodyPart.WEAPON,
        "PupMace",
        null,
        null,
        "64kInvMace",
        1755L,
        13L,
        1.0d,
        0L,
        0L,
        0L,
        40L,
        0L,
        17L,
        47L,
        1.0d,
        false,
        false,
        false,
        41542,
        1,
        119,
        "if(target.r_dark=5025?1d13+19:1d12+16)",
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
        List.of(new ItemDefinition.ItemBoost(870, 3, "5", 0, 0)),
        List.of(),
        false);
  }
}
