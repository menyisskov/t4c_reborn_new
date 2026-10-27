package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class SimulationLoopTest {
  @Test
  void finiteRunAdvancesOnlyFixedSimulationSteps() {
    var ticks = new ArrayList<SimulationLoop.Tick>();
    try (var loop = new SimulationLoop(34, ticks::add)) {
      loop.run(3);
      assertEquals(3, loop.completedTicks());
      assertEquals(3, ticks.size());
      var process = new ServerRuntimeMetrics(loop).snapshot();
      assertEquals(3, process.simulation().completedTicks());
      assertTrue(process.heapUsedBytes() >= 0);
      assertTrue(process.processCpuTimeNanos() == null || process.processCpuTimeNanos() >= 0);
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
    var clock = new AtomicLong();
    try (var loop =
        new SimulationLoop(
            34,
            tick -> {
              clock.addAndGet(7_000_000L);
              throw failure;
            }, clock::get, clock::addAndGet)) {
      assertSame(failure, assertThrows(IllegalStateException.class, () -> loop.run(10)));
      assertEquals(0, loop.completedTicks());
      var metrics=loop.metrics();
      assertEquals(1,metrics.failedTicks());
      assertEquals(1,metrics.measuredTicks());
      assertEquals(7_000_000L,metrics.totalTickNanos());
      assertFalse(metrics.running());
      assertTrue(metrics.consistent());
    }
  }
  @Test
  void stallMeasuresWorkAndSchedulerDebtWithoutInventingGameplayTicks() {
    var clock = new AtomicLong();
    var ticks = new ArrayList<SimulationLoop.Tick>();
    try(var loop=new SimulationLoop(10,tick -> {
      ticks.add(tick);
      clock.addAndGet(tick.number()==1 ? 600_000_000L : 10_000_000L);
    },clock::get,delay -> clock.addAndGet(delay+50_000_000L))) {
      loop.run(2);
      var metrics=loop.metrics();
      assertEquals(2,metrics.completedTicks());
      assertEquals(0,metrics.failedTicks());
      assertEquals(600_000_000L,metrics.maxTickNanos());
      assertEquals(610_000_000L,metrics.totalTickNanos());
      assertEquals(1,metrics.overBudgetTicks());
      assertEquals(500_000_000L,metrics.maxDeadlineDebtNanos());
      assertEquals(1,metrics.deadlineResets());
      assertEquals(600_000_000L,metrics.discardedScheduleNanos());
      assertEquals(50_000_000L,metrics.maxStartLatenessNanos());
      assertEquals(1,metrics.lateStarts());
      assertTrue(metrics.consistent());
      for(int i=0;i<ticks.size();i++) {
        assertEquals(i+1,ticks.get(i).number());
        assertEquals(0.1,ticks.get(i).deltaSeconds());
      }
    }
  }

}
