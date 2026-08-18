package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class LivreEnMauvaisEtat {
  private LivreEnMauvaisEtat() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.livre_en_mauvais_etat",
        "${item.livre_en_mauvais_etat}",
        BodyPart.BODY,
        null,
        null,
        null,
        "64kInvMisc1 5",
        0L,
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
        true,
        3453,
        6,
        128,
        null,
        null,
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
