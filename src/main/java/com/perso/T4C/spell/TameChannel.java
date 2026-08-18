package com.perso.T4C.spell;

import com.badlogic.gdx.math.Vector2;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class TameChannel {
  public enum Outcome {
    RUNNING,
    SUCCESS,
    INTERRUPTED_MOVED,
    INTERRUPTED_STUNNED,
    INTERRUPTED_TARGET_LOST,
    INTERRUPTED_CANCELLED
  }

  private final Supplier<Vector2> position;
  private final BooleanSupplier stunned;
  private final BooleanSupplier targetLost;
  private final Vector2 start;
  private final float duration;
  private final float moveTolerance;
  private float elapsed;
  private boolean cancelled;
  private Outcome outcome = Outcome.RUNNING;

  public TameChannel(
      float duration,
      float moveTolerance,
      Supplier<Vector2> position,
      BooleanSupplier stunned,
      BooleanSupplier targetLost) {
    this.duration = Math.max(0f, duration);
    this.moveTolerance = Math.max(0f, moveTolerance);
    this.position = position;
    this.stunned = stunned;
    this.targetLost = targetLost;
    Vector2 initial = position == null ? null : position.get();
    start = initial == null ? new Vector2() : new Vector2(initial);
  }

  public Outcome update(float delta) {
    if (outcome != Outcome.RUNNING) return outcome;
    if (cancelled) return outcome = Outcome.INTERRUPTED_CANCELLED;
    if (stunned != null && stunned.getAsBoolean()) return outcome = Outcome.INTERRUPTED_STUNNED;
    Vector2 current = position == null ? null : position.get();
    if (current == null || current.dst(start) > moveTolerance)
      return outcome = Outcome.INTERRUPTED_MOVED;
    if (targetLost != null && targetLost.getAsBoolean())
      return outcome = Outcome.INTERRUPTED_TARGET_LOST;
    elapsed += Math.max(0f, delta);
    if (elapsed >= duration) outcome = Outcome.SUCCESS;
    return outcome;
  }

  public void cancel() {
    cancelled = true;
  }

  public float getProgress() {
    return duration <= 0f ? 1f : Math.min(1f, elapsed / duration);
  }
}
