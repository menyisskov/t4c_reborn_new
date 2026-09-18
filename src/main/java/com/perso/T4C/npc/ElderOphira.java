package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// Quest-giver at the Avalon Sanctuary Inn & Hall door (1316,1493), worldZ 0. Establishes herself
// and the island's plight (the loyal Avalon Wilds vs. the corrupted Fading Veil) so a later
// quest-creator pass can wire GIVE_QUEST actions against giverNpc: "ElderOphira" — no quest logic
// yet.
@Spawn(type = "ElderOphira", x = 1316, y = 1493, z = 0, stationary = false, aggressive = false)
public final class ElderOphira extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Female Dying 1.wav";
  public static final String SOUND_HIT = "Female Hit 1.wav";

  public static final String ID = "ElderOphira";

  public static final String DISPLAY_NAME = "${npc.elderophira}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe")),
          0,
          List.of(),
          "${npc.welcome.elderophira}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elderophira.0.0}",
                      "${npc.topic_keyword.elderophira.0.1}"),
                  "${npc.topic.elderophira.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elderophira.1.0}",
                      "${npc.topic_keyword.elderophira.1.1}"),
                  "${npc.topic.elderophira.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elderophira.2.0}"),
                  "${npc.topic.elderophira.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elderophira.3.0}"),
                  "${npc.topic.elderophira.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elderophira.4.0}"),
                  "${npc.topic.elderophira.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elderophira.5.0}",
                      "${npc.topic_keyword.elderophira.5.1}"),
                  "${npc.topic.elderophira.5}",
                  List.of())),
          "ElderOphiraNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public ElderOphira(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
