package com.perso.T4C.server;

import com.perso.T4C.server.persistence.OnlineStore;
import java.io.IOException;
import java.net.BindException;

/**
 * Test helper for the "stop a server, start another on the same HTTP port" scenarios.
 *
 * <p>The public {@code PreviewServer(map, port, ...)} constructors derive the native game socket
 * port as {@code httpPort + 1}. After the first server was started on an OS-assigned port, that
 * neighbouring port is arbitrary: it may be an ephemeral client port or another test's listener,
 * which made the restart fail with "Address already in use" on busy CI runners. The restart under
 * test is the HTTP listener, so the game socket takes its own OS-assigned port here, and a bind
 * that still loses a race for the HTTP port is retried for a bounded time (never other failures).
 */
final class PreviewServerRestarts {
  private static final int ATTEMPTS = 40;
  private static final long PAUSE_MILLIS = 50;

  private PreviewServerRestarts() { }

  static PreviewServer onSamePort(PreviewMap map, int httpPort, OnlineStore store, String controlToken,
      ServerTransport transport) throws IOException, InterruptedException {
    BindException last = null;
    for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
      try {
        return new PreviewServer(HostedRegionPlan.single(map), httpPort, 0, store, controlToken, transport, false);
      } catch (BindException busy) {
        last = busy;
        Thread.sleep(PAUSE_MILLIS);
      }
    }
    throw last;
  }

  static PreviewServer onSamePort(PreviewMap map, int httpPort, OnlineStore store)
      throws IOException, InterruptedException {
    return onSamePort(map, httpPort, store, null, ServerTransport.local());
  }
}
