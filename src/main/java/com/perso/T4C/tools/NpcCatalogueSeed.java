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
        NpcDefBinaryIO.write(new File(Paths.NPCS_BIN), List.of(iraltok, moonrock, khiliam, jagarKar));
    }
}
