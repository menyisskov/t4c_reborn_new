package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Custodian", x = 1080, y = 1400, z = 0, stationary = true, aggressive = false)
public final class Custodian extends ScriptedNpc {
  private static final String DIALOG_STATE = "__CUSTODIAN_DIALOG_STATE";
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

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

          c.flag(DIALOG_STATE, 0);

          if (progress < 34) {

            c.sayKey("npc.cpp.intl.11950");

            return;
          }

          if (progress == 34) {

            c.sayKey("npc.cpp.intl.11951");

            return;
          }

          if (progress == 35) {

            c.sayKey("npc.cpp.intl.11952");

            c.askYesNo("READY");

            return;
          }

          if (c.hasFlag("ADDON_CUSTODIAN_ACCESS", 1)) {

            c.sayKey("npc.custodian.access_prompt");

            c.askYesNo("GO_UP");

            return;
          }

          c.sayKey("npc.welcome.custodian");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String keyword) {

          String k = keyword == null ? "" : keyword.toUpperCase(java.util.Locale.ROOT);

          int dialogueState = c.flag(DIALOG_STATE);

          if (dialogueState != 0) {

            return answerRitePhrase(c, dialogueState, k);
          }

          if (k.equals("ACCESS") && c.hasFlag("ADDON_CUSTODIAN_ACCESS", 1)) {

            c.sayKey("npc.topic.custodian.4");

            c.askYesNo("GO_UP");

            return true;
          }

          if (k.equals("BARBARIAN")) {

            c.sayKey(
                c.flag("ADDON_STORYLINE_PROGRESS") == 34
                    ? "npc.topic.custodian.0"
                    : "npc.cpp.intl.11957");

            return true;
          }

          if (k.equals("UNEDUCATED")) {

            c.sayKey(
                c.flag("ADDON_STORYLINE_PROGRESS") == 34
                    ? "npc.topic.custodian.1"
                    : "npc.cpp.intl.11957");

            return true;
          }

          if (k.contains("RITE") && k.contains("PASSAGE")) {

            if (c.flag("ADDON_STORYLINE_PROGRESS") == 34) {

              c.sayKey("npc.topic.custodian.2");

              c.flag("ADDON_STORYLINE_PROGRESS", 35);

            } else if (c.flag("ADDON_STORYLINE_PROGRESS") == 35)
              c.sayKey("npc.custodian.phrases");
            else c.sayKey("npc.cpp.intl.11957");

            return true;
          }

          if (k.equals("DOS VANESLAE NAVIDAL")
              || k.equals("KADRIM LOK UNGRIM DOK")
              || k.equals("SOMALINA OUNDI INESORA")
              || k.equals("THARRGRA NETHDROVAR")) {

            c.sayKey(
                c.flag("ADDON_STORYLINE_PROGRESS") == 35
                    ? "npc.topic.custodian.3"
                    : "npc.cpp.intl.11957");

            return true;
          }

          if (k.equals("NAME")) {

            c.sayKey("npc.topic.custodian.5");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.topic.custodian.6");

            return true;
          }

          if (k.equals("BYE")
              || k.equals("LEAVE")
              || k.equals("QUIT")
              || k.equals("FAREWELL")
              || k.equals("EXIT")) {

            c.sayKey("npc.topic.custodian.7");

            c.endConversation();

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String prompt, boolean yes) {

          if ("GO_UP".equals(prompt) && yes) {

            c.sayKey("npc.custodian.go_up");

            c.teleport(1081, 1465, 0);

            c.endConversation();

          } else if ("GO_UP".equals(prompt)) c.sayKey("npc.custodian.stay");
          else if ("READY".equals(prompt) && yes) {

            c.sayKey("npc.cpp.intl.11969");

            c.flag(DIALOG_STATE, 1);

          } else if ("READY".equals(prompt)) c.sayKey("npc.custodian.not_ready");
          else return false;

          return true;
        }

        private boolean answerRitePhrase(NpcBehaviorContext c, int state, String answer) {

          boolean correct =
              switch (state) {
                case 1 -> answer.contains("TO LOOK UPON ONE") && answer.contains("SELF");
                case 2 -> answer.contains("ONE MUST ONLY HAVE EYES");
                case 3 -> answer.contains("TO RECOGNIZE ONE") && answer.contains("OWN IGNORANCE");
                case 4 -> answer.contains("ONE MUST OPEN THESE EYES");
                default -> false;
              };

          if (!correct) {

            c.sayKey("npc.custodian.rite_failed");

            c.flag(DIALOG_STATE, 0);

            c.endConversation();

            return true;
          }

          if (state == 4) {

            c.sayKey("npc.custodian.rite_complete");

            c.flag("ADDON_CUSTODIAN_ACCESS", 1);

            c.flag(DIALOG_STATE, 0);

          } else {

            c.sayKey("npc.custodian.phrase_" + (state + 1));

            c.flag(DIALOG_STATE, state + 1);
          }

          return true;
        }
      };
}
