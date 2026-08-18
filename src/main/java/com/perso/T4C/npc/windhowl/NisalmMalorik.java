package com.perso.T4C.npc.windhowl;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "NisalmMalorik", x = 1621, y = 1175, z = 0, stationary = false, aggressive = false)
public final class NisalmMalorik extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "NisalmMalorik";

  public static final String DISPLAY_NAME = "${npc.nisalmmalorik}";

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
          "${npc.welcome.nisalmmalorik}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nisalmmalorik.0.0}",
                      "${npc.topic_keyword.nisalmmalorik.0.1}"),
                  "${npc.topic.nisalmmalorik.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nisalmmalorik.1.0}",
                      "${npc.topic_keyword.nisalmmalorik.1.1}",
                      "${npc.topic_keyword.nisalmmalorik.1.2}"),
                  "${npc.topic.nisalmmalorik.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nisalmmalorik.2.0}"),
                  "${npc.topic.nisalmmalorik.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nisalmmalorik.3.0}",
                      "${npc.topic_keyword.nisalmmalorik.3.1}",
                      "${npc.topic_keyword.nisalmmalorik.3.2}"),
                  "${npc.topic.nisalmmalorik.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nisalmmalorik.4.0}"),
                  "${npc.topic.nisalmmalorik.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nisalmmalorik.5.0}",
                      "${npc.topic_keyword.nisalmmalorik.5.1}",
                      "${npc.topic_keyword.nisalmmalorik.5.2}",
                      "${npc.topic_keyword.nisalmmalorik.5.3}",
                      "${npc.topic_keyword.nisalmmalorik.5.4}"),
                  "${npc.topic.nisalmmalorik.5}",
                  List.of())),
          "Nobleman",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  public NisalmMalorik(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }
}
