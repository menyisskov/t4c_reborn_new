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

public final class Librarian1 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Librarian1";

  public static final String DISPLAY_NAME = "${npc.librarian1}";

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
          "${npc.welcome.librarian1}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.librarian1.0.0}", "${npc.topic_keyword.librarian1.0.1}"),
                  "${npc.topic.librarian1.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.librarian1.1.0}", "${npc.topic_keyword.librarian1.1.1}"),
                  "${npc.topic.librarian1.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.librarian1.2.0}", "${npc.topic_keyword.librarian1.2.1}"),
                  "${npc.topic.librarian1.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.librarian1.3.0}", "${npc.topic_keyword.librarian1.3.1}"),
                  "${npc.topic.librarian1.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian1.4.0}"),
                  "${npc.topic.librarian1.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian1.5.0}"),
                  "${npc.topic.librarian1.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian1.6.0}"),
                  "${npc.topic.librarian1.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian1.7.0}"),
                  "${npc.topic.librarian1.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian1.8.0}"),
                  "${npc.topic.librarian1.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.librarian1.9.0}",
                      "${npc.topic_keyword.librarian1.9.1}",
                      "${npc.topic_keyword.librarian1.9.2}",
                      "${npc.topic_keyword.librarian1.9.3}",
                      "${npc.topic_keyword.librarian1.9.4}"),
                  "${npc.topic.librarian1.9}",
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

          c.sayKey("npc.librarian1.welcome");

          c.askYesNo("findings");
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String s, boolean yes) {

          if (!"findings".equals(s)) return false;

          c.sayKey(yes ? "npc.librarian1.findings" : "npc.librarian1.no");

          return true;
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("NAME")) {

            c.sayKey("npc.librarian1.name");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.librarian1.work");

            return true;
          }

          if (k.equals("EXCHANGE")
              || k.equals("ESSENCE")
              || k.equals("PHYSIC")
              || k.equals("BODY")
              || k.equals("CHAOS")
              || k.equals("OGRIMAR")
              || k.equals("LOTHAR")
              || k.equals("JUST")
              || k.equals("ABANDON")
              || k.equals("ELVENWEAVE")
              || k.equals("HEART")) {

            c.sayKey("npc.librarian1.lore");

            if (k.equals("HEART") && c.flag("ADDON_STORYLINE_PROGRESS") == 35)
              c.flag("ADDON_STORYLINE_PROGRESS", 36);

            return true;
          }

          return false;
        }
      };

  public Librarian1(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
