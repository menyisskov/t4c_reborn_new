package com.perso.T4C.harvest;

import com.badlogic.gdx.math.Vector2;
import java.util.function.Supplier;

public final class HarvestChannel {
  private final float duration;
  private float elapsed;
  private boolean cancelled;
  private final float moveToleranceSquared;
  private final Supplier<Vector2> position;
  private final Vector2 startPosition;

  public HarvestChannel(float duration) {
    this(duration, 0f, null);
  }

  public HarvestChannel(float duration, float moveTolerance, Supplier<Vector2> position) {
    this.duration = Math.max(0f, duration);
    this.moveToleranceSquared = Math.max(0f, moveTolerance) * Math.max(0f, moveTolerance);
    this.position = position;
    Vector2 initial = position == null ? null : position.get();
    this.startPosition = initial == null ? null : new Vector2(initial);
  }

  public boolean update(float delta) {
    if (cancelled) return false;
    Vector2 current = position == null ? null : position.get();
    if (startPosition != null
        && (current == null || current.dst2(startPosition) > moveToleranceSquared)) {
      cancelled = true;
      return false;
    }
    elapsed += Math.max(0f, delta);
    return elapsed >= duration;
  }

  public void cancel() {
    cancelled = true;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public float getProgress() {
    return duration <= 0f ? 1f : Math.min(1f, elapsed / duration);
  }
}
