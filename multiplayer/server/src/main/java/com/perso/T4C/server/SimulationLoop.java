package com.perso.T4C.server;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

/** Single-owner fixed-step loop. Simulation exceptions propagate rather than silently losing ticks. */
public final class SimulationLoop implements AutoCloseable {
  public record Tick(long number, double deltaSeconds) {}

  private final long stepNanos;
  private final double deltaSeconds;
  private final Consumer<Tick> simulation;
  private final AtomicBoolean started = new AtomicBoolean();
  private volatile boolean stopping;
  private volatile Thread owner;
  private volatile long completedTicks;

  public SimulationLoop(int ticksPerSecond, Consumer<Tick> simulation) {
    if (ticksPerSecond < 1 || ticksPerSecond > 1000) {
      throw new IllegalArgumentException("Tick rate must be between 1 and 1000");
    }
    stepNanos = 1_000_000_000L / ticksPerSecond;
    deltaSeconds = 1d / ticksPerSecond;
    this.simulation = Objects.requireNonNull(simulation, "simulation");
  }

  /** Runs on the calling thread, once per instance; zero ticks is a valid no-op smoke run. */
  public void run(long maximumTicks) {
    if (maximumTicks < 0) throw new IllegalArgumentException("Tick count cannot be negative");
    if (!started.compareAndSet(false, true)) throw new IllegalStateException("Loop already started");
    owner = Thread.currentThread();
    long deadline = System.nanoTime();
    try {
      while (!stopping && !owner.isInterrupted() && completedTicks < maximumTicks) {
        long remaining = deadline - System.nanoTime();
        if (remaining > 0) {
          LockSupport.parkNanos(this, remaining);
          continue;
        }
        simulation.accept(new Tick(completedTicks + 1, deltaSeconds));
        completedTicks++;
        deadline += stepNanos;
        // Limit catch-up after a long stall. Simulated time advances only for executed ticks;
        // never turn a scheduling pause into an oversized gameplay delta.
        long now = System.nanoTime();
        if (now - deadline > stepNanos * 4) deadline = now + stepNanos;
      }
    } finally {
      owner = null;
    }
  }

  public long completedTicks() {
    return completedTicks;
  }

  /** May be called from another thread; wake a sleeping loop immediately. */
  @Override
  public void close() {
    stopping = true;
    Thread running = owner;
    if (running != null) LockSupport.unpark(running);
  }
}
