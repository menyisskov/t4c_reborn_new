package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.mirror.HourglassTrials;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// T4C-0048, "The Hourglass Trials": stands a few steps from MirrorwardenYsmera (1736,1840) in the
// Colosseum - where she asks who you were, Osric only asks how fast you are now. Summons a
// Sandglass Sentinel (assets/monsters/sandglass_sentinel_*.json), a fixed-HP, fixed-stat opponent
// - unlike the Mirror's Echo, it never scales to the player, so a best time actually measures
// something. Tier rises with rebirths (HourglassTrials.tierFor). See mirror/HourglassTrials.java
// for the timing/best-time bookkeeping and MainGameScreen.handleHourglassTrialDeath for where a
// win is actually recorded.
@Spawn(type = "TrialWardenOsric", x = 1740, y = 1836, z = 0, stationary = true, aggressive = false)
public final class TrialWardenOsric extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "TrialWardenOsric";
  public static final String DISPLAY_NAME = "${npc.trialwardenosric}";
  public static final String SPRITE_BASE = null;

  private static final NpcSpec.DialogueTopic TRIAL_TOPIC =
      new NpcSpec.DialogueTopic(
          List.of(
              "${npc.topic_keyword.trialwardenosric.0.0}",
              "${npc.topic_keyword.trialwardenosric.0.1}"),
          "${npc.topic.trialwardenosric.0}",
          List.of());

  private static final NpcSpec.DialogueTopic BEST_TIMES_TOPIC =
      new NpcSpec.DialogueTopic(
          List.of("${npc.topic_keyword.trialwardenosric.1.0}"),
          "${npc.topic.trialwardenosric.1}",
          List.of());

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.trialwardenosric}",
          List.of(TRIAL_TOPIC, BEST_TIMES_TOPIC),
          "TrialWardenOsricNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public TrialWardenOsric(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {
    return new NpcBehavior() {
      @Override
      public boolean onKeyword(NpcBehaviorContext context, String keyword) {
        if (ScriptedNpc.matches(TRIAL_TOPIC, keyword)) {
          startTrial(context);
          return true;
        }
        if (ScriptedNpc.matches(BEST_TIMES_TOPIC, keyword)) {
          reportBestTimes(context);
          return true;
        }
        return false;
      }
    };
  }

  private static void startTrial(NpcBehaviorContext context) {
    Player player = context.player();
    if (HourglassTrials.hasActiveTrial(player)) {
      context.say(I18n.resolve("${npc.trialwardenosric.already_running}"));
      return;
    }
    int tier = HourglassTrials.tierFor(player);
    String monsterName = HourglassTrials.monsterNameForTier(tier);
    boolean summoned = context.summon(monsterName, context.npcTileX(), context.npcTileY(), 0);
    if (!summoned) {
      context.say(I18n.resolve("${npc.trialwardenosric.summon_failed}"));
      return;
    }
    HourglassTrials.start(player, tier);
    int best = HourglassTrials.bestTimeMillis(player, tier);
    context.say(
        best > 0
            ? I18n.message(
                "message.hourglass_trial_started_with_best",
                tier,
                HourglassTrials.formatMillis(best))
            : I18n.message("message.hourglass_trial_started", tier));
  }

  private static void reportBestTimes(NpcBehaviorContext context) {
    Player player = context.player();
    StringBuilder sb = new StringBuilder(I18n.resolve("${npc.trialwardenosric.best_times_header}"));
    for (int tier = HourglassTrials.MIN_TIER; tier <= HourglassTrials.MAX_TIER; tier++) {
      int best = HourglassTrials.bestTimeMillis(player, tier);
      sb.append('\n')
          .append(
              I18n.message(
                  "message.hourglass_trial_best_line",
                  tier,
                  best > 0
                      ? HourglassTrials.formatMillis(best)
                      : I18n.resolve("${npc.trialwardenosric.no_time_yet}")));
    }
    context.say(sb.toString());
  }
}
