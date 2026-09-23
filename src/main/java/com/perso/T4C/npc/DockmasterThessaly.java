package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// T4C-0024: the last real dock on this stretch of coast (1620,2628), worldZ 0, on walkable ground
// right beside the existing CaptainIronleg (1619,2623)/CorsairMagere (1615,2635) pirate NPCs -
// gives "passage_to_kraanhold" (see quest/definition/PassageToKraanhold.java), the access quest
// that unlocks fast travel to Windhowl Marches, the entrance province of Kraanhold (T4C-0024's
// new continent). Without this quest a fresh character has no route there at all - Kraanhold was
// painted into open ocean this pass, with no existing road or NPC anywhere near it, the same
// dead-end Avalon had before T4C-0019's HarbormasterRangor.
@Spawn(type = "DockmasterThessaly", x = 1620, y = 2628, z = 0, stationary = false, aggressive = false)
public final class DockmasterThessaly extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Female Dying 1.wav";
  public static final String SOUND_HIT = "Female Hit 1.wav";

  public static final String ID = "DockmasterThessaly";

  public static final String DISPLAY_NAME = "${npc.dockmasterthessaly}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.dockmasterthessaly}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dockmasterthessaly.0.0}",
                      "${npc.topic_keyword.dockmasterthessaly.0.1}"),
                  "${npc.topic.dockmasterthessaly.0}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.GIVE_QUEST, List.of("passage_to_kraanhold")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dockmasterthessaly.1.0}",
                      "${npc.topic_keyword.dockmasterthessaly.1.1}"),
                  "${npc.topic.dockmasterthessaly.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dockmasterthessaly.2.0}"),
                  "${npc.topic.dockmasterthessaly.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dockmasterthessaly.3.0}"),
                  "${npc.topic.dockmasterthessaly.3}",
                  List.of())),
          "DockmasterThessalyNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public DockmasterThessaly(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
