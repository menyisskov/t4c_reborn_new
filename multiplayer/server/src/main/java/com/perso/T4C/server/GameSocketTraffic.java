package com.perso.T4C.server;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fixed-cardinality byte and connection counters for the persistent native game socket (T4C-0343). The streams are
 * wrapped directly around the accepted socket, so the counts are the bytes the server actually read from and handed to
 * the TCP socket: every frame header and payload byte of the application protocol (the version handshake included).
 * TCP/IP and TLS framing below the socket are not visible to the JVM and are not counted. No peer, token, route or
 * payload enters a counter; nothing is retained per connection.
 */
final class GameSocketTraffic {
  record Snapshot(long connectionsAccepted, long connectionsRefusedFull, long connectionsOpen, long bytesRead, long bytesWritten) {}

  private final AtomicLong accepted = new AtomicLong(), refusedFull = new AtomicLong(), open = new AtomicLong(),
      bytesRead = new AtomicLong(), bytesWritten = new AtomicLong();

  void accepted() { accepted.incrementAndGet(); open.incrementAndGet(); }
  void refusedFull() { refusedFull.incrementAndGet(); }
  void closed() { open.decrementAndGet(); }

  InputStream counting(InputStream in) {
    return new FilterInputStream(in) {
      @Override public int read() throws IOException { int b = super.read(); if (b >= 0) bytesRead.incrementAndGet(); return b; }
      @Override public int read(byte[] buffer, int offset, int length) throws IOException {
        int n = super.read(buffer, offset, length); if (n > 0) bytesRead.addAndGet(n); return n;
      }
    };
  }

  OutputStream counting(OutputStream out) {
    return new FilterOutputStream(out) {
      @Override public void write(int b) throws IOException { out.write(b); bytesWritten.incrementAndGet(); }
      @Override public void write(byte[] buffer, int offset, int length) throws IOException { out.write(buffer, offset, length); bytesWritten.addAndGet(length); }
    };
  }

  Snapshot snapshot() { return new Snapshot(accepted.get(), refusedFull.get(), open.get(), bytesRead.get(), bytesWritten.get()); }
}
