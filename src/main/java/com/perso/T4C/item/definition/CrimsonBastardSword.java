package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CrimsonBastardSword {
  private CrimsonBastardSword() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.crimson_bastard_sword",
        "${item.crimson_bastard_sword}",
        BodyPart.WEAPON,
        "PupBattleSword",
        null,
        null,
        "64kInvWeapon 15",
        0L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        150L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40862,
        1,
        124,
        "1d48+90",
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
        List.of(new ItemDefinition.ItemSpell(10229, 0, 100)),
        List.of(new ItemDefinition.ItemBoost(563, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
