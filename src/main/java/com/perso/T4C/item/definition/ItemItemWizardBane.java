package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemWizardBane {
  private ItemItemWizardBane() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wizard_bane",
        "${item.wizard_bane}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        0L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        329L,
        0L,
        73L,
        73L,
        1.0d,
        false,
        false,
        false,
        41656,
        1,
        2,
        "1d71+130",
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
        List.of(
            new ItemDefinition.ItemSpell(10665, 0, 100),
            new ItemDefinition.ItemSpell(10674, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(918, 12, "10", 0, 0),
            new ItemDefinition.ItemBoost(919, 22, "10", 0, 0),
            new ItemDefinition.ItemBoost(920, 15, "10", 0, 0),
            new ItemDefinition.ItemBoost(921, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(922, 14, "10", 0, 0)),
        List.of(),
        false);
  }
}
