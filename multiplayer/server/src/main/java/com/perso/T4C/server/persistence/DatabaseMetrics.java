package com.perso.T4C.server.persistence;

import com.zaxxer.hikari.metrics.IMetricsTracker;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.metrics.PoolStats;

/**
 * Fixed-cardinality database operation measurements taken from the owned pool (T4C-0343). Every store operation
 * borrows one pooled connection, runs its SQL and returns it, so the borrow-to-return time is the database operation
 * latency as the server experiences it (SQL, transaction and driver round trips, in whole milliseconds as reported by
 * the pool), and the acquire time is the pool wait. Timeouts are acquisitions that failed. There is no per-operation
 * label, SQL text, identifier or history; histograms use fixed bounds and overflow into the last bucket.
 */
public final class DatabaseMetrics implements MetricsTrackerFactory {
  /** Upper bounds in milliseconds; the final bucket is everything above the last bound. */
  public static final long[] UPPER_MILLIS = {1, 5, 20, 100, 500, 2_000};

  public record Snapshot(long acquired, long acquireTotalNanos, long acquireMaxNanos, long[] acquireBuckets,
      long operations, long operationTotalMillis, long operationMaxMillis, long[] operationBuckets,
      long acquireTimeouts, long connectionsCreated, long connectionCreateTotalMillis,
      int poolActive, int poolIdle, int poolPending, int poolTotal, int poolMaximum, long[] upperBoundsMillis) {}

  private long acquired, acquireTotalNanos, acquireMaxNanos, operations, operationTotalMillis, operationMaxMillis,
      timeouts, created, createdTotalMillis;
  private final long[] acquireBuckets = new long[UPPER_MILLIS.length + 1], operationBuckets = new long[UPPER_MILLIS.length + 1];
  private volatile PoolStats stats;

  @Override public IMetricsTracker create(String poolName, PoolStats poolStats) {
    stats = poolStats;
    return new IMetricsTracker() {
      @Override public void recordConnectionAcquiredNanos(long nanos) { acquire(nanos); }
      @Override public void recordConnectionUsageMillis(long millis) { operation(millis); }
      @Override public void recordConnectionTimeout() { timeout(); }
      @Override public void recordConnectionCreatedMillis(long millis) { create(millis); }
    };
  }

  private static int bucket(long millis) {
    for (int i = 0; i < UPPER_MILLIS.length; i++) if (millis <= UPPER_MILLIS[i]) return i;
    return UPPER_MILLIS.length;
  }

  synchronized void acquire(long nanos) {
    nanos = Math.max(0, nanos);
    acquired = add(acquired, 1); acquireTotalNanos = add(acquireTotalNanos, nanos);
    acquireMaxNanos = Math.max(acquireMaxNanos, nanos);
    acquireBuckets[bucket(nanos / 1_000_000L)]++;
  }

  synchronized void operation(long millis) {
    millis = Math.max(0, millis);
    operations = add(operations, 1); operationTotalMillis = add(operationTotalMillis, millis);
    operationMaxMillis = Math.max(operationMaxMillis, millis);
    operationBuckets[bucket(millis)]++;
  }

  synchronized void timeout() { timeouts = add(timeouts, 1); }

  synchronized void create(long millis) { created = add(created, 1); createdTotalMillis = add(createdTotalMillis, Math.max(0, millis)); }

  private static long add(long a, long b) { long s = a + b; return s < 0 ? Long.MAX_VALUE : s; }

  public synchronized Snapshot snapshot() {
    var pool = stats;
    return new Snapshot(acquired, acquireTotalNanos, acquireMaxNanos, acquireBuckets.clone(),
        operations, operationTotalMillis, operationMaxMillis, operationBuckets.clone(),
        timeouts, created, createdTotalMillis,
        pool == null ? -1 : pool.getActiveConnections(), pool == null ? -1 : pool.getIdleConnections(),
        pool == null ? -1 : pool.getPendingThreads(), pool == null ? -1 : pool.getTotalConnections(),
        pool == null ? -1 : pool.getMaxConnections(), UPPER_MILLIS.clone());
  }
}
