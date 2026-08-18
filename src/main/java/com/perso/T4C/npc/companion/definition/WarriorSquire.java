package com.perso.T4C.npc.companion.definition;

import com.perso.T4C.npc.companion.CompanionDef;
import com.perso.T4C.npc.companion.CompanionSpellTrigger;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class WarriorSquire {
  private WarriorSquire() {}

  public static CompanionDef definition() {
    return new CompanionDef(
        "warrior_squire",
        "${companion.warrior_squire}",
        List.of(
            new CompanionDef.Part(BodyPart.BODY, "PupChainMailBody"),
            new CompanionDef.Part(BodyPart.LEGS, "PupChainMailLegs"),
            new CompanionDef.Part(BodyPart.FEET, "PupPlateBoots"),
            new CompanionDef.Part(BodyPart.HEAD, "PupChainMailCoif"),
            new CompanionDef.Part(BodyPart.WEAPON, "PupNormalSword"),
            new CompanionDef.Part(BodyPart.SHIELD, "PupRomanShield")),
        null,
        70,
        12.0f,
        5,
        11,
        1.0f,
        1.3f,
        List.of(
            new CompanionDef.SpellEntry(
                "spell.heal_light",
                CompanionSpellTrigger.HEAL_SELF,
                20,
                20.0f,
                0.35f,
                8,
                14,
                0.8f,
                0.0f)));
  }
}
