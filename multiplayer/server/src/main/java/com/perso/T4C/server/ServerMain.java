package com.perso.T4C.server;

/** Opt-in headless server. Preview mode never reads desktop saves or preferences. */
public final class ServerMain {
  private ServerMain() {}

  public static void main(String[] args) throws Exception {
    if(args.length==2 && "--provision-online-account".equals(args[0])) {
      var store=com.perso.T4C.server.persistence.OnlineStore.fromEnvironment();
      store.migrate();
      OnlineProvisioner.provisionAccount(store,java.nio.file.Path.of(args[1]));
      System.out.println("Private account-only profile created. Keep it local and do not share its contents.");
      return;
    }
    if (args.length==1 && "--check-content".equals(args[0])) {
      System.out.println("Equipment definitions: " + ItemCatalog.loadBundled().items().size());
      var content=MonsterCatalog.loadBundled();
      System.out.println("Monster definitions: "+content.monsters().size()+"; authored spawns: "+content.spawns().size());
      System.out.println("Custom spawns without registry definitions: "+content.spawns().stream()
          .filter(spawn -> spawn.definition()==null).map(com.perso.T4C.online.model.MonsterContent.Spawn::type)
          .distinct().sorted().toList());
      System.out.println("Data validation only; monster AI and specialized behaviors are not enabled.");
      return;
    }
    if ((args.length==4 || args.length==7) && "--provision-online".equals(args[0])) {
      var type=com.perso.T4C.helper.CharacterClass.WARRIOR;
      String gender="male";
      boolean evil=false;
      if (args.length==7) {
        type=java.util.Arrays.stream(com.perso.T4C.helper.CharacterClass.values())
            .filter(candidate -> candidate.id().equals(args[4])).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown character class"));
        gender=args[5];
        if (!java.util.Set.of("male","female").contains(gender)) throw new IllegalArgumentException("Expected male or female");
        if (!java.util.Set.of("good","evil").contains(args[6])) throw new IllegalArgumentException("Expected good or evil");
        evil="evil".equals(args[6]);
      }
      var store=com.perso.T4C.server.persistence.OnlineStore.fromEnvironment();
      store.migrate();
      OnlineProvisioner.provision(store,PreviewMap.load(java.nio.file.Path.of(args[1])),java.nio.file.Path.of(args[2]),args[3],type,gender,evil);
      System.out.println("Private online profile created. Keep it local and do not share its contents.");
      return;
    }
    if (args.length == 1 && "--migrate-online".equals(args[0])) {
      com.perso.T4C.server.persistence.OnlineStore.fromEnvironment().migrate();
      System.out.println("Online PostgreSQL schema is ready. Preview characters remain temporary.");
      return;
    }
    if (args.length > 0 && ("--preview".equals(args[0]) || "--online".equals(args[0]))) {
      // Optional trailing --stream-world0 (online only, default off): host all of world 0 as one streamed region.
      boolean streamWorld0=args.length==4 && "--stream-world0".equals(args[3]);
      if (streamWorld0 && !"--online".equals(args[0])) throw new IllegalArgumentException("--stream-world0 needs --online");
      if (args.length != 3 && !streamWorld0) throw new IllegalArgumentException("Expected --preview COLLISION_FILE PORT");
      // JDK HTTP implementation limits are set before its first use. Remote listeners require TLS.
      System.setProperty("jdk.httpserver.maxConnections", "128");
      System.setProperty("sun.net.httpserver.maxReqHeaders", "32");
      System.setProperty("sun.net.httpserver.maxReqHeaderSize", "8192");
      // JDK ServerImpl converts these settings from seconds to milliseconds.
      System.setProperty("sun.net.httpserver.maxReqTime", "5");
      System.setProperty("sun.net.httpserver.maxRspTime", "5");
      var transport=ServerTransport.fromEnvironment(System.getenv());
      int httpPort=Integer.parseInt(args[2]);
      String configuredGamePort=System.getenv("T4C_GAME_SOCKET_PORT");
      int gameSocketPort=configuredGamePort==null || configuredGamePort.isBlank()
          ? defaultGameSocketPort(httpPort) : Integer.parseInt(configuredGamePort);
      if(gameSocketPort<0 || gameSocketPort>65535) throw new IllegalArgumentException("Invalid game socket port");
      if(httpPort>0 && gameSocketPort!=defaultGameSocketPort(httpPort))
        throw new IllegalArgumentException("Native clients require the game socket on HTTP port + 1");
      var store="--online".equals(args[0]) ? com.perso.T4C.server.persistence.OnlineStore.pooledFromEnvironment() : null;
      boolean serverOwnsStore=false;
      try {
      if (store!=null) store.migrate();
      String controlFile=System.getenv("T4C_CONTROL_FILE");
      String controlToken=store!=null && controlFile!=null && !controlFile.isBlank()
          ? OnlineProvisioner.controlToken(java.nio.file.Path.of(controlFile)) : null;
      var plan=store==null ? HostedRegionPlan.single(PreviewMap.load(java.nio.file.Path.of(args[1])))
          : HostedRegionPlan.startingSlice(java.nio.file.Path.of(args[1]),streamWorld0);
      try (var server = new PreviewServer(plan,httpPort,gameSocketPort,store,controlToken,transport,store!=null)) {
        serverOwnsStore=true;
        Thread shutdown = new Thread(server::close, "preview-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdown);
        try {
          server.start();
          System.out.println("Online multiplayer listener: " + server.url());
          System.out.println(store==null ? "Temporary characters only. Stop with Ctrl+C. No saves are read or written."
              : "Persistent movement, melee combat and corpse recovery enabled; offline saves remain separate.");
          server.await();
        } finally {
          try { Runtime.getRuntime().removeShutdownHook(shutdown); }
          catch (IllegalStateException ignored) { }
        }
      }
      } finally { if(!serverOwnsStore && store!=null) store.close(); }
      return;
    }
    if (args.length == 1 && "--help".equals(args[0])) {
      System.out.println("Usage: java -jar t4c-server.jar [--ticks NON_NEGATIVE_COUNT]");
      System.out.println("       java -jar t4c-server.jar --preview COLLISION_FILE PORT");
      System.out.println("       java -jar t4c-server.jar --online COLLISION_FILE PORT");
      System.out.println("       java -jar t4c-server.jar --online COLLISION_FILE PORT --stream-world0 (host all of world 0 as one windowed region; off by default)");
      System.out.println("       java -jar t4c-server.jar --provision-online COLLISION_FILE PRIVATE_PROFILE_FILE NAME [CLASS male|female good|evil]");
      System.out.println("       java -jar t4c-server.jar --provision-online-account PRIVATE_PROFILE_FILE");
      System.out.println("       java -jar t4c-server.jar --migrate-online (T4C_DB_URL, T4C_DB_USER, T4C_DB_PASSWORD)");
      System.out.println("       java -jar t4c-server.jar --check-content");
      System.out.println("       Optional: T4C_GAME_SOCKET_PORT (defaults to HTTP port + 1; 0 selects an ephemeral port for local testing)");
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

  private static int defaultGameSocketPort(int httpPort) {
    if(httpPort==0) return 0;
    if(httpPort<0 || httpPort>=65535) throw new IllegalArgumentException("Set T4C_GAME_SOCKET_PORT when the HTTP listener uses 65535");
    return httpPort+1;
  }
}
