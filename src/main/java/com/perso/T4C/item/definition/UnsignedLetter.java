package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

// T4C-0050, "The Unsigned Letter": granted the moment a character is reborn for the first time
// (see quest/UnsignedLetterQuest.java). Not equippable - same non-equippable shape as
// BoundGodsigil/WyrmScales - carried until resolved with MirrorwardenYsmera in the Colosseum.
public final class UnsignedLetter {
  private UnsignedLetter() {}

  public static final String KEY = "item.unsigned_letter";

  public static ItemDefinition definition() {
    return new ItemDefinition(
        KEY,
        "${item.unsigned_letter}",
        null,
        null,
        null,
        null,
        "64kInvDestinyGem",
        0L,
        0L, // weightless (it's a folded letter) - see UnsignedLetterQuest.onRebirth: a freshly
            // reborn character's carry capacity just dropped with their reset stats, so this
            // can't be allowed to fail a canAdd() check the way a real 1-weight item could.
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        true,
        false,
        true,
        0,
        2,
        0,
        null,
        null,
        0,
        0,
        false,
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
