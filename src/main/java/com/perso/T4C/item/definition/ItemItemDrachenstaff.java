package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDrachenstaff {
  private ItemItemDrachenstaff() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.drachenstaff",
        "${item.drachenstaff}",
        BodyPart.WEAPON,
        "PupLichStaff",
        null,
        null,
        "64kInvLichStaff",
        0L,
        2L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        135L,
        0L,
        1.0d,
        false,
        false,
        false,
        41660,
        1,
        294,
        "1d9+13",
        "if(1575-self.agi/250*1575/2<1575/2?1575/2:1575-self.agi/250*1575/2)+1d788",
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
        List.of(new ItemDefinition.ItemBoost(940, 1, "50", 0, 0)),
        List.of(),
        false);
  }
}
