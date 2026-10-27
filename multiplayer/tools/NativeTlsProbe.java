package com.perso.T4C.server;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.server.persistence.OnlineStore;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLHandshakeException;

/** Bounded local TLS acceptance through two real native adapters and an owned persistent server. */
public final class NativeTlsProbe {
  private static final int START_SECONDS = 40;
  private record Player(OnlineStore.AccountKey key, OnlineStore.CharacterState created,
      OnlineConnection.Credentials credentials) {}

  public static void main(String[] args) throws Exception {
    if (args.length == 1 && args[0].equals("refuse")) { refuse(); return; }
    if (args.length != 4) throw new IllegalArgumentException(
        "Usage: NativeTlsProbe RUNTIME_DIR JAVA_EXE UNUSED_LOOPBACK_PORT NEW_OUTPUT_DIR");
    Path runtime = Path.of(args[0]).toRealPath(), java = Path.of(args[1]).toRealPath();
    int port = Integer.parseInt(args[2]);
    if (port < 1024 || port > 65535 || port == 18765 || port == 18766)
      throw new IllegalArgumentException("Use a dedicated loopback port outside owner QA");
    Path output = Path.of(args[3]).toAbsolutePath().normalize();
    if (!Files.isDirectory(output.getParent()) || Files.exists(output))
      throw new IllegalArgumentException("Output must be a new directory with an existing parent");
    output = output.getParent().toRealPath().resolve(output.getFileName());
    Path checkout = Path.of("").toAbsolutePath().normalize();
    if (runtime.startsWith(checkout) || output.startsWith(checkout))
      throw new IllegalArgumentException("Runtime and output must be outside this checkout");
    Path jar = runtime.resolve("t4c-server.jar");
    Path map = runtime.resolve("assets/maps/worldmap/worldmap.colbin");
    Path dungeon = runtime.resolve("assets/maps/dungeon/DungeonMap.colbin");
    if (!Files.isRegularFile(jar) || !Files.isRegularFile(map) || !Files.isRegularFile(dungeon))
      throw new IllegalArgumentException("Runtime snapshot is incomplete");
    for (String name : List.of("T4C_DB_URL", "T4C_DB_USER", "T4C_DB_PASSWORD"))
      if (System.getenv(name) == null || System.getenv(name).isBlank())
        throw new IllegalArgumentException("Dedicated PostgreSQL environment is incomplete");
    if (!System.getenv("T4C_DB_URL").matches(
        "jdbc:postgresql://(?:127\\.0\\.0\\.1|localhost):[0-9]{2,5}/t4c_tls_0216_[a-z0-9_]+"))
      throw new IllegalArgumentException("Use only a fresh local t4c_tls_0216_* database");
    for (String entry : System.getProperty("java.class.path").split(System.getProperty("path.separator"))) {
      String path = entry.endsWith("*") ? entry.substring(0, entry.length()-1) : entry;
      if (!Path.of(path).toAbsolutePath().normalize().startsWith(runtime))
        throw new IllegalArgumentException("Probe classpath must be wholly inside the copied runtime");
    }
    try (var socket = new ServerSocket()) {
      socket.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port));
    } catch (Exception occupied) { throw new IllegalArgumentException("Dedicated port is occupied", occupied); }
    Files.createDirectory(output);
    Path report = output.resolve("report.txt");
    Files.writeString(report, "T4C-0216 native TLS local probe started="+Instant.now()+" port="+port+"\n"
        +"No account secret, database password, fixture password or control token is recorded here.\n",
        StandardOpenOption.CREATE_NEW);
    Path password = output.resolve("fixture-password.txt");
    Files.writeString(password, UUID.randomUUID().toString().replace("-", ""), StandardOpenOption.CREATE_NEW);
    Path keystore = output.resolve("server.p12"), certificate = output.resolve("server.cer");
    Path truststore = output.resolve("client-trust.p12"), emptyTruststore = output.resolve("empty-trust.p12");
    String keytool = Path.of(java.getParent().toString(), "keytool"+
        (System.getProperty("os.name").startsWith("Windows") ? ".exe" : "")).toString();
    keytool(output, keytool, "-genkeypair", "-alias", "server", "-keyalg", "RSA", "-keysize", "2048",
        "-validity", "1", "-dname", "CN=localhost", "-ext", "SAN=dns:localhost", "-storetype", "PKCS12",
        "-keystore", keystore.toString(), "-storepass:file", password.toString(), "-noprompt");
    keytool(output, keytool, "-exportcert", "-alias", "server", "-keystore", keystore.toString(),
        "-storepass:file", password.toString(), "-file", certificate.toString());
    keytool(output, keytool, "-importcert", "-alias", "server", "-file", certificate.toString(),
        "-keystore", truststore.toString(), "-storetype", "PKCS12", "-storepass:file", password.toString(), "-noprompt");
    var empty = KeyStore.getInstance("PKCS12");
    empty.load(null, null);
    try (var stream = Files.newOutputStream(emptyTruststore, StandardOpenOption.CREATE_NEW)) {
      empty.store(stream, Files.readString(password).toCharArray());
    }
    // Standard JSSE default trust and hostname checks. Set before creating any HTTPS client.
    System.setProperty("javax.net.ssl.trustStore", truststore.toString());
    System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
    System.setProperty("javax.net.ssl.trustStorePassword", Files.readString(password));
    URI endpoint = URI.create("https://localhost:"+port);
    Process server = null;
    String stage = "startup";
    try (var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        var store = OnlineStore.fromEnvironment()) {
      var command = new ProcessBuilder(java.toString(), "-Djava.awt.headless=true", "-jar", jar.toString(),
          "--online", map.toString(), Integer.toString(port));
      command.directory(runtime.toFile());
      command.environment().put("T4C_BIND_ADDRESS", "127.0.0.1");
      command.environment().put("T4C_PUBLIC_ORIGIN", endpoint.toString());
      command.environment().put("T4C_TLS_KEYSTORE", keystore.toString());
      command.environment().put("T4C_TLS_PASSWORD_FILE", password.toString());
      command.environment().put("T4C_CONTROL_FILE", output.resolve("control.token").toString());
      command.redirectOutput(output.resolve("server.out.log").toFile());
      command.redirectError(output.resolve("server.err.log").toFile());
      server = command.start();
      healthy(http, endpoint, server);
      stage = "provisioning";
      var plan = HostedRegionPlan.startingSlice(map);
      var spawn = plan.regions().getFirst().spec();
      var firstKit = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(2161));
      var secondKit = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(2162));
      Player first = provision(store, "TLS probe first", spawn.world(),spawn.spawnX()+.5,spawn.spawnY()+.5,firstKit);
      Player second = provision(store, "TLS probe second", spawn.world(),spawn.spawnX()+.5,spawn.spawnY()+.5,secondKit);
      stage = "refusal before authentication";
      refusal(java, output, emptyTruststore, password, endpoint, first);
      refusal(java, output, truststore, password, URI.create("https://127.0.0.1:"+port), second);
      require(!inUse(store, first) && !inUse(store, second), "TLS refusals allocated a character lease");
      append(report, "Unknown CA and wrong hostname: standard JSSE handshake refusal observed; both native adapters disconnected with no private view or account lease.\n");
      stage = "trusted two-adapter movement";
      double movedX;
      try (var a = new OnlineConnection(endpoint, first.credentials());
          var b = new OnlineConnection(endpoint, second.credentials())) {
        connected(a, first); connected(b, second);
        require(a.view().map().region().equals(b.view().map().region()), "Adapters joined different regions");
        waitFor(() -> sees(a,b) && sees(b,a), 8, "Adapters did not observe each other");
        var before = own(a.view());
        a.direction(1,0);
        try { waitFor(() -> a.view()!=null && own(a.view()).x()>before.x()+.15, 8,
            "Native direction did not move the authoritative actor"); }
        finally { a.stopInput(); }
        movedX = own(a.view()).x();
        waitFor(() -> b.view()!=null && b.view().snapshot().players().stream()
            .anyMatch(p -> p.id()==a.view().ownId() && p.x()>=movedX-.05), 8,
            "Second adapter did not see first actor move");
      }
      stage = "reconnect";
      waitFor(() -> !inUse(store,first) && !inUse(store,second), 8, "Native leave did not release leases");
      var savedA = store.load(first.key().accountId(),first.created().id());
      var savedB = store.load(second.key().accountId(),second.created().id());
      require(savedA.x()>first.created().x()+.15 && savedB.id().equals(second.created().id()),
          "Committed movement or second identity missing");
      try (var a = new OnlineConnection(endpoint,first.credentials());
          var b = new OnlineConnection(endpoint,second.credentials())) {
        connected(a,first); connected(b,second);
        require(a.view().character().id().equals(savedA.id()) && b.view().character().id().equals(savedB.id()),
            "Reconnect changed private character identities");
        require(Math.abs(own(a.view()).x()+a.view().map().originX()-savedA.x())<.25,
            "Reconnect did not restore committed global position");
        waitFor(() -> sees(a,b) && sees(b,a), 8, "Reconnected adapters lost mutual state");
      }
      append(report,"Trusted TLS: two native joins, mutual state, authoritative movement, committed position, two reconnects.\n");
      stage = "graceful shutdown";
      shutdown(http,endpoint,server,output.resolve("control.token"));
      server = null;
      append(report,"PASS: owned TLS server exited 0. Native adapters used certificate-only JVM trust.\n");
      System.out.println("PASS T4C-0216; sanitized report: "+report);
    } catch (Exception | Error failure) {
      append(report,"FAIL at "+stage+": "+failure.getClass().getSimpleName()+". Preserve private fixture/logs.\n");
      throw failure;
    } finally {
      if (server!=null && server.isAlive()) {
        boolean graceful=false;
        try (var cleanup = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()) {
          if (Files.isRegularFile(output.resolve("control.token"))) {
            shutdown(cleanup,endpoint,server,output.resolve("control.token"));
            graceful=true;
          }
        } catch (Exception | Error ignored) { /* Retain the original failure and private logs. */ }
        if (!graceful && server.isAlive()) {
          server.destroyForcibly(); server.waitFor(10,TimeUnit.SECONDS);
          append(report,"Owned-server cleanup required forced fallback; inspect private logs.\n");
        }
      }
    }
  }

  private static void refuse() throws Exception {
    try (var input = new BufferedReader(new InputStreamReader(System.in,StandardCharsets.UTF_8))) {
      System.setProperty("javax.net.ssl.trustStorePassword",input.readLine());
      URI endpoint = URI.create(input.readLine());
      var credentials = new OnlineConnection.Credentials(input.readLine(),UUID.fromString(input.readLine()));
      try (var client = HttpClient.newHttpClient()) {
        try {
          client.send(HttpRequest.newBuilder(endpoint.resolve("/map")).timeout(Duration.ofSeconds(3)).GET().build(),
              HttpResponse.BodyHandlers.discarding());
          throw new AssertionError("TLS handshake unexpectedly accepted the refusal endpoint");
        } catch (java.io.IOException expected) {
          boolean handshake=false;
          for (Throwable cause=expected;cause!=null;cause=cause.getCause())
            if (cause instanceof SSLHandshakeException) handshake=true;
          require(handshake,"Refusal was not an SSL handshake failure");
        }
      }
      try (var connection = new OnlineConnection(endpoint,credentials)) {
        waitFor(() -> connection.status().equals("disconnected"), 10,"TLS refusal did not disconnect");
        require(connection.view()==null,"TLS refusal exposed a private view");
      }
    }
  }

  private static void refusal(Path java,Path output,Path truststore,Path password,URI endpoint,Player player)
      throws Exception {
    String mode = endpoint.getHost().equals("localhost") ? "unknown-ca" : "wrong-host";
    var process = new ProcessBuilder(java.toString(),"-Djavax.net.ssl.trustStore="+truststore,
        "-Djavax.net.ssl.trustStoreType=PKCS12",
        "-cp",System.getProperty("java.class.path"),NativeTlsProbe.class.getName(),"refuse");
    process.redirectOutput(output.resolve(mode+".out.log").toFile());
    process.redirectError(output.resolve(mode+".err.log").toFile());
    Process child = process.start();
    try {
      try (var input = child.getOutputStream()) {
        input.write((Files.readString(password)+"\n"+endpoint+"\n"+player.key().secret()+"\n"
            +player.created().id()+"\n")
            .getBytes(StandardCharsets.UTF_8));
      }
      require(child.waitFor(15,TimeUnit.SECONDS) && child.exitValue()==0,
          "Native "+mode+" refusal did not complete; inspect private child log");
    } finally { if(child.isAlive()) { child.destroyForcibly(); child.waitFor(5,TimeUnit.SECONDS); } }
  }

  private static Player provision(OnlineStore store,String name,int world,double x,double y,
      OnlineCharacterCreator.Prepared kit) throws Exception {
    var key = store.provisionAccount();
    var created = store.create(key.accountId(),name,world,x,y,kit.progress(),kit.gold());
    return new Player(key,created,new OnlineConnection.Credentials(key.secret(),created.id()));
  }

  private static void healthy(HttpClient http,URI endpoint,Process child) throws Exception {
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(START_SECONDS);
    while(System.nanoTime()<until) {
      require(child.isAlive(),"Owned server exited before readiness");
      try {
        var reply=http.send(HttpRequest.newBuilder(endpoint.resolve("/health")).timeout(Duration.ofSeconds(2)).GET()
            .build(),HttpResponse.BodyHandlers.ofString());
        if(reply.statusCode()==200 && reply.body().contains("local-persistent")) return;
      } catch(java.io.IOException ignored) { }
      Thread.sleep(200);
    }
    throw new AssertionError("Owned HTTPS server did not become healthy");
  }

  private static void connected(OnlineConnection connection,Player player) throws Exception {
    waitFor(() -> connection.view()!=null || connection.status().equals("disconnected"),12,
        "Native adapter did not join");
    var view=connection.view();
    require(view!=null && view.character()!=null && view.character().id().equals(player.created().id()),
        "Native adapter failed authenticated join");
    connection.confirmRegionReady(view.map().locationEpoch());
    waitFor(() -> connection.view()!=null && connection.view().snapshot()!=null
        && connection.view().snapshot().players().stream().anyMatch(p -> p.id()==connection.view().ownId()),
        8,"Native adapter did not publish a usable own actor");
  }

  private static OnlineConnection.Actor own(OnlineConnection.View view) {
    return view.snapshot().players().stream().filter(actor -> actor.id()==view.ownId()).findFirst()
        .orElseThrow(() -> new AssertionError("Own actor absent"));
  }
  private static boolean sees(OnlineConnection first,OnlineConnection second) {
    var a=first.view(); var b=second.view();
    return a!=null && b!=null && a.snapshot().players().stream().anyMatch(p -> p.id()==b.ownId());
  }
  private static boolean inUse(OnlineStore store,Player player) throws Exception {
    return store.roster(player.key().secret(),null).stream()
        .filter(e -> e.character().id().equals(player.created().id())).findFirst().orElseThrow().inUse();
  }
  private static void shutdown(HttpClient http,URI endpoint,Process child,Path control) throws Exception {
    require(child.isAlive() && Files.isRegularFile(control),"Owned server/control unavailable");
    String token=Files.readString(control).trim();
    require(token.matches("[0-9a-f]{64}"),"Private control token malformed");
    var reply=http.send(HttpRequest.newBuilder(endpoint.resolve("/admin/shutdown"))
        .timeout(Duration.ofSeconds(5)).header("Origin",endpoint.toString())
        .header("Authorization","Bearer "+token).header("Content-Type","text/plain")
        .POST(HttpRequest.BodyPublishers.ofString("")).build(),HttpResponse.BodyHandlers.discarding());
    require(reply.statusCode()==200,"Private HTTPS admin stop rejected");
    require(child.waitFor(100,TimeUnit.SECONDS) && child.exitValue()==0,"Owned server did not exit cleanly");
  }
  private static void keytool(Path output,String exe,String...args) throws Exception {
    var command=new java.util.ArrayList<String>(); command.add(exe); command.addAll(List.of(args));
    var process=new ProcessBuilder(command).redirectErrorStream(true)
        .redirectOutput(output.resolve("keytool-"+System.nanoTime()+".log").toFile()).start();
    try { require(process.waitFor(30,TimeUnit.SECONDS) && process.exitValue()==0,"Fixture keytool failed"); }
    finally { if(process.isAlive()) { process.destroyForcibly(); process.waitFor(5,TimeUnit.SECONDS); } }
  }
  private static void waitFor(Check condition,int seconds,String message) throws Exception {
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(seconds);
    while(System.nanoTime()<until) { if(condition.ok()) return; Thread.sleep(25); }
    throw new AssertionError(message);
  }
  private interface Check { boolean ok() throws Exception; }
  private static void append(Path file,String value) throws Exception {
    Files.writeString(file,value,StandardCharsets.UTF_8,StandardOpenOption.APPEND);
  }
  private static void require(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
}
