package com.perso.T4C.server;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.LongConsumer;

/** Single-owner fixed-step loop. Simulation exceptions propagate rather than silently losing ticks. */
public final class SimulationLoop implements AutoCloseable {
  public record Tick(long number, double deltaSeconds) {}
  /** Nanoseconds are durations, not wall-clock timestamps. Failed callbacks are attempted, not completed ticks. */
  public record MetricsSnapshot(long stepNanos,long completedTicks,long failedTicks,long measuredTicks,
      long totalTickNanos,long maxTickNanos,long lastTickNanos,long overBudgetTicks,
      long lateStarts,long totalStartLatenessNanos,long maxStartLatenessNanos,
      long lastDeadlineDebtNanos,long maxDeadlineDebtNanos,long deadlineResets,long discardedScheduleNanos,
      long elapsedNanos,boolean running,boolean consistent) {}


  private final long stepNanos;
  private final double deltaSeconds;
  private final Consumer<Tick> simulation;
  private final LongSupplier clock;
  private final LongConsumer testWait;
  private volatile long metricsVersion;
  private volatile long failedTicks,measuredTicks,totalTickNanos,maxTickNanos,lastTickNanos,overBudgetTicks;
  private volatile long lateStarts,totalStartLatenessNanos,maxStartLatenessNanos,lastDeadlineDebtNanos,maxDeadlineDebtNanos;
  private volatile long deadlineResets,discardedScheduleNanos,startedAt,endedAt;
  private volatile boolean metricsStarted,finished;

  private final AtomicBoolean started = new AtomicBoolean();
  private volatile boolean stopping;
  private volatile Thread owner;
  private volatile long completedTicks;

  public SimulationLoop(int ticksPerSecond, Consumer<Tick> simulation) {
    this(ticksPerSecond,simulation,System::nanoTime,null);
  }

  /** Test seam only; production always uses monotonic system time and interruptible parking. */
  SimulationLoop(int ticksPerSecond,Consumer<Tick> simulation,LongSupplier clock,LongConsumer testWait) {
    if (ticksPerSecond < 1 || ticksPerSecond > 1000) {
      throw new IllegalArgumentException("Tick rate must be between 1 and 1000");
    }
    stepNanos = 1_000_000_000L / ticksPerSecond;
    deltaSeconds = 1d / ticksPerSecond;
    this.simulation = Objects.requireNonNull(simulation, "simulation");
    this.clock=Objects.requireNonNull(clock);this.testWait=testWait;
  }

  /** Runs on the calling thread, once per instance; zero ticks is a valid no-op smoke run. */
  public void run(long maximumTicks) {
    if (maximumTicks < 0) throw new IllegalArgumentException("Tick count cannot be negative");
    if (!started.compareAndSet(false, true)) throw new IllegalStateException("Loop already started");
    owner = Thread.currentThread();
    long deadline = clock.getAsLong();
    metricsVersion++;startedAt=deadline;metricsStarted=true;metricsVersion++;
    try {
      while (!stopping && !owner.isInterrupted() && completedTicks < maximumTicks) {
        long tickStart=clock.getAsLong();
        long remaining = deadline - tickStart;
        if (remaining > 0) {
          if(testWait==null) LockSupport.parkNanos(this,remaining);else testWait.accept(remaining);
          continue;
        }
        boolean succeeded=false;
        try { simulation.accept(new Tick(completedTicks + 1, deltaSeconds));succeeded=true; }
        finally {
          long tickEnd=clock.getAsLong(),duration=Math.max(0,tickEnd-tickStart),lateness=Math.max(0,tickStart-deadline);
          metricsVersion++;
          measuredTicks++;lastTickNanos=duration;totalTickNanos=saturatedAdd(totalTickNanos,duration);maxTickNanos=Math.max(maxTickNanos,duration);
          if(duration>stepNanos) overBudgetTicks++;
          if(lateness>0) lateStarts++;
          totalStartLatenessNanos=saturatedAdd(totalStartLatenessNanos,lateness);maxStartLatenessNanos=Math.max(maxStartLatenessNanos,lateness);
          if(succeeded) completedTicks++;else failedTicks++;
          metricsVersion++;
        }
        deadline += stepNanos;
        // Existing catch-up policy: no oversized delta and no fictitious completed gameplay ticks.
        long now=clock.getAsLong(),debt=Math.max(0,now-deadline);
        metricsVersion++;lastDeadlineDebtNanos=debt;maxDeadlineDebtNanos=Math.max(maxDeadlineDebtNanos,debt);
        if (now - deadline > stepNanos * 4) {
          deadlineResets++;discardedScheduleNanos=saturatedAdd(discardedScheduleNanos,now+stepNanos-deadline);
          deadline = now + stepNanos;
        }
        metricsVersion++;
      }
    } finally {
      metricsVersion++;endedAt=clock.getAsLong();finished=true;metricsVersion++;
      owner = null;
    }
  }

  public long completedTicks() {
    return completedTicks;
  }

  private static long saturatedAdd(long value,long increment) {
    return increment>Long.MAX_VALUE-value ? Long.MAX_VALUE : value+increment;
  }

  /** Bounded seqlock reads: sampling cannot wait for or block the simulation owner. */
  public MetricsSnapshot metrics() {
    MetricsSnapshot result=null;
    for(int attempt=0;attempt<3;attempt++) {
      long before=metricsVersion;
      long elapsed=metricsStarted ? Math.max(0,(finished ? endedAt : clock.getAsLong())-startedAt) : 0;
      result=new MetricsSnapshot(stepNanos,completedTicks,failedTicks,measuredTicks,totalTickNanos,maxTickNanos,lastTickNanos,
          overBudgetTicks,lateStarts,totalStartLatenessNanos,maxStartLatenessNanos,lastDeadlineDebtNanos,maxDeadlineDebtNanos,
          deadlineResets,discardedScheduleNanos,elapsed,metricsStarted && !finished,false);
      if((before&1)==0 && before==metricsVersion) return new MetricsSnapshot(result.stepNanos(),result.completedTicks(),result.failedTicks(),result.measuredTicks(),
          result.totalTickNanos(),result.maxTickNanos(),result.lastTickNanos(),result.overBudgetTicks(),result.lateStarts(),result.totalStartLatenessNanos(),
          result.maxStartLatenessNanos(),result.lastDeadlineDebtNanos(),result.maxDeadlineDebtNanos(),result.deadlineResets(),result.discardedScheduleNanos(),
          result.elapsedNanos(),result.running(),true);
    }
    return result;
  }

  /** May be called from another thread; wake a sleeping loop immediately. */
  @Override
  public void close() {
    stopping = true;
    Thread running = owner;
    if (running != null) LockSupport.unpark(running);
  }
}
