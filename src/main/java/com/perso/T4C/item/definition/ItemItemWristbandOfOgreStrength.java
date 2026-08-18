package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemWristbandOfOgreStrength {
  private ItemItemWristbandOfOgreStrength() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wristband_of_ogre_strength",
        "${item.wristband_of_ogre_strength}",
        BodyPart.BRACER,
        null,
        null,
        null,
        "64kInvBracelet",
        4754L,
        2L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        40L,
        53L,
        0.0d,
        false,
        false,
        false,
        40718,
        2,
        237,
        null,
        "0",
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
        List.of(new ItemDefinition.ItemBoost(441, 3, "25", 0, 0)),
        List.of(),
        false);
  }
}
