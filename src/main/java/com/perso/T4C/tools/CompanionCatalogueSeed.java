package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.CompanionDefBinaryIO;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.CompanionDef;
import com.perso.T4C.npc.CompanionSpellTrigger;
import com.perso.T4C.player.BodyPart;

import java.io.File;
import java.util.List;
import java.util.Map;

/** Rebuilds the companion catalogue backing SUMMON_COMPANION dialogue actions. */
public final class CompanionCatalogueSeed {
    private CompanionCatalogueSeed() {
    }

    public static void main(String[] args) throws Exception {
        I18n.update(Map.of(
                "companion.mage_apprentice", "Apprenti mage",
                "companion.warrior_squire", "Écuyer d'armes"
        ));

        // Ranged caster: fragile in melee, but opens with fire darts and keeps
        // both itself and its owner standing.
        CompanionDef mage = new CompanionDef(
                "mage_apprentice",
                "Apprenti mage",
                List.of(
                        new CompanionDef.Part(BodyPart.BODY, "PupNecromanRobe"),
                        new CompanionDef.Part(BodyPart.LEGS, "PupLeatherPants"),
                        new CompanionDef.Part(BodyPart.FEET, "PupBlackLeatherBoots"),
                        new CompanionDef.Part(BodyPart.WEAPON, "PupGemStaff")
                ),
                null,
                35, 6f,          // hp: 35 at level 1, +6 per level
                2, 5, 0.4f,      // weak melee, it is a caster
                1.5f,            // attack cooldown
                List.of(
                        // Healing outranks damage so the pair survives long fights.
                        new CompanionDef.SpellEntry("spell.heal_light", CompanionSpellTrigger.HEAL_OWNER,
                                30, 12f, 0.5f, 10, 16, 1.2f, 0f),
                        new CompanionDef.SpellEntry("spell.heal_light", CompanionSpellTrigger.HEAL_SELF,
                                20, 15f, 0.4f, 8, 14, 1.0f, 0f),
                        new CompanionDef.SpellEntry("spell.fire_dart", CompanionSpellTrigger.ATTACK,
                                10, 3f, 0f, 6, 12, 1.5f, 8f)
                ));

        // Melee bruiser: no offensive spells, only patches itself up.
        CompanionDef squire = new CompanionDef(
                "warrior_squire",
                "Écuyer d'armes",
                List.of(
                        new CompanionDef.Part(BodyPart.BODY, "PupChainMailBody"),
                        new CompanionDef.Part(BodyPart.LEGS, "PupChainMailLegs"),
                        new CompanionDef.Part(BodyPart.FEET, "PupPlateBoots"),
                        new CompanionDef.Part(BodyPart.HEAD, "PupChainMailCoif"),
                        new CompanionDef.Part(BodyPart.WEAPON, "PupNormalSword"),
                        new CompanionDef.Part(BodyPart.SHIELD, "PupRomanShield")
                ),
                null,
                70, 12f,
                5, 11, 1.0f,
                1.3f,
                List.of(new CompanionDef.SpellEntry("spell.heal_light", CompanionSpellTrigger.HEAL_SELF,
                        20, 20f, 0.35f, 8, 14, 0.8f, 0f)));

        CompanionDefBinaryIO.write(new File(Paths.COMPANIONS_BIN), List.of(mage, squire));
        System.out.println("Wrote 2 companion definition(s) to " + Paths.COMPANIONS_BIN);
    }
}
