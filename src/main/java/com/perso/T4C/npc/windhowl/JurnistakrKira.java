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
import java.util.List;

public final class JurnistakrKira extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "JurnistakrKira";

  public static final String DISPLAY_NAME = "${npc.jurnistakrkira}";

  public static final String SPRITE_BASE = "Priest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.jurnistakrkira}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jurnistakrkira.0.0}",
                      "${npc.topic_keyword.jurnistakrkira.0.1}"),
                  "${npc.topic.jurnistakrkira.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jurnistakrkira.1.0}",
                      "${npc.topic_keyword.jurnistakrkira.1.1}",
                      "${npc.topic_keyword.jurnistakrkira.1.2}",
                      "${npc.topic_keyword.jurnistakrkira.1.3}"),
                  "${npc.topic.jurnistakrkira.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jurnistakrkira.2.0}",
                      "${npc.topic_keyword.jurnistakrkira.2.1}",
                      "${npc.topic_keyword.jurnistakrkira.2.2}"),
                  "${npc.topic.jurnistakrkira.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jurnistakrkira.3.0}"),
                  "${npc.topic.jurnistakrkira.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jurnistakrkira.4.0}"),
                  "${npc.topic.jurnistakrkira.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jurnistakrkira.5.0}"),
                  "${npc.topic.jurnistakrkira.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jurnistakrkira.6.0}"),
                  "${npc.topic.jurnistakrkira.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jurnistakrkira.7.0}"),
                  "${npc.topic.jurnistakrkira.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jurnistakrkira.8.0}"),
                  "${npc.topic.jurnistakrkira.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jurnistakrkira.9.0}",
                      "${npc.topic_keyword.jurnistakrkira.9.1}",
                      "${npc.topic_keyword.jurnistakrkira.9.2}",
                      "${npc.topic_keyword.jurnistakrkira.9.3}",
                      "${npc.topic_keyword.jurnistakrkira.9.4}"),
                  "${npc.topic.jurnistakrkira.9}",
                  List.of())),
          "Priest",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public JurnistakrKira(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
