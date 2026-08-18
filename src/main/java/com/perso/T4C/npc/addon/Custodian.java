package com.perso.T4C.npc.addon;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Custodian extends ScriptedNpc {

  public static final String ID = "Custodian";

  public static final String DISPLAY_NAME = "${npc.custodian}";

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
          "${npc.welcome.custodian}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.custodian.0.0}"),
                  "${npc.topic.custodian.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.custodian.1.0}"),
                  "${npc.topic.custodian.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.custodian.2.0}", "${npc.topic_keyword.custodian.2.1}"),
                  "${npc.topic.custodian.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.custodian.3.0}",
                      "${npc.topic_keyword.custodian.3.1}",
                      "${npc.topic_keyword.custodian.3.2}",
                      "${npc.topic_keyword.custodian.3.3}"),
                  "${npc.topic.custodian.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.custodian.4.0}"),
                  "${npc.topic.custodian.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.custodian.5.0}"),
                  "${npc.topic.custodian.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.custodian.6.0}"),
                  "${npc.topic.custodian.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.custodian.7.0}",
                      "${npc.topic_keyword.custodian.7.1}",
                      "${npc.topic_keyword.custodian.7.2}",
                      "${npc.topic_keyword.custodian.7.3}",
                      "${npc.topic_keyword.custodian.7.4}"),
                  "${npc.topic.custodian.7}",
                  List.of())),
          "HighPriestGuntharNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public Custodian(NpcContext context) throws GameException {

    super(SPEC, context);
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

        @Override
        public void onInitialise(NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          int progress = c.flag("ADDON_STORYLINE_PROGRESS");

          if (progress < 34) {

            c.sayKey("npc.custodian.blocked");

            return;
          }

          if (progress == 34) {

            c.sayKey("npc.custodian.ritePrompt");

            c.askYesNo("RITE");

            return;
          }

          if (c.hasFlag("ADDON_CUSTODIAN_ACCESS", 1)) {

            c.sayKey("npc.custodian.accessPrompt");

            c.askYesNo("GO_UP");

            return;
          }

          c.sayKey("npc.welcome.custodian");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String keyword) {

          String k = keyword == null ? "" : keyword.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("ACCESS") && c.hasFlag("ADDON_CUSTODIAN_ACCESS", 1)) {

            c.sayKey("npc.custodian.accessPrompt");

            c.askYesNo("GO_UP");

            return true;
          }

          if (k.equals("NAME")) {

            c.sayKey("npc.custodian.name");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.custodian.work");

            return true;
          }

          if (k.equals("BYE")
              || k.equals("LEAVE")
              || k.equals("QUIT")
              || k.equals("FAREWELL")
              || k.equals("EXIT")) {

            c.sayKey("npc.custodian.bye");

            c.endConversation();

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String prompt, boolean yes) {

          if ("GO_UP".equals(prompt) && yes) {

            c.sayKey("npc.custodian.goUp");

            c.teleport(1081, 1465, 0);

            c.endConversation();

          } else if ("GO_UP".equals(prompt)) c.sayKey("npc.custodian.later");
          else if ("RITE".equals(prompt) && yes) c.sayKey("npc.custodian.firstPhrase");
          else if ("RITE".equals(prompt)) c.sayKey("npc.custodian.refuse");

          return true;
        }
      };
}
