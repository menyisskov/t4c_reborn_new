package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CrystalSwordOfMight {
  private CrystalSwordOfMight() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.crystal_sword_of_might",
        "${item.crystal_sword_of_might}",
        BodyPart.WEAPON,
        "PupSkeletonSword",
        null,
        null,
        "64kInvSkeletonSword",
        77615L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        251L,
        21L,
        53L,
        50L,
        1.0d,
        false,
        false,
        false,
        41382,
        1,
        471,
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
        List.of(new ItemDefinition.ItemSpell(10436, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(847, 10027, "15", 0, 0),
            new ItemDefinition.ItemBoost(848, 6, "10", 0, 0),
            new ItemDefinition.ItemBoost(849, 3, "10", 0, 0),
            new ItemDefinition.ItemBoost(850, 2, "10", 0, 0)),
        List.of(),
        false);
  }
}
