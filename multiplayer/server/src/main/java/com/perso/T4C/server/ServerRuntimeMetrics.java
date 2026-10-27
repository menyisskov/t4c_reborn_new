package com.perso.T4C.server;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Objects;

/** On-demand process observations; no polling thread, identifiers, I/O, or forced collection. */
public final class ServerRuntimeMetrics {
  public record Snapshot(
      long sampledAtNanos, long processUptimeMillis, Long processCpuTimeNanos,
      int availableProcessors, long heapUsedBytes, long heapCommittedBytes, long heapMaxBytes,
      long nonHeapUsedBytes, long nonHeapCommittedBytes, long nonHeapMaxBytes,
      SimulationLoop.MetricsSnapshot simulation) {}

  private final SimulationLoop simulation;
  private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
  private final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
  private final OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();

  public ServerRuntimeMetrics(SimulationLoop simulation) {
    this.simulation = Objects.requireNonNull(simulation);
  }

  public Snapshot snapshot() {
    var heap = memory.getHeapMemoryUsage();
    var nonHeap = memory.getNonHeapMemoryUsage();
    Long cpu = null;
    if (operatingSystem instanceof com.sun.management.OperatingSystemMXBean extended) {
      long observed = extended.getProcessCpuTime();
      if (observed >= 0) cpu = observed;
    }
    return new Snapshot(System.nanoTime(), runtime.getUptime(), cpu,
        operatingSystem.getAvailableProcessors(), heap.getUsed(), heap.getCommitted(), heap.getMax(),
        nonHeap.getUsed(), nonHeap.getCommitted(), nonHeap.getMax(), simulation.metrics());
  }
}
