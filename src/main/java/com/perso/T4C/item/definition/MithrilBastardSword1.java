package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilBastardSword1 {
  private MithrilBastardSword1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_bastard_sword_1",
        "${item.mithril_bastard_sword_1}",
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
        314L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40376,
        1,
        124,
        "1d78+147",
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
        List.of(new ItemDefinition.ItemBoost(320, 8, "self.true_attack*15/100", 0, 0)),
        List.of(),
        false);
  }
}
