package com.perso.T4C.server;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.server.persistence.OnlineStore;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/** Bounded local acceptance of two real native adapters during hostile HTTP body admission. */
public final class NativeHttpResilienceProbe {
  private static final int START_SECONDS=40;
  private record Player(OnlineStore.AccountKey key, OnlineStore.CharacterState created,
      OnlineConnection.Credentials credentials) {}
  private interface Check { boolean ok() throws Exception; }

  public static void main(String[] args) throws Exception {
    if(args.length!=4) throw new IllegalArgumentException(
        "Usage: NativeHttpResilienceProbe RUNTIME_DIR JAVA_EXE UNUSED_LOOPBACK_PORT NEW_OUTPUT_DIR");
    Path runtime=Path.of(args[0]).toRealPath(),java=Path.of(args[1]).toRealPath();
    int port=Integer.parseInt(args[2]);
    if(port!=18777) throw new IllegalArgumentException("Use only granted loopback port 18777");
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
        "jdbc:postgresql://127\\.0\\.0\\.1:55432/t4c_http_native_0226_[a-z0-9_]+"))
      throw new IllegalArgumentException("Use only a granted fresh local probe database");
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
    Files.writeString(report,"T4C-0226 native HTTP resilience started="+Instant.now()+" port="+port+"\n"
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
      stage="private provisioning";
      var spawn=HostedRegionPlan.startingSlice(map).regions().getFirst().spec();
      var kitA=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(2261));
      var kitB=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(2262));
      Player a=provision(store,"HTTP probe A",spawn.world(),spawn.spawnX()+.5,spawn.spawnY()+.5,kitA);
      Player b=provision(store,"HTTP probe B",spawn.world(),spawn.spawnX()+.5,spawn.spawnY()+.5,kitB);
      stage="two native joins and region readiness";
      try(var nativeA=new OnlineConnection(endpoint,a.credentials());
          var nativeB=new OnlineConnection(endpoint,b.credentials())) {
        connected(nativeA,a);connected(nativeB,b);
        waitFor(() -> sees(nativeA,nativeB) && sees(nativeB,nativeA),8,
            "Both native sessions did not observe each other");
        require(inUse(store,a) && inUse(store,b),"Both private leases were not active");
        var beforeA=store.load(a.key().accountId(),a.created().id());
        var beforeB=store.load(b.key().accountId(),b.created().id());
        double startAX=own(nativeA.view()).x(),startBY=own(nativeB.view()).y();
        long tickA=nativeA.view().snapshot().tick(),tickB=nativeB.view().snapshot().tick();
        stage="held public and forged bodies";
        var sockets=new ArrayList<Socket>();
        long began=System.nanoTime();
        try {
          for(int i=0;i<8;i++) sockets.add(slowBody(port,endpoint,"/join",null));
          for(int i=0;i<8;i++) sockets.add(slowBody(port,endpoint,"/input","f".repeat(64)));
          for(int i=8;i<16;i++)
            require(responseLine(sockets.get(i)).contains(" 401 "),
                "Forged token was not refused before the body arrived");
          require(healthStatus(http,endpoint)==200,"Health diagnostic stalled behind held public bodies");
          nativeA.direction(1,0);nativeB.direction(0,1);
          try {
            waitFor(() -> nativeA.view()!=null && nativeB.view()!=null
                && nativeA.status().equals("connected") && nativeB.status().equals("connected")
                && nativeA.view().snapshot().tick()>=tickA+3
                && nativeB.view().snapshot().tick()>=tickB+3
                && own(nativeA.view()).x()>startAX+.15
                && own(nativeB.view()).y()>startBY+.15,3,
                "Native snapshots or legal cardinal movement stalled during hostile bodies");
          } finally { nativeA.stopInput();nativeB.stopInput(); }
          long elapsed=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-began);
          require(elapsed<4000,"Meaningful admission phase exceeded 4 seconds: "+elapsed+" ms");
          require(twoActors(nativeA,nativeB),"Hostile requests allocated an extra actor");
          require(inUse(store,a) && inUse(store,b),"A private lease was lost during hostile bodies");
          append(report,"Held eight incomplete joins and eight forged input bodies; all forged tokens returned 401 "
              +"before bodies; health 200; both native adapters stayed connected, advanced at least three ticks "
              +"and moved cardinally >0.15; exactly two visible actors and both leases active; phase "
              +elapsed+" ms.\n");
        } finally {
          for(var socket:sockets) try { socket.close(); } catch(Exception ignored) { }
        }
        stage="post-attack recovery";
        require(healthStatus(http,endpoint)==200,"Health did not recover after attackers closed");
        double recoverAX=own(nativeA.view()).x(),recoverBY=own(nativeB.view()).y();
        nativeA.direction(1,0);nativeB.direction(0,1);
        try {
          waitFor(() -> nativeA.view()!=null && nativeB.view()!=null
              && own(nativeA.view()).x()>recoverAX+.15 && own(nativeB.view()).y()>recoverBY+.15,
              8,"Normal native movement did not recover after attacker sockets closed");
        } finally { nativeA.stopInput();nativeB.stopInput(); }
        waitFor(() -> store.load(a.key().accountId(),a.created().id()).x()>beforeA.x()+.15
            && store.load(b.key().accountId(),b.created().id()).y()>beforeB.y()+.15,
            8,"Native movement did not commit to private persistent records");
        samePrivateProgress(beforeA,store.load(a.key().accountId(),a.created().id()));
        samePrivateProgress(beforeB,store.load(b.key().accountId(),b.created().id()));
        require(twoActors(nativeA,nativeB) && inUse(store,a) && inUse(store,b),
            "Native sessions or actor count changed after attacker close");
        append(report,"After attacker close: health 200, both native sessions moved again, "
            +"committed location advanced, and private gold, experience and progress stayed unchanged.\n");
      }
      stage="normal leave and graceful shutdown";
      waitFor(() -> !inUse(store,a) && !inUse(store,b),8,"Normal leaves retained a private lease");
      shutdown(http,endpoint,server,output.resolve("control.token"));
      server=null;
      try(var socket=new ServerSocket()) {
        socket.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),port));
      }
      append(report,"PASS: two native sessions, zero retained leases, owned server exit 0, loopback port free.\n");
      System.out.println("PASS T4C-0226; sanitized report: "+report);
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
        } catch(Exception | Error ignored) { }
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
  private static int healthStatus(HttpClient http,URI endpoint) throws Exception {
    return http.send(HttpRequest.newBuilder(endpoint.resolve("/health")).timeout(Duration.ofSeconds(2)).GET()
        .build(),HttpResponse.BodyHandlers.discarding()).statusCode();
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
  private static OnlineConnection.Actor own(OnlineConnection.View view) {
    return view.snapshot().players().stream().filter(p -> p.id()==view.ownId()).findFirst()
        .orElseThrow(() -> new AssertionError("Own actor absent"));
  }
  private static boolean sees(OnlineConnection first,OnlineConnection second) {
    var a=first.view();var b=second.view();
    return a!=null && b!=null && a.snapshot().players().stream().anyMatch(p -> p.id()==b.ownId());
  }
  private static boolean twoActors(OnlineConnection first,OnlineConnection second) {
    var a=first.view();var b=second.view();
    return a!=null && b!=null && a.snapshot()!=null && b.snapshot()!=null
        && a.snapshot().players().size()==2 && b.snapshot().players().size()==2
        && sees(first,second) && sees(second,first);
  }
  private static boolean inUse(OnlineStore store,Player player) throws Exception {
    return store.roster(player.key().secret(),null).stream()
        .filter(e -> e.character().id().equals(player.created().id())).findFirst().orElseThrow().inUse();
  }
  private static void samePrivateProgress(OnlineStore.CharacterState before,OnlineStore.CharacterState after) {
    require(before.id().equals(after.id()) && before.accountId().equals(after.accountId())
        && before.progress().equals(after.progress()) && before.gold()==after.gold()
        && before.experience()==after.experience(),
        "Hostile HTTP changed a private identity, gold, experience or progress");
  }
  private static Socket slowBody(int port,URI endpoint,String path,String token) throws Exception {
    var socket=new Socket();socket.connect(new InetSocketAddress("127.0.0.1",port),1000);
    int declaredLength=path.equals("/join") ? 101 : 64;
    String headers="POST "+path+" HTTP/1.1\r\nHost: 127.0.0.1:"+port+"\r\nOrigin: "+endpoint
        +"\r\nContent-Type: text/plain\r\nContent-Length: "+declaredLength+"\r\n"
        +(path.equals("/join") ? OnlineProtocol.VERSION_HEADER+": "+OnlineProtocol.VERSION+"\r\n"
            : "Authorization: Bearer "+token+"\r\n")
        +"Connection: close\r\n\r\n";
    socket.getOutputStream().write(headers.getBytes(StandardCharsets.US_ASCII));
    socket.getOutputStream().flush();
    return socket;
  }
  private static String responseLine(Socket socket) throws Exception {
    socket.setSoTimeout(1000);
    var bytes=new ByteArrayOutputStream();
    for(int i=0;i<80;i++) {
      int next=socket.getInputStream().read();
      if(next<0 || next=='\n') break;
      bytes.write(next);
    }
    return bytes.toString(StandardCharsets.US_ASCII);
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
