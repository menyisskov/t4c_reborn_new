package com.perso.T4C.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.world.MovementReconciler.Input;
import com.perso.T4C.world.MovementReconciler.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MovementReconcilerTest {
  private static final double PER_TICK = MovementRules.Swept.tilesPerTick(300f, 34);

  /** The server rule on open ground with an optional wall at x >= wallX. */
  private static MovementReconciler.Rule serverRule(double wallX) {
    return (from, in) -> {
      var p =
          MovementRules.Swept.step(
              from.x(), from.y(), in.dx(), in.dy(), PER_TICK, (x, y) -> x < wallX);
      return new Position(p.x(), p.y());
    };
  }

  @Test
  void whenTheServerAgreesNothingMovesAndAcknowledgedInputsAreForgotten() {
    var rule = serverRule(1000);
    var r = new MovementReconciler(rule, new Position(5, 5), 0);
    Position server = new Position(5, 5);
    List<Input> sent = new ArrayList<>();
    for (int i = 0; i < 10; i++) sent.add(r.predict(1, 0, 0.03));
    for (int i = 0; i < 6; i++) server = rule.apply(server, sent.get(i));
    var c = r.acknowledge(sent.get(5).sequence(), server);
    assertEquals(0.0, c.error(), 1e-9);
    assertFalse(c.snap());
    assertEquals(4, r.unacknowledged());
  }

  @Test
  void aServerWallPullsThePredictionBackAndReplaysTheRest() {
    var client = new MovementReconciler(serverRule(1000), new Position(5, 5), 100);
    var server = serverRule(5.5); // the client does not know about this wall
    Position serverPos = new Position(5, 5);
    List<Input> sent = new ArrayList<>();
    for (int i = 0; i < 8; i++) sent.add(client.predict(1, 0, 0.03));
    double predictedBefore = client.predicted().x();
    for (int i = 0; i < 5; i++) serverPos = server.apply(serverPos, sent.get(i));
    var c = client.acknowledge(sent.get(4).sequence(), serverPos);
    assertTrue(serverPos.x() < 5.5);
    assertTrue(c.error() > 0.5, "prediction was ahead of the server");
    assertTrue(client.predicted().x() < predictedBefore);
    // Three unacknowledged inputs were replayed on top of the server position.
    assertEquals(3, client.unacknowledged());
    assertEquals(serverPos.x() + 3 * PER_TICK, client.predicted().x(), 1e-9);
  }

  @Test
  void bigErrorsSnapSmallOnesDoNot() {
    var r = new MovementReconciler((p, in) -> new Position(p.x() + in.dx(), p.y()), new Position(0, 0), 0);
    var first = r.predict(1, 0, 0);
    var small = r.acknowledge(first.sequence(), new Position(0.4, 0));
    assertEquals(0.6, small.error(), 1e-9);
    assertFalse(small.snap());
    r.predict(1, 0, 0);
    var big = r.acknowledge(first.sequence(), new Position(-5, 0));
    assertTrue(big.snap());
  }

  @Test
  void aLateSnapshotIsIgnored() {
    var r = new MovementReconciler(serverRule(1000), new Position(5, 5), 0);
    var a = r.predict(1, 0, 0.03);
    var b = r.predict(1, 0, 0.03);
    r.acknowledge(b.sequence(), new Position(6, 5));
    Position kept = r.predicted();
    var late = r.acknowledge(a.sequence(), new Position(5.2, 5));
    assertEquals(0.0, late.error());
    assertEquals(kept, r.predicted());
  }

  @Test
  void replayingAgainstAMatchingServerStaysExactOverARandomRun() {
    var rule = serverRule(1000);
    var r = new MovementReconciler(rule, new Position(20, 20), 0);
    Position server = new Position(20, 20);
    Random random = new Random(9);
    List<Input> inFlight = new ArrayList<>();
    for (int frame = 0; frame < 600; frame++) {
      inFlight.add(r.predict(random.nextInt(3) - 1, random.nextInt(3) - 1, 0.03));
      if (frame % 3 == 0 && inFlight.size() > 2) {
        // The server has applied all but the last two, snapshots arrive with that delay.
        while (inFlight.size() > 2) {
          Input applied = inFlight.remove(0);
          server = rule.apply(server, applied);
          r.acknowledge(applied.sequence(), server);
        }
      }
    }
    // Everything the server has applied, replayed over it, equals what the client predicted.
    Position check = server;
    for (Input in : inFlight) check = rule.apply(check, in);
    assertEquals(check, r.predicted());
  }

  @Test
  void resetForgetsHistory() {
    var r = new MovementReconciler(serverRule(1000), new Position(1, 1), 0);
    r.predict(1, 0, 0.03);
    r.reset(new Position(50, 50));
    assertEquals(0, r.unacknowledged());
    assertEquals(new Position(50, 50), r.predicted());
    assertEquals(1, r.nextSequence());
  }
}
