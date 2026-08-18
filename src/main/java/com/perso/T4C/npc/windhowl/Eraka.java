package com.perso.T4C.npc.windhowl;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Eraka extends ScriptedNpc {

  public static final String ID = "Eraka";

  public static final String DISPLAY_NAME = "${npc.eraka}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.eraka}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eraka.0.0}", "${npc.topic_keyword.eraka.0.1}"),
                  "${npc.topic.eraka.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.eraka.1.0}",
                      "${npc.topic_keyword.eraka.1.1}",
                      "${npc.topic_keyword.eraka.1.2}"),
                  "${npc.topic.eraka.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eraka.2.0}"), "${npc.topic.eraka.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.eraka.3.0}",
                      "${npc.topic_keyword.eraka.3.1}",
                      "${npc.topic_keyword.eraka.3.2}"),
                  "${npc.topic.eraka.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eraka.4.0}"), "${npc.topic.eraka.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eraka.5.0}"), "${npc.topic.eraka.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eraka.6.0}"), "${npc.topic.eraka.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eraka.7.0}", "${npc.topic_keyword.eraka.7.1}"),
                  "${npc.topic.eraka.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eraka.8.0}"), "${npc.topic.eraka.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eraka.9.0}"), "${npc.topic.eraka.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eraka.10.0}"), "${npc.topic.eraka.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eraka.11.0}", "${npc.topic_keyword.eraka.11.1}"),
                  "${npc.topic.eraka.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eraka.12.0}"), "${npc.topic.eraka.12}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eraka.13.0}"), "${npc.topic.eraka.13}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eraka.14.0}", "${npc.topic_keyword.eraka.14.1}"),
                  "${npc.topic.eraka.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.eraka.15.0}",
                      "${npc.topic_keyword.eraka.15.1}",
                      "${npc.topic_keyword.eraka.15.2}",
                      "${npc.topic_keyword.eraka.15.3}",
                      "${npc.topic_keyword.eraka.15.4}"),
                  "${npc.topic.eraka.15}",
                  List.of())),
          "Guard_Two",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 310, 65535, "1d29+21"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__QUEST_USER_IS_A_TRAITOR") >= 3) {

          c.sayKey("npc.eraka.traitor");

          c.npc().provoke();

        } else c.sayKey("npc.eraka.wounded");
      }
    };
  }

  public Eraka(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
