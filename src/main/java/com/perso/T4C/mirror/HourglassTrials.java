package com.perso.T4C.mirror;

import com.perso.T4C.player.Player;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * T4C-0048: The Hourglass Trials. A personal speed challenge in the Colosseum, a few steps from
 * the Mirror of Echoes (T4C-0042) - where the Mirror asks "who were you", the Hourglass asks "how
 * fast are you now". Unlike the Mirror's Echo, which is built from the player's own stats and
 * scales to match them, each trial's **Sandglass Sentinel is a fixed-HP, fixed-stat opponent** -
 * five tiers (`assets/monsters/sandglass_sentinel_*.json`), same monster every time a given tier
 * is fought, so a best time actually means something. The tier a character faces rises with
 * rebirths (one tier every 10 rebirths, capped at the top tier) - a proven, many-times-reborn
 * character gets a harder clock to beat, the same "further along, harder challenge" shape the
 * Mirror's ten trials use.
 *
 * <p>Per-character best times are plain quest flags (milliseconds, well under the int range for
 * any real fight), same as every other piece of state this session's passes have added. The
 * in-flight timer, by contrast, does NOT need to survive a relog - a fight abandoned mid-way is
 * just abandoned - so it lives in a small in-memory map here, the same shape {@code
 * WarbandCampState} uses for its own non-persistent encounter state.
 */
public final class HourglassTrials {
  public static final int MIN_TIER = 1;
  public static final int MAX_TIER = 5;

  /** One rebirth-tier step every this many rebirths (see class doc). */
  private static final int REBIRTHS_PER_TIER = 10;

  private static final String[] MONSTER_NAMES = {
    "Sandglass Sentinel I",
    "Sandglass Sentinel II",
    "Sandglass Sentinel III",
    "Sandglass Sentinel IV",
    "Sandglass Sentinel V"
  };

  private static final Map<Player, Long> ACTIVE_TRIAL_STARTS = new ConcurrentHashMap<>();
  private static final Map<Player, Integer> ACTIVE_TRIAL_TIER = new ConcurrentHashMap<>();

  /** Test seam: real code always uses {@link System#currentTimeMillis()}. */
  private static LongSupplier clock = System::currentTimeMillis;

  static void useClock(LongSupplier testClock) {
    clock = testClock == null ? System::currentTimeMillis : testClock;
  }

  private HourglassTrials() {}

  /** The tier a character currently faces: 1 at 0 rebirths, +1 every 10 rebirths, capped at 5. */
  public static int tierFor(Player player) {
    int rebirths = player == null ? 0 : Math.max(0, player.getRebirthCount());
    return Math.min(MAX_TIER, MIN_TIER + rebirths / REBIRTHS_PER_TIER);
  }

  /** {@code MonsterRegistry} name of the Sandglass Sentinel for this tier (1..5, clamped). */
  public static String monsterNameForTier(int tier) {
    int index = Math.max(MIN_TIER, Math.min(MAX_TIER, tier)) - 1;
    return MONSTER_NAMES[index];
  }

  /** The tier a monster canonical name belongs to, or 0 if it isn't a Sandglass Sentinel. */
  public static int tierOfMonsterName(String canonicalName) {
    for (int i = 0; i < MONSTER_NAMES.length; i++) {
      if (MONSTER_NAMES[i].equalsIgnoreCase(canonicalName)) return i + 1;
    }
    return 0;
  }

  public static String bestTimeFlag(int tier) {
    return "hourglass.best_ms.tier" + Math.max(MIN_TIER, Math.min(MAX_TIER, tier));
  }

  /** Best time this character has ever posted at this tier, in milliseconds, or 0 if none yet. */
  public static int bestTimeMillis(Player player, int tier) {
    return player == null ? 0 : Math.max(0, player.getQuestFlag(bestTimeFlag(tier)));
  }

  /** True while {@code player} has a Sandglass Sentinel alive and running against the clock. */
  public static boolean hasActiveTrial(Player player) {
    return player != null && ACTIVE_TRIAL_STARTS.containsKey(player);
  }

  /** Call the moment the Sentinel is actually summoned - starts the clock. */
  public static void start(Player player, int tier) {
    if (player == null) return;
    ACTIVE_TRIAL_STARTS.put(player, clock.getAsLong());
    ACTIVE_TRIAL_TIER.put(player, Math.max(MIN_TIER, Math.min(MAX_TIER, tier)));
  }

  /** What finishing a trial produced: how long it took, the tier, and whether it's a new best. */
  public record Result(long elapsedMillis, int tier, boolean newBest) {}

  /**
   * Call when a Sandglass Sentinel dies. Stops the clock, records a new best if this run beat it,
   * and returns the result - or {@code null} if this player has no trial in flight (a Sentinel
   * that died to someone else's fight, or the player already relogged mid-trial).
   */
  public static Result finish(Player player) {
    if (player == null) return null;
    Long startedAt = ACTIVE_TRIAL_STARTS.remove(player);
    Integer tier = ACTIVE_TRIAL_TIER.remove(player);
    if (startedAt == null || tier == null) return null;
    long elapsed = Math.max(0L, clock.getAsLong() - startedAt);
    int previousBest = bestTimeMillis(player, tier);
    boolean newBest = previousBest == 0 || elapsed < previousBest;
    if (newBest) {
      player.setQuestFlag(bestTimeFlag(tier), (int) Math.min(Integer.MAX_VALUE, elapsed));
    }
    return new Result(elapsed, tier, newBest);
  }

  /** Clears an in-flight trial without recording a result (the player gave up, relogged, etc). */
  public static void abandon(Player player) {
    if (player == null) return;
    ACTIVE_TRIAL_STARTS.remove(player);
    ACTIVE_TRIAL_TIER.remove(player);
  }

  /** {@code mm:ss.ttt}, the way a speedrun time is normally shown. */
  public static String formatMillis(long millis) {
    long m = Math.max(0, millis);
    long minutes = m / 60_000L;
    long seconds = (m % 60_000L) / 1000L;
    long tenths = (m % 1000L) / 100L;
    return String.format(java.util.Locale.ROOT, "%d:%02d.%d", minutes, seconds, tenths);
  }
}
