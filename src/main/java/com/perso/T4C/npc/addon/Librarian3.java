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

@Spawn(type = "Librarian3", x = 1066, y = 1464, z = 0, stationary = true, aggressive = false)
public final class Librarian3 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Librarian3";

  public static final String DISPLAY_NAME = "${npc.librarian3}";

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
          "${npc.welcome.librarian3}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian3.0.0}"),
                  "${npc.topic.librarian3.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian3.1.0}"),
                  "${npc.topic.librarian3.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian3.2.0}"),
                  "${npc.topic.librarian3.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian3.3.0}"),
                  "${npc.topic.librarian3.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian3.4.0}"),
                  "${npc.topic.librarian3.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian3.5.0}"),
                  "${npc.topic.librarian3.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian3.6.0}"),
                  "${npc.topic.librarian3.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.librarian3.7.0}",
                      "${npc.topic_keyword.librarian3.7.1}",
                      "${npc.topic_keyword.librarian3.7.2}",
                      "${npc.topic_keyword.librarian3.7.3}",
                      "${npc.topic_keyword.librarian3.7.4}"),
                  "${npc.topic.librarian3.7}",
                  List.of())),
          "HighPriestGuntharNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          c.sayKey("npc.librarian3.welcome");

          c.askYesNo("findings");
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String s, boolean yes) {

          if (!"findings".equals(s)) return false;

          c.sayKey(yes ? "npc.librarian3.findings" : "npc.librarian3.no");

          return true;
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("NAME")) {

            c.sayKey("npc.librarian3.name");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.librarian3.work");

            return true;
          }

          if (k.equals("DARKSEED")
              || k.equals("ARTHERK")
              || k.equals("GLURIURL")
              || k.equals("LOTHAR")
              || k.equals("CORRUPTION")) {

            c.sayKey("npc.librarian3.lore");

            if (k.equals("CORRUPTION") && c.flag("ADDON_STORYLINE_PROGRESS") == 35)
              c.flag("ADDON_STORYLINE_PROGRESS", 36);

            return true;
          }

          return false;
        }
      };

  public Librarian3(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
