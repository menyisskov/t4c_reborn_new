package com.perso.T4C.server;

/** Opt-in lifecycle foundation. No world, network listener, assets, preferences or saves are loaded. */
public final class ServerMain {
  private ServerMain() {}

  public static void main(String[] args) {
    if (args.length == 1 && "--help".equals(args[0])) {
      System.out.println("Usage: java -jar t4c-server.jar [--ticks NON_NEGATIVE_COUNT]");
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
