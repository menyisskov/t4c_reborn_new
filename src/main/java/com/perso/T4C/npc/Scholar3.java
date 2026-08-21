package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Scholar3", x = 1175, y = 1435, z = 0, stationary = false, aggressive = false)
public final class Scholar3 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Scholar3";

  public static final String DISPLAY_NAME = "${npc.scholar3}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots")),
          0,
          List.of(),
          "${npc.welcome.scholar3}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.scholar3.0.0}"),
                  "${npc.topic.scholar3.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.scholar3.1.0}",
                      "${npc.topic_keyword.scholar3.1.1}",
                      "${npc.topic_keyword.scholar3.1.2}"),
                  "${npc.topic.scholar3.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.scholar3.2.0}",
                      "${npc.topic_keyword.scholar3.2.1}",
                      "${npc.topic_keyword.scholar3.2.2}",
                      "${npc.topic_keyword.scholar3.2.3}",
                      "${npc.topic_keyword.scholar3.2.4}"),
                  "${npc.topic.scholar3.2}",
                  List.of())),
          "HighPriestGuntharNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public Scholar3(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
