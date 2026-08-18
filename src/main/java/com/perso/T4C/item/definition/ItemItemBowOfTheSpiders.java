package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBowOfTheSpiders {
  private ItemItemBowOfTheSpiders() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bow_of_the_spiders",
        "${item.bow_of_the_spiders}",
        BodyPart.WEAPON,
        "PupShortBow",
        null,
        null,
        "64kInvShortBowFancy",
        0L,
        6L,
        0.0d,
        0L,
        0L,
        0L,
        31L,
        184L,
        0L,
        0L,
        1150.0d,
        false,
        true,
        false,
        41667,
        9,
        449,
        "1d30+68+5*arrow_dmg/4",
        "1150",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10681, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
