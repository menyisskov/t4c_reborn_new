package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemJaggedGypsyDagger {
  private ItemItemJaggedGypsyDagger() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.jagged_gypsy_dagger",
        "${item.jagged_gypsy_dagger}",
        BodyPart.WEAPON,
        "PupBattleDagger",
        null,
        null,
        "64kInvBattleDagger",
        12351L,
        4L,
        0.0d,
        0L,
        0L,
        0L,
        126L,
        0L,
        18L,
        28L,
        1.0d,
        false,
        false,
        false,
        41475,
        1,
        274,
        "1d23+40",
        "if(675-self.agi/250*675/2<600?600:675-self.agi/250*675/2)+1d338",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10402, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(799, 6, "5", 0, 0),
            new ItemDefinition.ItemBoost(800, 4, "5", 0, 0)),
        List.of(),
        false);
  }
}
