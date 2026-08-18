package com.perso.T4C.npc.remort;

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

public final class ColosseumClerkXP extends ScriptedNpc {

  public static final String ID = "ColosseumClerkXP";

  public static final String DISPLAY_NAME = "${npc.colosseumclerkxp}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots")),
          0,
          List.of(),
          "${npc.welcome.colosseumclerkxp.initial}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumclerkxp.0.0}",
                      "${npc.topic_keyword.colosseumclerkxp.0.1}"),
                  "${npc.topic.colosseumclerkxp.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.colosseumclerkxp.1.0}"),
                  "${npc.topic.colosseumclerkxp.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumclerkxp.2.0}",
                      "${npc.topic_keyword.colosseumclerkxp.2.1}",
                      "${npc.topic_keyword.colosseumclerkxp.2.2}"),
                  "${npc.topic.colosseumclerkxp.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumclerkxp.3.0}",
                      "${npc.topic_keyword.colosseumclerkxp.3.1}"),
                  "${npc.topic.colosseumclerkxp.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumclerkxp.4.0}",
                      "${npc.topic_keyword.colosseumclerkxp.4.1}"),
                  "${npc.topic.colosseumclerkxp.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.colosseumclerkxp.5.0}"),
                  "${npc.topic.colosseumclerkxp.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.colosseumclerkxp.6.0}"),
                  "${npc.topic.colosseumclerkxp.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.colosseumclerkxp.7.0}"),
                  "${npc.topic.colosseumclerkxp.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumclerkxp.8.0}",
                      "${npc.topic_keyword.colosseumclerkxp.8.1}",
                      "${npc.topic_keyword.colosseumclerkxp.8.2}",
                      "${npc.topic_keyword.colosseumclerkxp.8.3}",
                      "${npc.topic_keyword.colosseumclerkxp.8.4}"),
                  "${npc.topic.colosseumclerkxp.8}",
                  List.of())),
          "ColosseumClerkNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public ColosseumClerkXP(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  @Override
  protected boolean canTalkThroughWalls() {

    return true;
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        private boolean allowed(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          if (c.flag("__FLAG_NUMBER_OF_REMORTS") < 1) {

            c.sayKey("npc.colosseumclerkxp.no_seraph");

            c.teleport(343, 492, 0);

            c.endConversation();

            return false;
          }

          if (c.globalFlag("ACK_COLOSSEUM") == 0) {

            c.sayKey("npc.welcome.colosseumclerkxp");

            c.teleport(343, 492, 0);

            c.endConversation();

            return false;
          }

          return true;
        }

        @Override
        public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          if (allowed(c)) ColosseumClerk.nativeBehavior().onConversationStart(c);
        }

        @Override
        public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

          return !allowed(c) || ColosseumClerk.nativeBehavior().onKeyword(c, text);
        }

        @Override
        public boolean onYesNo(
            com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean answer) {

          return !allowed(c) || ColosseumClerk.nativeBehavior().onYesNo(c, state, answer);
        }

        @Override
        public void onInitialise(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          ColosseumClerk.nativeBehavior().onInitialise(c);
        }
      };
}
