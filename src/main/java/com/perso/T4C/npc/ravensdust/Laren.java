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

public final class Laren extends ScriptedNpc {

  public static final String ID = "Laren";

  public static final String DISPLAY_NAME = "${npc.laren}";

  public static final String SPRITE_BASE = "PaysanModel1";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.laren}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.laren.0.0}"), "${npc.topic.laren.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.laren.1.0}"), "${npc.topic.laren.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.laren.2.0}"), "${npc.topic.laren.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.laren.3.0}"), "${npc.topic.laren.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.laren.4.0}", "${npc.topic_keyword.laren.4.1}"),
                  "${npc.topic.laren.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.laren.5.0}"), "${npc.topic.laren.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.laren.6.0}"), "${npc.topic.laren.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.laren.7.0}",
                      "${npc.topic_keyword.laren.7.1}",
                      "${npc.topic_keyword.laren.7.2}"),
                  "${npc.topic.laren.7}",
                  List.of())),
          "Peasant",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  public Laren(NpcContext context) throws GameException {

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
