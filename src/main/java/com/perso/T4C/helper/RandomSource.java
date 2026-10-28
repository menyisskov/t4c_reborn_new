package com.perso.T4C.helper;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.random.RandomGenerator;

/**
 * The one random source behind spell damage, heal and buff resolution.
 *
 * <p>By default {@link #current()} is exactly {@link ThreadLocalRandom#current()}, so live play
 * draws the same values, the same number of times, in the same order as before this seam existed. A
 * test or the server can scope a seeded generator to the current thread with {@link
 * #use(RandomGenerator)} (try-with-resources); the override never reaches other threads and is
 * restored on close, including when scopes nest.
 *
 * <p>Fixed draw order for one spell resolution (the order the code consumes the generator, which
 * the golden tests pin): (1) the cast success roll in {@code SpellCastingService} / {@code
 * SpellCastRules}; (2) per target in {@code SpellEffectManager.impactFor}: the damage or heal
 * amount (a heal formula's dice left to right, else the min..max fallback), then each effect in
 * authored order (dispel chance roll); (3) the caster's own {@code Player.applyHeal} amount; (4)
 * buff/proc hook chance rolls in {@code SpellEffectManager} in hook registration order. Dice inside
 * a formula are drawn left to right, one {@code nextInt(faces)} per die.
 *
 * <p>Cost: while no override is active (the normal case) {@link #current()} reads one atomic int
 * and returns the thread-local generator, with no {@code ThreadLocal} lookup.
 */
public final class RandomSource {
  private static final AtomicInteger ACTIVE = new AtomicInteger();
  private static final ThreadLocal<RandomGenerator> OVERRIDE = new ThreadLocal<>();

  private RandomSource() {}

  /** The generator spell resolution must draw from on the calling thread. */
  public static RandomGenerator current() {
    if (ACTIVE.get() != 0) {
      RandomGenerator override = OVERRIDE.get();
      if (override != null) return override;
    }
    return ThreadLocalRandom.current();
  }

  /** Scopes {@code generator} to the calling thread until the returned handle is closed. */
  public static Scope use(RandomGenerator generator) {
    Objects.requireNonNull(generator, "generator");
    RandomGenerator previous = OVERRIDE.get();
    OVERRIDE.set(generator);
    ACTIVE.incrementAndGet();
    return new Scope(previous, Thread.currentThread());
  }

  /** Restores the previous override when closed; must be closed on the thread that opened it. */
  public static final class Scope implements AutoCloseable {
    private final RandomGenerator previous;
    private final Thread owner;
    private boolean closed;

    private Scope(RandomGenerator previous, Thread owner) {
      this.previous = previous;
      this.owner = owner;
    }

    @Override
    public void close() {
      if (closed) return;
      if (Thread.currentThread() != owner) {
        throw new IllegalStateException("RandomSource scope closed on a different thread");
      }
      closed = true;
      if (previous == null) OVERRIDE.remove();
      else OVERRIDE.set(previous);
      ACTIVE.decrementAndGet();
    }
  }
}
