package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.NpcDef;

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
        NpcOriginalAppearance.Appearance iraltokAppearance = NpcOriginalAppearance.forName("Iraltok");
        NpcDef iraltok = new NpcDef(
                "Iraltok",
                "Iraltok",
                iraltokAppearance.parts(),
                iraltokAppearance.spriteBase(),
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
        // Companions now live in their own catalogue (CompanionCatalogueSeed),
        // so they are no longer NPC definitions.
        String moonrockWelcome = "Je suis Moonrock, prêtresse de ce temple. Si vos blessures vous accablent, je peux invoquer la grâce des dieux pour vous soigner.";
        I18n.update(Map.of(
                "npc.moonrock", "Moonrock",
                "npc.welcome.moonrock", moonrockWelcome,
                "npc.topic_keyword.moonrock.0.0", "soigner",
                "npc.topic_keyword.moonrock.0.1", "heal"
        ));
        NpcOriginalAppearance.Appearance moonrockAppearance = NpcOriginalAppearance.forName("Moonrock");
        NpcDef moonrock = new NpcDef(
                "Moonrock", "Moonrock",
                moonrockAppearance.parts(), moonrockAppearance.spriteBase(), 0,
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
        NpcOriginalAppearance.Appearance khiliamAppearance = NpcOriginalAppearance.forName("Khiliam");
        NpcDef khiliam = new NpcDef(
                "Khiliam", "Khiliam",
                khiliamAppearance.parts(), khiliamAppearance.spriteBase(), 0,
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
        NpcOriginalAppearance.Appearance jagarKarAppearance = NpcOriginalAppearance.forName("JagarKar");
        NpcDef jagarKar = new NpcDef(
                "JagarKar", "JagarKar",
                jagarKarAppearance.parts(), jagarKarAppearance.spriteBase(), 0, List.of(), jagarKarWelcome,
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
        defs.add(catalogueNpc("Darkfang", "DarkFang"));
        defs.add(catalogueNpc("Edgar", "Edgar Gimplestratten"));
        defs.add(catalogueNpc("ElmertMerkiss", "Elmert Merkiss"));
        defs.add(fali());
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
        defs.add(murmuntag());
        defs.add(ortanalas());
        defs.add(catalogueNpc("Pig", "A pig"));
        defs.add(rolph());
        defs.add(catalogueNpc("Shadow", "A dark figure...", List.of("peek")));
        defs.add(catalogueNpc("Sigfried", "Sigfried"));
        defs.add(catalogueNpc("TwinNevanis", "Nevanis"));
        defs.add(catalogueNpc("TwinShovanis", "Shovanis", List.of("spell.dust_devil", "spell.curse", "spell.word_of_recall")));
        defs.add(catalogueNpc("Uranos", "Uranos", List.of("spell.stone_shard", "spell.shatter")));
        defs.add(lighthavenSamaritan());
        NpcDefBinaryIO.write(new File(Paths.NPCS_BIN), defs);
    }

    static NpcDef lighthavenSamaritan() {
        int[] keywordCounts = {2, 3, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 5};
        List<NpcDef.DialogTopic> topics = new ArrayList<>(keywordCounts.length);
        for (int topicIndex = 0; topicIndex < keywordCounts.length; topicIndex++) {
            List<String> keywords = new ArrayList<>(keywordCounts[topicIndex]);
            for (int keywordIndex = 0; keywordIndex < keywordCounts[topicIndex]; keywordIndex++) {
                keywords.add(I18n.placeholder("npc.topic_keyword.lighthavensamaritan."
                        + topicIndex + "." + keywordIndex));
            }
            List<NpcDef.Action> actions = topicIndex == 1
                    ? List.of(new NpcDef.Action(ActionType.GIVE_QUEST,
                            List.of(QuestCatalogueSeed.QUEST_ID)))
                    : List.of();
            topics.add(new NpcDef.DialogTopic(
                    keywords,
                    topicIndex == 1 ? null
                            : I18n.placeholder("npc.topic.lighthavensamaritan." + topicIndex),
                    actions));
        }
        return new NpcDef(
                LighthavenSamaritanQuestMigration.NPC_NAME,
                I18n.placeholder("npc.lighthavensamaritan"),
                LighthavenSamaritanAppearanceMigration.originalParts(),
                null,
                0,
                List.of(),
                I18n.placeholder("npc.welcome.lighthavensamaritan"),
                topics
        );
    }

    /**
     * Murmuntag keeps its catalogue dialogue and gains an "aide" topic summoning
     * the mage companion that fights alongside the player.
     */
    static NpcDef murmuntag() {
        NpcDef base = catalogueNpc("Murmuntag", "Murmuntag", List.of("attack"));
        String identity = I18n.normalizedKey(base.getName());
        String helpResponse = "Les monstres pullulent dans ces terres. Un mage de ma connaissance "
                + "va t'accompagner et combattre à tes côtés.";
        int topicIndex = base.getTopics().size();
        I18n.update(Map.of(
                "npc.topic." + identity + "." + topicIndex, helpResponse,
                "npc.topic_keyword." + identity + "." + topicIndex + ".0", "aide",
                "npc.topic_keyword." + identity + "." + topicIndex + ".1", "aider"));

        List<NpcDef.DialogTopic> topics = new ArrayList<>(base.getTopics());
        topics.add(new NpcDef.DialogTopic(
                List.of("aide", "aider"),
                helpResponse,
                List.of(new NpcDef.Action(ActionType.SUMMON_COMPANION, "mage_apprentice"))));
        return new NpcDef(base.getName(), base.getDisplayName(), base.getParts(),
                base.getSpriteBase(), base.getPatrolRadiusTiles(), base.getFleeShouts(),
                base.getWelcomeText(), topics);
    }

    static NpcDef ortanalas() {
        NpcDef base = catalogueNpc("Ortanalas", "Ortanalas",
                List.of("attack", "archery", "stun_blow", "powerful_blow"));
        return OrtanalasGoblinQuestMigration.update(base);
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
        NpcOriginalAppearance.Appearance appearance = NpcOriginalAppearance.forName(name);
        return new NpcDef(name, displayName, appearance.parts(),
                appearance.spriteBase(), 0, List.of(), "Bonjour, je suis " + displayName + ".", topics);
    }

    private static NpcDef fali() {
        String welcome = "S'il y a une chose de bien avec tous ces gardes, c'est que personne n'a essayé de me voler quoi que ce soit depuis leur arrivée. Vous pouvez regarder, mais restons civilisés.";
        String identity = "fali";
        I18n.update(Map.ofEntries(
                Map.entry("npc." + identity, "Fali"), Map.entry("npc.welcome." + identity, welcome),
                Map.entry("npc.topic." + identity + ".0", "Je m'appelle Fali."),
                Map.entry("npc.topic." + identity + ".1", "Je vends toutes sortes d'objets. N'hésitez pas à regarder."),
                Map.entry("npc.topic." + identity + ".2", "Voici ce que j'ai en boutique."),
                Map.entry("npc.topic_keyword." + identity + ".0.0", "nom"), Map.entry("npc.topic_keyword." + identity + ".0.1", "qui êtes-vous"),
                Map.entry("npc.topic_keyword." + identity + ".1.0", "travail"), Map.entry("npc.topic_keyword." + identity + ".1.1", "métier"), Map.entry("npc.topic_keyword." + identity + ".1.2", "occupation"),
                Map.entry("npc.topic_keyword." + identity + ".2.0", "regarder"), Map.entry("npc.topic_keyword." + identity + ".2.1", "acheter")));
        NpcOriginalAppearance.Appearance appearance = NpcOriginalAppearance.forName("Fali");
        return new NpcDef("Fali", "Fali", appearance.parts(), appearance.spriteBase(), 0, List.of(), welcome,
                List.of(
                        topic(List.of("nom", "qui êtes-vous"), "Je m'appelle Fali."),
                        topic(List.of("travail", "métier", "occupation"), "Je vends toutes sortes d'objets. N'hésitez pas à regarder."),
                        shopTopic(List.of("regarder", "acheter"), "Voici ce que j'ai en boutique.",
                                "item.apple", "item.torch", "item.light_healing_potion", "item.potion_of_mana", "item.potion_of_healing", "item.iron_ring")));
    }

    private static NpcDef rolph() {
        String welcome = "Bienvenue ! Les affaires marchent bien depuis l'arrivée des gardes. Tout le monde achète des armures ces temps-ci.";
        String identity = "rolph";
        I18n.update(Map.ofEntries(
                Map.entry("npc." + identity, "Rolph"), Map.entry("npc.welcome." + identity, welcome),
                Map.entry("npc.topic." + identity + ".0", "Je m'appelle Rolph, le plus grand armurier du royaume !"),
                Map.entry("npc.topic." + identity + ".1", "Je possède la meilleure armurerie de tout Goldmoon."),
                Map.entry("npc.topic." + identity + ".2", "Je forge les armures les plus résistantes de Goldmoon."),
                Map.entry("npc.topic_keyword." + identity + ".0.0", "nom"), Map.entry("npc.topic_keyword." + identity + ".0.1", "qui êtes-vous"),
                Map.entry("npc.topic_keyword." + identity + ".1.0", "travail"), Map.entry("npc.topic_keyword." + identity + ".1.1", "métier"), Map.entry("npc.topic_keyword." + identity + ".1.2", "occupation"),
                Map.entry("npc.topic_keyword." + identity + ".2.0", "achète"),
                Map.entry("npc.topic_keyword." + identity + ".2.1", "armure")));
        NpcOriginalAppearance.Appearance appearance = NpcOriginalAppearance.forName("Rolph");
        return new NpcDef("Rolph", "Rolph", appearance.parts(), appearance.spriteBase(), 0, List.of(), welcome,
                List.of(
                        topic(List.of("nom", "qui êtes-vous"), "Je m'appelle Rolph, le plus grand armurier du royaume !"),
                        topic(List.of("travail", "métier", "occupation"), "Je possède la meilleure armurerie de tout Goldmoon."),
                        shopTopic(List.of("achète", "armure"), "Je forge les armures les plus résistantes de Goldmoon.",
                                "item.cloth_pants", "item.cloth_vest", "item.leather_belt", "item.leather_gloves", "item.leather_helmet", "item.leather_pants", "item.leather_boots", "item.leather_armor", "item.red_cape", "item.studded_leather_belt", "item.studded_leather_gloves", "item.studded_leather_helmet", "item.studded_leather_pants", "item.studded_leather_boots", "item.studded_leather_armor", "item.wooden_shield")));
    }

    private static NpcDef.DialogTopic topic(List<String> keywords, String response) {
        return new NpcDef.DialogTopic(keywords, response, List.of());
    }

    private static NpcDef.DialogTopic shopTopic(List<String> keywords, String response, String... items) {
        return new NpcDef.DialogTopic(keywords, response,
                List.of(new NpcDef.Action(ActionType.OPEN_SHOP, List.of(items))));
    }

}
