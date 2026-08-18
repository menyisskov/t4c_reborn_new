package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class Brother7 extends ScriptedNpc {

  public static final String ID = "Brother7";

  public static final String DISPLAY_NAME = "${npc.brother7}";

  public static final String SPRITE_BASE = "Zombie";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.brother7}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother7.0.0}"),
                  "${npc.topic.brother7.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother7.1.0}",
                      "${npc.topic_keyword.brother7.1.1}",
                      "${npc.topic_keyword.brother7.1.2}",
                      "${npc.topic_keyword.brother7.1.3}",
                      "${npc.topic_keyword.brother7.1.4}"),
                  "${npc.topic.brother7.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother7.2.0}",
                      "${npc.topic_keyword.brother7.2.1}",
                      "${npc.topic_keyword.brother7.2.2}",
                      "${npc.topic_keyword.brother7.2.3}",
                      "${npc.topic_keyword.brother7.2.4}"),
                  "${npc.topic.brother7.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother7.3.0}", "${npc.topic_keyword.brother7.3.1}"),
                  "${npc.topic.brother7.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother7.4.0}", "${npc.topic_keyword.brother7.4.1}"),
                  "${npc.topic.brother7.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother7.5.0}", "${npc.topic_keyword.brother7.5.1}"),
                  "${npc.topic.brother7.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother7.6.0}",
                      "${npc.topic_keyword.brother7.6.1}",
                      "${npc.topic_keyword.brother7.6.2}",
                      "${npc.topic_keyword.brother7.6.3}"),
                  "${npc.topic.brother7.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother7.7.0}",
                      "${npc.topic_keyword.brother7.7.1}",
                      "${npc.topic_keyword.brother7.7.2}",
                      "${npc.topic_keyword.brother7.7.3}",
                      "${npc.topic_keyword.brother7.7.4}"),
                  "${npc.topic.brother7.7}",
                  List.of())),
          "DeadBrotherNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new DeadBrotherBehavior(7);
  }

  public Brother7(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
