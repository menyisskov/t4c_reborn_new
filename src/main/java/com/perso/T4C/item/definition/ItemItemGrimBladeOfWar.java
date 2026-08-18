package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGrimBladeOfWar {
  private ItemItemGrimBladeOfWar() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.grim_blade_of_war",
        "${item.grim_blade_of_war}",
        BodyPart.WEAPON,
        "PupRealDarkSword",
        null,
        null,
        "64kInvRealDarkSword",
        44221L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        266L,
        35L,
        0L,
        35L,
        1.0d,
        false,
        false,
        false,
        41390,
        1,
        275,
        "1d41+77",
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
            new ItemDefinition.ItemBoost(809, 10001, "10", 0, 0),
            new ItemDefinition.ItemBoost(810, 10002, "10", 0, 0),
            new ItemDefinition.ItemBoost(811, 8, "50", 0, 0),
            new ItemDefinition.ItemBoost(812, 22, "10", 0, 0)),
        List.of(),
        false);
  }
}
