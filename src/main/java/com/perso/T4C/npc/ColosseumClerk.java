package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
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

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onInitialise(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

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

          if (c.globalFlag("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA") > 0
              && !k.equals("BYE")
              && !k.equals("LEAVE")
              && !k.equals("EXIT")
              && !k.equals("QUIT")) {

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

          if (k.equals("BYE") || k.equals("LEAVE") || k.equals("EXIT") || k.equals("QUIT")) {

            c.endConversation();

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(
            com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

          if (!"fight".equals(state)) return false;

          if (c.globalFlag("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA") > 0) {

            c.sayKey("npc.colosseumclerk.busy");

            return true;
          }

          if (!yes) {

            c.sayKey("npc.colosseumclerk.fight.cancel");

            return true;
          }

          int level = c.flag("__ARENA_LEVEL");

          initialiseArenaSlice(c);

          level = c.flag("__FLAG_USER_LEVEL_SLICE");

          c.flag("__ARENA_LEVEL", level);

          c.viewFlag("ARENA_RETURN_X", (int) (c.player().getPositionVector().x / 32f));

          c.viewFlag("ARENA_RETURN_Y", (int) (c.player().getPositionVector().y / 32f));

          c.viewFlag("ARENA_RETURN_WORLD", c.player().getCoordinates().getZ());

          int opponents = c.flag("__ARENA_OPPONENTS");

          if (opponents < 1 || opponents > 3) opponents = 1;

          if (c.globalFlag("__COLOSSEUM_MONSTER_DISABLE") == 1) {

            c.sayKey("npc.colosseumclerk.fight.disabled");

            return true;
          }

          c.globalFlag("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA", opponents);

          boolean summoned = true;

          for (int i = 0; i < opponents; i++) {

            int x = opponents == 1 ? 1707 : (i == 0 ? 1710 : 1735);

            int y = opponents == 1 ? 1853 : (i == 0 ? 1825 : 1850);

            summoned &= c.summon("ArenaMob" + level, x, y, 0);
          }

          c.flag("__FLAG_USER_HAS_CHANGED_DIFFICULTY_LEVEL", 0);

          c.sayKey(
              summoned
                  ? "npc.colosseumclerk.fight.started"
                  : "npc.colosseumclerk.fight.unavailable");

          return true;
        }

        private void initialiseArenaSlice(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          if (c.flag("__FLAG_USER_LEVEL_SLICE") != 0) return;

          int l = c.player().getLevel();

          int slice =
              l <= 40 ? 40 : l <= 200 ? ((l + 9) / 10) * 10 : l <= 450 ? ((l + 24) / 25) * 25 : 500;

          c.flag("__FLAG_USER_LEVEL_SLICE", Math.min(500, Math.max(40, slice)));

          c.flag("__FLAG_ARENA_LEVEL", c.flag("__FLAG_USER_LEVEL_SLICE"));
        }

        private int increase(int level) {

          return level < 200
              ? Math.min(200, level + 10)
              : level < 475 ? Math.min(475, level + 25) : 500;
        }

        private int decrease(int level) {

          return level > 225
              ? Math.max(225, level - 25)
              : level > 50 ? Math.max(40, level - 10) : 40;
        }
      };
}
