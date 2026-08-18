package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemWoodenClub3 {
  private ItemItemWoodenClub3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wooden_club_3",
        "${item.wooden_club_3}",
        BodyPart.WEAPON,
        "PupOgreClub",
        null,
        null,
        "64kInvOgreClub",
        38L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40677,
        1,
        447,
        "1d4+2",
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
        List.of(new ItemDefinition.ItemBoost(415, 8, "self.true_attack*50/100", 0, 0)),
        List.of(),
        false);
  }
}
