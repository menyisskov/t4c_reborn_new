package com.perso.T4C.npc.arakas;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class WindhowlSamaritan extends ScriptedNpc {

  public static final String ID = "WindhowlSamaritan";

  public static final String DISPLAY_NAME = "${npc.windhowlsamaritan}";

  public static final String SPRITE_BASE = "PaysanModel1";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.windhowlsamaritan}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowlsamaritan.0.0}",
                      "${npc.topic_keyword.windhowlsamaritan.0.1}"),
                  "${npc.topic.windhowlsamaritan.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowlsamaritan.1.0}",
                      "${npc.topic_keyword.windhowlsamaritan.1.1}",
                      "${npc.topic_keyword.windhowlsamaritan.1.2}"),
                  "${npc.topic.windhowlsamaritan.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowlsamaritan.2.0}",
                      "${npc.topic_keyword.windhowlsamaritan.2.1}",
                      "${npc.topic_keyword.windhowlsamaritan.2.2}"),
                  "${npc.topic.windhowlsamaritan.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowlsamaritan.3.0}"),
                  "${npc.topic.windhowlsamaritan.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowlsamaritan.4.0}"),
                  "${npc.topic.windhowlsamaritan.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowlsamaritan.5.0}",
                      "${npc.topic_keyword.windhowlsamaritan.5.1}"),
                  "${npc.topic.windhowlsamaritan.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowlsamaritan.6.0}"),
                  "${npc.topic.windhowlsamaritan.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowlsamaritan.7.0}"),
                  "${npc.topic.windhowlsamaritan.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowlsamaritan.8.0}"),
                  "${npc.topic.windhowlsamaritan.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowlsamaritan.9.0}"),
                  "${npc.topic.windhowlsamaritan.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowlsamaritan.10.0}"),
                  "${npc.topic.windhowlsamaritan.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowlsamaritan.11.0}"),
                  "${npc.topic.windhowlsamaritan.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowlsamaritan.12.0}",
                      "${npc.topic_keyword.windhowlsamaritan.12.1}"),
                  "${npc.topic.windhowlsamaritan.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowlsamaritan.13.0}",
                      "${npc.topic_keyword.windhowlsamaritan.13.1}"),
                  "${npc.topic.windhowlsamaritan.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowlsamaritan.14.0}"),
                  "${npc.topic.windhowlsamaritan.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowlsamaritan.15.0}"),
                  "${npc.topic.windhowlsamaritan.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowlsamaritan.16.0}",
                      "${npc.topic_keyword.windhowlsamaritan.16.1}",
                      "${npc.topic_keyword.windhowlsamaritan.16.2}",
                      "${npc.topic_keyword.windhowlsamaritan.16.3}"),
                  "${npc.topic.windhowlsamaritan.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowlsamaritan.17.0}",
                      "${npc.topic_keyword.windhowlsamaritan.17.1}",
                      "${npc.topic_keyword.windhowlsamaritan.17.2}",
                      "${npc.topic_keyword.windhowlsamaritan.17.3}",
                      "${npc.topic_keyword.windhowlsamaritan.17.4}"),
                  "${npc.topic.windhowlsamaritan.17}",
                  List.of())),
          "Samaritan",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        int offensive = c.flag("__OFFENSIVE_TALK");

        if (offensive < 10) c.sayKey("npc.samaritan.welcome");
        else {

          c.sayKey("npc.samaritan.offensive");

          c.flag("__OFFENSIVE_TALK", offensive - 1);
        }
      }
    };
  }

  public WindhowlSamaritan(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
