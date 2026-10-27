package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class HttpTrafficMetricsTest {
  @Test
  void incompleteResponseAndReadKeepOnlySuccessfulApplicationBytesAndFixedLabels() {
    var metrics = new HttpTrafficMetrics();
    var attempt = metrics.begin("PATCH", "/secret/path-with-account-id");
    attempt.read(3);
    attempt.readFailed();
    attempt.selected(503);
    attempt.headersSent();
    attempt.responseFailed();
    assertEquals(1, metrics.snapshot().inflight());
    attempt.finish();
    attempt.finish();

    var snapshot = metrics.snapshot();
    assertEquals(0, snapshot.inflight());
    assertEquals(1, snapshot.completed());
    assertEquals(3, snapshot.requestBodyBytesRead());
    assertEquals(0, snapshot.responseBodyBytesFullyWritten());
    assertEquals(1, snapshot.readFailures());
    assertEquals(1, snapshot.responseFailures());
    assertEquals(1, snapshot.headersSent());
    assertEquals("other", snapshot.rows().getFirst().route());
    assertEquals("OTHER", snapshot.rows().getFirst().method());
    assertEquals("503", snapshot.rows().getFirst().selectedStatus());
    assertFalse(new Gson().toJson(snapshot).contains("secret/path-with-account-id"));

    var cast = metrics.begin("POST", "/cast");
    cast.selected(200);
    cast.finish();
    var use = metrics.begin("POST", "/use");
    use.selected(200);
    use.finish();
    var labeled = metrics.snapshot().rows();
    assertTrue(labeled.stream().anyMatch(row -> row.route().equals("/cast") && row.method().equals("POST")));
    assertTrue(labeled.stream().anyMatch(row -> row.route().equals("/use") && row.method().equals("POST")));
    assertTrue(labeled.stream().anyMatch(row -> row.route().equals("other") && row.method().equals("OTHER")));
  }

  @Test
  void concurrentHandlersPublishActiveThenExactlyOneCompletionEach() throws Exception {
    var metrics = new HttpTrafficMetrics();
    int workers = 12;
    var entered = new CountDownLatch(workers);
    var release = new CountDownLatch(1);
    try (var pool = Executors.newFixedThreadPool(workers)) {
      var tasks = new java.util.ArrayList<java.util.concurrent.Future<?>>();
      for (int i = 0; i < workers; i++) tasks.add(pool.submit(() -> {
        var attempt = metrics.begin("GET", "/state");
        entered.countDown();
        try { assertTrue(release.await(5, TimeUnit.SECONDS)); }
        catch (InterruptedException interrupted) { Thread.currentThread().interrupt();throw new AssertionError(interrupted); }
        attempt.selected(200);
        attempt.headersSent();
        attempt.fullyWritten(7);
        attempt.finish();
      }));
      try {
        assertTrue(entered.await(5, TimeUnit.SECONDS));
        assertEquals(workers, metrics.snapshot().inflight());
      } finally { release.countDown(); }
      for (var task : tasks) task.get(5, TimeUnit.SECONDS);
    }
    var snapshot = metrics.snapshot();
    assertEquals(0, snapshot.inflight());
    assertEquals(workers, snapshot.completed());
    assertEquals(workers * 7, snapshot.responseBodyBytesFullyWritten());
    assertEquals(workers, java.util.Arrays.stream(snapshot.handlerLifetimeBuckets()).sum());
    assertEquals(1, snapshot.rows().size());
    assertEquals("/state", snapshot.rows().getFirst().route());
  }
}
