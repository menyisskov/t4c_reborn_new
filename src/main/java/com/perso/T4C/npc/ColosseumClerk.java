package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "ColosseumClerk", x = 1730, y = 1830, z = 0, stationary = true, aggressive = false)
public final class ColosseumClerk extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ColosseumClerk";

  public static final String DISPLAY_NAME = "${npc.colosseumclerk}";

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
          "${npc.welcome.colosseumclerk.initial}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumclerk.0.0}",
                      "${npc.topic_keyword.colosseumclerk.0.1}"),
                  "${npc.topic.colosseumclerk.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.colosseumclerk.1.0}"),
                  "${npc.topic.colosseumclerk.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumclerk.2.0}",
                      "${npc.topic_keyword.colosseumclerk.2.1}",
                      "${npc.topic_keyword.colosseumclerk.2.2}"),
                  "${npc.topic.colosseumclerk.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumclerk.3.0}",
                      "${npc.topic_keyword.colosseumclerk.3.1}"),
                  "${npc.topic.colosseumclerk.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumclerk.4.0}",
                      "${npc.topic_keyword.colosseumclerk.4.1}"),
                  "${npc.topic.colosseumclerk.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.colosseumclerk.5.0}"),
                  "${npc.topic.colosseumclerk.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.colosseumclerk.6.0}"),
                  "${npc.topic.colosseumclerk.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.colosseumclerk.7.0}"),
                  "${npc.topic.colosseumclerk.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumclerk.8.0}",
                      "${npc.topic_keyword.colosseumclerk.8.1}",
                      "${npc.topic_keyword.colosseumclerk.8.2}",
                      "${npc.topic_keyword.colosseumclerk.8.3}",
                      "${npc.topic_keyword.colosseumclerk.8.4}"),
                  "${npc.topic.colosseumclerk.8}",
                  List.of())),
          "ColosseumClerkNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public ColosseumClerk(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  @Override
  protected boolean canTalkThroughWalls() {

    return true;
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  public static NpcBehavior nativeBehavior() {

    return BEHAVIOR;
  }

  private static final int[] ARENA_SLICES = {
    50, 60, 70, 80, 90, 100, 120, 130, 140, 150, 160, 170, 180, 190, 200, 225, 300, 325, 350, 375,
    400, 425, 450, 475, 500
  };

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onInitialise(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          clearStuckArenaOccupancy(c);

          initialiseArenaSlice(c);

          if (c.globalFlag("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA") > 0) {

            c.sayKey("npc.colosseumclerk.busy");

            return;
          }

          c.sayKey("npc.welcome.colosseumclerk.initial");
        }

        @Override
        public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.trim().toUpperCase(java.util.Locale.ROOT);

          if (c.globalFlag("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA") > 0) {

            c.sayKey("npc.colosseumclerk.busy");

            return true;
          }

          if (k.equals("FIGHT") || k.equals("ARENA")) {

            c.sayKey("npc.colosseumclerk.fight.ask");

            c.askYesNo("fight");

            return true;
          }

          if (k.equals("INCREASE") || k.equals("DECREASE")) {

            if (c.globalFlag("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA") > 0) {

              c.sayKey("npc.colosseumclerk.busy");

              return true;
            }

            initialiseArenaSlice(c);

            int level = c.flag("__FLAG_USER_LEVEL_SLICE");

            if (c.flag("__FLAG_USER_HAS_CHANGED_DIFFICULTY_LEVEL") == 1) {

              c.sayKey("npc.colosseumclerk.difficulty.locked");

              return true;
            }

            int next = k.equals("INCREASE") ? increase(level) : decrease(level);

            c.flag("__FLAG_USER_LEVEL_SLICE", next);

            c.flag("__FLAG_ARENA_LEVEL", next);

            c.flag("__FLAG_USER_HAS_CHANGED_DIFFICULTY_LEVEL", 1);

            c.say(I18n.message("npc.colosseumclerk.difficulty", next));

            return true;
          }

          if (k.equals("ONE") || k.equals("TWO") || k.equals("THREE")) {

            int opponents = k.equals("ONE") ? 1 : k.equals("TWO") ? 2 : 3;

            c.flag("__ARENA_OPPONENTS", opponents);

            c.say(I18n.message("npc.colosseumclerk.opponents", opponents));

            return true;
          }

          // The original script uses a two-step confirmation: LEAVE/BYE/QUIT/FAREWELL/EXIT
          // asks whether the player is leaving, then LEAVING performs the teleport.
          if (k.startsWith("LEAVING")) {

            c.sayKey("npc.colosseumclerk.leave");

            c.teleport(343, 492, 0);

            c.endConversation();

            return true;
          }

          if (k.equals("BYE")
              || k.equals("LEAVE")
              || k.equals("EXIT")
              || k.equals("QUIT")
              || k.equals("FAREWELL")) {

            c.sayKey("npc.topic.colosseumclerk.8");

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(
            com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

          if (!"fight".equals(state)) return false;

          clearStuckArenaOccupancy(c);

          if (c.globalFlag("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA") > 0) {

            c.sayKey("npc.colosseumclerk.busy");

            return true;
          }

          if (!yes) {

            c.sayKey("npc.colosseumclerk.fight.cancel");

            return true;
          }

          initialiseArenaSlice(c);

          int level = snapArenaSlice(c.flag("__FLAG_USER_LEVEL_SLICE"));

          c.flag("__FLAG_USER_LEVEL_SLICE", level);

          c.flag("__FLAG_ARENA_LEVEL", level);

          c.viewFlag("ARENA_RETURN_X", (int) (c.player().getPositionVector().x / 32f));

          c.viewFlag("ARENA_RETURN_Y", (int) (c.player().getPositionVector().y / 32f));

          c.viewFlag("ARENA_RETURN_WORLD", c.player().getCoordinates().getZ());

          int opponents = c.flag("__ARENA_OPPONENTS");

          if (opponents < 1 || opponents > 3) opponents = 1;

          if (c.globalFlag("__COLOSSEUM_MONSTER_DISABLE") == 1) {

            c.sayKey("npc.colosseumclerk.fight.disabled");

            return true;
          }

          String mob = resolveArenaMob(level);

          if (mob == null) {

            c.sayKey("npc.colosseumclerk.fight.unavailable");

            return true;
          }

          int spawned = 0;

          for (int i = 0; i < opponents; i++) {

            int x = opponents == 1 ? 1707 : (i == 0 ? 1710 : 1735);

            int y = opponents == 1 ? 1853 : (i == 0 ? 1825 : 1850);

            if (c.summon(mob, x, y, 0)) spawned++;
          }

          c.globalFlag("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA", spawned);

          c.flag("__FLAG_USER_HAS_CHANGED_DIFFICULTY_LEVEL", 0);

          c.sayKey(
              spawned > 0
                  ? "npc.colosseumclerk.fight.started"
                  : "npc.colosseumclerk.fight.unavailable");

          return true;
        }

        private void initialiseArenaSlice(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          int current = c.flag("__FLAG_USER_LEVEL_SLICE");

          if (current != 0) {

            int snapped = snapArenaSlice(current);

            if (snapped != current) {

              c.flag("__FLAG_USER_LEVEL_SLICE", snapped);

              c.flag("__FLAG_ARENA_LEVEL", snapped);
            }

            return;
          }

          int l = c.player().getLevel();

          int slice =
              l <= 40 ? 40 : l <= 200 ? ((l + 9) / 10) * 10 : l <= 450 ? ((l + 24) / 25) * 25 : 500;

          slice = snapArenaSlice(Math.min(500, Math.max(40, slice)));

          c.flag("__FLAG_USER_LEVEL_SLICE", slice);

          c.flag("__FLAG_ARENA_LEVEL", slice);
        }

        private void clearStuckArenaOccupancy(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          if (c.globalFlag("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA") <= 0) return;

          int slice = c.flag("__FLAG_USER_LEVEL_SLICE");

          if (slice == 0 || MonsterRegistry.findByName("ArenaMobXP" + slice) == null) {

            c.globalFlag("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA", 0);
          }
        }

        private static String resolveArenaMob(int level) {

          String xp = "ArenaMobXP" + level;

          return MonsterRegistry.findByName(xp) != null ? xp : null;
        }

        private int increase(int level) {

          for (int slice : ARENA_SLICES) if (slice > level) return slice;

          return ARENA_SLICES[ARENA_SLICES.length - 1];
        }

        private int decrease(int level) {

          for (int i = ARENA_SLICES.length - 1; i >= 0; i--) {

            if (ARENA_SLICES[i] < level) return ARENA_SLICES[i];
          }

          return ARENA_SLICES[0];
        }

        private static int snapArenaSlice(int level) {

          int best = ARENA_SLICES[0];

          for (int slice : ARENA_SLICES) {

            int delta = Math.abs(slice - level);

            int bestDelta = Math.abs(best - level);

            if (delta < bestDelta || (delta == bestDelta && slice < best)) best = slice;
          }

          return best;
        }
      };
}
