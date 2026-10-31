package com.perso.T4C.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.world.MovementReconciler;
import com.perso.T4C.world.MovementRules;
import org.junit.jupiter.api.Test;

class RemoteMovementRuleTest {
  private static final MovementRules.Terrain OPEN = new MovementRules.Terrain() {
    @Override public boolean hasCollision(float x, float y) { return false; }
    @Override public boolean hasPlayerCollision(float x, float y) { return false; }
    @Override public boolean canMoveForPlayer(float x, float y, float toX, float toY) { return true; }
    @Override public boolean isPlayerPassableTile(int x, int y) { return false; }
  };

  @Test
  void reversalAndReleasedInputFinishTheReservedTile() {
    var rule = new RemoteMovementRule(OPEN, () -> true, () -> null);
    var at = new MovementReconciler.Position(0, 0);
    at = rule.apply(at, new MovementReconciler.Input(0, 1, 0, 0.05));
    assertEquals(6.4, at.x(), 0.001);
    at = rule.apply(at, new MovementReconciler.Input(1, -1, 0, 0.05));
    assertEquals(12.8, at.x(), 0.001);
    at = rule.apply(at, new MovementReconciler.Input(2, 0, 0, 0.05));
    assertEquals(19.2, at.x(), 0.001);
  }

  @Test
  void replayStartsWithTheServersUnfinishedReservation() {
    var rule = new RemoteMovementRule(OPEN, () -> true, () -> null);
    var reconciler = new MovementReconciler(rule, new MovementReconciler.Position(0, 0), 0);
    reconciler.predict(1, 0, 0.05);
    reconciler.predict(-1, 0, 0.05);
    var correction = reconciler.acknowledge(
        0, new MovementReconciler.Position(6.4, 0),
        new MovementRules.Reservation(32, 0, 1, 0));
    assertEquals(12.8, correction.position().x(), 0.001);
    var next = reconciler.predict(0, 0, 0.05);
    assertEquals(2, next.sequence());
    assertEquals(19.2, reconciler.predicted().x(), 0.001);
  }
}
