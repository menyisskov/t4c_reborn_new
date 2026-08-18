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

public final class Grimish extends ScriptedNpc {

  public static final String ID = "Grimish";

  public static final String DISPLAY_NAME = "${npc.grimish}";

  public static final String SPRITE_BASE = "GoblinBoss";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.grimish}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.0.0}", "${npc.topic_keyword.grimish.0.1}"),
                  "${npc.topic.grimish.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.1.0}"), "${npc.topic.grimish.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.grimish.2.0}",
                      "${npc.topic_keyword.grimish.2.1}",
                      "${npc.topic_keyword.grimish.2.2}"),
                  "${npc.topic.grimish.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.3.0}"), "${npc.topic.grimish.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.4.0}", "${npc.topic_keyword.grimish.4.1}"),
                  "${npc.topic.grimish.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.5.0}"), "${npc.topic.grimish.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.6.0}"), "${npc.topic.grimish.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.7.0}"), "${npc.topic.grimish.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.8.0}"), "${npc.topic.grimish.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.9.0}"), "${npc.topic.grimish.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.10.0}", "${npc.topic_keyword.grimish.10.1}"),
                  "${npc.topic.grimish.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.11.0}"),
                  "${npc.topic.grimish.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.12.0}"),
                  "${npc.topic.grimish.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.13.0}"),
                  "${npc.topic.grimish.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.14.0}"),
                  "${npc.topic.grimish.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.15.0}"),
                  "${npc.topic.grimish.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.16.0}"),
                  "${npc.topic.grimish.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.17.0}"),
                  "${npc.topic.grimish.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.18.0}"),
                  "${npc.topic.grimish.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.19.0}"),
                  "${npc.topic.grimish.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grimish.20.0}"),
                  "${npc.topic.grimish.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.grimish.21.0}",
                      "${npc.topic_keyword.grimish.21.1}",
                      "${npc.topic_keyword.grimish.21.2}",
                      "${npc.topic_keyword.grimish.21.3}"),
                  "${npc.topic.grimish.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.grimish.22.0}",
                      "${npc.topic_keyword.grimish.22.1}",
                      "${npc.topic_keyword.grimish.22.2}",
                      "${npc.topic_keyword.grimish.22.3}"),
                  "${npc.topic.grimish.22}",
                  List.of())),
          "GrimishNPC",
          new NpcSpec.CombatProfile(45, 996, 60, 55, 55, 22, 550, 190, "1d64+49"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (Math.random() < .4)
          c.shoutKey(
              Math.random() < .5 ? "npc.grimish.attacked.argh" : "npc.grimish.attacked.ouch");
      }

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.shoutKey("npc.grimish.death");
      }
    };
  }

  public Grimish(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
