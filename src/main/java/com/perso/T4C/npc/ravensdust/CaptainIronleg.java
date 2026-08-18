package com.perso.T4C.npc.ravensdust;

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

public final class CaptainIronleg extends ScriptedNpc {

  public static final String ID = "CaptainIronleg";

  public static final String DISPLAY_NAME = "${npc.captainironleg}";

  public static final String SPRITE_BASE = "PaysanModel1";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.captainironleg}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.captainironleg.0.0}",
                      "${npc.topic_keyword.captainironleg.0.1}"),
                  "${npc.topic.captainironleg.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.captainironleg.1.0}"),
                  "${npc.topic.captainironleg.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.captainironleg.2.0}",
                      "${npc.topic_keyword.captainironleg.2.1}"),
                  "${npc.topic.captainironleg.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.captainironleg.3.0}"),
                  "${npc.topic.captainironleg.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.captainironleg.4.0}",
                      "${npc.topic_keyword.captainironleg.4.1}"),
                  "${npc.topic.captainironleg.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.captainironleg.5.0}"),
                  "${npc.topic.captainironleg.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.captainironleg.6.0}"),
                  "${npc.topic.captainironleg.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.captainironleg.7.0}",
                      "${npc.topic_keyword.captainironleg.7.1}"),
                  "${npc.topic.captainironleg.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.captainironleg.8.0}"),
                  "${npc.topic.captainironleg.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.captainironleg.9.0}",
                      "${npc.topic_keyword.captainironleg.9.1}"),
                  "${npc.topic.captainironleg.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.captainironleg.10.0}",
                      "${npc.topic_keyword.captainironleg.10.1}",
                      "${npc.topic_keyword.captainironleg.10.2}",
                      "${npc.topic_keyword.captainironleg.10.3}"),
                  "${npc.topic.captainironleg.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.captainironleg.11.0}",
                      "${npc.topic_keyword.captainironleg.11.1}",
                      "${npc.topic_keyword.captainironleg.11.2}",
                      "${npc.topic_keyword.captainironleg.11.3}",
                      "${npc.topic_keyword.captainironleg.11.4}"),
                  "${npc.topic.captainironleg.11}",
                  List.of())),
          "Peasant",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public CaptainIronleg(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
