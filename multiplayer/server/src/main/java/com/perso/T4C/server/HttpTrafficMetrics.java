package com.perso.T4C.server;

import java.util.ArrayList;
import java.util.List;

/** Fixed-cardinality application HTTP measurements; no request-derived label is retained. */
final class HttpTrafficMetrics {
  private static final long[] UPPER_NANOS = {
      1_000_000L, 5_000_000L, 20_000_000L, 100_000_000L,
      500_000_000L, 2_000_000_000L, 10_000_000_000L
  };

  private enum Route {
    ROOT("/"), CLIENT_JS("/client.js"), STYLE_CSS("/style.css"), STRINGS("/strings"),
    ACCOUNT_CHARACTERS("/account/characters"), MAP("/map"), STATE("/state"),
    CHARACTER("/character"), CORPSES("/corpses"), HEALTH("/health"),
    ADMIN_METRICS("/admin/metrics"), JOIN("/join"), ADMIN_SHUTDOWN("/admin/shutdown"),
    INPUT("/input"), ATTACK("/attack"), RECOVER("/recover"), EQUIP("/equip"),
    UNEQUIP("/unequip"), INTERACT("/interact"), PICKUP("/pickup"),
    REGION_READY("/region-ready"), LEAVE("/leave"), OTHER("other");

    final String label;
    Route(String label) { this.label = label; }
    static Route of(String path) {
      for (Route route : values()) if (route != OTHER && route.label.equals(path)) return route;
      return OTHER;
    }
  }

  private enum Method {
    GET, POST, OTHER;
    static Method of(String method) {
      if ("GET".equals(method)) return GET;
      if ("POST".equals(method)) return POST;
      return OTHER;
    }
  }

  private enum Status {
    UNSENT, S200, S400, S401, S403, S404, S405, S409, S413, S415, S426, S503, OTHER;
    static Status of(int code) {
      return switch (code) {
        case 200 -> S200;
        case 400 -> S400;
        case 401 -> S401;
        case 403 -> S403;
        case 404 -> S404;
        case 405 -> S405;
        case 409 -> S409;
        case 413 -> S413;
        case 415 -> S415;
        case 426 -> S426;
        case 503 -> S503;
        default -> OTHER;
      };
    }
    String label() { return this == UNSENT ? "unsent" : this == OTHER ? "other" : name().substring(1); }
  }

  public record Row(String route, String method, String selectedStatus, long completed,
      long requestBodyBytesRead, long responseBodyBytesFullyWritten, long readFailures,
      long responseFailures, long headersSent, long[] handlerLifetimeBuckets) {}

  public record Snapshot(long inflight, long completed, long requestBodyBytesRead,
      long responseBodyBytesFullyWritten, long readFailures, long responseFailures,
      long headersSent, long[] handlerLifetimeUpperBoundsMillis,
      long[] handlerLifetimeBuckets, List<Row> rows) {}

  private static final class Counts {
    long completed, readBytes, writtenBytes, readFailures, responseFailures, headersSent;
    final long[] durations = new long[UPPER_NANOS.length + 1];
  }

  private final Counts[][][] rows = new Counts[Route.values().length][Method.values().length]
      [Status.values().length];
  private final Counts total = new Counts();
  private long inflight;

  synchronized Request begin(String method, String path) {
    inflight = add(inflight, 1);
    return new Request(Method.of(method), Route.of(path), System.nanoTime());
  }

  synchronized Snapshot snapshot() {
    var observed = new ArrayList<Row>();
    for (Route route : Route.values()) for (Method method : Method.values())
      for (Status status : Status.values()) {
        var counts = rows[route.ordinal()][method.ordinal()][status.ordinal()];
        if (counts != null && counts.completed != 0)
          observed.add(new Row(route.label, method.name(), status.label(), counts.completed,
              counts.readBytes, counts.writtenBytes, counts.readFailures,
              counts.responseFailures, counts.headersSent, counts.durations.clone()));
      }
    return new Snapshot(inflight, total.completed, total.readBytes, total.writtenBytes,
        total.readFailures, total.responseFailures, total.headersSent,
        new long[] {1, 5, 20, 100, 500, 2_000, 10_000}, total.durations.clone(),
        List.copyOf(observed));
  }

  final class Request {
    private final Method method;
    private final Route route;
    private final long began;
    private Status selectedStatus = Status.UNSENT;
    private long readBytes, writtenBytes, readFailures, responseFailures, headersSent;
    private boolean finished;

    private Request(Method method, Route route, long began) {
      this.method = method;
      this.route = route;
      this.began = began;
    }

    void read(int bytes) {
      if (bytes <= 0) return;
      synchronized (HttpTrafficMetrics.this) {
        readBytes = add(readBytes, bytes);
      }
    }

    void readFailed() { synchronized (HttpTrafficMetrics.this) { readFailures = add(readFailures, 1); } }
    void selected(int status) { synchronized (HttpTrafficMetrics.this) { selectedStatus = Status.of(status); } }
    void headersSent() { synchronized (HttpTrafficMetrics.this) { headersSent = add(headersSent, 1); } }
    void fullyWritten(int bytes) {
      synchronized (HttpTrafficMetrics.this) {
        writtenBytes = add(writtenBytes, bytes);
      }
    }
    void responseFailed() {
      synchronized (HttpTrafficMetrics.this) { responseFailures = add(responseFailures, 1); }
    }

    void finish() {
      synchronized (HttpTrafficMetrics.this) {
        if (finished) return;
        finished = true;
        inflight--;
        int bucket = bucket(Math.max(0, System.nanoTime() - began));
        var counts = rows[route.ordinal()][method.ordinal()][selectedStatus.ordinal()];
        if (counts == null) rows[route.ordinal()][method.ordinal()][selectedStatus.ordinal()]
            = counts = new Counts();
        complete(counts, bucket);
        complete(total, bucket);
      }
    }

    private void complete(Counts counts, int bucket) {
      counts.completed = add(counts.completed, 1);
      counts.durations[bucket] = add(counts.durations[bucket], 1);
      counts.readBytes = add(counts.readBytes, readBytes);
      counts.writtenBytes = add(counts.writtenBytes, writtenBytes);
      counts.readFailures = add(counts.readFailures, readFailures);
      counts.responseFailures = add(counts.responseFailures, responseFailures);
      counts.headersSent = add(counts.headersSent, headersSent);
    }
  }

  private static int bucket(long nanos) {
    for (int i = 0; i < UPPER_NANOS.length; i++) if (nanos <= UPPER_NANOS[i]) return i;
    return UPPER_NANOS.length;
  }

  private static long add(long left, long right) {
    return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
  }
}
