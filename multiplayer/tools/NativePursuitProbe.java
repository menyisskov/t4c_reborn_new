package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.PLAYER_MELEE_ATTACK_RANGE;

import com.google.gson.JsonObject;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.OnlineEndpoint;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.server.persistence.OnlineStore;
import java.net.URI;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/** Two real native adapters observe one server-authoritative, provoked guard pursuit. */
public final class NativePursuitProbe {
  private static final int PORT = 18771;
  private static final int HANDOFF_PORT = 18778;
  private static final int START_SECONDS = 30, STOP_SECONDS = 100, LEASE_SECONDS = 45;
  private static final String GUARD = "Olin Haad Guard 3";
  private static final int GUARD_X = 2935, GUARD_Y = 1041;
  private record Player(OnlineStore.AccountKey key, OnlineStore.CharacterState created,
      OnlineConnection.Credentials credentials) {}
  private record Motion(double firstX, double firstY, double lastX, double lastY,
      double firstDistance, double lastDistance, int samples, int settledSamples,
      long settledMillis, boolean towardAttacker) {}

  public static void main(String[] args) throws Exception {
    boolean handoff = args.length == 5 && "--target-handoff".equals(args[4]);
    if (args.length != 4 && !handoff) throw new IllegalArgumentException(
        "Usage: NativePursuitProbe RUNTIME_DIR http://127.0.0.1:18771 NEW_OUTPUT_DIR BASE_COMMIT [--target-handoff uses 18778]");
    Path runtime = Path.of(args[0]).toRealPath();
    URI endpoint = OnlineEndpoint.validate(URI.create(args[1]));
    int port = handoff ? HANDOFF_PORT : PORT;
    if (!endpoint.toString().equals("http://127.0.0.1:" + port))
      throw new IllegalArgumentException("Use only the reserved isolated loopback endpoint");
    Path output = Path.of(args[2]).toAbsolutePath().normalize();
    if (!args[3].matches("[0-9a-f]{40}") || !Files.isDirectory(output.getParent()) || Files.exists(output))
      throw new IllegalArgumentException("Supply a committed source SHA and a new output directory");
    output = output.getParent().toRealPath().resolve(output.getFileName());
    Path checkout = Path.of("").toAbsolutePath().normalize();
    Path privateTemp = Path.of(System.getProperty("java.io.tmpdir")).toRealPath();
    if (runtime.startsWith(checkout) || output.startsWith(checkout)
        || !runtime.startsWith(privateTemp) || !output.startsWith(privateTemp))
      throw new IllegalArgumentException("Runtime and output must be under private TEMP outside the checkout");
    Path serverJar = runtime.resolve("t4c-server.jar");
    Path clientJar = runtime.resolve("client/T4C.jar");
    Path collision = runtime.resolve("assets/maps/worldmap/worldmap.colbin");
    Path dungeon = runtime.resolve("assets/maps/dungeon/DungeonMap.colbin");
    Path candidateManifest = runtime.resolve("candidate-manifest.tsv");
    if (!Files.isRegularFile(serverJar) || !Files.isRegularFile(clientJar)
        || !Files.isRegularFile(collision) || !Files.isRegularFile(dungeon)
        || !Files.isRegularFile(candidateManifest) || Files.size(candidateManifest) > 64 * 1024)
      throw new IllegalArgumentException("Incomplete copied runtime");
    var manifestLines = Files.readAllLines(candidateManifest, StandardCharsets.UTF_8);
    if (manifestLines.size() < 2 || !manifestLines.get(0).equals("baseCommit\t" + args[3])
        || manifestLines.stream().skip(1).anyMatch(line -> !line.matches(
            "candidateSource\\t[0-9a-fA-F]{64}\\t(?:multiplayer|src)/[A-Za-z0-9_./-]+")))
      throw new IllegalArgumentException("Candidate source manifest does not match the declared base commit");
    for (String entry : System.getProperty("java.class.path").split(java.io.File.pathSeparator)) {
      String literal = entry.endsWith("*") ? entry.substring(0, entry.length() - 1) : entry;
      if (!Path.of(literal).toAbsolutePath().normalize().startsWith(runtime))
        throw new IllegalArgumentException("Probe classpath must use only the copied runtime");
    }
    String url = System.getenv("T4C_DB_URL"), user = System.getenv("T4C_DB_USER");
    String password = System.getenv("T4C_DB_PASSWORD");
    String dbPattern = handoff
        ? "jdbc:postgresql://127\\.0\\.0\\.1:55432/t4c_target_0229_[a-z0-9_]+"
        : "jdbc:postgresql://127\\.0\\.0\\.1:55432/t4c_pursuit_0212_[a-z0-9_]+(?:\\?.*)?";
    if (url == null || !url.matches(dbPattern)
        || !"t4c_online".equals(user) || password == null || password.isBlank())
      throw new IllegalArgumentException("Use a fresh dedicated database for this probe mode on 55432");
    for (String setting : List.of("T4C_TLS_KEYSTORE", "T4C_TLS_PASSWORD_FILE", "T4C_PUBLIC_ORIGIN"))
      if (System.getenv(setting) != null && !System.getenv(setting).isBlank())
        throw new IllegalArgumentException("This isolated probe requires local HTTP transport");
    if (System.getenv("T4C_BIND_ADDRESS") != null
        && !"127.0.0.1".equals(System.getenv("T4C_BIND_ADDRESS")))
      throw new IllegalArgumentException("Server bind must be loopback");
    try (var socket = new ServerSocket()) {
      socket.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port));
    } catch (Exception occupied) {
      throw new IllegalArgumentException("Dedicated pursuit port is already in use", occupied);
    }

    var plan = HostedRegionPlan.startingSlice(collision);
    var region = plan.require(0, GUARD_X, GUARD_Y);
    var map = region.map();
    var content = MonsterCatalog.loadBundled();
    var spawn = content.spawns().stream()
        .filter(s -> s.world() == 0 && s.x() == GUARD_X && s.y() == GUARD_Y
            && GUARD.equals(s.definition()) && !s.stationary() && !s.aggressive())
        .findFirst().orElseThrow(() -> new IllegalArgumentException("Passive guard fixture changed"));
    if (!GUARD.equals(spawn.type())) throw new IllegalArgumentException("Guard fixture type changed");
    var regionSpawns = content.region(0, map.originX, map.originY, map.width, map.height);
    int guardIndex = -1;
    for (int i = 0; i < regionSpawns.size(); i++)
      if (regionSpawns.get(i).persistenceKey().equals(spawn.persistenceKey())) {
        guardIndex = i;
        break;
      }
    if (guardIndex < 0) throw new IllegalArgumentException("Authored guard is outside hosted region");
    final long guardId = guardIndex + 1L; // PreviewWorld assigns region-order IDs starting at one.
    double ax = GUARD_X - 0.5, ay = GUARD_Y + 0.5;
    if (handoff) {
      targetHandoff(runtime, endpoint, output, args[3], spawn, guardId, map, ax, ay);
      return;
    }
    double bx = 2978.5, by = 1035.5;
    if (!map.canStand(ax - map.originX, ay - map.originY)
        || !map.canStand(bx - map.originX, by - map.originY)
        || !map.allowsCombat(ax - map.originX, ay - map.originY,
            GUARD_X - map.originX, GUARD_Y - map.originY, false))
      throw new IllegalArgumentException("Guard/observer fixture is blocked or sanctuary protected");
    Files.createDirectory(output);
    Path report = output.resolve("report.txt");
    Files.writeString(report, "T4C-0212 native pursuit acceptance PREP; started=" + Instant.now()
        + "; baseCommit(operator supplied)=" + args[3]
        + "; candidateManifestSha256=" + sha(candidateManifest)
        + "; candidateSourceFiles=" + (manifestLines.size() - 1)
        + "; protocol=" + OnlineProtocol.VERSION
        + "; endpoint=" + endpoint + "\n"
        + "serverJarSha256=" + sha(serverJar) + "; clientJarSha256=" + sha(clientJar) + "\n"
        + "Two OnlineConnection adapters; no renderer. Credentials remain only in private profiles.\n",
        StandardOpenOption.CREATE_NEW);
    Path java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toRealPath();
    Process child = null;
    String stage = "start owned server";
    try (var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        var store = OnlineStore.fromEnvironment()) {
      child = start(java, serverJar, collision, output, 1);
      healthy(http, endpoint, child);
      int schemaVersion = schema(url, user, password);
      require(schemaVersion == 11, "Integrated pursuit candidate requires measured schema 11");
      append(report, "schema=" + schemaVersion + "\n");
      stage = "provision observer and wait for west-flank guard";
      var firstKit = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "female", false, new Random(2121));
      var secondKit = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "female", false, new Random(2122));
      Player b = provision(store, "Pursuit observer", bx, by, secondKit);
      privateProfile(output.resolve("player-b.json"), b.credentials());
      var bBaseline = store.load(b.key().accountId(), b.created().id());
      stage = "join and provoke";
      Player a;
      OnlineStore.CharacterState afterClose;
      OnlineStore.SpawnState checkpoint;
      try (var observer = new OnlineConnection(endpoint, b.credentials())) {
        var bv = connected(observer, b);
        CreatureView initial = awaitWestFlank(observer, map, guardId);
        require(initial.health() > 0, "Guard was unavailable before provocation");
        a = provision(store, "Pursuit attacker", ax, ay, firstKit);
        privateProfile(output.resolve("player-a.json"), a.credentials());
        try (var attacker = new OnlineConnection(endpoint, a.credentials())) {
        var av = connected(attacker, a);
        require(av.map().region().equals(bv.map().region()), "Adapters did not join the same hosted region");
        int aHp = own(av).hp();
        int bHp = own(bv).hp();
        var beforeLife = store.loadOrCreateSpawn(spawn.persistenceKey());
        var attackView = awaitMeleeWindow(attacker, observer, map, guardId);
        var attackGuard = guard(observer.view(), guardId);
        require(attackView != null && pixelDistance(own(attackView).x(), own(attackView).y(),
            attackGuard.x(), attackGuard.y()) < 48
            && map.allowsCombat(own(attackView).x(), own(attackView).y(),
                attackGuard.x(), attackGuard.y(), false),
            "Attacker did not reach live guard through legal terrain");
        var hit = attacker.attack(guardId).toCompletableFuture().get(8, TimeUnit.SECONDS);
        require(hit.accepted(), "Provocation was explicitly rejected");
        var provokedLife = store.loadOrCreateSpawn(spawn.persistenceKey());
        require(provokedLife.eventId().equals(beforeLife.eventId())
            && a.created().id().equals(provokedLife.targetCharacter()),
            "Accepted hit did not commit this living guard's attacker target");
        append(report, "acceptedProvocation=true; guardId=" + guardId + "; guardInitialLocal="
            + initial.x() + "," + initial.y() + "; attackGuardLocal="
            + attackGuard.x() + "," + attackGuard.y() + "; guardInitialHp=" + initial.health() + "\n");
        stage = "west retreat and observer motion";
        var postHit = attacker.view();
        require(postHit != null, "Attacker view missing after accepted hit");
        double retreatStartX = own(postHit).x(), retreatStartY = own(postHit).y();
        validateWestRay(map, retreatStartX, retreatStartY, attackGuard);
        Motion motion = walkAndObserve(attacker, observer, map, retreatStartX,
            retreatStartY, guardId, initial, av.map().locationEpoch());
        require(motion.towardAttacker,
            "Observer did not see the targeted guard close on the stopped attacker");
        var currentA = attacker.view();
        var currentB = observer.view();
        require(currentA != null && own(currentA).hp() > 0, "Attacker died before pursuit proof");
        require(currentB != null && currentB.map().region().equals(bv.map().region())
            && own(currentB).hp() == bHp, "Observer HP or region changed");
        var guardAfter = currentB.snapshot().creatures().stream().filter(c -> c.id() == guardId)
            .findFirst().orElseThrow(() -> new AssertionError("Guard unavailable after pursuit"));
        var savedB = store.load(b.key().accountId(), b.created().id());
        require(savedB.progress().equals(bBaseline.progress()) && savedB.gold() == bBaseline.gold()
            && savedB.experience() == bBaseline.experience() && savedB.world() == bBaseline.world()
            && savedB.locationEpoch() == bBaseline.locationEpoch()
            && savedB.x() == bBaseline.x() && savedB.y() == bBaseline.y()
            && savedB.progress().vitals().hp() == bBaseline.progress().vitals().hp(),
            "Observer committed HP, position or progress changed");
        require(Math.abs(own(currentB).x() + currentB.map().originX() - savedB.x()) < .25
            && Math.abs(own(currentB).y() + currentB.map().originY() - savedB.y()) < .25,
            "Observer public position did not match its committed position");
        append(report, "guardObservedLocal=" + motion.lastX + "," + motion.lastY
            + "; stoppedAttackerStartDistancePixels=" + motion.firstDistance
            + "; stoppedAttackerEndDistancePixels=" + motion.lastDistance + "; towardAttacker="
            + motion.towardAttacker + "; observerSamples=" + motion.samples
            + "; settledSamples=" + motion.settledSamples + "; settledMillis=" + motion.settledMillis
            + "; guardFinalHp=" + guardAfter.health() + "; attackerHp=" + aHp + "->"
            + own(currentA).hp() + "; observerHp=" + bHp + "->" + own(currentB).hp() + "\n"
            + "observerProgressSha256=" + progressSha(savedB) + "\n");
        checkpoint = checkpoint(store, spawn.persistenceKey(), beforeLife, a.created().id(),
            av.map().locationEpoch(), attacker, observer, guardId);
        append(report, "checkpointRevision="
            + checkpoint.stateRevision() + "; checkpointMode=" + checkpoint.mode()
            + "; checkpointTargetEpoch=" + checkpoint.targetEpoch() + "\n");
        attacker.stopInput();
        }
      }
      stage = "normal lease release and reconnect";
      available(store, a, 45);
      afterClose = store.load(a.key().accountId(), a.created().id());
      append(report, "postCloseCommittedRevision=" + afterClose.revision()
          + "; postCloseProgressSha256=" + progressSha(afterClose) + "\n");
      try (var rejoined = new OnlineConnection(endpoint, a.credentials())) {
        connected(rejoined, a);
        boolean matching = false;
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        while (System.nanoTime() < deadline) {
          var v = rejoined.view();
          var saved = store.load(a.key().accountId(), a.created().id());
          if (privateMatches(v, saved)
              && saved.revision() >= afterClose.revision()) { matching = true; break; }
          Thread.sleep(50);
        }
        require(matching, "Rejoined native private view did not match a committed record");
      }
      stage = "graceful checkpoint flush and restart";
      available(store, a, LEASE_SECONDS);
      available(store, b, LEASE_SECONDS);
      graceful(http, endpoint, child, output.resolve("control-1.token"));
      child = null;
      var afterStop = store.loadOrCreateSpawn(spawn.persistenceKey());
      require(afterStop.eventId().equals(checkpoint.eventId())
          && afterStop.health().equals(checkpoint.health())
          && afterStop.currentX() != null && afterStop.currentY() != null
          && afterStop.stateRevision() >= checkpoint.stateRevision(),
          "Graceful stop lost the living guard checkpoint or healed it");
      child = start(java, serverJar, collision, output, 2);
      healthy(http, endpoint, child);
      var restored = store.loadOrCreateSpawn(spawn.persistenceKey());
      samePose(afterStop, restored, "restart changed committed living guard pose");
      available(store, a, LEASE_SECONDS);
      available(store, b, LEASE_SECONDS);
      try (var observer = new OnlineConnection(endpoint, b.credentials())) {
        var bView = connected(observer, b);
        var restoredGuard = bView.snapshot().creatures().stream()
            .filter(c -> c.id() == guardId).findFirst()
            .orElseThrow(() -> new AssertionError("Guard missing after restart"));
        require(Math.abs(restoredGuard.x() + bView.map().originX() - restored.currentX()) < .25
            && Math.abs(restoredGuard.y() + bView.map().originY() - restored.currentY()) < .25
            && restoredGuard.health() == restored.health(),
            "Native observer did not receive restored committed guard position/HP");
        try (var attacker = new OnlineConnection(endpoint, a.credentials())) {
          connected(attacker, a);
          boolean matching = false;
          long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
          while (System.nanoTime() < deadline) {
            var savedA = store.load(a.key().accountId(), a.created().id());
            var savedB = store.load(b.key().accountId(), b.created().id());
            if (privateMatches(attacker.view(), savedA)
                && privateMatches(observer.view(), savedB)) { matching = true; break; }
            Thread.sleep(50);
          }
          require(matching,
              "Restarted native private state disagreed with committed players");
        }
      }
      available(store, a, LEASE_SECONDS);
      available(store, b, LEASE_SECONDS);
      graceful(http, endpoint, child, output.resolve("control-2.token"));
      child = null;
      append(report, "PASS: committed provocation; settled observer-authoritative pursuit; passive observer isolation; durable creature checkpoint; two private reconnects and owned graceful restart with living guard pose/HP/life preserved.\n"
          + "Reset/home, death/respawn, stale-CAS race, displaced loot/quest, renderer and full Beta gates remain separate.\n");
      System.out.println("PASS T4C-0212 native pursuit probe; sanitized report: " + report);
    } catch (Exception | Error failure) {
      append(report, "FAIL stage=" + stage + "; type=" + failure.getClass().getSimpleName() + "\n");
      throw failure;
    } finally {
      if (child != null && child.isAlive()) {
        try (var cleanup = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()) {
          Path control = output.resolve(Files.exists(output.resolve("control-2.token"))
              ? "control-2.token" : "control-1.token");
          graceful(cleanup, endpoint, child, control);
        } catch (Exception | Error cleanupFailure) {
          append(report, "Owned child cleanup did not exit cleanly; inspect private logs.\n");
          child.destroyForcibly();
          child.waitFor(10, TimeUnit.SECONDS);
        }
      }
    }
  }

  /** A separate two-adapter target handoff fixture; the original pursuit probe remains unchanged. */
  private static void targetHandoff(Path runtime, URI endpoint, Path output, String baseCommit,
      com.perso.T4C.online.model.MonsterContent.Spawn spawn, long guardId, PreviewMap map,
      double ax, double ay) throws Exception {
    require(map.canStand(ax - map.originX, ay - map.originY),
        "Disposable near-guard attacker fixture is blocked");
    Files.createDirectory(output);
    Path report = output.resolve("report.txt");
    Path java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toRealPath();
    Path jar = runtime.resolve("t4c-server.jar");
    Path collision = runtime.resolve("assets/maps/worldmap/worldmap.colbin");
    Files.writeString(report, "T4C-0229 native target handoff; started=" + Instant.now()
        + "; baseCommit=" + baseCommit + "; endpoint=" + endpoint
        + "; protocol=" + OnlineProtocol.VERSION + "; serverJarSha256=" + sha(jar)
        + "; clientJarSha256=" + sha(runtime.resolve("client/T4C.jar"))
        + "; candidateManifestSha256=" + sha(runtime.resolve("candidate-manifest.tsv"))
        + "\nTwo native OnlineConnection adapters; disposable near-guard initial characters; no renderer.\n",
        StandardOpenOption.CREATE_NEW);
    Process child = null;
    String stage = "start owned server";
    try (var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        var store = OnlineStore.fromEnvironment()) {
      child = start(java, jar, collision, output, 1, HANDOFF_PORT);
      healthy(http, endpoint, child);
      require(schema(System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"),
          System.getenv("T4C_DB_PASSWORD")) == 11, "Target handoff requires schema 11");
      var kitA = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "female", false,
          new Random(2281));
      var kitB = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "female", false,
          new Random(2282));
      var a = provision(store, "Handoff attacker", ax, ay, kitA);
      privateProfile(output.resolve("player-a.json"), a.credentials());
      var beforeLife = store.loadOrCreateSpawn(spawn.persistenceKey());
      stage = "A provokes and leaves";
      long aEpoch;
      try (var attacker = new OnlineConnection(endpoint, a.credentials())) {
        var joined = connected(attacker, a);
        aEpoch = joined.map().locationEpoch();
        require(attacker.unequip(BodyPart.WEAPON).toCompletableFuture()
            .get(8, TimeUnit.SECONDS).accepted(), "A could not unequip the starter greatsword");
        var guard = awaitWestFlank(attacker, map, guardId);
        require(guard.health() > 0 && Math.abs(own(attacker.view()).x() - 30.5) < .25,
            "Attacker was not at the legal west-flank melee point");
        require(attacker.attack(guardId).toCompletableFuture().get(8, TimeUnit.SECONDS).accepted(),
            "A's real native attack was rejected");
        var provoked = store.loadOrCreateSpawn(spawn.persistenceKey());
        require(provoked.eventId().equals(beforeLife.eventId())
            && (provoked.health() == null || provoked.health() > 0)
            && a.created().id().equals(provoked.targetCharacter())
            && Long.valueOf(aEpoch).equals(provoked.targetEpoch()),
            "A's accepted hit did not durably target the same living guard");
        attacker.stopInput();
      }
      available(store, a, LEASE_SECONDS);
      var savedA = store.load(a.key().accountId(), a.created().id());
      require(savedA.progress().vitals().hp() > 0, "A died before handoff");
      stage = "B commits target handoff and takes a strike";
      var b = provision(store, "Handoff second player", ax, ay, kitB);
      privateProfile(output.resolve("player-b.json"), b.credentials());
      int bInitialHp;
      long bEpoch;
      OnlineStore.SpawnState switched;
      try (var second = new OnlineConnection(endpoint, b.credentials())) {
        var joined = connected(second, b);
        bInitialHp = own(joined).hp();
        bEpoch = joined.map().locationEpoch();
        require(second.unequip(BodyPart.WEAPON).toCompletableFuture()
            .get(8, TimeUnit.SECONDS).accepted(), "B could not unequip the starter greatsword");
        awaitHandoffMelee(second, map, guardId);
        require(a.created().id().equals(store.loadOrCreateSpawn(spawn.persistenceKey()).targetCharacter()),
            "Absent A's committed target changed before B's hit");
        require(second.attack(guardId).toCompletableFuture().get(8, TimeUnit.SECONDS).accepted(),
            "B's real native handoff hit was rejected");
        switched = store.loadOrCreateSpawn(spawn.persistenceKey());
        require(switched.eventId().equals(beforeLife.eventId())
            && (switched.health() == null || switched.health() > 0)
            && b.created().id().equals(switched.targetCharacter())
            && Long.valueOf(bEpoch).equals(switched.targetEpoch()),
            "B's accepted hit did not durably replace A's target/epoch");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        boolean struck = false;
        while (System.nanoTime() < deadline) {
          var committed = store.load(b.key().accountId(), b.created().id());
          if (committed.progress().vitals().hp() < bInitialHp
              && committed.progress().vitals().hp() > 0) { struck = true; break; }
          Thread.sleep(100);
        }
        require(struck, "No committed guard strike damaged only the new target B");
        second.stopInput();
      }
      available(store, b, LEASE_SECONDS);
      var savedB = store.load(b.key().accountId(), b.created().id());
      require(savedB.progress().vitals().hp() < bInitialHp
          && store.load(a.key().accountId(), a.created().id()).equals(savedA),
          "B's strike changed absent A or did not persist B's HP");
      append(report, "acceptedAHop=true; acceptedBHop=true; sameLife=true; targetEpoch="
          + bEpoch + "; aHp=" + savedA.progress().vitals().hp() + "; bHp="
          + bInitialHp + "->" + savedB.progress().vitals().hp() + "\n");
      stage = "A reconnect does not steal B's target";
      try (var rejoined = new OnlineConnection(endpoint, a.credentials())) {
        connected(rejoined, a);
        Thread.sleep(2200);
        require(privateMatches(rejoined.view(), savedA)
            && store.load(a.key().accountId(), a.created().id()).equals(savedA)
            && b.created().id().equals(store.loadOrCreateSpawn(spawn.persistenceKey()).targetCharacter()),
            "A rejoin changed private state or stole B's durable target");
      }
      available(store, a, LEASE_SECONDS);
      var savedAAfterSecondLeave = store.load(a.key().accountId(), a.created().id());
      require(samePrivateApartFromRevision(savedA, savedAAfterSecondLeave, false),
          "A's second normal leave changed private state beyond checkpoint revision");
      stage = "graceful stop and restart";
      graceful(http, endpoint, child, output.resolve("control-1.token"));
      append(report, "ownedServer1Exit=" + child.exitValue() + "\n");
      child = null;
      var stopped = store.loadOrCreateSpawn(spawn.persistenceKey());
      require(b.created().id().equals(stopped.targetCharacter())
          && Long.valueOf(bEpoch).equals(stopped.targetEpoch())
          && stopped.currentX() != null && stopped.currentY() != null
          && "RETALIATION".equals(stopped.mode())
          && store.load(a.key().accountId(), a.created().id()).equals(savedAAfterSecondLeave)
          && store.load(b.key().accountId(), b.created().id()).equals(savedB),
          "Graceful stop lost B's target or changed an absent player's private state");
      child = start(java, jar, collision, output, 2, HANDOFF_PORT);
      healthy(http, endpoint, child);
      var restored = store.loadOrCreateSpawn(spawn.persistenceKey());
      samePose(stopped, restored, "Restart changed committed guard life, target or pose");
      require(store.load(a.key().accountId(), a.created().id()).equals(savedAAfterSecondLeave)
          && store.load(b.key().accountId(), b.created().id()).equals(savedB),
          "Restart changed an absent player's committed private baseline");
      try (var first = new OnlineConnection(endpoint, a.credentials());
          var second = new OnlineConnection(endpoint, b.credentials())) {
        connected(first, a);
        connected(second, b);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        boolean matching = false;
        while (System.nanoTime() < deadline) {
          var currentB = store.load(b.key().accountId(), b.created().id());
          if (privateMatches(first.view(), savedAAfterSecondLeave)
              && store.load(a.key().accountId(), a.created().id()).equals(savedAAfterSecondLeave)
              && samePrivateApartFromRevision(savedB, currentB, true)
              && privateMatches(second.view(), currentB)) {
            matching = true; break;
          }
          Thread.sleep(50);
        }
        require(matching, "Restarted native private views did not converge to committed records");
        require(b.created().id().equals(store.loadOrCreateSpawn(spawn.persistenceKey()).targetCharacter()),
            "Restarted A rejoin stole B's target");
      }
      available(store, a, LEASE_SECONDS);
      available(store, b, LEASE_SECONDS);
      graceful(http, endpoint, child, output.resolve("control-2.token"));
      append(report, "ownedServer2Exit=" + child.exitValue() + "\n");
      child = null;
      append(report, "PASS: native A logout, B accepted hit/epoch handoff and committed strike,"
          + " absent A unchanged, A reconnect isolation, owned graceful restart preserves life/target/pose/private state.\n"
          + "No renderer, remote deployment, reset/home or full Beta claim.\n");
      System.out.println("PASS T4C-0229 native target handoff; sanitized report: " + report);
    } catch (Exception | Error failure) {
      append(report, "FAIL stage=" + stage + "; type=" + failure.getClass().getSimpleName() + "\n");
      throw failure;
    } finally {
      if (child != null && child.isAlive()) {
        try (var cleanup = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()) {
          Path control = output.resolve(Files.exists(output.resolve("control-2.token"))
              ? "control-2.token" : "control-1.token");
          graceful(cleanup, endpoint, child, control);
          append(report, "ownedCleanupExit=" + child.exitValue() + "\n");
        } catch (Exception | Error cleanupFailure) {
          append(report, "Owned child cleanup failed; inspect private logs.\n");
          child.destroyForcibly();
          child.waitFor(10, TimeUnit.SECONDS);
        }
      }
    }
  }

  private static Player provision(OnlineStore store, String name, double x, double y,
      OnlineCharacterCreator.Prepared kit) throws Exception {
    var key = store.provisionAccount();
    var created = store.create(key.accountId(), name, 0, x, y, kit.progress(), kit.gold());
    return new Player(key, created, new OnlineConnection.Credentials(key.secret(), created.id()));
  }

  private static void privateProfile(Path file, OnlineConnection.Credentials credentials) throws Exception {
    var profile = new JsonObject();
    profile.addProperty("secret", credentials.secret());
    profile.addProperty("characterId", credentials.characterId().toString());
    Files.writeString(file, profile.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
  }

  private static OnlineConnection.View connected(OnlineConnection adapter, Player player) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(12);
    while (System.nanoTime() < deadline) {
      var v = adapter.view();
      if (v != null && v.character() != null && adapter.status().equals("connected")
          && player.created().id().equals(v.character().id())) {
        adapter.confirmRegionReady(v.map().locationEpoch());
        long tick = v.snapshot().tick();
        long readyDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < readyDeadline) {
          var next = adapter.view();
          if (next != null && next.map().locationEpoch() == v.map().locationEpoch()
              && next.snapshot().tick() > tick + 1) return next;
          Thread.sleep(25);
        }
        throw new AssertionError("Native region readiness did not advance snapshots");
      }
      Thread.sleep(25);
    }
    throw new AssertionError("Native adapter did not join the provisioned character");
  }

  private static OnlineConnection.Actor own(OnlineConnection.View view) {
    return view.snapshot().players().stream().filter(p -> p.id() == view.ownId())
        .findFirst().orElseThrow(() -> new AssertionError("Own actor missing"));
  }

  private static CreatureView guard(OnlineConnection.View view, long id) {
    return view.snapshot().creatures().stream()
        .filter(c -> c.id() == id && GUARD.equals(c.definition()))
        .findFirst().orElseThrow(() -> new AssertionError("Authored passive guard missing"));
  }

  private static CreatureView awaitWestFlank(OnlineConnection observer, PreviewMap map,
      long guardId) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
    while (System.nanoTime() < deadline) {
      var view = observer.view();
      if (view != null) {
        var creature = guard(view, guardId);
        if (creature.health() > 0 && creature.x() > 30.75
            && pixelDistance(30.5, 12.5, creature.x(), creature.y()) < 48
            && map.allowsCombat(30.5, 12.5, creature.x(), creature.y(), false)
            && guardCanReachGoal(map, (int) creature.x(), (int) creature.y(), 27, 12))
          return creature;
      }
      Thread.sleep(100);
    }
    throw new AssertionError("Authored guard never entered the verified west-flank melee window");
  }

  private static CreatureView awaitHandoffMelee(OnlineConnection second, PreviewMap map,
      long guardId) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
    while (System.nanoTime() < deadline) {
      var view = second.view();
      if (view != null) {
        var actor = own(view);
        var creature = guard(view, guardId);
        if (creature.health() > 0
            && pixelDistance(actor.x(), actor.y(), creature.x(), creature.y())
                <= PLAYER_MELEE_ATTACK_RANGE
            && map.allowsCombat(actor.x(), actor.y(), creature.x(), creature.y(), false))
          return creature;
      }
      Thread.sleep(100);
    }
    throw new AssertionError("Living guard did not enter B's actual legal native melee range");
  }

  private static OnlineConnection.View awaitMeleeWindow(OnlineConnection attacker,
      OnlineConnection observer, PreviewMap map, long guardId) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
    while (System.nanoTime() < deadline) {
      var av = attacker.view();
      var bv = observer.view();
      if (av != null && bv != null && av.map().region().equals(bv.map().region())) {
        var actor = own(av);
        var creature = guard(bv, guardId);
        require(Math.abs(actor.x() - 30.5) < .25 && Math.abs(actor.y() - 12.5) < .25,
            "West-flank attacker moved before the single authored hit");
        if (creature.health() > 0 && creature.x() > 30.75
            && pixelDistance(actor.x(), actor.y(), creature.x(), creature.y()) < 48
            && map.allowsCombat(actor.x(), actor.y(), creature.x(), creature.y(), false)
            && guardCanReachGoal(map, (int) creature.x(), (int) creature.y(), 27, 12))
          return av;
      }
      Thread.sleep(100);
    }
    throw new AssertionError("Guard left the verified west-flank melee window before attack");
  }

  private static void validateWestRay(PreviewMap map, double x, double y,
      CreatureView guard) {
    require(Math.abs(x - 30.5) < .25 && Math.abs(y - 12.5) < .25,
        "Accepted hit changed the west-flank attacker pose");
    for (int i = 0; i <= 35; i++) {
      double sampleX = x - i * .1;
      require(map.canStand(sampleX, y)
          && map.combatEndpointsSafe(guard.x(), guard.y(), sampleX, y, false),
          "West retreat ray lacks 3 tiles plus a half-tile legal margin");
    }
    int endX = (int) (x - 3.0), endY = (int) y;
    require(endX >= 0 && endX < map.width && endY >= 0 && endY < map.height
        && guardCanReachGoal(map, (int) guard.x(), (int) guard.y(), endX, endY),
        "West retreat endpoint is outside the guard radius-one reachable component");
  }

  private static Motion walkAndObserve(OnlineConnection a, OnlineConnection b, PreviewMap map,
      double startX, double startY, long guardId, CreatureView first, long expectedEpoch)
      throws Exception {
    double lastX = first.x(), lastY = first.y();
    double firstDistance = Double.NaN, lastDistance = Double.NaN;
    double stoppedX = Double.NaN, stoppedY = Double.NaN;
    double stoppedCreatureX = Double.NaN, stoppedCreatureY = Double.NaN;
    boolean toward = false;
    int samples = 0, settledSamples = 0;
    boolean retreated = false;
    double lastActorX = Double.NaN, lastActorY = Double.NaN;
    int lastHp = -1;
    long lastEpoch = -1;
    long settledAt = 0, settledTick = -1, settledMillis = 0;
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(16);
    while (System.nanoTime() < deadline) {
      var av = a.view();
      var bv = b.view();
      require(av != null && bv != null && av.map().region().equals(bv.map().region()),
          "Adapter disconnected or crossed region during pursuit");
      var actor = own(av);
      lastActorX = actor.x(); lastActorY = actor.y();
      lastHp = actor.hp(); lastEpoch = av.map().locationEpoch();
      require(lastHp > 0 && lastEpoch == expectedEpoch,
          "Attacker died, respawned or changed location epoch during escape");
      var creature = bv.snapshot().creatures().stream().filter(c -> c.id() == guardId)
          .findFirst().orElseThrow(() -> new AssertionError("Provoked guard disappeared"));
      require(creature.health() > 0 && map.canStand(creature.x(), creature.y()),
          "Guard died or entered blocked terrain before motion proof");
      lastX = creature.x(); lastY = creature.y();
      samples++;
      if (!retreated) {
        require(Math.abs(actor.y() - startY) < .25 && startX - actor.x() < 3.5
            && map.canStand(actor.x(), actor.y()),
            "Straight west retreat changed row, crossed collision or exceeded margin");
        // The server finishes a claimed tile (single-player's grid reservation rule), so a released key
        // still completes the tile it was gliding into. Stop once the third west tile is claimed (past 2.0
        // tiles from the tile-centre start); the glide then ends exactly 3.0 tiles away, never 4.
        if (startX - actor.x() >= 2.05) {
          retreated = true;
          a.stopInput();
        } else a.direction(-1, 0);
      } else {
        a.stopInput();
        if (Double.isNaN(stoppedX)
            || pixelDistance(stoppedX, stoppedY, actor.x(), actor.y()) > 2) {
          stoppedX = actor.x(); stoppedY = actor.y();
          stoppedCreatureX = creature.x(); stoppedCreatureY = creature.y();
          firstDistance = pixelDistance(creature.x(), creature.y(), stoppedX, stoppedY);
          settledAt = System.nanoTime();
          settledTick = -1;
          settledSamples = 0;
        }
        if (bv.snapshot().tick() > settledTick) {
          settledTick = bv.snapshot().tick();
          settledSamples++;
        }
        settledMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - settledAt);
        lastDistance = pixelDistance(creature.x(), creature.y(), stoppedX, stoppedY);
        toward = settledSamples >= 3 && settledMillis >= 500
            && firstDistance - lastDistance > 8
            && pixelDistance(stoppedCreatureX, stoppedCreatureY, creature.x(), creature.y()) > 8;
      }
      if (retreated && toward) break;
      Thread.sleep(30);
    }
    a.stopInput();
    require(retreated, "Attacker failed straight west retreat: actor=" + lastActorX + ","
        + lastActorY + ", guard=" + lastX + "," + lastY + ", hp=" + lastHp
        + ", epoch=" + lastEpoch);
    return new Motion(first.x(), first.y(), lastX, lastY, firstDistance, lastDistance,
        samples, settledSamples, settledMillis, toward);
  }

  private static OnlineStore.SpawnState checkpoint(OnlineStore store, String key,
      OnlineStore.SpawnState before, java.util.UUID target, long targetEpoch,
      OnlineConnection attacker, OnlineConnection observer, long guardId) throws Exception {
    long started = System.nanoTime();
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
    String last = "no samples";
    while (System.nanoTime() < deadline) {
      var saved = store.loadOrCreateSpawn(key);
      var view = observer.view();
      var attackerView = attacker.view();
      require(attackerView != null && own(attackerView).hp() > 0
          && attackerView.map().locationEpoch() == targetEpoch,
          "Attacker died, respawned or changed location epoch before durable checkpoint");
      var creature = view == null ? null : view.snapshot().creatures().stream()
          .filter(c -> c.id() == guardId).findFirst().orElse(null);
      double delta = creature == null || saved.currentX() == null || saved.currentY() == null
          ? Double.NaN : pixelDistance(creature.x() + view.map().originX(),
              creature.y() + view.map().originY(), saved.currentX(), saved.currentY());
      last = "mode=" + saved.mode() + ", target=" + target.equals(saved.targetCharacter())
          + ", epoch=" + saved.targetEpoch() + ", revision=" + saved.stateRevision()
          + ", position=" + (saved.currentX() != null) + ", publicPoseDeltaPx=" + delta
          + ", savedHp=" + saved.health() + ", publicHp="
          + (creature == null ? "missing" : creature.health());
      if (System.nanoTime() - started >= TimeUnit.SECONDS.toNanos(5)
          && saved.currentX() != null && saved.currentY() != null && view != null
          && saved.stateRevision() > before.stateRevision()
          && saved.eventId().equals(before.eventId())
          && "RETALIATION".equals(saved.mode())
          && target.equals(saved.targetCharacter())
          && saved.targetEpoch() != null && saved.targetEpoch() == targetEpoch) {
        if (creature != null && creature.health() == saved.health()
            && Math.abs(creature.x() + view.map().originX() - saved.currentX()) < .25
            && Math.abs(creature.y() + view.map().originY() - saved.currentY()) < .25)
          return saved;
      }
      Thread.sleep(100);
    }
    throw new AssertionError("No durable guarded creature checkpoint matched public current pose: " + last);
  }

  private static void samePose(OnlineStore.SpawnState expected, OnlineStore.SpawnState actual,
      String message) {
    require(expected.currentX() != null && expected.currentY() != null
        && actual.currentX() != null && actual.currentY() != null
        && expected.mode() != null && actual.mode() != null
        && expected.eventId().equals(actual.eventId())
        && java.util.Objects.equals(expected.health(), actual.health())
        && expected.currentX().equals(actual.currentX())
        && expected.currentY().equals(actual.currentY())
        && expected.mode().equals(actual.mode())
        && java.util.Objects.equals(expected.targetCharacter(), actual.targetCharacter())
        && java.util.Objects.equals(expected.targetEpoch(), actual.targetEpoch())
        && actual.stateRevision() >= expected.stateRevision(), message);
  }

  private static boolean privateMatches(OnlineConnection.View view, OnlineStore.CharacterState saved) {
    if (view == null || view.character() == null || view.snapshot() == null
        || view.character().progress() == null) return false;
    var character = view.character();
    var actor = view.snapshot().players().stream().filter(p -> p.id() == view.ownId())
        .findFirst().orElse(null);
    return actor != null && character.id().equals(saved.id())
        && character.revision() == saved.revision()
        && character.progress().equals(saved.progress())
        && character.gold() == saved.gold() && character.experience() == saved.experience()
        && character.locationEpoch() == saved.locationEpoch()
        && view.map().world() == saved.world()
        && actor.hp() == saved.progress().vitals().hp()
        && Math.abs(actor.x() + view.map().originX() - saved.x()) < .25
        && Math.abs(actor.y() + view.map().originY() - saved.y()) < .25;
  }

  /** A normal final checkpoint advances revision; a later B strike may also lower B's HP. */
  private static boolean samePrivateApartFromRevision(OnlineStore.CharacterState expected,
      OnlineStore.CharacterState actual, boolean allowHpLoss) {
    if (!expected.id().equals(actual.id()) || !expected.accountId().equals(actual.accountId())
        || !expected.name().equals(actual.name()) || expected.world() != actual.world()
        || Double.compare(expected.x(), actual.x()) != 0 || Double.compare(expected.y(), actual.y()) != 0
        || expected.gold() != actual.gold() || expected.experience() != actual.experience()
        || expected.locationEpoch() != actual.locationEpoch()
        || expected.portalCooldownUntil() != actual.portalCooldownUntil()
        || actual.revision() < expected.revision()) return false;
    var progress = actual.progress();
    if (allowHpLoss) {
      var prior = expected.progress().vitals();
      var current = progress.vitals();
      if (current.hp() <= 0 || current.hp() > prior.hp()) return false;
      progress = new CharacterProgress(progress.version(), progress.characterClass(), progress.gender(),
          progress.attributes(), new CharacterProgress.Vitals(prior.hp(), current.maxHp(),
              current.mana(), current.maxMana()), progress.advancement(), progress.inventory(),
          progress.equipment(), progress.skills(), progress.spells(), progress.questFlags(),
          progress.storage(), progress.storageGold(), progress.respawn(), progress.buffs(),
          progress.cooldowns(), progress.hiddenUntil(), progress.companion());
    }
    return expected.progress().equals(progress);
  }

  /** Conservative static version of the creature's radius-one intermediate-cell A* clearance. */
  private static boolean[] guardReachable(PreviewMap map, int guardX, int guardY) {
    int width = map.width, area = width * map.height;
    boolean[] seen = new boolean[area];
    var queue = new ArrayDeque<Integer>();
    int start = guardY * width + guardX;
    seen[start] = true;
    queue.add(start);
    while (!queue.isEmpty()) {
      int node = queue.remove(), x = node % width, y = node / width;
      for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
        if (dx == 0 && dy == 0) continue;
        int nx = x + dx, ny = y + dy;
        if (nx < 0 || nx >= width || ny < 0 || ny >= map.height
            || !creatureClear(map, nx, ny)
            || (dx != 0 && dy != 0 && (!creatureClear(map, x + dx, y)
                || !creatureClear(map, x, y + dy)))) continue;
        int next = ny * width + nx;
        if (!seen[next]) { seen[next] = true; queue.add(next); }
      }
    }
    return seen;
  }

  private static boolean guardCanReachGoal(PreviewMap map, int guardX, int guardY,
      int goalX, int goalY) {
    if (goalX < 0 || goalX >= map.width || goalY < 0 || goalY >= map.height
        || !map.canStand(goalX + .5, goalY + .5)) return false;
    boolean[] reachable = guardReachable(map, guardX, guardY);
    if (reachable[goalY * map.width + goalX]) return true;
    // Production A* requires radius-one clearance at intermediate cells, while
    // the destination needs only its own legal footprint.
    for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
      if (dx == 0 && dy == 0) continue;
      int previousX = goalX - dx, previousY = goalY - dy;
      if (previousX < 0 || previousX >= map.width || previousY < 0
          || previousY >= map.height || !reachable[previousY * map.width + previousX]
          || (dx != 0 && dy != 0 && (!creatureClear(map, previousX + dx, previousY)
              || !creatureClear(map, previousX, previousY + dy)))) continue;
      return true;
    }
    return false;
  }

  private static boolean creatureClear(PreviewMap map, int x, int y) {
    for (int yy = y - 1; yy <= y + 1; yy++)
      for (int xx = x - 1; xx <= x + 1; xx++)
        if (!map.canStand(xx + .5, yy + .5)) return false;
    return true;
  }

  private static void available(OnlineStore store, Player player, int seconds) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
    while (System.nanoTime() < deadline) {
      boolean inUse = store.roster(player.key().secret(), null).stream()
          .filter(r -> r.character().id().equals(player.created().id()))
          .findFirst().orElseThrow().inUse();
      if (!inUse) return;
      Thread.sleep(250);
    }
    throw new AssertionError("Normal server lease did not release before reconnect");
  }

  private static Process start(Path java, Path jar, Path map, Path output, int number)
      throws Exception {
    return start(java, jar, map, output, number, PORT);
  }

  private static Process start(Path java, Path jar, Path map, Path output, int number, int port)
      throws Exception {
    Path control = output.resolve("control-" + number + ".token");
    var builder = new ProcessBuilder(java.toString(), "-Djava.awt.headless=true", "-jar",
        jar.toString(), "--online", map.toString(), Integer.toString(port));
    builder.directory(jar.getParent().toFile());
    builder.environment().put("T4C_CONTROL_FILE", control.toString());
    builder.environment().put("T4C_BIND_ADDRESS", "127.0.0.1");
    builder.redirectOutput(output.resolve("server-" + number + ".out.log").toFile());
    builder.redirectError(output.resolve("server-" + number + ".err.log").toFile());
    return builder.start();
  }

  private static void healthy(HttpClient http, URI endpoint, Process child) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(START_SECONDS);
    while (System.nanoTime() < deadline) {
      require(child.isAlive(), "Owned pursuit server exited before health readiness");
      try {
        var reply = http.send(HttpRequest.newBuilder(endpoint.resolve("/health"))
            .timeout(Duration.ofSeconds(2)).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (reply.statusCode() == 200 && reply.body().contains("local-persistent")) return;
      } catch (java.io.IOException ignored) { }
      Thread.sleep(200);
    }
    throw new AssertionError("Owned pursuit server did not become healthy");
  }

  private static void graceful(HttpClient http, URI endpoint, Process child, Path control)
      throws Exception {
    require(child.isAlive() && Files.isRegularFile(control) && Files.size(control) <= 65,
        "Owned pursuit server/control unavailable");
    String token = Files.readString(control).trim();
    require(token.matches("[0-9a-f]{64}"), "Private control token malformed");
    var reply = http.send(HttpRequest.newBuilder(endpoint.resolve("/admin/shutdown"))
        .timeout(Duration.ofSeconds(4)).header("Origin", endpoint.toString())
        .header("Authorization", "Bearer " + token).header("Content-Type", "text/plain")
        .POST(HttpRequest.BodyPublishers.ofString("")).build(),
        HttpResponse.BodyHandlers.discarding());
    require(reply.statusCode() == 200, "Private pursuit shutdown was rejected");
    require(child.waitFor(STOP_SECONDS, TimeUnit.SECONDS),
        "Graceful pursuit server drain exceeded deadline");
    require(child.exitValue() == 0, "Graceful pursuit server exited with failure");
  }

  private static int schema(String url, String user, String password) throws Exception {
    try (var connection = DriverManager.getConnection(url, user, password);
        var statement = connection.createStatement();
        var rows = statement.executeQuery("SELECT max(version) FROM online_schema")) {
      if (!rows.next()) throw new AssertionError("Missing online schema");
      return rows.getInt(1);
    }
  }

  private static String sha(Path file) throws Exception {
    try (var input = Files.newInputStream(file)) {
      var digest = MessageDigest.getInstance("SHA-256");
      byte[] buffer = new byte[64 * 1024];
      for (int n; (n = input.read(buffer)) >= 0;) digest.update(buffer, 0, n);
      return HexFormat.of().formatHex(digest.digest());
    }
  }

  private static String progressSha(OnlineStore.CharacterState state) throws Exception {
    byte[] json = new com.google.gson.Gson().toJson(state.progress()).getBytes(StandardCharsets.UTF_8);
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json));
  }

  private static double pixelDistance(double ax, double ay, double bx, double by) {
    return Math.hypot((ax - bx) * 32, (ay - by) * 16);
  }

  private static void append(Path file, String text) throws Exception {
    Files.writeString(file, text, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
}
