package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Theopold", x = 625, y = 290, z = 2, stationary = false, aggressive = false)
public final class Theopold extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public static final String ID = "Theopold";

  public static final String DISPLAY_NAME = "${npc.theopold}";

  public static final String SPRITE_BASE = "Zombie";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.theopold}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theopold.0.0}", "${npc.topic_keyword.theopold.0.1}"),
                  "${npc.topic.theopold.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theopold.1.0}"),
                  "${npc.topic.theopold.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theopold.2.0}", "${npc.topic_keyword.theopold.2.1}"),
                  "${npc.topic.theopold.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theopold.3.0}"),
                  "${npc.topic.theopold.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theopold.4.0}", "${npc.topic_keyword.theopold.4.1}"),
                  "${npc.topic.theopold.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theopold.5.0}", "${npc.topic_keyword.theopold.5.1}"),
                  "${npc.topic.theopold.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theopold.6.0}"),
                  "${npc.topic.theopold.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theopold.7.0}"),
                  "${npc.topic.theopold.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theopold.8.0}"),
                  "${npc.topic.theopold.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theopold.9.0}"),
                  "${npc.topic.theopold.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theopold.10.0}"),
                  "${npc.topic.theopold.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theopold.11.0}", "${npc.topic_keyword.theopold.11.1}"),
                  "${npc.topic.theopold.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theopold.12.0}",
                      "${npc.topic_keyword.theopold.12.1}",
                      "${npc.topic_keyword.theopold.12.2}",
                      "${npc.topic_keyword.theopold.12.3}"),
                  "${npc.topic.theopold.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theopold.13.0}"),
                  "${npc.topic.theopold.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theopold.14.0}"),
                  "${npc.topic.theopold.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theopold.15.0}",
                      "${npc.topic_keyword.theopold.15.1}",
                      "${npc.topic_keyword.theopold.15.2}",
                      "${npc.topic_keyword.theopold.15.3}",
                      "${npc.topic_keyword.theopold.15.4}"),
                  "${npc.topic.theopold.15}",
                  List.of())),
          "DeadBrotherNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        int q = c.flag("__QUEST_THEOPOLD_RIDDLES");

        if (k.equals("RIDDLE")) {

          c.sayKey(q >= 6 ? "npc.theopold.ready" : "npc.theopold.riddle.ask");

          if (q >= 6) c.askYesNo("theopold_cuthana");
          else c.askYesNo("theopold_riddles");

          return true;
        }

        if (k.equals("RIDDLES")) {

          return answer(c, 2, "royal_key_3");
        }

        if (k.contains("LETTER") && k.contains(" A")) {

          return answer(c, 0, "royal_key_1");
        }

        if (k.contains("LETTER M")) {

          return answer(c, 1, "royal_key_2");
        }

        if (k.contains("UNICORN")) {

          return answer(c, 4, "royal_key_5");
        }

        if (k.contains("STUCK HERE")) {

          return answer(c, 3, "royal_key_4");
        }

        if (k.equals("NOTHING")) {

          return answer(c, 5, "royal_key_6");
        }

        if (k.equals("RETURN")) {

          c.sayKey("npc.theopold.return");

          c.teleport(680, 2385, 1);

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      private boolean answer(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, int expected, String key) {

        int q = c.flag("__QUEST_THEOPOLD_RIDDLES");

        if (q != expected) {

          c.sayKey("npc.theopold.wrong");

          return true;
        }

        if (!c.hasItem(key)) {

          c.sayKey("npc.theopold.no_key");

          c.teleport(420, 2500, 0);

          return true;
        }

        c.takeItem(key);

        c.flag("__QUEST_THEOPOLD_RIDDLES", expected + 1);

        c.giveXp(10000);

        c.sayKey("npc.theopold.correct");

        return true;
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("theopold_riddles".equals(s)) {

          if (yes) c.sayKey("npc.theopold.first");

          return true;
        }

        if ("theopold_cuthana".equals(s)) {

          if (yes && c.hasItem("royal_key_6")) {

            c.takeItem("royal_key_6");

            c.giveXp(10000);

            c.teleport(655, 350, 2);

            c.sayKey("npc.theopold.pass");

          } else if (yes) c.sayKey("npc.theopold.no_key");

          return true;
        }

        return false;
      }
    };
  }

  public Theopold(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
