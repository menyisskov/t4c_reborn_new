package com.perso.T4C.server;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.server.persistence.OnlineStore;
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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Local two-native-session acceptance of operator account-key rotation and writer fencing. */
public final class NativeKeyRotationProbe {
  private static final int START_SECONDS=40;
  private record Player(OnlineStore.AccountKey key,OnlineStore.CharacterState created,
      OnlineConnection.Credentials credentials) {}
  private interface Check { boolean ok() throws Exception; }

  public static void main(String[] args) throws Exception {
    if(args.length!=4) throw new IllegalArgumentException(
        "Usage: NativeKeyRotationProbe RUNTIME_DIR JAVA_EXE UNUSED_LOOPBACK_PORT NEW_OUTPUT_DIR");
    Path runtime=Path.of(args[0]).toRealPath(),java=Path.of(args[1]).toRealPath();
    int port=Integer.parseInt(args[2]);
    if(port<1024 || port>65535 || port==18765 || port==18766)
      throw new IllegalArgumentException("Use a dedicated loopback port outside owner QA");
    Path output=Path.of(args[3]).toAbsolutePath().normalize();
    if(!Files.isDirectory(output.getParent()) || Files.exists(output))
      throw new IllegalArgumentException("Output must be new, with an existing parent");
    output=output.getParent().toRealPath().resolve(output.getFileName());
    Path checkout=Path.of("").toAbsolutePath().normalize();
    if(runtime.startsWith(checkout) || output.startsWith(checkout))
      throw new IllegalArgumentException("Runtime and output must be outside this checkout");
    Path jar=runtime.resolve("t4c-server.jar"),map=runtime.resolve("assets/maps/worldmap/worldmap.colbin");
    if(!Files.isRegularFile(jar) || !Files.isRegularFile(map)
        || !Files.isRegularFile(runtime.resolve("assets/maps/dungeon/DungeonMap.colbin")))
      throw new IllegalArgumentException("Runtime snapshot is incomplete");
    for(String name:List.of("T4C_DB_URL","T4C_DB_USER","T4C_DB_PASSWORD"))
      if(System.getenv(name)==null || System.getenv(name).isBlank())
        throw new IllegalArgumentException("Dedicated PostgreSQL environment is incomplete");
    if(!System.getenv("T4C_DB_URL").matches(
        "jdbc:postgresql://(?:127\\.0\\.0\\.1|localhost):[0-9]{2,5}/t4c_rotation_0218_[a-z0-9_]+"))
      throw new IllegalArgumentException("Use only a fresh local t4c_rotation_0218_* database");
    for(String name:List.of("T4C_TLS_KEYSTORE","T4C_TLS_PASSWORD_FILE","T4C_PUBLIC_ORIGIN"))
      if(System.getenv(name)!=null && !System.getenv(name).isBlank())
        throw new IllegalArgumentException("This probe requires local loopback HTTP");
    String bind=System.getenv("T4C_BIND_ADDRESS");
    if(bind!=null && !bind.isBlank() && !bind.equals("127.0.0.1"))
      throw new IllegalArgumentException("This probe requires a loopback listener");
    for(String entry:System.getProperty("java.class.path").split(System.getProperty("path.separator"))) {
      String path=entry.endsWith("*") ? entry.substring(0,entry.length()-1) : entry;
      if(!Path.of(path).toAbsolutePath().normalize().startsWith(runtime))
        throw new IllegalArgumentException("Probe classpath must be wholly in copied runtime");
    }
    try(var socket=new ServerSocket()) {
      socket.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),port));
    } catch(Exception occupied) { throw new IllegalArgumentException("Dedicated port is occupied",occupied); }
    Files.createDirectory(output);
    Path report=output.resolve("report.txt");
    Files.writeString(report,"T4C-0218 native key rotation started="+Instant.now()+" port="+port+"\n"
        +"No account key, database password or control token is recorded here.\n",StandardOpenOption.CREATE_NEW);
    URI endpoint=URI.create("http://127.0.0.1:"+port);
    Process server=null;
    String stage="startup";
    try(var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        var store=OnlineStore.fromEnvironment()) {
      var command=new ProcessBuilder(java.toString(),"-Djava.awt.headless=true","-jar",jar.toString(),
          "--online",map.toString(),Integer.toString(port));
      command.directory(runtime.toFile());
      command.environment().put("T4C_BIND_ADDRESS","127.0.0.1");
      command.environment().put("T4C_CONTROL_FILE",output.resolve("control.token").toString());
      command.redirectOutput(output.resolve("server.out.log").toFile());
      command.redirectError(output.resolve("server.err.log").toFile());
      server=command.start();
      healthy(http,endpoint,server);
      stage="provisioning";
      var spawn=HostedRegionPlan.startingSlice(map).regions().getFirst().spec();
      var kitA=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(2181));
      var kitB=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(2182));
      Player a=provision(store,"Rotation probe A",spawn.world(),spawn.spawnX()+.5,spawn.spawnY()+.5,kitA);
      Player b=provision(store,"Rotation probe B",spawn.world(),spawn.spawnX()+.5,spawn.spawnY()+.5,kitB);
      stage="two active native sessions";
      try(var oldA=new OnlineConnection(endpoint,a.credentials());
          var activeB=new OnlineConnection(endpoint,b.credentials())) {
        connected(oldA,a);connected(activeB,b);
        waitFor(() -> sees(oldA,activeB) && sees(activeB,oldA),8,"Initial mutual state missing");
        require(inUse(store,a) && inUse(store,b),"Both real session leases were not active");
        var beforeB=store.load(b.key().accountId(),b.created().id());
        stage="rotation fence";
        var replacement=store.rotateAccountKey(a.key().accountId());
        var fenced=store.load(a.key().accountId(),a.created().id());
        oldA.direction(1,0);
        require(!replacement.secret().equals(a.key().secret()),"Rotation returned original key");
        require(rosterStatus(http,endpoint,a.key().secret())==401,
            "Old account key still reads roster");
        require(rosterStatus(http,endpoint,replacement.secret())==200,
            "New account key cannot read roster");
        require(rosterStatus(http,endpoint,b.key().secret())==200,
            "B account key was affected by A rotation");
        require(joinStatus(http,endpoint,a.credentials())==409,
            "Old key was not denied by actual join endpoint");
        try(var oldRejoin=new OnlineConnection(endpoint,a.credentials())) {
          waitFor(() -> oldRejoin.status().equals("disconnected"),10,"Old native key did not disconnect");
          require(oldRejoin.view()==null,"Old native key received a private view");
        }
        try { waitFor(() -> oldA.status().equals("disconnected"),15,
            "Rotated active session did not fail closed after attempted movement"); }
        finally { oldA.stopInput(); }
        var afterFence=store.load(a.key().accountId(),a.created().id());
        sameCommitted(fenced,afterFence,"Revoked native session committed after rotation");
        require(activeB.view()!=null && activeB.status().equals("connected"),
            "B native session disconnected during A rotation");
        stage="independent B movement";
        double bStart=own(activeB.view()).x();
        activeB.direction(1,0);
        try { waitFor(() -> activeB.view()!=null && own(activeB.view()).x()>bStart+.15,8,
            "B native movement stopped after A rotation"); }
        finally { activeB.stopInput(); }
        waitFor(() -> store.load(b.key().accountId(),b.created().id()).x()>beforeB.x()+.15,8,
            "B native movement did not reach the committed store");
        var movedB=store.load(b.key().accountId(),b.created().id());
        require(movedB.id().equals(beforeB.id()) && movedB.progress().equals(beforeB.progress())
            && movedB.x()>beforeB.x()+.15,"B identity/progress or committed movement changed unexpectedly");
        stage="replacement key native reconnect";
        waitFor(() -> !inUse(store,replacement.secret(),a.created().id()),8,
            "Fenced A lease remained held");
        try(var newA=connectReplacement(endpoint,new OnlineConnection.Credentials(replacement.secret(),a.created().id()),a)) {
          waitFor(() -> sees(newA,activeB) && sees(activeB,newA),8,
              "Replacement A and B did not share state");
          require(newA.view().character().id().equals(fenced.id()),"Replacement changed A identity");
          sameIdentityProgress(afterFence,store.load(a.key().accountId(),a.created().id()),
              "New-key join changed committed A progress");
          require(activeB.view()!=null && activeB.view().character().id().equals(b.created().id()),
              "B lost its private identity");
        }
        append(report,"A rotated while A/B native sessions active: old roster/join/native rejoin refused; "
            +"fenced old writer disconnected without post-rotation commit; new key rejoined same committed character. "
            +"B stayed connected, moved and retained progress.\n");
      }
      stage="graceful shutdown";
      shutdown(http,endpoint,server,output.resolve("control.token"));
      server=null;
      append(report,"PASS: owned persistent server exited 0. No lease override or mutation replay.\n");
      System.out.println("PASS T4C-0218; sanitized report: "+report);
    } catch(Exception | Error failure) {
      append(report,"FAIL at "+stage+": "+failure.getClass().getSimpleName()+". Preserve private logs.\n");
      throw failure;
    } finally {
      if(server!=null && server.isAlive()) {
        boolean graceful=false;
        try(var cleanup=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()) {
          if(Files.isRegularFile(output.resolve("control.token"))) {
            shutdown(cleanup,endpoint,server,output.resolve("control.token"));graceful=true;
          }
        } catch(Exception | Error ignored) { /* Preserve original failure and logs. */ }
        if(!graceful && server.isAlive()) {
          server.destroyForcibly();server.waitFor(10,TimeUnit.SECONDS);
          append(report,"Owned-server cleanup required forced fallback.\n");
        }
      }
    }
  }

  private static Player provision(OnlineStore store,String name,int world,double x,double y,
      OnlineCharacterCreator.Prepared kit) throws Exception {
    var key=store.provisionAccount();
    var created=store.create(key.accountId(),name,world,x,y,kit.progress(),kit.gold());
    return new Player(key,created,new OnlineConnection.Credentials(key.secret(),created.id()));
  }
  private static void healthy(HttpClient http,URI endpoint,Process child) throws Exception {
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(START_SECONDS);
    while(System.nanoTime()<until) {
      require(child.isAlive(),"Owned server exited before health");
      try {
        var reply=http.send(HttpRequest.newBuilder(endpoint.resolve("/health")).timeout(Duration.ofSeconds(2)).GET()
            .build(),HttpResponse.BodyHandlers.ofString());
        if(reply.statusCode()==200 && reply.body().contains("local-persistent")) return;
      } catch(java.io.IOException ignored) { }
      Thread.sleep(200);
    }
    throw new AssertionError("Owned persistent server did not become healthy");
  }
  private static void connected(OnlineConnection connection,Player player) throws Exception {
    waitFor(() -> connection.view()!=null || connection.status().equals("disconnected"),12,
        "Native adapter did not join");
    var view=connection.view();
    require(view!=null && view.character()!=null && view.character().id().equals(player.created().id()),
        "Native adapter failed selected-character join");
    connection.confirmRegionReady(view.map().locationEpoch());
    waitFor(() -> connection.view()!=null && connection.view().snapshot()!=null
        && connection.view().snapshot().players().stream().anyMatch(p -> p.id()==connection.view().ownId()),
        8,"Native adapter did not publish own actor");
  }
  private static OnlineConnection connectReplacement(URI endpoint,OnlineConnection.Credentials credentials,Player player)
      throws Exception {
    for(int attempt=0;attempt<3;attempt++) {
      var connection=new OnlineConnection(endpoint,credentials);
      try { connected(connection,player); return connection; }
      catch(Exception | Error unavailable) {
        connection.close();
        if(attempt==2) throw unavailable;
        Thread.sleep(500);
      }
    }
    throw new AssertionError("Replacement connection unavailable");
  }
  private static OnlineConnection.Actor own(OnlineConnection.View view) {
    return view.snapshot().players().stream().filter(p -> p.id()==view.ownId()).findFirst()
        .orElseThrow(() -> new AssertionError("Own actor absent"));
  }
  private static boolean sees(OnlineConnection first,OnlineConnection second) {
    var a=first.view(); var b=second.view();
    return a!=null && b!=null && a.snapshot().players().stream().anyMatch(p -> p.id()==b.ownId());
  }
  private static boolean inUse(OnlineStore store,Player player) throws Exception {
    return inUse(store,player.key().secret(),player.created().id());
  }
  private static boolean inUse(OnlineStore store,String key,UUID character) throws Exception {
    return store.roster(key,null).stream()
        .filter(e -> e.character().id().equals(character)).findFirst().orElseThrow().inUse();
  }
  private static int rosterStatus(HttpClient http,URI endpoint,String key) throws Exception {
    return http.send(HttpRequest.newBuilder(endpoint.resolve("/account/characters"))
        .header(OnlineProtocol.VERSION_HEADER,Integer.toString(OnlineProtocol.VERSION))
        .header("Authorization","Bearer "+key).timeout(Duration.ofSeconds(4)).GET().build(),
        HttpResponse.BodyHandlers.discarding()).statusCode();
  }
  private static int joinStatus(HttpClient http,URI endpoint,OnlineConnection.Credentials credentials)
      throws Exception {
    return http.send(HttpRequest.newBuilder(endpoint.resolve("/join"))
        .header(OnlineProtocol.VERSION_HEADER,Integer.toString(OnlineProtocol.VERSION))
        .header("Origin",endpoint.toString()).header("Content-Type","text/plain")
        .timeout(Duration.ofSeconds(4)).POST(HttpRequest.BodyPublishers.ofString(
            credentials.secret()+" "+credentials.characterId())).build(),
        HttpResponse.BodyHandlers.discarding()).statusCode();
  }
  private static void sameCommitted(OnlineStore.CharacterState before,OnlineStore.CharacterState after,String message) {
    require(before.id().equals(after.id()) && before.accountId().equals(after.accountId())
        && before.world()==after.world() && Double.compare(before.x(),after.x())==0
        && Double.compare(before.y(),after.y())==0 && before.revision()==after.revision()
        && before.progress().equals(after.progress()) && before.gold()==after.gold()
        && before.experience()==after.experience(),message);
  }
  private static void sameIdentityProgress(OnlineStore.CharacterState before,OnlineStore.CharacterState after,
      String message) {
    require(before.id().equals(after.id()) && before.accountId().equals(after.accountId())
        && before.world()==after.world() && Double.compare(before.x(),after.x())==0
        && Double.compare(before.y(),after.y())==0 && after.revision()>=before.revision()
        && before.progress().equals(after.progress()) && before.gold()==after.gold()
        && before.experience()==after.experience(),message);
  }
  private static void shutdown(HttpClient http,URI endpoint,Process child,Path control) throws Exception {
    require(child.isAlive() && Files.isRegularFile(control),"Owned server/control unavailable");
    String token=Files.readString(control).trim();
    require(token.matches("[0-9a-f]{64}"),"Private control token malformed");
    var reply=http.send(HttpRequest.newBuilder(endpoint.resolve("/admin/shutdown"))
        .timeout(Duration.ofSeconds(5)).header("Origin",endpoint.toString())
        .header("Authorization","Bearer "+token).header("Content-Type","text/plain")
        .POST(HttpRequest.BodyPublishers.ofString("")).build(),HttpResponse.BodyHandlers.discarding());
    require(reply.statusCode()==200,"Private admin stop rejected");
    require(child.waitFor(100,TimeUnit.SECONDS) && child.exitValue()==0,"Owned server did not exit cleanly");
  }
  private static void waitFor(Check condition,int seconds,String message) throws Exception {
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(seconds);
    while(System.nanoTime()<until) { if(condition.ok()) return; Thread.sleep(25); }
    throw new AssertionError(message);
  }
  private static void append(Path file,String value) throws Exception {
    Files.writeString(file,value,StandardCharsets.UTF_8,StandardOpenOption.APPEND);
  }
  private static void require(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
}
