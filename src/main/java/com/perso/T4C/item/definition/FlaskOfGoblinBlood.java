package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class FlaskOfGoblinBlood {
  private FlaskOfGoblinBlood() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.flask_of_goblin_blood",
        "${item.flask_of_goblin_blood}",
        null,
        null,
        null,
        null,
        "64kInvPotions 6",
        25L,
        2L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40226,
        5,
        258,
        null,
        "0",
        0,
        1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10103, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
