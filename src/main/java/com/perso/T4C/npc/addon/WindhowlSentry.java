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

public final class WindhowlSentry extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "WindhowlSentry";

  public static final String DISPLAY_NAME = "${npc.windhowlsentry}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupRomanShield"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.windhowlsentry}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowlsentry.0.0}"),
                  "${npc.topic.windhowlsentry.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowlsentry.1.0}",
                      "${npc.topic_keyword.windhowlsentry.1.1}"),
                  "${npc.topic.windhowlsentry.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowlsentry.2.0}"),
                  "${npc.topic.windhowlsentry.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowlsentry.3.0}"),
                  "${npc.topic.windhowlsentry.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowlsentry.4.0}",
                      "${npc.topic_keyword.windhowlsentry.4.1}",
                      "${npc.topic_keyword.windhowlsentry.4.2}",
                      "${npc.topic_keyword.windhowlsentry.4.3}",
                      "${npc.topic_keyword.windhowlsentry.4.4}"),
                  "${npc.topic.windhowlsentry.4}",
                  List.of())),
          "WindhowlSentryNPC",
          new NpcSpec.CombatProfile(100, 1000000, 115, 104, 104, 100000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          if (c.flag("ADDON_USER_HAS_KILLED_CARMAN") == 1
              && c.flag("ADDON_USER_REWARDED_FOR_KILLING_CARMAN") == 0)
            c.sayKey("npc.sentry.reward.ready");
          else if (c.flag("ADDON_USER_REWARDED_FOR_KILLING_CARMAN") == 1)
            c.sayKey("npc.sentry.reward.done");
          else c.sayKey("npc.sentry.welcome");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("HELP")) {

            c.sayKey("npc.sentry.help");

            return true;
          }

          if (k.contains("TAKE") || k.contains("CARE")) {

            if (c.globalFlag("ADDON_CARMAN_PRESENT") == 0) {

              c.sayKey("npc.sentry.task");

              c.askYesNo("accept");

            } else c.sayKey("npc.sentry.reward.done");

            return true;
          }

          if (k.equals("REWARD")) {

            if (c.flag("ADDON_USER_HAS_KILLED_CARMAN") == 1
                && c.flag("ADDON_USER_REWARDED_FOR_KILLING_CARMAN") == 0) {

              int level = c.player().getLevel();

              c.giveGold(level * 500);

              c.giveXp(level * level * 100);

              c.flag("ADDON_USER_REWARDED_FOR_KILLING_CARMAN", 1);

              c.flag("ADDON_USER_HAS_KILLED_CARMAN", 0);

              c.flag("ADDON_USER_SPAWNED_CARMAN", 0);

              c.sayKey("npc.sentry.reward.given");

            } else c.sayKey("npc.sentry.reward.none");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.sentry.work");

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if (!"accept".equals(state)) return false;

          if (yes) {

            c.sayKey("npc.sentry.accept");

            c.summon("CARMANSPAWNER", 1663, 1040, 0);

            c.flag("ADDON_USER_SPAWNED_CARMAN", 1);

            c.globalFlag("ADDON_CARMAN_PRESENT", 1);

          } else c.sayKey("npc.sentry.decline");

          return true;
        }
      };

  public WindhowlSentry(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
