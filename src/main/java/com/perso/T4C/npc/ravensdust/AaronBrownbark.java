package com.perso.T4C.npc.ravensdust;

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

public final class AaronBrownbark extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public static final String ID = "AaronBrownbark";

  public static final String DISPLAY_NAME = "${npc.aaronbrownbark}";

  public static final String SPRITE_BASE = "TreeEnt";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.aaronbrownbark}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aaronbrownbark.0.0}",
                      "${npc.topic_keyword.aaronbrownbark.0.1}"),
                  "${npc.topic.aaronbrownbark.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aaronbrownbark.1.0}",
                      "${npc.topic_keyword.aaronbrownbark.1.1}",
                      "${npc.topic_keyword.aaronbrownbark.1.2}"),
                  "${npc.topic.aaronbrownbark.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aaronbrownbark.2.0}",
                      "${npc.topic_keyword.aaronbrownbark.2.1}",
                      "${npc.topic_keyword.aaronbrownbark.2.2}"),
                  "${npc.topic.aaronbrownbark.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aaronbrownbark.3.0}"),
                  "${npc.topic.aaronbrownbark.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aaronbrownbark.4.0}"),
                  "${npc.topic.aaronbrownbark.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aaronbrownbark.5.0}"),
                  "${npc.topic.aaronbrownbark.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aaronbrownbark.6.0}"),
                  "${npc.topic.aaronbrownbark.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aaronbrownbark.7.0}"),
                  "${npc.topic.aaronbrownbark.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aaronbrownbark.8.0}"),
                  "${npc.topic.aaronbrownbark.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aaronbrownbark.9.0}"),
                  "${npc.topic.aaronbrownbark.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aaronbrownbark.10.0}"),
                  "${npc.topic.aaronbrownbark.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aaronbrownbark.11.0}",
                      "${npc.topic_keyword.aaronbrownbark.11.1}"),
                  "${npc.topic.aaronbrownbark.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aaronbrownbark.12.0}",
                      "${npc.topic_keyword.aaronbrownbark.12.1}"),
                  "${npc.topic.aaronbrownbark.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aaronbrownbark.13.0}"),
                  "${npc.topic.aaronbrownbark.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aaronbrownbark.14.0}"),
                  "${npc.topic.aaronbrownbark.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aaronbrownbark.15.0}"),
                  "${npc.topic.aaronbrownbark.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aaronbrownbark.16.0}"),
                  "${npc.topic.aaronbrownbark.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aaronbrownbark.17.0}",
                      "${npc.topic_keyword.aaronbrownbark.17.1}"),
                  "${npc.topic.aaronbrownbark.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aaronbrownbark.18.0}",
                      "${npc.topic_keyword.aaronbrownbark.18.1}",
                      "${npc.topic_keyword.aaronbrownbark.18.2}",
                      "${npc.topic_keyword.aaronbrownbark.18.3}"),
                  "${npc.topic.aaronbrownbark.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aaronbrownbark.19.0}"),
                  "${npc.topic.aaronbrownbark.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aaronbrownbark.20.0}",
                      "${npc.topic_keyword.aaronbrownbark.20.1}",
                      "${npc.topic_keyword.aaronbrownbark.20.2}",
                      "${npc.topic_keyword.aaronbrownbark.20.3}",
                      "${npc.topic_keyword.aaronbrownbark.20.4}"),
                  "${npc.topic.aaronbrownbark.20}",
                  List.of())),
          "BrownbarkNPC",
          new NpcSpec.CombatProfile(70, 3758, 85, 77, 77, 35, 850, 290, "1d110+84"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        if (!c.hasItem("green_gemstone")) c.sayKey("npc.aaronbrownbark.no_gem");
        else if (c.flag("__FLAG_HAVE_MET_BROWNBARK") == 0) {

          c.flag("__FLAG_HAVE_MET_BROWNBARK", 1);

          c.sayKey("npc.aaronbrownbark.first");

        } else c.sayKey("npc.aaronbrownbark.returning");
      }
    };
  }

  public AaronBrownbark(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
