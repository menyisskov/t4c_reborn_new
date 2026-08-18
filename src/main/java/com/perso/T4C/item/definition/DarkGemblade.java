package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DarkGemblade {
  private DarkGemblade() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dark_gemblade",
        "${item.dark_gemblade}",
        BodyPart.WEAPON,
        "PupRealDarkSword",
        null,
        null,
        "64kInvRealDarkSword",
        53995L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        242L,
        0L,
        53L,
        43L,
        1.0d,
        false,
        false,
        false,
        41395,
        1,
        275,
        "1d53+100",
        "if(937-self.agi/250*937/2<600?600:937-self.agi/250*937/2)+1d469",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10412, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(814, 24, "5", 0, 0),
            new ItemDefinition.ItemBoost(815, 3, "5", 0, 0),
            new ItemDefinition.ItemBoost(816, 6, "5", 0, 0),
            new ItemDefinition.ItemBoost(817, 1, "5", 0, 0),
            new ItemDefinition.ItemBoost(818, 2, "5", 0, 0),
            new ItemDefinition.ItemBoost(819, 4, "5", 0, 0)),
        List.of(),
        false);
  }
}
