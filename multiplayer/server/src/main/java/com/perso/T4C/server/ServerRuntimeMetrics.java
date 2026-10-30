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
      SimulationLoop.MetricsSnapshot simulation, HttpTrafficMetrics.Snapshot httpTraffic,
      GameSocketTraffic.Snapshot gameSocket, com.perso.T4C.server.persistence.DatabaseMetrics.Snapshot database) {}

  private final SimulationLoop simulation;
  private final HttpTrafficMetrics httpTraffic;
  private final GameSocketTraffic gameSocket;
  private final java.util.function.Supplier<com.perso.T4C.server.persistence.DatabaseMetrics.Snapshot> database;
  private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
  private final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
  private final OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();

  public ServerRuntimeMetrics(SimulationLoop simulation) {
    this(simulation, new HttpTrafficMetrics());
  }

  ServerRuntimeMetrics(SimulationLoop simulation, HttpTrafficMetrics httpTraffic) {
    this(simulation, httpTraffic, new GameSocketTraffic(), () -> null);
  }

  ServerRuntimeMetrics(SimulationLoop simulation, HttpTrafficMetrics httpTraffic, GameSocketTraffic gameSocket,
      java.util.function.Supplier<com.perso.T4C.server.persistence.DatabaseMetrics.Snapshot> database) {
    this.simulation = Objects.requireNonNull(simulation);
    this.httpTraffic = Objects.requireNonNull(httpTraffic);
    this.gameSocket = Objects.requireNonNull(gameSocket);
    this.database = Objects.requireNonNull(database);
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
        nonHeap.getUsed(), nonHeap.getCommitted(), nonHeap.getMax(), simulation.metrics(),
        httpTraffic.snapshot(), gameSocket.snapshot(), database.get());
  }
}
