package com.perso.T4C.tools;

import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.player.BodyPart;

import java.util.List;
import java.util.Map;

/** Exact NPC appearances reconstructed from the original WDA client tables and NPC DLL sources. */
final class NpcOriginalAppearance {
    record Appearance(List<NpcDef.Part> parts, String spriteBase) {}

    private static final Map<String, Appearance> BY_NAME = Map.ofEntries(
            entry("Araknor", male(
                    part(BodyPart.BODY, "PupNecromanRobe"), part(BodyPart.FEET, "PupBlackLeatherBoots"),
                    part(BodyPart.HEAD, "PupHornedHelmet"), part(BodyPart.LEGS, "PupLeatherPants"))),
            entry("Balork", sprite("Demon")),
            entry("BrotherKiran", male(part(BodyPart.BODY, "PupWhiteRobe"), part(BodyPart.FEET, "PupLeatherBoots"))),
            entry("DelvarIrongrip", guard()),
            entry("Darkfang", sprite("DragonSTMOV")),
            entry("Edgar", cloth(false)),
            entry("ElmertMerkiss", guard()),
            entry("Fali", cloth(true)),
            entry("Geena", cloth(true)),
            entry("Guardman", guard()),
            entry("Halam", cloth(false)),
            entry("Iraltok", mage()),
            entry("Isulgur", noble(true)),
            entry("JagarKar", noble(true)),
            entry("Jalus", cloth(false)),
            entry("Kalastor", male(
                    part(BodyPart.BODY, "PupBodyClothSet1"), part(BodyPart.FEET, "PupLeatherBoots"),
                    part(BodyPart.LEGS, "PupLegsClothSet1"), part(BodyPart.WEAPON, "PupBattleDagger"))),
            entry("Khiliam", female(
                    part(BodyPart.BODY, "WoWhiteRobe"), part(BodyPart.FEET, "WoLeatherBoots"),
                    part(BodyPart.WEAPON, "PupGemStaff"))),
            entry("KirlorDhul", male(
                    part(BodyPart.BODY, "PupChainMailBody"), part(BodyPart.FEET, "PupPlateFoot"),
                    part(BodyPart.LEFT_HAND, "PupLeatherGloveL"), part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
                    part(BodyPart.HEAD, "PupChainMailCoif"), part(BodyPart.LEGS, "PupChainMailLegs"),
                    part(BodyPart.BACK, "PupRedCape"))),
            entry("Lothan", mage()),
            entry("Markam", noble(false)),
            entry("MarnecSunim", cloth(false)),
            entry("Mithrand", noble(true)),
            entry("Moonrock", female(
                    part(BodyPart.BODY, "WoWhiteRobe"), part(BodyPart.FEET, "WoLeatherBoots"),
                    part(BodyPart.WEAPON, "PupWoodenStaff"))),
            entry("Murmuntag", sprite("Orc")),
            entry("Ortanalas", male(
                    part(BodyPart.BODY, "PupLeatherBody"), part(BodyPart.FEET, "PupLeatherBoots"),
                    part(BodyPart.LEFT_HAND, "PupLeatherGloveL"), part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
                    part(BodyPart.LEGS, "PupStuddedLegs"), part(BodyPart.WEAPON, "PupBattleSword"),
                    part(BodyPart.SHIELD, "PupRomanShield"))),
            entry("Pig", sprite("Pig")),
            entry("Rolph", cloth(false)),
            entry("Shadow", male(
                    part(BodyPart.BODY, "PupLeatherBody"), part(BodyPart.FEET, "PupLeatherBoots"),
                    part(BodyPart.LEFT_HAND, "PupLeatherGloveL"), part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
                    part(BodyPart.LEGS, "PupStuddedLegs"), part(BodyPart.WEAPON, "PupBattleDagger"),
                    part(BodyPart.BACK, "PupRedCape"))),
            entry("Sigfried", cloth(false)),
            entry("TwinNevanis", male(part(BodyPart.BODY, "PupWhiteRobe"), part(BodyPart.FEET, "PupLeatherBoots"))),
            entry("TwinShovanis", mage()),
            entry("Uranos", male(
                    part(BodyPart.BODY, "PupNecromanRobe"), part(BodyPart.FEET, "PupBlackLeatherBoots"),
                    part(BodyPart.LEFT_HAND, "PupLeatherGloveL"), part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
                    part(BodyPart.LEGS, "PupLeatherPants"), part(BodyPart.BACK, "PupRedCape"))),
            // MonsterStatSetup assigns Samaritan twice; the later assignment is the effective one.
            entry("LighthavenSamaritan", sprite("PaysanModel1"))
    );

    private NpcOriginalAppearance() {}

    static Appearance forName(String name) {
        Appearance appearance = BY_NAME.get(name);
        if (appearance == null) throw new IllegalArgumentException("No verified original appearance for NPC: " + name);
        return appearance;
    }

    private static Map.Entry<String, Appearance> entry(String name, Appearance appearance) {
        return Map.entry(name, appearance);
    }

    private static Appearance sprite(String base) { return new Appearance(List.of(), base); }
    private static Appearance male(NpcDef.Part... parts) { return new Appearance(List.of(parts), null); }
    private static Appearance female(NpcDef.Part... parts) { return new Appearance(List.of(parts), null); }
    private static NpcDef.Part part(BodyPart slot, String sprite) { return new NpcDef.Part(slot, sprite); }

    private static Appearance mage() {
        return male(part(BodyPart.BODY, "PupNecromanRobe"), part(BodyPart.FEET, "PupBlackLeatherBoots"),
                part(BodyPart.LEGS, "PupLeatherPants"));
    }

    private static Appearance cloth(boolean female) {
        return female
                ? female(part(BodyPart.BODY, "WoClothBody"), part(BodyPart.FEET, "WoLeatherBoots"),
                        part(BodyPart.LEGS, "WoClothRobe"))
                : male(part(BodyPart.BODY, "PupBodyClothSet1"), part(BodyPart.FEET, "PupLeatherBoots"),
                        part(BodyPart.LEGS, "PupLegsClothSet1"));
    }

    private static Appearance noble(boolean helm) {
        var parts = new java.util.ArrayList<NpcDef.Part>(List.of(
                part(BodyPart.BODY, "PupChainMailBody"), part(BodyPart.FEET, "PupPlateFoot"),
                part(BodyPart.LEFT_HAND, "PupLeatherGloveL"), part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
                part(BodyPart.LEGS, "PupLeatherPants")));
        if (helm) parts.add(part(BodyPart.HEAD, "PupChainMailCoif"));
        return new Appearance(List.copyOf(parts), null);
    }

    private static Appearance guard() {
        return male(part(BodyPart.BODY, "PupChainMailBody"), part(BodyPart.FEET, "PupPlateFoot"),
                part(BodyPart.LEFT_HAND, "PupLeatherGloveL"), part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
                part(BodyPart.HEAD, "PupChainMailCoif"), part(BodyPart.LEGS, "PupChainMailLegs"),
                part(BodyPart.WEAPON, "PupBattleSword"), part(BodyPart.SHIELD, "PupRomanShield"),
                part(BodyPart.BACK, "PupRedCape"));
    }
}
