package com.perso.T4C.npc.addon;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Efnisien extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Efnisien";

  public static final String DISPLAY_NAME = "${npc.efnisien}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupLichStaff")),
          0,
          List.of(),
          "${npc.welcome.efnisien}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.efnisien.0.0}"),
                  "${npc.topic.efnisien.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.efnisien.1.0}"),
                  "${npc.topic.efnisien.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.efnisien.2.0}"),
                  "${npc.topic.efnisien.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.efnisien.3.0}"),
                  "${npc.topic.efnisien.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.efnisien.4.0}"),
                  "${npc.topic.efnisien.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.efnisien.5.0}", "${npc.topic_keyword.efnisien.5.1}"),
                  "${npc.topic.efnisien.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.efnisien.6.0}", "${npc.topic_keyword.efnisien.6.1}"),
                  "${npc.topic.efnisien.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.efnisien.7.0}"),
                  "${npc.topic.efnisien.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.efnisien.8.0}"),
                  "${npc.topic.efnisien.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.efnisien.9.0}"),
                  "${npc.topic.efnisien.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.efnisien.10.0}"),
                  "${npc.topic.efnisien.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.efnisien.11.0}"),
                  "${npc.topic.efnisien.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.efnisien.12.0}"),
                  "${npc.topic.efnisien.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.efnisien.13.0}",
                      "${npc.topic_keyword.efnisien.13.1}",
                      "${npc.topic_keyword.efnisien.13.2}",
                      "${npc.topic_keyword.efnisien.13.3}",
                      "${npc.topic_keyword.efnisien.13.4}"),
                  "${npc.topic.efnisien.13}",
                  List.of())),
          "EfnisienNPC",
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

          if (p == 0) c.sayKey("npc.efnisien.progress.0");
          else if (p == 1) c.sayKey("npc.efnisien.progress.1");
          else if (p == 2) c.sayKey("npc.efnisien.progress.2");
          else if (p < 5) c.sayKey("npc.efnisien.progress.before5");
          else if (p == 5) c.sayKey("npc.efnisien.progress.5");
          else if (p == 6) c.sayKey("npc.efnisien.progress.6");
          else if (p < 39) c.sayKey("npc.efnisien.progress.before39");
          else if (p == 39) c.sayKey("npc.efnisien.progress.39");
          else if (p == 40) {

            c.sayKey("npc.efnisien.progress.40");

            c.flag("ADDON_STORYLINE_PROGRESS", 41);

          } else if (p == 41) c.sayKey("npc.efnisien.progress.41");
          else c.sayKey("npc.efnisien.progress.done");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (k.equals("NAME") || k.equals("EFNISIEN")) {

            c.sayKey("npc.efnisien.name");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.efnisien.work");

            return true;
          }

          if (k.equals("REFUSED")) {

            c.sayKey(p == 1 ? "npc.topic.efnisien.9" : "npc.efnisien.busy");

            if (p == 1) c.askYesNo("visit");

            return true;
          }

          if (k.equals("VISIT")) {

            c.sayKey(p == 1 ? "npc.topic.efnisien.9" : "npc.efnisien.busy");

            if (p == 1) c.askYesNo("visit");

            return true;
          }

          if (k.equals("GUARD")
              || k.equals("ASSASSIN")
              || k.contains("OLIN")
              || k.contains("HAAD")) {

            c.sayKey(p == 1 ? "npc.efnisien.lore" : "npc.efnisien.busy");

            return true;
          }

          return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if (!"visit".equals(state)) return false;

          c.sayKey("npc.topic.efnisien.10");

          return true;
        }
      };

  public Efnisien(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
