package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SwordOfLight {
  private SwordOfLight() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sword_of_light",
        "${item.sword_of_light}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvGlinting Sword",
        3321L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        68L,
        0L,
        0L,
        23L,
        1.0d,
        false,
        false,
        false,
        41543,
        1,
        202,
        "1d17+27",
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
        List.of(new ItemDefinition.ItemBoost(868, 4, "5", 0, 0)),
        List.of(),
        false);
  }
}
