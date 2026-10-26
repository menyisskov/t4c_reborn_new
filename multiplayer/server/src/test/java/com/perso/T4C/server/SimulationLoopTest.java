package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SimulationLoopTest {
  @Test
  void finiteRunAdvancesOnlyFixedSimulationSteps() {
    var ticks = new ArrayList<SimulationLoop.Tick>();
    try (var loop = new SimulationLoop(34, ticks::add)) {
      loop.run(3);
      assertEquals(3, loop.completedTicks());
      assertEquals(3, ticks.size());
      for (int i = 0; i < ticks.size(); i++) {
        assertEquals(i + 1, ticks.get(i).number());
        assertEquals(1d / 34, ticks.get(i).deltaSeconds());
      }
    }
  }

  @Test
  void shutdownWakesTheLoopWithoutAnotherSimulationStep() throws Exception {
    var ticked = new CountDownLatch(1);
    try (var executor = Executors.newSingleThreadExecutor();
        var loop = new SimulationLoop(1, tick -> ticked.countDown())) {
      var task = executor.submit(() -> loop.run(Long.MAX_VALUE));
      try {
        assertTrue(ticked.await(5, TimeUnit.SECONDS));
        loop.close();
        task.get(2, TimeUnit.SECONDS);
        assertEquals(1, loop.completedTicks());
      } finally {
        loop.close();
      }
    }
  }

  @Test
  void simulationFailureStopsTheOwnerAndIsNotCountedAsCommittedTime() {
    var failure = new IllegalStateException("broken world");
    try (var loop =
        new SimulationLoop(
            34,
            tick -> {
              throw failure;
            })) {
      assertSame(failure, assertThrows(IllegalStateException.class, () -> loop.run(10)));
      assertEquals(0, loop.completedTicks());
    }
  }
}
