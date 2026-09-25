package com.perso.T4C.monster;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Shared, in-memory encounter state for a Warband camp (T4C-0045). A camp's raiders and its
 * banner-bearer are ordinary respawning {@code @Spawn} monsters (see {@link WarbandRaider},
 * {@link WarbandBannerBearer}) - this class tracks the *encounter*, not the monster instances
 * themselves, the same way {@code EchoOfSelf} tracks its own trial state statically.
 *
 * <p>The mechanic: killing the banner-bearer opens a scatter window. Only raiders killed inside
 * that window count toward clearing the camp - so the banner-bearer has to fall first. Clearing
 * the whole band (banner-bearer, then every raider, within the window) is the cue - read by
 * {@code MainGameScreen}'s monster-death handling - to summon the (pure-JSON, on-demand)
 * {@value #WARLORD_NAME} monster (see {@code assets/monsters/warband_warlord.json}).
 */
public final class WarbandCampState {
  /** {@code MonsterRegistry} name of the on-demand boss summoned when a camp is cleared. */
  public static final String WARLORD_NAME = "Warband Warlord";

  public enum Camp {
    /** Windhowl Marches, level ~120 Centaur war party (see WindhowlMarches zone, T4C-0005). */
    WINDHOWL_WAR_PARTY(5, 120_000L, 600_000L);

    final int raiderCount;
    final long scatterWindowMillis;
    final long warlordCooldownMillis;

    Camp(int raiderCount, long scatterWindowMillis, long warlordCooldownMillis) {
      this.raiderCount = raiderCount;
      this.scatterWindowMillis = scatterWindowMillis;
      this.warlordCooldownMillis = warlordCooldownMillis;
    }
  }

  private static final class State {
    final AtomicLong bannerDownUntil = new AtomicLong(0L);
    final AtomicInteger raidersFelledWhileScattered = new AtomicInteger(0);
    final AtomicLong warlordCooldownUntil = new AtomicLong(0L);
  }

  private static final Map<Camp, State> STATE = new ConcurrentHashMap<>();

  private WarbandCampState() {}

  private static State stateOf(Camp camp) {
    return STATE.computeIfAbsent(camp, c -> new State());
  }

  /** True while this camp's banner-bearer is down and its raiders are still scattered. */
  public static boolean isScattered(Camp camp) {
    return System.currentTimeMillis() < stateOf(camp).bannerDownUntil.get();
  }

  /** Call when the camp's banner-bearer dies: opens the scatter window. */
  public static void bannerFell(Camp camp) {
    State s = stateOf(camp);
    s.bannerDownUntil.set(System.currentTimeMillis() + camp.scatterWindowMillis);
    s.raidersFelledWhileScattered.set(0);
  }

  /**
   * Call when one of the camp's raiders dies. Returns {@code true} exactly once per scatter
   * window - the instant the last raider falls while the band is still scattered - which is the
   * caller's cue to summon the warlord. A raider felled outside the window (banner-bearer still
   * standing, or the window already lapsed) never counts.
   */
  public static boolean raiderFellAndCampJustCleared(Camp camp) {
    if (!isScattered(camp)) return false;
    State s = stateOf(camp);
    int felled = s.raidersFelledWhileScattered.incrementAndGet();
    if (felled < camp.raiderCount) return false;
    long now = System.currentTimeMillis();
    if (now < s.warlordCooldownUntil.get()) return false;
    s.warlordCooldownUntil.set(now + camp.warlordCooldownMillis);
    s.bannerDownUntil.set(0L);
    return true;
  }
}
