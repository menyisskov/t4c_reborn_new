package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.player.BodyPart;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/** Rebuilds the reset NPC catalogue with the reference NPC examples. */
public final class NpcCatalogueSeed {
    private NpcCatalogueSeed() {
    }

    public static void main(String[] args) throws Exception {
        String welcome = "Moi Iraltok, le chercheur de connaissances, vous salue, mon ami. "
                + "Je suis aussi un grand scribe arcanique et je peux vous enseigner plusieurs sorts.";
        I18n.update(Map.of(
                "npc.iraltok", "Iraltok",
                "npc.welcome.iraltok", welcome,
                "npc.topic_keyword.iraltok.0.0", "sorts"
        ));
        NpcDef iraltok = new NpcDef(
                "Iraltok",
                "Iraltok",
                List.of(
                        new NpcDef.Part(BodyPart.BODY, "PupNecromanRobe"),
                        new NpcDef.Part(BodyPart.LEGS, "PupLeatherPants"),
                        new NpcDef.Part(BodyPart.FEET, "PupBlackLeatherBoots")
                ),
                null,
                0,
                List.of(),
                welcome,
                List.of(new NpcDef.DialogTopic(
                        List.of("sorts"),
                        null,
                        List.of(new NpcDef.Action(
                                ActionType.OPEN_SPELL_LEARNING,
                                List.of("spell.flaming_arrow")))
                ))
        );
        String moonrockWelcome = "Je suis Moonrock, prêtresse de ce temple. Si vos blessures vous accablent, je peux invoquer la grâce des dieux pour vous soigner.";
        I18n.update(Map.of(
                "npc.moonrock", "Moonrock",
                "npc.welcome.moonrock", moonrockWelcome,
                "npc.topic_keyword.moonrock.0.0", "soigner",
                "npc.topic_keyword.moonrock.0.1", "heal"
        ));
        NpcDef moonrock = new NpcDef(
                "Moonrock", "Moonrock",
                List.of(
                        new NpcDef.Part(BodyPart.BODY, "WoWhiteRobe"),
                        new NpcDef.Part(BodyPart.LEGS, "PupNakedLegs"),
                        new NpcDef.Part(BodyPart.FEET, "PupNakedFoot")
                ), null, 0,
                List.of("${npc.flee_shout.moonrock.0}", "${npc.flee_shout.moonrock.1}"),
                moonrockWelcome,
                List.of(new NpcDef.DialogTopic(
                        List.of("soigner", "heal"), null,
                        List.of(new NpcDef.Action(ActionType.HEAL))))
        );
        String khiliamWelcome = "Je suis Khiliam, gardien de la lumière. Si vous souhaitez éclairer votre chemin dans les ténèbres, je peux vous enseigner le sort Lumière.";
        I18n.update(Map.of(
                "npc.khiliam", "Khiliam",
                "npc.welcome.khiliam", khiliamWelcome,
                "npc.topic_keyword.khiliam.0.0", "lumière",
                "npc.topic_keyword.khiliam.0.1", "light",
                "npc.topic_keyword.khiliam.0.2", "sorts"
        ));
        NpcDef khiliam = new NpcDef(
                "Khiliam", "Khiliam",
                List.of(
                        new NpcDef.Part(BodyPart.BODY, "WoWhiteRobe"),
                        new NpcDef.Part(BodyPart.LEGS, "PupNakedLegs"),
                        new NpcDef.Part(BodyPart.FEET, "PupNakedFoot")
                ), null, 0,
                List.of("${npc.flee_shout.moonrock.0}", "${npc.flee_shout.moonrock.1}"),
                khiliamWelcome,
                List.of(new NpcDef.DialogTopic(
                        List.of("lumière", "light", "sorts"), null,
                        List.of(new NpcDef.Action(ActionType.OPEN_SPELL_LEARNING,
                                List.of("spell.light"))))));
        String jagarKarWelcome = "Je suis JagarKar, maître d'armes. Si vous désirez perfectionner votre art du combat, du tir à l'arc ou de l'esquive, je peux vous enseigner.";
        I18n.update(Map.of(
                "npc.jagarkar", "JagarKar",
                "npc.welcome.jagarkar", jagarKarWelcome,
                "npc.topic_keyword.jagarkar.0.0", "skills",
                "npc.topic_keyword.jagarkar.0.1", "entraînement",
                "npc.topic_keyword.jagarkar.0.2", "enseigner"
        ));
        NpcDef jagarKar = new NpcDef(
                "JagarKar", "JagarKar",
                List.of(
                        new NpcDef.Part(BodyPart.BODY, "PupPlateBody"),
                        new NpcDef.Part(BodyPart.HEAD, "PupPlateHelm"),
                        new NpcDef.Part(BodyPart.LEGS, "PupNakedLegs"),
                        new NpcDef.Part(BodyPart.FEET, "PupNakedFoot"),
                        new NpcDef.Part(BodyPart.WEAPON, "PupNormalSword"),
                        new NpcDef.Part(BodyPart.SHIELD, "PupRomanShield")
                ), null, 0, List.of(), jagarKarWelcome,
                List.of(new NpcDef.DialogTopic(
                        List.of("skills", "entraînement", "enseigner"), null,
                        List.of(new NpcDef.Action(ActionType.OPEN_SKILL_LEARNING,
                                List.of("attack", "archery", "dodge")))))
        );
        List<NpcDef> defs = new ArrayList<>(List.of(iraltok, moonrock, khiliam, jagarKar));
        defs.add(catalogueNpc("Araknor", "Araknor", List.of("spell.lesser_drain")));
        defs.add(catalogueNpc("Balork", "Balork"));
        defs.add(catalogueNpc("BrotherKiran", "Brother Kiran"));
        defs.add(catalogueNpc("DelvarIrongrip", "Delvar Irongrip"));
        defs.add(catalogueNpc("Dragon", "DarkFang"));
        defs.add(catalogueNpc("Edgar", "Edgar Gimplestratten"));
        defs.add(catalogueNpc("ElmertMerkiss", "Elmert Merkiss"));
        defs.add(catalogueNpc("Fali", "Fali"));
        defs.add(catalogueNpc("Geena", "Geena"));
        defs.add(catalogueNpc("Guardman", "A guardsman"));
        defs.add(catalogueNpc("Halam", "Halam"));
        defs.add(catalogueNpc("Isulgur", "Isulgur"));
        defs.add(catalogueNpc("Jalus", "Jalus"));
        defs.add(catalogueNpc("Kalastor", "Kalastor", List.of("peek", "dodge", "archery")));
        defs.add(catalogueNpc("KirlorDhul", "Kirlor Dhul"));
        defs.add(catalogueNpc("Lothan", "Lothan", List.of("spell.poison_arrow", "spell.poison", "spell.ice_shard")));
        defs.add(catalogueNpc("Markam", "Markam"));
        defs.add(catalogueNpc("MarnecSunim", "Marnec Sunim"));
        defs.add(catalogueNpc("Mithrand", "Mithrand"));
        defs.add(catalogueNpc("Murmuntag", "Murmuntag", List.of("attack")));
        defs.add(catalogueNpc("Ortanalas", "Ortanalas", List.of("attack", "archery", "stun_blow", "powerful_blow")));
        defs.add(catalogueNpc("Pig", "A pig"));
        defs.add(catalogueNpc("Rolph", "Rolph"));
        defs.add(catalogueNpc("Shadow", "A dark figure...", List.of("peek")));
        defs.add(catalogueNpc("Sigfried", "Sigfried"));
        defs.add(catalogueNpc("TwinNevanis", "Nevanis"));
        defs.add(catalogueNpc("TwinShovanis", "Shovanis", List.of("spell.dust_devil", "spell.curse", "spell.word_of_recall")));
        defs.add(catalogueNpc("Uranos", "Uranos", List.of("spell.stone_shard", "spell.shatter")));
        NpcDefBinaryIO.write(new File(Paths.NPCS_BIN), defs);
    }

    private static NpcDef catalogueNpc(String name, String displayName, String... targets) {
        return catalogueNpc(name, displayName, List.of(targets));
    }

    private static NpcDef catalogueNpc(String name, String displayName, List<String> targets) {
        List<NpcDef.Action> actions = new ArrayList<>();
        if (!targets.isEmpty()) {
            boolean spell = targets.stream().anyMatch(target -> target.startsWith("spell."));
            actions.add(new NpcDef.Action(spell ? ActionType.OPEN_SPELL_LEARNING : ActionType.OPEN_SKILL_LEARNING, targets));
        }
        List<NpcDef.DialogTopic> topics = new ArrayList<>();
        topics.add(new NpcDef.DialogTopic(List.of("nom", "name"),
                "Je suis " + displayName + ". Que puis-je faire pour vous ?", List.of()));
        topics.add(new NpcDef.DialogTopic(List.of("travail", "work"),
                actions.isEmpty()
                        ? "Je m'occupe de mes affaires et je connais bien les environs."
                        : "Je peux vous transmettre une partie de mon savoir si vous êtes prêt à apprendre.",
                actions));
        String identity = I18n.normalizedKey(name);
        I18n.update(Map.of(
                "npc." + identity, displayName,
                "npc.welcome." + identity, "Bonjour, je suis " + displayName + ".",
                "npc.topic." + identity + ".0", "Je suis " + displayName + ". Que puis-je faire pour vous ?",
                "npc.topic." + identity + ".1", actions.isEmpty()
                        ? "Je m'occupe de mes affaires et je connais bien les environs."
                        : "Je peux vous transmettre une partie de mon savoir si vous êtes prêt à apprendre.",
                "npc.topic_keyword." + identity + ".0.0", "nom",
                "npc.topic_keyword." + identity + ".0.1", "name",
                "npc.topic_keyword." + identity + ".1.0", "travail",
                "npc.topic_keyword." + identity + ".1.1", "work"));
        return new NpcDef(name, displayName, appearanceFor(name),
                null, 0, List.of(), "Bonjour, je suis " + displayName + ".", topics);
    }

    /** Appearance names translated from MonsterStatSetup.cpp. */
    private static List<NpcDef.Part> appearanceFor(String name) {
        return switch (name) {
            case "Araknor" -> parts("PupNecromanRobe", "PupLeatherPants", "PupBlackLeatherBoots", "PupHornedHelmet");
            case "Dragon", "Murmuntag", "Balork", "Pig" -> List.of();
            case "DelvarIrongrip" -> parts("PupChainMailBody", "PupChainMailLegs", "PupPlateBoots", "PupChainMailCoif", "PupNormalSword", "PupRomanShield", "PupRedCape");
            case "Guardman" -> parts("PupChainMailBody", "PupChainMailLegs", "PupPlateBoots", "PupChainMailCoif", "PupNormalSword", "PupRomanShield", "PupRedCape");
            case "Markam", "Isulgur" -> parts("PupChainMailBody", "PupLeatherPants", "PupPlateBoots", "PupChainMailCoif");
            case "KirlorDhul" -> parts("PupChainMailBody", "PupChainMailLegs", "PupPlateBoots", "PupChainMailCoif", "PupRedCape");
            case "Shadow" -> parts("PupLeatherArmor", "PupStuddedLegs", "PupLeatherBoots", "PupDagger", "PupRedCape");
            case "Ortanalas" -> parts("PupLeatherArmor", "PupStuddedLegs", "PupLeatherBoots", "PupNormalSword", "PupRomanShield", "PupRedCape");
            case "Uranos" -> parts("PupNecromanRobe", "PupLeatherPants", "PupBlackLeatherBoots", "PupRedCape");
            case "Lothan", "TwinShovanis" -> parts("PupNecromanRobe", "PupLeatherPants", "PupBlackLeatherBoots");
            case "Jalus", "Halam", "Kalastor", "MarnecSunim", "Fali", "Geena" -> parts("PupClothBody", "PupClothLegs", "PupLeatherBoots");
            default -> parts("PupClothBody", "PupNakedLegs", "PupNakedFoot");
        };
    }

    private static List<NpcDef.Part> parts(String body, String legs, String feet, String... extras) {
        List<NpcDef.Part> result = new ArrayList<>();
        result.add(new NpcDef.Part(BodyPart.BODY, body));
        result.add(new NpcDef.Part(BodyPart.LEGS, legs));
        result.add(new NpcDef.Part(BodyPart.FEET, feet));
        for (String extra : extras) {
            BodyPart slot = extra.contains("Helmet") || extra.contains("Coif") ? BodyPart.HEAD
                    : extra.contains("Sword") || extra.contains("Dagger") ? BodyPart.WEAPON
                    : extra.contains("Shield") ? BodyPart.SHIELD : BodyPart.CAPE;
            result.add(new NpcDef.Part(slot, extra));
        }
        return result;
    }
}
