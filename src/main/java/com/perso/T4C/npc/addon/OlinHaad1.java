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

public final class OlinHaad1 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "OlinHaad1";

  public static final String DISPLAY_NAME = "${npc.olinhaad1}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupSimpleStaff")),
          0,
          List.of(),
          "${npc.welcome.olinhaad1}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.0.0}"),
                  "${npc.topic.olinhaad1.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.olinhaad1.1.0}", "${npc.topic_keyword.olinhaad1.1.1}"),
                  "${npc.topic.olinhaad1.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.2.0}"),
                  "${npc.topic.olinhaad1.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.3.0}"),
                  "${npc.topic.olinhaad1.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.4.0}"),
                  "${npc.topic.olinhaad1.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.5.0}"),
                  "${npc.topic.olinhaad1.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.6.0}"),
                  "${npc.topic.olinhaad1.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.olinhaad1.7.0}", "${npc.topic_keyword.olinhaad1.7.1}"),
                  "${npc.topic.olinhaad1.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.olinhaad1.8.0}", "${npc.topic_keyword.olinhaad1.8.1}"),
                  "${npc.topic.olinhaad1.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.9.0}"),
                  "${npc.topic.olinhaad1.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.10.0}"),
                  "${npc.topic.olinhaad1.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.11.0}"),
                  "${npc.topic.olinhaad1.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.12.0}"),
                  "${npc.topic.olinhaad1.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.13.0}"),
                  "${npc.topic.olinhaad1.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.14.0}"),
                  "${npc.topic.olinhaad1.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.15.0}"),
                  "${npc.topic.olinhaad1.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.16.0}"),
                  "${npc.topic.olinhaad1.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.17.0}"),
                  "${npc.topic.olinhaad1.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.18.0}"),
                  "${npc.topic.olinhaad1.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.olinhaad1.19.0}", "${npc.topic_keyword.olinhaad1.19.1}"),
                  "${npc.topic.olinhaad1.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.20.0}"),
                  "${npc.topic.olinhaad1.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.21.0}"),
                  "${npc.topic.olinhaad1.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.olinhaad1.22.0}", "${npc.topic_keyword.olinhaad1.22.1}"),
                  "${npc.topic.olinhaad1.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.olinhaad1.23.0}"),
                  "${npc.topic.olinhaad1.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.olinhaad1.24.0}",
                      "${npc.topic_keyword.olinhaad1.24.1}",
                      "${npc.topic_keyword.olinhaad1.24.2}",
                      "${npc.topic_keyword.olinhaad1.24.3}",
                      "${npc.topic_keyword.olinhaad1.24.4}"),
                  "${npc.topic.olinhaad1.24}",
                  List.of())),
          "OlinHaad1NPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public OlinHaad1(NpcContext context) throws GameException {

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
        public void onConversationStart(NpcBehaviorContext c) {

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (p < 2) c.sayKey("npc.olin.progress.before2");
          else if (p < 4) {

            if (c.flag("ADDON_NUMBER_OF_OLIN_HAAD_GUARDS_KILLED") > 19) {

              c.sayKey("npc.olin.progress.2");

              c.flag("ADDON_STORYLINE_PROGRESS", 3);

            } else {

              c.sayKey("npc.olin.progress.before2");

              if (p == 2) c.flag("ADDON_STORYLINE_PROGRESS", 3);
            }

          } else if (p == 4) c.sayKey("npc.olin.progress.4");
          else if (p == 5) c.sayKey("npc.olin.progress.5");
          else if (p < 23) c.sayKey("npc.olin.progress.before23");
          else if (p == 23) c.sayKey("npc.olin.progress.23");
          else if (p < 36) c.sayKey("npc.olin.progress.before36");
          else if (p == 36) c.sayKey("npc.olin.progress.36");
          else if (p == 37) c.sayKey("npc.olin.progress.37");
          else if (p == 38) c.sayKey("npc.olin.progress.38");
          else c.sayKey("npc.olin.progress.done");
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String s, boolean yes) {

          if (!"fight".equals(s)) return false;

          c.sayKey(yes ? "npc.olin.fight.yes" : "npc.olin.fight.no");

          c.teleport(1825, 2550, 1);

          return true;
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (k.equals("FIGHT")) {

            c.sayKey(p == 3 ? "npc.olin.fight" : "npc.olin.busy");

            return true;
          }

          if (k.equals("SEEK") || k.equals("SAME")) {

            if (p == 3) {

              c.sayKey("npc.olin.challenge");

              c.askYesNo("fight");

            } else c.sayKey("npc.olin.busy");

            return true;
          }

          if (k.equals("THEY")
              || k.equals("HARBINGER")
              || k.equals("PROPHECY")
              || k.equals("HIS")
              || k.equals("SEEDS")
              || k.equals("YOU")
              || k.equals("ME")
              || k.equals("NEW")
              || k.equals("BREED")) {

            if (p == 4) {

              String key =
                  k.equals("THEY")
                      ? "npc.topic.olinhaad1.2"
                      : k.equals("HARBINGER")
                          ? "npc.topic.olinhaad1.3"
                          : k.equals("PROPHECY")
                              ? "npc.topic.olinhaad1.4"
                              : k.equals("HIS")
                                  ? "npc.topic.olinhaad1.5"
                                  : k.equals("SEEDS")
                                      ? "npc.topic.olinhaad1.6"
                                      : (k.equals("YOU") || k.equals("ME"))
                                          ? "npc.topic.olinhaad1.7"
                                          : "npc.topic.olinhaad1.8";

              c.sayKey(key);

            } else c.sayKey("npc.olin.busy");

            return true;
          }

          if (k.equals("ANSWER")) {

            if (p == 4) {

              c.sayKey("npc.topic.olinhaad1.10");

              c.flag("ADDON_STORYLINE_PROGRESS", 5);

            } else c.sayKey("npc.olin.busy");

            return true;
          }

          if (k.equals("NOMAD") || k.equals("DARKSTONE") || k.equals("HEARTSTONE")) {

            c.sayKey(
                p == 23
                    ? "npc.topic.olinhaad1."
                        + (k.equals("NOMAD") ? "11" : k.equals("DARKSTONE") ? "12" : "13")
                    : "npc.olin.busy");

            return true;
          }

          if (k.equals("LIES")) {

            if (p == 23) {

              c.sayKey("npc.topic.olinhaad1.14");

              c.flag("ADDON_STORYLINE_PROGRESS", 24);

              c.castTargetSpell(10751);

              for (int i = 0; i < 3; i++) c.summon("Olin Haad Private Guard", 2962, 1348, 0);

            } else c.sayKey("npc.olin.busy");

            return true;
          }

          if (k.equals("DARE")) {

            if (p == 36) {

              c.sayKey("npc.topic.olinhaad1.15");

              c.castTargetSpell(10758);

            } else c.sayKey("npc.olin.busy");

            return true;
          }

          if (k.equals("INVINCIBLE")) {

            c.sayKey(p == 37 ? "npc.topic.olinhaad1.16" : "npc.olin.busy");

            return true;
          }

          if (k.equals("HERALD")) {

            c.sayKey(p == 37 ? "npc.topic.olinhaad1.16" : "npc.olin.busy");

            return true;
          }

          return false;
        }
      };
}
