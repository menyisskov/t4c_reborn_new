package com.perso.T4C.server;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.quest.CrimsonscaleLetterRules;
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
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Isolated, two-adapter PostgreSQL restart QA. Never operates on a pre-existing server process. */
public final class NativeRestartProbe {
  private static final int START_SECONDS=30, STOP_SECONDS=100, LEASE_SECONDS=45;
  private static final String[] REQUIRED_DB={"T4C_DB_URL","T4C_DB_USER","T4C_DB_PASSWORD"};

  private record Player(OnlineStore.AccountKey key,OnlineStore.CharacterState created,
      OnlineConnection.Credentials credentials) {}

  public static void main(String[] args) throws Exception {
    if(args.length!=4) throw new IllegalArgumentException(
        "Usage: NativeRestartProbe RUNTIME_DIR JAVA_EXE UNUSED_LOOPBACK_PORT NEW_OUTPUT_DIR");
    Path runtime=Path.of(args[0]).toRealPath(),javaExe=Path.of(args[1]).toRealPath();
    int port=Integer.parseInt(args[2]);
    if(port<1024 || port>65535 || port==18765 || port==18766)
      throw new IllegalArgumentException("Select a dedicated loopback port other than the reserved QA ports");
    Path output=Path.of(args[3]).toAbsolutePath().normalize();
    if(!Files.isDirectory(output.getParent()) || Files.exists(output))
      throw new IllegalArgumentException("Output must be a new directory with an existing parent");
    output=output.getParent().toRealPath().resolve(output.getFileName());
    Path checkout=Path.of("").toAbsolutePath().normalize();
    if(runtime.startsWith(checkout) || output.startsWith(checkout))
      throw new IllegalArgumentException("Runtime snapshot and output must be outside this checkout");
    Path jar=runtime.resolve("t4c-server.jar"),map=runtime.resolve("assets/maps/worldmap/worldmap.colbin");
    Path dungeon=runtime.resolve("assets/maps/dungeon/DungeonMap.colbin");
    if(!Files.isRegularFile(jar) || !Files.isRegularFile(map) || !Files.isRegularFile(dungeon))
      throw new IllegalArgumentException("Runtime snapshot is missing its server JAR or two collision maps");
    for(String name:REQUIRED_DB) if(System.getenv(name)==null || System.getenv(name).isBlank())
      throw new IllegalArgumentException("Private PostgreSQL environment is incomplete");
    String databaseUrl=System.getenv("T4C_DB_URL");
    if(!databaseUrl.matches("jdbc:postgresql://[^/?#]+/t4c_restart_[a-z0-9_]+(?:\\?.*)?"))
      throw new IllegalArgumentException("Use an explicitly named t4c_restart_* PostgreSQL database");
    for(String name:List.of("T4C_TLS_KEYSTORE","T4C_TLS_PASSWORD_FILE","T4C_PUBLIC_ORIGIN"))
      if(System.getenv(name)!=null && !System.getenv(name).isBlank())
        throw new IllegalArgumentException("This probe requires local HTTP server transport");
    String bind=System.getenv("T4C_BIND_ADDRESS");
    if(bind!=null && !bind.isBlank() && !bind.equals("127.0.0.1"))
      throw new IllegalArgumentException("Server bind must be loopback");
    // The probe and the child must use one copied binary set, never live target classes.
    for(String entry:System.getProperty("java.class.path").split(java.io.File.pathSeparator)) {
      String literal=entry.endsWith("*") ? entry.substring(0,entry.length()-1) : entry;
      if(!Path.of(literal).toAbsolutePath().normalize().startsWith(runtime))
        throw new IllegalArgumentException("Probe classpath must be entirely inside the runtime snapshot");
    }
    try(var socket=new ServerSocket()) {
      socket.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),port));
    } catch(Exception occupied) { throw new IllegalArgumentException("Dedicated port is already in use",occupied); }
    Files.createDirectory(output);
    Path report=output.resolve("report.txt");
    Files.writeString(report,"T4C-0207 native restart probe; started="+Instant.now()+"; port="+port+"\n"
        +"Database credentials and control tokens are never recorded.\n",
        StandardOpenOption.CREATE_NEW);
    URI endpoint=URI.create("http://127.0.0.1:"+port);
    Process child=null;
    String stage="startup";
    try(var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        var store=OnlineStore.fromEnvironment()) {
      child=start(javaExe,jar,map,output,port,1);
      healthy(http,endpoint,child);
      var npc=NpcInteractionCatalog.loadBundled().require(CrimsonscaleLetterRules.NPC);
      var firstKit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(207));
      var secondKit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(208));
      Player first=provision(store,"Restart probe first",npc.world(),npc.x()+.5,npc.y()+.5,firstKit);
      Player second=provision(store,"Restart probe second",npc.world(),npc.x()+.5,npc.y()+.5,secondKit);
      stage="opening reward and graceful shutdown";
      OnlineStore.CharacterState[] beforeGraceful;
      try(var a=new OnlineConnection(endpoint,first.credentials());
          var b=new OnlineConnection(endpoint,second.credentials())) {
        connected(a,first);connected(b,second);
        talk(a);talk(b);
        beforeGraceful=saved(store,first,second);
        for(var state:beforeGraceful) letter(state);
        require(!letter(beforeGraceful[0]).equals(letter(beforeGraceful[1])),
            "Both accounts received the same letter identity");
        assertViews(a,b,beforeGraceful);
        record(report,"before-graceful",beforeGraceful);
        graceful(http,endpoint,child,output.resolve("control-1.token"));
      }
      child=null;
      var afterGraceful=saved(store,first,second);
      stableReward(beforeGraceful,afterGraceful,"graceful shutdown");
      record(report,"after-graceful",afterGraceful);
      stage="graceful restart";
      child=start(javaExe,jar,map,output,port,2);healthy(http,endpoint,child);
      available(store,first,second,LEASE_SECONDS);
      OnlineStore.CharacterState[] beforeForce;
      long killedAt;
      try(var a=new OnlineConnection(endpoint,first.credentials());
          var b=new OnlineConnection(endpoint,second.credentials())) {
        connected(a,first);connected(b,second);
        assertViews(a,b,afterGraceful);
        stableReward(afterGraceful,saved(store,first,second),"graceful reconnect");
        // A direction intent has no commit acknowledgement. A checkpoint may race the kill.
        beforeForce=saved(store,first,second);
        record(report,"before-force",beforeForce);
        a.direction(1,0);
        Thread.sleep(150);
        a.stopInput();
        force(child);
        killedAt=System.nanoTime();
      }
      child=null;
      require(!free(store,first) && !free(store,second),
          "Forced child stop unexpectedly released a still-live character lease");
      append(report,"Both database leases remained in use immediately after forced kill.\n");
      var afterForce=saved(store,first,second);
      for(int i=0;i<2;i++) {
        letter(afterForce[i]);
        require(afterForce[i].progress().equals(beforeForce[i].progress())
            && afterForce[i].gold()==beforeForce[i].gold()
            && afterForce[i].experience()==beforeForce[i].experience()
            && afterForce[i].revision()>=beforeForce[i].revision(),
            "Forced stop changed committed reward/progress or regressed revision");
      }
      record(report,"after-force-committed",afterForce);
      append(report,"Pending direction was unacknowledged; only after-force-committed is the recovery baseline.\n");
      stage="forced restart and real lease expiry";
      child=start(javaExe,jar,map,output,port,3);healthy(http,endpoint,child);
      available(store,first,second,LEASE_SECONDS);
      append(report,"Real lease wait after kill: "+TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-killedAt)+" ms.\n");
      try(var a=new OnlineConnection(endpoint,first.credentials());
          var b=new OnlineConnection(endpoint,second.credentials())) {
        connected(a,first);connected(b,second);
        assertViews(a,b,afterForce);
        stableReward(afterForce,saved(store,first,second),"forced reconnect");
      }
      graceful(http,endpoint,child,output.resolve("control-3.token"));child=null;
      var finalSaved=saved(store,first,second);
      stableReward(afterForce,finalSaved,"final graceful shutdown");
      record(report,"after-final-stop",finalSaved);
      append(report,"PASS: two native adapters; both committed letter UUIDs, flags, complete progress, balances and revisions; graceful exit 0; forced child kill; actual lease-expiry recovery.\n");
      System.out.println("PASS T4C-0207; sanitized report: "+report);
    } catch(Exception | Error failure) {
      append(report,"FAIL at "+stage+": "+failure.getClass().getSimpleName()+" (details retained in child logs or console).\n");
      throw failure;
    } finally {
      // Only our own Process handle is eligible. Preserve failed logs/data; never touch shared QA.
      if(child!=null && child.isAlive()) { child.destroyForcibly();child.waitFor(10,TimeUnit.SECONDS); }
    }
  }

  private static Player provision(OnlineStore store,String name,int world,double x,double y,
      OnlineCharacterCreator.Prepared kit) throws Exception {
    var key=store.provisionAccount();
    var created=store.create(key.accountId(),name,world,x,y,kit.progress(),kit.gold());
    return new Player(key,created,new OnlineConnection.Credentials(key.secret(),created.id()));
  }
  private static Process start(Path java,Path jar,Path map,Path output,int port,int number) throws Exception {
    Path control=output.resolve("control-"+number+".token");
    var command=new ProcessBuilder(java.toString(),"-Djava.awt.headless=true","-jar",jar.toString(),
        "--online",map.toString(),Integer.toString(port));
    command.directory(jar.getParent().toFile());
    command.environment().put("T4C_CONTROL_FILE",control.toString());
    command.environment().put("T4C_BIND_ADDRESS","127.0.0.1");
    command.redirectOutput(output.resolve("server-"+number+".out.log").toFile());
    command.redirectError(output.resolve("server-"+number+".err.log").toFile());
    return command.start();
  }
  private static void healthy(HttpClient http,URI endpoint,Process child) throws Exception {
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(START_SECONDS);
    while(System.nanoTime()<until) {
      require(child.isAlive(),"Owned server exited before health became ready");
      try {
        var reply=http.send(HttpRequest.newBuilder(endpoint.resolve("/health")).timeout(Duration.ofSeconds(2)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
        if(reply.statusCode()==200 && reply.body().contains("local-persistent")) return;
      } catch(java.io.IOException ignored) { }
      Thread.sleep(200);
    }
    throw new AssertionError("Owned server did not become healthy");
  }
  private static void graceful(HttpClient http,URI endpoint,Process child,Path control) throws Exception {
    require(child.isAlive() && Files.isRegularFile(control),"Owned server/control unavailable for graceful stop");
    String token=Files.readString(control).trim();
    require(token.matches("[0-9a-f]{64}"),"Private control token malformed");
    var reply=http.send(HttpRequest.newBuilder(endpoint.resolve("/admin/shutdown"))
        .timeout(Duration.ofSeconds(4)).header("Origin",endpoint.toString())
        .header("Authorization","Bearer "+token).header("Content-Type","text/plain")
        .POST(HttpRequest.BodyPublishers.ofString("")).build(),HttpResponse.BodyHandlers.discarding());
    require(reply.statusCode()==200,"Private admin shutdown was rejected");
    require(child.waitFor(STOP_SECONDS,TimeUnit.SECONDS),"Graceful server drain exceeded deadline");
    require(child.exitValue()==0,"Graceful server exited with failure");
  }
  private static void force(Process child) throws Exception {
    require(child.isAlive(),"Owned server already exited before force phase");
    child.destroyForcibly();
    require(child.waitFor(10,TimeUnit.SECONDS),"Forced child did not exit");
  }
  private static void available(OnlineStore store,Player a,Player b,int seconds) throws Exception {
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(seconds);
    while(System.nanoTime()<until) {
      if(free(store,a) && free(store,b)) return;
      Thread.sleep(250);
    }
    throw new AssertionError("Character lease did not become available by real expiry/release");
  }
  private static boolean free(OnlineStore store,Player p) throws Exception {
    return store.roster(p.key().secret(),null).stream().filter(e->e.character().id().equals(p.created().id()))
        .findFirst().orElseThrow().inUse()==false;
  }
  private static void connected(OnlineConnection connection,Player player) throws Exception {
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(12);
    while(connection.view()==null && !connection.status().equals("disconnected") && System.nanoTime()<until)
      Thread.sleep(25);
    require(connection.view()!=null && connection.view().character().id().equals(player.created().id()),
        "Native adapter did not join selected character");
    connection.confirmRegionReady(connection.view().map().locationEpoch());
  }
  private static void talk(OnlineConnection connection) throws Exception {
    for(int i=0;i<10;i++) {
      var result=connection.interact(CrimsonscaleLetterRules.NPC).toCompletableFuture().get(5,TimeUnit.SECONDS);
      if(result.accepted()) return;
      Thread.sleep(100);
    }
    throw new AssertionError("Opening NPC remained unavailable");
  }
  private static OnlineStore.CharacterState[] saved(OnlineStore store,Player a,Player b) throws Exception {
    return new OnlineStore.CharacterState[]{store.load(a.key().accountId(),a.created().id()),
        store.load(b.key().accountId(),b.created().id())};
  }
  private static UUID letter(OnlineStore.CharacterState state) {
    var letters=state.progress().inventory().stream().filter(i->i.key().equals(CrimsonscaleLetterRules.ITEM)).toList();
    require(letters.size()==1 && letters.getFirst().quantity()==1
        && state.progress().questFlags().getOrDefault(CrimsonscaleLetterRules.LETTER_FLAG,0)==1,
        "Committed opening reward is missing or duplicated");
    return letters.getFirst().id();
  }
  private static void assertViews(OnlineConnection a,OnlineConnection b,OnlineStore.CharacterState[] expected) {
    var clients=List.of(a,b);
    for(int i=0;i<2;i++) {
      var view=clients.get(i).view();
      require(view!=null && view.character()!=null && view.character().revision()==expected[i].revision()
          && view.character().progress().equals(expected[i].progress())
          && view.character().gold()==expected[i].gold()
          && view.character().experience()==expected[i].experience(),
          "Native private state differs from committed record");
    }
  }
  private static void stableReward(OnlineStore.CharacterState[] expected,OnlineStore.CharacterState[] actual,String phase) {
    for(int i=0;i<2;i++) {
      var before=expected[i];
      var after=actual[i];
      require(before.id().equals(after.id()) && before.accountId().equals(after.accountId())
          && before.name().equals(after.name())
          && before.progress().equals(after.progress()) && before.gold()==after.gold()
          && before.experience()==after.experience() && before.world()==after.world()
          && Double.compare(before.x(),after.x())==0 && Double.compare(before.y(),after.y())==0
          && before.locationEpoch()==after.locationEpoch()
          && before.portalCooldownUntil()==after.portalCooldownUntil()
          && after.revision()>=before.revision()
          && letter(before).equals(letter(after)),"Committed reward or identity changed during "+phase);
    }
  }
  private static void record(Path report,String phase,OnlineStore.CharacterState[] states) throws Exception {
    for(int i=0;i<2;i++) {
      var s=states[i];
      append(report,phase+" player="+(i+1)+" revision="+s.revision()
          +" progressVersion="+s.progress().version()+" world="+s.world()
          +" x="+s.x()+" y="+s.y()+" gold="+s.gold()+" xp="+s.experience()
          +" letter="+letter(s)+" flag=1 progressSha256="+progressHash(s)+"\n");
    }
  }
  private static String progressHash(OnlineStore.CharacterState state) throws Exception {
    byte[] encoded=new com.google.gson.Gson().toJson(state.progress()).getBytes(StandardCharsets.UTF_8);
    return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(encoded));
  }
  private static void append(Path file,String value) throws Exception {
    Files.writeString(file,value,StandardCharsets.UTF_8,StandardOpenOption.APPEND);
  }
  private static void require(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
}
