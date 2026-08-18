package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.Vector2;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class TameChannelTest {
  @Test
  void completesAtExactDuration() {
    Vector2 p = new Vector2(1, 2);
    TameChannel channel = new TameChannel(5f, 4f, () -> p, () -> false, () -> false);
    assertEquals(TameChannel.Outcome.RUNNING, channel.update(4.99f));
    assertEquals(TameChannel.Outcome.SUCCESS, channel.update(.01f));
    assertEquals(1f, channel.getProgress());
  }

  @Test
  void interruptionsAreDetectedInPriorityOrder() {
    Vector2 p = new Vector2();
    AtomicBoolean stunned = new AtomicBoolean(true);
    TameChannel channel = new TameChannel(5f, 4f, () -> p, stunned::get, () -> true);
    p.x = 10;
    assertEquals(TameChannel.Outcome.INTERRUPTED_STUNNED, channel.update(1f));
  }

  @Test
  void movementTargetLossAndCancellationInterrupt() {
    Vector2 moved = new Vector2();
    TameChannel movement = new TameChannel(5f, 4f, () -> moved, () -> false, () -> false);
    moved.x = 5;
    assertEquals(TameChannel.Outcome.INTERRUPTED_MOVED, movement.update(0));
    TameChannel lost = new TameChannel(5f, 4f, Vector2::new, () -> false, () -> true);
    assertEquals(TameChannel.Outcome.INTERRUPTED_TARGET_LOST, lost.update(0));
    TameChannel cancelled = new TameChannel(5f, 4f, Vector2::new, () -> false, () -> false);
    cancelled.cancel();
    assertEquals(TameChannel.Outcome.INTERRUPTED_CANCELLED, cancelled.update(0));
  }
}
