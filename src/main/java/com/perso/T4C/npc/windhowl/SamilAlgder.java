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
import java.util.List;

public final class SamilAlgder extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "SamilAlgder";

  public static final String DISPLAY_NAME = "${npc.samilalgder}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupGoldenMorningStar")),
          0,
          List.of(),
          "${npc.welcome.samilalgder}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.samilalgder.0.0}",
                      "${npc.topic_keyword.samilalgder.0.1}"),
                  "${npc.topic.samilalgder.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.samilalgder.1.0}",
                      "${npc.topic_keyword.samilalgder.1.1}",
                      "${npc.topic_keyword.samilalgder.1.2}"),
                  "${npc.topic.samilalgder.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.samilalgder.2.0}",
                      "${npc.topic_keyword.samilalgder.2.1}",
                      "${npc.topic_keyword.samilalgder.2.2}"),
                  "${npc.topic.samilalgder.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samilalgder.3.0}"),
                  "${npc.topic.samilalgder.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samilalgder.4.0}"),
                  "${npc.topic.samilalgder.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samilalgder.5.0}"),
                  "${npc.topic.samilalgder.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samilalgder.6.0}"),
                  "${npc.topic.samilalgder.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samilalgder.7.0}"),
                  "${npc.topic.samilalgder.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samilalgder.8.0}"),
                  "${npc.topic.samilalgder.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samilalgder.9.0}"),
                  "${npc.topic.samilalgder.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samilalgder.10.0}"),
                  "${npc.topic.samilalgder.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.samilalgder.11.0}",
                      "${npc.topic_keyword.samilalgder.11.1}",
                      "${npc.topic_keyword.samilalgder.11.2}",
                      "${npc.topic_keyword.samilalgder.11.3}",
                      "${npc.topic_keyword.samilalgder.11.4}"),
                  "${npc.topic.samilalgder.11}",
                  List.of())),
          "SamilAlgderNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public SamilAlgder(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
