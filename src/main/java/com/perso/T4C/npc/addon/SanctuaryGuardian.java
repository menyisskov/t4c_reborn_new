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

@Spawn(type = "SanctuaryGuardian", x = 695, y = 1323, z = 0, stationary = true, aggressive = false)
public final class SanctuaryGuardian extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "SanctuaryGuardian";

  public static final String DISPLAY_NAME = "${npc.sanctuaryguardian}";

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
          "${npc.welcome.sanctuaryguardian}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.sanctuaryguardian.0.0}",
                      "${npc.topic_keyword.sanctuaryguardian.0.1}"),
                  "${npc.topic.sanctuaryguardian.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sanctuaryguardian.1.0}"),
                  "${npc.topic.sanctuaryguardian.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sanctuaryguardian.2.0}"),
                  "${npc.topic.sanctuaryguardian.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sanctuaryguardian.3.0}"),
                  "${npc.topic.sanctuaryguardian.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.sanctuaryguardian.4.0}",
                      "${npc.topic_keyword.sanctuaryguardian.4.1}",
                      "${npc.topic_keyword.sanctuaryguardian.4.2}",
                      "${npc.topic_keyword.sanctuaryguardian.4.3}",
                      "${npc.topic_keyword.sanctuaryguardian.4.4}"),
                  "${npc.topic.sanctuaryguardian.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sanctuaryguardian.5.0}"),
                  "${npc.topic.sanctuaryguardian.5}",
                  List.of())),
          "OlinHaad1NPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public SanctuaryGuardian(NpcContext context) throws GameException {

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

          c.sayKey("npc.sanctuary.welcome");
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String s, boolean yes) {

          if (!"pass".equals(s)) return false;

          if (yes) {

            c.sayKey("npc.sanctuary.pass");

            c.teleport(691, 1319, 0);

          } else c.sayKey("npc.sanctuary.no");

          return true;
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.contains("WORD") || k.contains("POWER")) {

            c.sayKey("npc.sanctuary.word");

            return true;
          }

          if (k.equals("ETHEREAL")) {

            if (c.flag("ADDON_STORYLINE_PROGRESS") >= 33) {

              c.sayKey("npc.sanctuary.warning");

              c.askYesNo("pass");

            } else c.sayKey("npc.sanctuary.weak");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.sanctuary.work");

            return true;
          }

          if (k.equals("NAME")) {

            c.sayKey("npc.sanctuary.name");

            return true;
          }

          return false;
        }
      };
}
