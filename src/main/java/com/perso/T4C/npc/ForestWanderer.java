package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "ForestWanderer", x = 2620, y = 2065, z = 0, stationary = true, aggressive = false)
public final class ForestWanderer extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ForestWanderer";

  public static final String DISPLAY_NAME = "${npc.forestwanderer}";

  public static final String SPRITE_BASE = "@invisible";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.forestwanderer}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.forestwanderer.0.0}",
                      "${npc.topic_keyword.forestwanderer.0.1}"),
                  "${npc.topic.forestwanderer.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.forestwanderer.1.0}",
                      "${npc.topic_keyword.forestwanderer.1.1}",
                      "${npc.topic_keyword.forestwanderer.1.2}"),
                  "${npc.topic.forestwanderer.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.forestwanderer.2.0}",
                      "${npc.topic_keyword.forestwanderer.2.1}"),
                  "${npc.topic.forestwanderer.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.forestwanderer.3.0}",
                      "${npc.topic_keyword.forestwanderer.3.1}",
                      "${npc.topic_keyword.forestwanderer.3.2}",
                      "${npc.topic_keyword.forestwanderer.3.3}"),
                  "${npc.topic.forestwanderer.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.forestwanderer.4.0}",
                      "${npc.topic_keyword.forestwanderer.4.1}"),
                  "${npc.topic.forestwanderer.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.forestwanderer.5.0}",
                      "${npc.topic_keyword.forestwanderer.5.1}",
                      "${npc.topic_keyword.forestwanderer.5.2}",
                      "${npc.topic_keyword.forestwanderer.5.3}",
                      "${npc.topic_keyword.forestwanderer.5.4}"),
                  "${npc.topic.forestwanderer.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.forestwanderer.6.0}",
                      "${npc.topic_keyword.forestwanderer.6.1}"),
                  "${npc.topic.forestwanderer.6}",
                  List.of())),
          "ForestWandererNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public ForestWanderer(NpcContext context) throws GameException {

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
        public void onInitialise(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          c.systemMessageKey("npc.forestwanderer.greeting");
        }

        @Override
        public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.contains("LOST") || k.contains("HOME")) {

            c.systemMessageKey("npc.forestwanderer.askHome");

            c.askYesNo("home");

            return true;
          }

          if (k.equals("HELP") || k.equals("HINT")) {

            c.systemMessageKey("npc.forestwanderer.hint");

            return true;
          }

          if (k.equals("FUCK") || k.equals("SUCK") || k.equals("ASSHOLE") || k.equals("ASS")) {

            c.teleport(2778, 2006, 0);

            return true;
          }

          if (k.equals("BYE")
              || k.equals("LEAVE")
              || k.equals("QUIT")
              || k.equals("FAREWELL")
              || k.equals("EXIT")) {

            c.endConversation();

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(
            com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

          if (!"home".equals(state)) return false;

          if (!yes) {

            c.systemMessageKey("npc.forestwanderer.no");

            return true;
          }

          int gifts = Math.min(6, c.flag("__QUEST_FOREST_WANDERER_GIFTS") + 1);

          c.flag("__QUEST_FOREST_WANDERER_GIFTS", gifts);

          java.util.List<String> giftsPool =
              java.util.List.of(
                  "drachensword",
                  "drachenstaff",
                  "drachenhelm",
                  "drachenshield",
                  "drachenrobe",
                  "drachenplate");

          c.giveItem(giftsPool.get((int) (Math.random() * gifts)));

          int[][] spots = {
            {2568, 1945}, {2638, 1901}, {2897, 1915}, {2970, 1950}, {2881, 2130}, {2620, 2065}
          };

          int[] spot = spots[(int) (Math.random() * spots.length)];

          c.summon("FORESTWANDERER", spot[0], spot[1], 0);

          c.systemMessageKey("npc.forestwanderer.returning");

          c.teleport(715, 1295, 0);

          c.selfDestructNpc();

          return true;
        }
      };
}
