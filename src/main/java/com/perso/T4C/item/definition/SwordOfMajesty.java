package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class SwordOfMajesty {
  private SwordOfMajesty() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sword_of_majesty",
        "${item.sword_of_majesty}",
        null,
        null,
        null,
        null,
        "64kInvNormalSword",
        0L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        184L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40902,
        1,
        2,
        "1d34+63",
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
        List.of(),
        List.of(),
        false);
  }
}
