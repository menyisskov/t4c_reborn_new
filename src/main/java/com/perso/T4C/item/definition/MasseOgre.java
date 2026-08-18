package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MasseOgre {
  private MasseOgre() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.masse_ogre",
        "${item.masse_ogre}",
        BodyPart.WEAPON,
        "PupSkavenClub",
        null,
        null,
        "64kInvSkavenClub",
        1000000L,
        0L,
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
        3969,
        1,
        463,
        "1d1",
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
        List.of(),
        List.of(),
        false);
  }
}
