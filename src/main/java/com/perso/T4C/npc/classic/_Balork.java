package com.perso.T4C.npc.classic;

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

@Spawn(type = "_Balork", x = 0, y = 0, z = 1, stationary = false, aggressive = false)
public final class _Balork extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Demon Attack.wav";
  public static final String SOUND_DEATH = "Demon Dying.wav";
  public static final String SOUND_HIT = "Demon Hit.wav";

  public static final String ID = "_Balork";

  public static final String DISPLAY_NAME = "${npc.balork}";

  public static final String SPRITE_BASE = "Demon";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.balork}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.balork.0.0}", "${npc.topic_keyword.balork.0.1}"),
                  "${npc.topic.balork.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.balork.1.0}",
                      "${npc.topic_keyword.balork.1.1}",
                      "${npc.topic_keyword.balork.1.2}"),
                  "${npc.topic.balork.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.balork.2.0}"), "${npc.topic.balork.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.balork.3.0}",
                      "${npc.topic_keyword.balork.3.1}",
                      "${npc.topic_keyword.balork.3.2}",
                      "${npc.topic_keyword.balork.3.3}"),
                  "${npc.topic.balork.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.balork.4.0}", "${npc.topic_keyword.balork.4.1}"),
                  "${npc.topic.balork.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.balork.5.0}"), "${npc.topic.balork.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.balork.6.0}"), "${npc.topic.balork.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.balork.7.0}",
                      "${npc.topic_keyword.balork.7.1}",
                      "${npc.topic_keyword.balork.7.2}",
                      "${npc.topic_keyword.balork.7.3}",
                      "${npc.topic_keyword.balork.7.4}"),
                  "${npc.topic.balork.7}",
                  List.of())),
          "Balork",
          new NpcSpec.CombatProfile(15, 508, 51, 53, 35, 7, 190, 70, "1d17+12"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if ((c.flag("__FLAG_NUMBER_OF_REMORTS") >= 1 && c.flag("__QUEST_FIXED_ALIGNMENT") == 1)
            || c.flag("__QUEST_FIXED_ALIGNMENT") == 1
            || c.flag("__BALORK_BRAND") == 1) {

          c.npc().provoke();

        } else c.sayKey("npc.balork.welcome.normal");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT).trim();

        if ("DARKNESS".equals(k)) {

          if (c.flag("__BALORK_KILLED") == 1) c.npc().provoke();

          return true;
        }

        if ("CHURCH".equals(k)
            || "KILHIAM".equals(k)
            || "MOONROCK".equals(k)
            || "ARTHERK".equals(k)) {

          if (c.flag("__BALORK_KILLED") == 1) c.npc().provoke();
          else if (c.flag("__KNOWS_EVIL_PASSWORD") != 1) c.askYesNo("work");

          return true;
        }

        if ("EVIL".equals(k) || "FOUL DEED".equals(k)) {

          c.askYesNo("sword");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if ("work".equals(state)) {

          c.npc().provoke();

          return true;
        }

        if ("sword".equals(state)) {

          if (yes) c.flag("__KNOWS_EVIL_PASSWORD", 1);

          return true;
        }

        return false;
      }

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__QUEST_ISLAND_ACCESS") == 0 && c.flag("__QUEST_FIXED_ALIGNMENT") != -1) {

          c.flag("__BALORK_BRAND", 1);

          c.flag("__BALORK_KILLED", 1);

          if (c.karma() >= -50 && c.karma() <= 50) c.karma(c.karma() + 5 * (500 - c.karma()) / 500);
        }
      }
    };
  }

  public _Balork(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
