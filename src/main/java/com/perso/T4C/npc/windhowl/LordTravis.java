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

public final class LordTravis extends ScriptedNpc {

  public static final String ID = "LordTravis";

  public static final String DISPLAY_NAME = "${npc.lordtravis}";

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
          "${npc.welcome.lordtravis}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordtravis.0.0}", "${npc.topic_keyword.lordtravis.0.1}"),
                  "${npc.topic.lordtravis.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordtravis.1.0}",
                      "${npc.topic_keyword.lordtravis.1.1}",
                      "${npc.topic_keyword.lordtravis.1.2}"),
                  "${npc.topic.lordtravis.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordtravis.2.0}"),
                  "${npc.topic.lordtravis.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordtravis.3.0}",
                      "${npc.topic_keyword.lordtravis.3.1}",
                      "${npc.topic_keyword.lordtravis.3.2}"),
                  "${npc.topic.lordtravis.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordtravis.4.0}"),
                  "${npc.topic.lordtravis.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordtravis.5.0}",
                      "${npc.topic_keyword.lordtravis.5.1}",
                      "${npc.topic_keyword.lordtravis.5.2}",
                      "${npc.topic_keyword.lordtravis.5.3}",
                      "${npc.topic_keyword.lordtravis.5.4}"),
                  "${npc.topic.lordtravis.5}",
                  List.of())),
          "Nobleman",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__QUEST_USER_IS_A_TRAITOR") >= 3) {

          c.sayKey("npc.eraka.traitor");

          c.npc().provoke();

        } else c.sayKey("npc.travis.welcome");
      }
    };
  }

  public LordTravis(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
