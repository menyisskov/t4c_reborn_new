package com.perso.T4C.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Bounded exchange dispatch and short-lived admission; no credentials survive an exchange. */
final class HttpRequestAdmission {
  static final int MAX_EXCHANGES=128, MAX_PUBLIC=2, MAX_ROSTER=2, MAX_PER_SESSION=2;

  interface Permit extends AutoCloseable { @Override void close(); }

  private final Semaphore publicRequests=new Semaphore(MAX_PUBLIC);
  private final Semaphore rosterRequests=new Semaphore(MAX_ROSTER);
  private final Map<String,Integer> sessions=new HashMap<>();

  Permit publicRequest() { return take(publicRequests); }
  Permit rosterRequest() { return take(rosterRequests); }

  private static Permit take(Semaphore permits) {
    if(!permits.tryAcquire()) throw new PreviewWorld.Rejected(503,"busy");
    return once(permits::release);
  }

  synchronized Permit session(String token) {
    int active=sessions.getOrDefault(token,0);
    if(active>=MAX_PER_SESSION || (active==0 && sessions.size()>=MAX_EXCHANGES))
      throw new PreviewWorld.Rejected(503,"busy");
    sessions.put(token,active+1);
    return once(() -> {
      synchronized(HttpRequestAdmission.this) {
        int remaining=sessions.get(token)-1;
        if(remaining==0) sessions.remove(token);
        else sessions.put(token,remaining);
      }
    });
  }

  synchronized int activeSessions() { return sessions.size(); }

  private static Permit once(Runnable release) {
    var closed=new AtomicBoolean();
    return () -> { if(closed.compareAndSet(false,true)) release.run(); };
  }

  /** HttpServer submits exchanges before headers are parsed. Reject above the cap without a FIFO queue. */
  static final class ExchangeExecutor extends AbstractExecutorService {
    private final Semaphore slots=new Semaphore(MAX_EXCHANGES);
    private final ExecutorService tasks;

    ExchangeExecutor() {
      ThreadFactory factory=Thread.ofVirtual().name("preview-http-",0).factory();
      tasks=Executors.newThreadPerTaskExecutor(factory);
    }

    @Override public void execute(Runnable task) {
      if(!slots.tryAcquire()) throw new RejectedExecutionException("HTTP exchanges busy");
      try {
        tasks.execute(() -> { try { task.run(); } finally { slots.release(); } });
      } catch(RuntimeException|Error failure) { slots.release();throw failure; }
    }

    int active() { return MAX_EXCHANGES-slots.availablePermits(); }
    @Override public void shutdown() { tasks.shutdown(); }
    @Override public java.util.List<Runnable> shutdownNow() { return tasks.shutdownNow(); }
    @Override public boolean isShutdown() { return tasks.isShutdown(); }
    @Override public boolean isTerminated() { return tasks.isTerminated(); }
    @Override public boolean awaitTermination(long timeout,TimeUnit unit) throws InterruptedException {
      return tasks.awaitTermination(timeout,unit);
    }
  }
}
