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

@Spawn(type = "AssystAnter", x = 1639, y = 1191, z = 0, stationary = false, aggressive = false)
public final class AssystAnter extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "AssystAnter";

  public static final String DISPLAY_NAME = "${npc.assystanter}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.assystanter}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.assystanter.0.0}",
                      "${npc.topic_keyword.assystanter.0.1}"),
                  "${npc.topic.assystanter.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.assystanter.1.0}",
                      "${npc.topic_keyword.assystanter.1.1}",
                      "${npc.topic_keyword.assystanter.1.2}"),
                  "${npc.topic.assystanter.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.assystanter.2.0}",
                      "${npc.topic_keyword.assystanter.2.1}",
                      "${npc.topic_keyword.assystanter.2.2}"),
                  "${npc.topic.assystanter.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.assystanter.3.0}"),
                  "${npc.topic.assystanter.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.assystanter.4.0}"),
                  "${npc.topic.assystanter.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.assystanter.5.0}"),
                  "${npc.topic.assystanter.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.assystanter.6.0}"),
                  "${npc.topic.assystanter.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.assystanter.7.0}"),
                  "${npc.topic.assystanter.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.assystanter.8.0}",
                      "${npc.topic_keyword.assystanter.8.1}",
                      "${npc.topic_keyword.assystanter.8.2}",
                      "${npc.topic_keyword.assystanter.8.3}",
                      "${npc.topic_keyword.assystanter.8.4}"),
                  "${npc.topic.assystanter.8}",
                  List.of())),
          "Nobleman",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public AssystAnter(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
