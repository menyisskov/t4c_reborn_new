package com.perso.T4C.world;

import java.util.ArrayDeque;
import java.util.Objects;

/**
 * Client-side prediction bookkeeping for the player's own movement, with no engine and no network.
 * The client applies each input with a movement rule at once and remembers it under a sequence
 * number. When a snapshot arrives carrying the last sequence the server applied and the server's
 * position, the reconciler drops the acknowledged inputs, restarts from the server position and
 * replays the rest with the same rule. A remote session will feed it; nothing uses it yet.
 *
 * <p>Both sides must use one rule for the replay to agree (see {@link MovementRules}); with
 * different rules the correction simply becomes large and snaps.
 */
public final class MovementReconciler {
  /** Errors at or above this many tiles snap; smaller ones are eased in by the renderer. */
  public static final double SNAP_DISTANCE_TILES = 1.0;

  public record Position(double x, double y) {
    double distance(Position other) {
      return Math.hypot(x - other.x, y - other.y);
    }
  }

  /** One frame of input as sent to the server. */
  public record Input(long sequence, int dx, int dy, double dt) {}

  /** The movement rule for one input; must be the same rule the server applies. */
  @FunctionalInterface
  public interface Rule {
    Position apply(Position from, Input input);
  }

  /** What a reconciliation changed: how far the prediction moved and whether to snap. */
  public record Correction(double error, boolean snap, Position position) {}

  private final Rule rule;
  private final ArrayDeque<Input> pending = new ArrayDeque<>();
  private Position predicted;
  private long nextSequence;
  private long lastAcknowledged = -1;

  public MovementReconciler(Rule rule, Position start, long firstSequence) {
    this.rule = Objects.requireNonNull(rule, "rule");
    this.predicted = Objects.requireNonNull(start, "start");
    this.nextSequence = firstSequence;
  }

  /** Applies a new input locally, remembers it, and returns it so the caller can send it. */
  public Input predict(int dx, int dy, double dt) {
    Input input = new Input(nextSequence++, dx, dy, dt);
    pending.addLast(input);
    predicted = rule.apply(predicted, input);
    return input;
  }

  /**
   * Reconciles with a snapshot. Acknowledgements that go backwards are ignored (a late snapshot).
   */
  public Correction acknowledge(long appliedSequence, Position serverPosition) {
    if (appliedSequence < lastAcknowledged) {
      return new Correction(0, false, predicted);
    }
    lastAcknowledged = appliedSequence;
    while (!pending.isEmpty() && pending.peekFirst().sequence() <= appliedSequence) {
      pending.pollFirst();
    }
    Position before = predicted;
    Position replayed = serverPosition;
    for (Input input : pending) {
      replayed = rule.apply(replayed, input);
    }
    predicted = replayed;
    double error = before.distance(replayed);
    return new Correction(error, error >= SNAP_DISTANCE_TILES, replayed);
  }

  /** Teleports, respawns and map changes: forget the history and continue from here. */
  public void reset(Position position) {
    pending.clear();
    predicted = Objects.requireNonNull(position, "position");
  }

  public Position predicted() {
    return predicted;
  }

  public int unacknowledged() {
    return pending.size();
  }

  public long nextSequence() {
    return nextSequence;
  }
}
