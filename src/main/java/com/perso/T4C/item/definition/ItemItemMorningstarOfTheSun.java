package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMorningstarOfTheSun {
  private ItemItemMorningstarOfTheSun() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.morningstar_of_the_sun",
        "${item.morningstar_of_the_sun}",
        BodyPart.WEAPON,
        "PupGoldenMorningStar",
        null,
        null,
        "64kInvGoldenMorningStar",
        32533L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        140L,
        0L,
        0L,
        201L,
        1.0d,
        false,
        false,
        false,
        40632,
        1,
        280,
        "if(target.r_dark=5025?1d48+90:1d42+78)",
        "if(862-self.agi/250*862/2<600?600:862-self.agi/250*862/2)+1d431",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10405, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(150, 4, "10", 0, 0),
            new ItemDefinition.ItemBoost(151, 22, "10", 0, 0),
            new ItemDefinition.ItemBoost(783, 23, "10", 0, 0)),
        List.of(),
        false);
  }
}
