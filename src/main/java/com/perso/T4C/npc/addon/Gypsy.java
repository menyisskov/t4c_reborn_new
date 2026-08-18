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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Gypsy", x = 2370, y = 360, z = 0, stationary = false, aggressive = false)
public final class Gypsy extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Gypsy";

  public static final String DISPLAY_NAME = "${npc.gypsy}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoClothBody"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe")),
          0,
          List.of(),
          "${npc.welcome.gypsy}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gypsy.0.0}", "${npc.topic_keyword.gypsy.0.1}"),
                  "${npc.topic.gypsy.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gypsy.1.0}", "${npc.topic_keyword.gypsy.1.1}"),
                  "${npc.topic.gypsy.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gypsy.2.0}"), "${npc.topic.gypsy.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gypsy.3.0}"), "${npc.topic.gypsy.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gypsy.4.0}"), "${npc.topic.gypsy.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gypsy.5.0}"), "${npc.topic.gypsy.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gypsy.6.0}"), "${npc.topic.gypsy.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gypsy.7.0}", "${npc.topic_keyword.gypsy.7.1}"),
                  "${npc.topic.gypsy.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gypsy.8.0}"), "${npc.topic.gypsy.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gypsy.9.0}"), "${npc.topic.gypsy.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gypsy.10.0}"), "${npc.topic.gypsy.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gypsy.11.0}"), "${npc.topic.gypsy.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gypsy.12.0}"), "${npc.topic.gypsy.12}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gypsy.13.0}",
                      "${npc.topic_keyword.gypsy.13.1}",
                      "${npc.topic_keyword.gypsy.13.2}",
                      "${npc.topic_keyword.gypsy.13.3}",
                      "${npc.topic_keyword.gypsy.13.4}"),
                  "${npc.topic.gypsy.13}",
                  List.of())),
          "GypsyNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (p < 16) c.sayKey("npc.gypsy.progress.before16");
          else if (p == 16) c.sayKey("npc.gypsy.progress.16");
          else if (p == 17) c.sayKey("npc.gypsy.progress.17");
          else if (p == 18) c.sayKey("npc.gypsy.progress.18");
          else if (p == 19) c.sayKey("npc.gypsy.progress.19");
          else if (p < 42) c.sayKey("npc.gypsy.progress.before42");
          else if (p == 42) c.sayKey("npc.gypsy.progress.42");
          else c.sayKey("npc.gypsy.progress.complete");
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if ("first".equals(state)) {

            c.sayKey(yes ? "npc.gypsy.question.second" : "npc.gypsy.question.first_no");

            c.askYesNo("second");

            return true;
          }

          if ("second".equals(state)) {

            c.sayKey(yes ? "npc.gypsy.question.third" : "npc.gypsy.question.third_no");

            c.askYesNo("third");

            return true;
          }

          if ("third".equals(state)) {

            c.sayKey(yes ? "npc.gypsy.question.giver" : "npc.gypsy.question.harvester");

            c.flag("ADDON_STORYLINE_PROGRESS", 17);

            return true;
          }

          return false;
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          String pending = c.pendingYesNo();

          if ("third".equals(pending)) {

            if (k.contains("HARVESTER") || k.contains("GIVER")) {

              c.sayKey(
                  k.contains("GIVER")
                      ? "npc.gypsy.question.giver"
                      : "npc.gypsy.question.harvester");

              c.flag("ADDON_STORYLINE_PROGRESS", 17);

            } else c.sayKey("npc.gypsy.question.third");

            return true;
          }

          if ("item".equals(pending)) {

            if (k.contains("REGENERATION")) {

              giveChoice(
                  c, "AmuletOfRegeneration", "GemOfRegeneration", "npc.gypsy.choice.regeneration");

            } else if (k.contains("RENEWAL")) {

              giveChoice(c, "AmuletOfRenewal", "GemOfRenewal", "npc.gypsy.choice.renewal");

            } else if (k.contains("QUIVER")) {

              c.giveItem("TrueshotQuiver");

              c.sayKey("npc.gypsy.choice.quiver");

              c.flag("ADDON_STORYLINE_PROGRESS", 19);

            } else c.sayKey("npc.gypsy.choice.again");

            return true;
          }

          if (k.equals("NOMAD") && p == 16) {

            c.sayKey("npc.gypsy.nomad");

            c.askYesNo("first");

            return true;
          }

          if (k.equals("SOMETHING") && p == 18) {

            c.sayKey("npc.gypsy.choice.ask");

            c.askYesNo("item");

            return true;
          }

          if (k.equals("TRAVELER") || k.equals("TRAVELLER")) {

            c.sayKey(p == 16 ? "npc.gypsy.traveler" : "npc.gypsy.silence");

            return true;
          }

          if (k.equals("SEER")) {

            c.sayKey(p == 16 ? "npc.gypsy.seer" : "npc.gypsy.silence");

            return true;
          }

          if (k.contains("FOURTH") || k.contains("COMING")) {

            c.sayKey(p == 42 ? "npc.gypsy.fourth" : "npc.gypsy.silence");

            return true;
          }

          if (k.contains("RESPECT") || k.contains("GUARDIAN")) {

            if (p == 42) {

              c.sayKey("npc.gypsy.reward");

              for (String i :
                  new String[] {
                    "QuiverOfStability",
                    "AmuletOfRejuvenation",
                    "GemOfRejuvenation",
                    "AmuletOfReplenishment",
                    "GemOfReplenishment"
                  }) c.giveItem(i);

              c.flag("ADDON_STORYLINE_PROGRESS", 43);

            } else c.sayKey("npc.gypsy.silence");

            return true;
          }

          return false;
        }

        private void giveChoice(NpcBehaviorContext c, String a, String g, String message) {

          c.giveItem(a);

          c.giveItem(g);

          c.sayKey(message);

          c.flag("ADDON_STORYLINE_PROGRESS", 19);
        }
      };

  public Gypsy(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
