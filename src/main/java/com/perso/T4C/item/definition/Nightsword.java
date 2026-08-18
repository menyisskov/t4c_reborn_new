package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Nightsword {
  private Nightsword() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.nightsword",
        "${item.nightsword}",
        BodyPart.WEAPON,
        "PupRealDarkSword",
        null,
        null,
        "64kInvRealDarkSword",
        3370L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        65L,
        0L,
        30L,
        25L,
        1.0d,
        false,
        false,
        false,
        40076,
        1,
        275,
        "1d13+20",
        "if(787-self.agi/250*787/2<600?600:787-self.agi/250*787/2)+1d394",
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
        List.of(new ItemDefinition.ItemBoost(37, 6, "10", 0, 0)),
        List.of(),
        false);
  }
}
