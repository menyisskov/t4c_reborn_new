package com.perso.T4C.server;

/** Opt-in headless server. Preview mode never reads desktop saves or preferences. */
public final class ServerMain {
  private ServerMain() {}

  public static void main(String[] args) throws Exception {
    if (args.length > 0 && "--preview".equals(args[0])) {
      if (args.length != 3) throw new IllegalArgumentException("Expected --preview COLLISION_FILE PORT");
      // JDK HTTP implementation limits are set before its first use. This listener is loopback-only.
      System.setProperty("jdk.httpserver.maxConnections", "128");
      System.setProperty("sun.net.httpserver.maxReqHeaders", "32");
      System.setProperty("sun.net.httpserver.maxReqHeaderSize", "8192");
      System.setProperty("sun.net.httpserver.maxReqTime", "5000");
      System.setProperty("sun.net.httpserver.maxRspTime", "5000");
      try (var server = new PreviewServer(PreviewMap.load(java.nio.file.Path.of(args[1])), Integer.parseInt(args[2]))) {
        Thread shutdown = new Thread(server::close, "preview-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdown);
        try {
          server.start();
          System.out.println("Local multiplayer movement preview: " + server.url());
          System.out.println("Temporary characters only. Stop with Ctrl+C. No saves are read or written.");
          server.await();
        } finally {
          try { Runtime.getRuntime().removeShutdownHook(shutdown); }
          catch (IllegalStateException ignored) { }
        }
      }
      return;
    }
    if (args.length == 1 && "--help".equals(args[0])) {
      System.out.println("Usage: java -jar t4c-server.jar [--ticks NON_NEGATIVE_COUNT]");
      System.out.println("       java -jar t4c-server.jar --preview COLLISION_FILE PORT");
      return;
    }
    long maximumTicks = Long.MAX_VALUE;
    if (args.length != 0) {
      if (args.length != 2 || !"--ticks".equals(args[0])) {
        throw new IllegalArgumentException("Expected --ticks NON_NEGATIVE_COUNT or --help");
      }
      maximumTicks = Long.parseLong(args[1]);
      if (maximumTicks < 0) throw new IllegalArgumentException("Tick count cannot be negative");
    }
    // Match an existing native timing mode without reading the desktop player's preferences.
    try (var loop = new SimulationLoop(34, tick -> {})) {
      Thread main = Thread.currentThread();
      Thread shutdown =
          new Thread(
              () -> {
                loop.close();
                try {
                  main.join(5000);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              },
              "t4c-server-shutdown");
      Runtime.getRuntime().addShutdownHook(shutdown);
      try {
        System.out.println(
            "T4C headless foundation started: no world, network or persistence configured.");
        loop.run(maximumTicks);
        System.out.println("T4C headless foundation stopped after " + loop.completedTicks() + " ticks.");
      } finally {
        try {
          Runtime.getRuntime().removeShutdownHook(shutdown);
        } catch (IllegalStateException ignored) {
          // JVM shutdown has already started and owns the hook.
        }
      }
    }
  }
}
