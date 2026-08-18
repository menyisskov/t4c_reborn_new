package com.perso.T4C.npc.companion.definition;

import com.perso.T4C.npc.companion.CompanionDef;
import com.perso.T4C.npc.companion.CompanionSpellTrigger;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MageApprentice {
  private MageApprentice() {}

  public static CompanionDef definition() {
    return new CompanionDef(
        "mage_apprentice",
        "${companion.mage_apprentice}",
        List.of(
            new CompanionDef.Part(BodyPart.BODY, "PupNecromanRobe"),
            new CompanionDef.Part(BodyPart.LEGS, "PupLeatherPants"),
            new CompanionDef.Part(BodyPart.FEET, "PupBlackLeatherBoots"),
            new CompanionDef.Part(BodyPart.WEAPON, "PupGemStaff")),
        null,
        35,
        6.0f,
        2,
        5,
        0.4f,
        1.5f,
        List.of(
            new CompanionDef.SpellEntry(
                "spell.heal_light",
                CompanionSpellTrigger.HEAL_OWNER,
                30,
                12.0f,
                0.5f,
                10,
                16,
                1.2f,
                0.0f),
            new CompanionDef.SpellEntry(
                "spell.heal_light",
                CompanionSpellTrigger.HEAL_SELF,
                20,
                15.0f,
                0.4f,
                8,
                14,
                1.0f,
                0.0f),
            new CompanionDef.SpellEntry(
                "spell.fire_dart",
                CompanionSpellTrigger.ATTACK,
                10,
                3.0f,
                0.0f,
                6,
                12,
                1.5f,
                8.0f)));
  }
}
