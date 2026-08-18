package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemBoneswordOfTheBerserker {
  private ItemItemBoneswordOfTheBerserker() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bonesword_of_the_berserker",
        "${item.bonesword_of_the_berserker}",
        BodyPart.WEAPON,
        "PupRealDarkSword",
        null,
        null,
        "64kInvRealDarkSword",
        12720L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        141L,
        22L,
        20L,
        25L,
        1.0d,
        false,
        false,
        false,
        41614,
        1,
        275,
        "1d25+44",
        "if(937-self.agi/250*937/2<600?600:937-self.agi/250*937/2)+1d469",
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
            new ItemDefinition.ItemBoost(923, 3, "10", 0, 0),
            new ItemDefinition.ItemBoost(924, 8, "10", 0, 0),
            new ItemDefinition.ItemBoost(925, 10001, "5", 0, 0),
            new ItemDefinition.ItemBoost(926, 10002, "5", 0, 0),
            new ItemDefinition.ItemBoost(966, 10009, "-25", 0, 0)),
        List.of(),
        false);
  }
}
