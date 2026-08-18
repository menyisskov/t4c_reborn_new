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

@Spawn(type = "Librarian2", x = 1078, y = 1452, z = 0, stationary = true, aggressive = false)
public final class Librarian2 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Librarian2";

  public static final String DISPLAY_NAME = "${npc.librarian2}";

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
          "${npc.welcome.librarian2}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.librarian2.0.0}", "${npc.topic_keyword.librarian2.0.1}"),
                  "${npc.topic.librarian2.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian2.1.0}"),
                  "${npc.topic.librarian2.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian2.2.0}"),
                  "${npc.topic.librarian2.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian2.3.0}"),
                  "${npc.topic.librarian2.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.librarian2.4.0}"),
                  "${npc.topic.librarian2.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.librarian2.5.0}",
                      "${npc.topic_keyword.librarian2.5.1}",
                      "${npc.topic_keyword.librarian2.5.2}",
                      "${npc.topic_keyword.librarian2.5.3}",
                      "${npc.topic_keyword.librarian2.5.4}"),
                  "${npc.topic.librarian2.5}",
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

          c.sayKey("npc.librarian2.welcome");

          c.askYesNo("findings");
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String s, boolean yes) {

          if (!"findings".equals(s)) return false;

          c.sayKey(yes ? "npc.librarian2.findings" : "npc.librarian2.no");

          return true;
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("NAME")) {

            c.sayKey("npc.librarian2.name");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.librarian2.work");

            return true;
          }

          if (k.equals("SECOND")
              || k.equals("COMING")
              || k.equals("CONSEQUENCE")
              || k.equals("HUMAN")) {

            c.sayKey("npc.librarian2.lore");

            return true;
          }

          return false;
        }
      };

  public Librarian2(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
