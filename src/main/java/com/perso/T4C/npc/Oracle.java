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
import java.util.Locale;

@Spawn(type = "Oracle", x = 2724, y = 2192, z = 2, stationary = false, aggressive = false)
public final class Oracle extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Oracle";

  public static final String DISPLAY_NAME = "${npc.oracle}";

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
          "${npc.welcome.oracle}",
          List.of(
              topic(
                  "${npc.topic_keyword.oracle.0.0}",
                  "${npc.topic_keyword.oracle.0.1}",
                  "${npc.topic_keyword.oracle.0.2}",
                  "${npc.topic.oracle.0}"),
              topic(
                  "${npc.topic_keyword.oracle.1.0}",
                  "${npc.topic_keyword.oracle.1.1}",
                  "${npc.topic.oracle.1}"),
              topic(
                  "${npc.topic_keyword.oracle.2.0}",
                  "${npc.topic_keyword.oracle.2.1}",
                  "${npc.topic.oracle.2}"),
              topic(
                  "${npc.topic_keyword.oracle.3.0}",
                  "${npc.topic_keyword.oracle.3.1}",
                  "${npc.topic.oracle.3}"),
              topic("${npc.topic_keyword.oracle.4.0}", "${npc.topic.oracle.4}"),
              topic("${npc.topic_keyword.oracle.5.0}", "${npc.topic.oracle.5}"),
              topic(
                  "${npc.topic_keyword.oracle.6.0}",
                  "${npc.topic_keyword.oracle.6.1}",
                  "${npc.topic.oracle.6}"),
              topic(
                  "${npc.topic_keyword.oracle.7.0}",
                  "${npc.topic_keyword.oracle.7.1}",
                  "${npc.topic_keyword.oracle.7.2}",
                  "${npc.topic.oracle.7}"),
              topic("${npc.topic_keyword.oracle.8.0}", "${npc.topic.oracle.8}"),
              topic("${npc.topic_keyword.oracle.9.0}", "${npc.topic.oracle.9}"),
              topic(
                  "${npc.topic_keyword.oracle.10.0}",
                  "${npc.topic_keyword.oracle.10.1}",
                  "${npc.topic.oracle.10}"),
              topic(
                  "${npc.topic_keyword.oracle.11.0}",
                  "${npc.topic_keyword.oracle.11.1}",
                  "${npc.topic.oracle.11}"),
              topic("${npc.topic_keyword.oracle.12.0}", "${npc.topic.oracle.12}"),
              topic("${npc.topic_keyword.oracle.13.0}", "${npc.topic.oracle.13}"),
              topic(
                  "${npc.topic_keyword.oracle.14.0}",
                  "${npc.topic_keyword.oracle.14.1}",
                  "${npc.topic.oracle.14}"),
              topic("${npc.topic_keyword.oracle.15.0}", "${npc.topic.oracle.15}"),
              topic("${npc.topic_keyword.oracle.16.0}", "${npc.topic.oracle.16}"),
              topic(
                  "${npc.topic_keyword.oracle.17.0}",
                  "${npc.topic_keyword.oracle.17.1}",
                  "${npc.topic.oracle.17}"),
              topic(
                  "${npc.topic_keyword.oracle.18.0}",
                  "${npc.topic_keyword.oracle.18.1}",
                  "${npc.topic.oracle.18}")),
          "OracleNPC",
          new NpcSpec.CombatProfile(100, 1_000_000, 500, 500, 500, 1_000_000, 1, 65_535, "1d3"));

  private static NpcSpec.DialogueTopic topic(String key, String response) {

    return new NpcSpec.DialogueTopic(List.of(key), response, List.of());
  }

  private static NpcSpec.DialogueTopic topic(String key1, String key2, String response) {

    return new NpcSpec.DialogueTopic(List.of(key1, key2), response, List.of());
  }

  private static NpcSpec.DialogueTopic topic(
      String key1, String key2, String key3, String response) {

    return new NpcSpec.DialogueTopic(List.of(key1, key2, key3), response, List.of());
  }

  public Oracle(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  public static NpcBehavior behavior() {

    return BEHAVIOR;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        private static final String DEFEATED_ASSISTANT = "__FLAG_USER_HAS_DEFEATED_ASSISTANT";

        private static final String REMORTS = "__FLAG_NUMBER_OF_REMORTS";

        private static final String CONVERSATION = "__FLAG_CONVERSATION_WITH_ORACLE";

        private static final String INSULTED = "npc:Oracle:__FLAG_ORACLE_INSULTED";

        private static final String REBIRTH = "REBIRTH";

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          c.flag(INSULTED, 0);

          int killed = c.flag("__FLAG_USER_HAS_KILLED_MAKRSH_PTANGH");

          if (killed == 1 || killed == 3) {

            c.flag("__FLAG_USER_HAS_KILLED_MAKRSH_PTANGH", 2);

            c.sayKey("npc.oracle.native.tremor");

            return;
          }

          if (c.flag(DEFEATED_ASSISTANT) == 1 && c.flag(REMORTS) >= 3) {

            c.sayKey("${npc.topic.oracle.1}");

          } else if (c.flag(DEFEATED_ASSISTANT) == 1) {

            c.sayKey("npc.oracle.native.victory");

          } else if (c.flag(CONVERSATION) == 0) {

            c.sayKey("npc.oracle.native.welcome");

            c.flag(CONVERSATION, 1);

          } else {

            c.sayKey("npc.oracle.native.welcomeBack");
          }
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String input) {

          String keyword = normalize(input);

          if (keyword.contains("ready") && keyword.contains("reborn")) {

            offerRebirth(c);

            return true;
          }

          if (keyword.equals("reborn") || keyword.equals("rebirth")) {

            c.sayKey("${npc.topic.oracle.2}");

            return true;
          }

          if (keyword.equals("i know kung")) {

            c.sayKey("${npc.topic.oracle.5}");

            c.npc().provoke();

            return true;
          }

          if (keyword.equals("purity") || keyword.equals("spirit") || keyword.equals("soul")) {

            c.sayKey("${npc.topic.oracle.0}");

            return true;
          }

          if (keyword.equals("key")) {

            if (!c.hasItem("trial_key")) c.giveItem("trial_key");

            c.sayKey("${npc.topic.oracle.16}");

            return true;
          }

          if (keyword.contains("final") && keyword.contains("step")) {

            c.sayKey("${npc.topic.oracle.14}");

            c.teleport(1073, 540, 0);

            return true;
          }

          if (keyword.equals("leave")
              || keyword.equals("bye")
              || keyword.equals("quit")
              || keyword.equals("farewell")) {

            c.sayKey("${npc.topic.oracle.18}");

            c.endConversation();

            return true;
          }

          if (keyword.equals("fuck")
              || keyword.equals("suck")
              || keyword.equals("asshole")
              || keyword.equals("ass")) {

            if (c.flag(INSULTED) == 0) {

              c.sayKey("${npc.topic.oracle.17}");

              c.flag(INSULTED, 1);

            } else {

              c.sayKey("npc.oracle.native.warned");
            }

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean answer) {

          if (!REBIRTH.equals(state)) return false;

          if (!answer) {

            c.sayKey("npc.oracle.native.veryWell");

            return true;
          }

          if (!com.perso.T4C.npc.behavior.RebirthBehavior.canRebirth(c.player())) {

            c.sayKey("npc.oracle.native.rebirthLimit");

            return true;
          }

          c.sayKey("npc.oracle.native.letItBe");

          com.perso.T4C.npc.behavior.RebirthBehavior.perform(c.player());

          c.teleport(1315, 920, 1);

          return true;
        }

        @Override
        public void onAttacked(NpcBehaviorContext c) {

          c.shoutKey("npc.oracle.native.attacked");
        }

        private static void offerRebirth(NpcBehaviorContext c) {

          if (c.flag(DEFEATED_ASSISTANT) != 1) {

            c.sayKey("${npc.topic.oracle.0}");

            return;
          }

          if (!com.perso.T4C.npc.behavior.RebirthBehavior.canRebirth(c.player())) {

            c.sayKey("npc.oracle.native.rebirthLimit");

            return;
          }

          int minimumLevel =
              com.perso.T4C.npc.behavior.RebirthBehavior.requiredLevelFor(c.flag(REMORTS) + 1);

          if (c.player().getLevel() < minimumLevel) {

            c.sayKey("npc.oracle.native.tooWeak");

            return;
          }

          c.sayKey("npc.oracle.native.confirm");

          c.askYesNo(REBIRTH);
        }

        private static String normalize(String text) {

          return text == null
              ? ""
              : text.toLowerCase(Locale.ROOT)
                  .replaceAll("[^\\p{L}\\p{N}]+", " ")
                  .trim()
                  .replaceAll("\\s+", " ");
        }
      };
}
