package com.perso.T4C.server;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.quest.CrimsonscaleLetterRules;
import com.perso.T4C.server.persistence.OnlineStore;
import com.google.gson.JsonParser;
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
public final class NativeDatabaseRecoveryProbe {
  private static final int START_SECONDS=30, STOP_SECONDS=100, LEASE_SECONDS=50;
  private static final String[] REQUIRED_DB={"T4C_DB_URL","T4C_DB_USER","T4C_DB_PASSWORD"};

  private record Player(OnlineStore.AccountKey key,OnlineStore.CharacterState created,
      OnlineConnection.Credentials credentials) {}
  private record RawSession(String token,long epoch) {}

  public static void main(String[] args) throws Exception {
    if(args.length!=8) throw new IllegalArgumentException(
        "Usage: NativeDatabaseRecoveryProbe RUNTIME_DIR JAVA_EXE PORT NEW_OUTPUT_DIR WSL_HELPER RUN_ID SOURCE_SHA SCHEMA_VERSION");
    Path runtime=Path.of(args[0]).toRealPath(),javaExe=Path.of(args[1]).toRealPath();
    int port=Integer.parseInt(args[2]);
    if(port!=18768) throw new IllegalArgumentException("Only reserved isolated server port 18768 is allowed");
    String helper=args[4],runId=args[5],sourceSha=args[6],schema=args[7];
    if(!helper.matches("/mnt/c/[A-Za-z0-9_./ -]+/db-recovery-cluster\\.sh")
        || helper.contains("..") || !runId.matches("t4c_db_0208_[0-9a-f]{12}")
        || !sourceSha.matches("[0-9a-f]{40}") || !schema.matches("[0-9]{1,2}"))
      throw new IllegalArgumentException("Invalid isolated helper, run identity or source metadata");
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
    if(!databaseUrl.equals("jdbc:postgresql://127.0.0.1:55433/"+runId))
      throw new IllegalArgumentException("Use only the owned loopback PostgreSQL cluster/database");
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
    Files.writeString(report,"T4C-0208 isolated PostgreSQL crash/recovery; started="+Instant.now()+"; serverPort="+port
        +" dbPort=55433 source="+sourceSha+" schema="+schema+" protocol="+OnlineProtocol.VERSION+"\n"
        +"serverJarSha256="+fileHash(jar)+" clientJarSha256="+fileHash(runtime.resolve("client/T4C.jar"))+"\n"
        +"Database credentials, session tokens and control tokens are never recorded.\n",
        StandardOpenOption.CREATE_NEW);
    URI endpoint=URI.create("http://127.0.0.1:"+port);
    Process child=null;
    String stage="startup";
    try(var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()) {
      String beforePostmaster=control(helper,runId,"status",output);
      require(beforePostmaster.startsWith("UP pid="),"Owned PostgreSQL cluster is not ready");
      append(report,"before-crash-postmaster="+beforePostmaster+"\n");
      child=start(javaExe,jar,map,output,port,1);
      healthy(http,endpoint,child);
      int actualSchema=readSchema();
      require(Integer.toString(actualSchema).equals(schema),"Owned database schema differs from selected runtime metadata");
      append(report,"verifiedDatabaseSchema="+actualSchema+"\n");
      var npc=NpcInteractionCatalog.loadBundled().require(CrimsonscaleLetterRules.NPC);
      var firstKit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(207));
      var secondKit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(208));
      Player first,second;
      stage="provision and first committed letter";
      try(var store=OnlineStore.fromEnvironment()) {
        first=provision(store,"DB recovery first",npc.world(),npc.x()+.5,npc.y()+.5,firstKit);
        second=provision(store,"DB recovery second",npc.world(),npc.x()+.5,npc.y()+.5,secondKit);
      }
      OnlineStore.CharacterState[] baseline;
      try(var a=new OnlineConnection(endpoint,first.credentials())) {
        connected(a,first);
        try(var b=new OnlineConnection(endpoint,second.credentials())) {
          connected(b,second);
          talk(a);
          try(var store=OnlineStore.fromEnvironment()) {
            baseline=saved(store,first,second);
            letter(baseline[0]);noLetter(baseline[1]);
            assertViews(a,b,baseline);
            recordState(report,"before-outage-A",baseline[0]);
            recordState(report,"before-outage-B",baseline[1]);
          }
        }
        stage="B native lease release";
        try(var store=OnlineStore.fromEnvironment()) {
          available(store,second,LEASE_SECONDS);
          baseline[1]=store.load(second.key().accountId(),second.created().id());
          noLetter(baseline[1]);
          recordState(report,"before-outage-B-after-adapter-close",baseline[1]);
        }
        stage="B private session and readiness preflight";
        RawSession rawSession=rawJoin(http,endpoint,second,baseline[1]);
        append(report,"B private join, current revision, region-ready and NPC range preflight passed.\n");
        stage="owned database crash and outage request";
        require(control(helper,runId,"crash",output).equals("CRASH_OUTAGE_OK"),"Owned database outage not proven");
        require(control(helper,runId,"status",output).equals("DOWN"),"Database still running after crash");
        require(child.isAlive(),"Owned game server exited during database outage");
        String outcome=rawInteractDuringOutage(http,endpoint,rawSession,baseline[1],CrimsonscaleLetterRules.NPC);
        append(report,"during-outage-B-attempt="+outcome+" (single request, never replayed)\n");
        require(child.isAlive(),"Owned game server exited after outage interaction");
        stage="actual PostgreSQL recovery";
        String afterPostmaster=control(helper,runId,"start",output);
        require(afterPostmaster.startsWith("RECOVERY_OK pid="),"PostgreSQL crash recovery not confirmed");
        require(readSchemaAfterRecovery()==actualSchema,"Owned database schema changed across crash recovery");
        append(report,"after-recovery-postmaster="+afterPostmaster+"\n");
      }
      stage="committed-state reconciliation and lease expiry";
      OnlineStore.CharacterState[] recovered;
      OnlineStore.CharacterState[] readyForReconnect;
      try(var store=OnlineStore.fromEnvironment()) {
        recovered=saved(store,first,second);
        stableState(baseline[0],recovered[0],"first committed letter");
        stableState(baseline[1],recovered[1],"second pending interaction");
        noLetter(recovered[1]);
        Thread.sleep(3000);
        var settled=saved(store,first,second);
        stableState(recovered[1],settled[1],"no automatic replay after database recovery");
        noLetter(settled[1]);
        recordState(report,"after-recovery-A",recovered[0]);
        recordState(report,"after-recovery-B",settled[1]);
        available(store,first,second,LEASE_SECONDS);
        readyForReconnect=saved(store,first,second);
        stableState(recovered[0],readyForReconnect[0],"first lease expiry");
        stableState(settled[1],readyForReconnect[1],"second lease expiry");
        noLetter(readyForReconnect[1]);
        recordState(report,"after-lease-expiry-A",readyForReconnect[0]);
        recordState(report,"after-lease-expiry-B",readyForReconnect[1]);
      }
      stage="native reconnect and explicit post-recovery B interaction";
      OnlineStore.CharacterState[] afterExplicit;
      try(var a=new OnlineConnection(endpoint,first.credentials());
          var b=new OnlineConnection(endpoint,second.credentials())) {
        connected(a,first);connected(b,second);
        assertViews(a,b,readyForReconnect);
        talk(b);
        try(var store=OnlineStore.fromEnvironment()) {
          afterExplicit=saved(store,first,second);
          stableState(readyForReconnect[0],afterExplicit[0],"first after native reconnect");
          letter(afterExplicit[1]);
          require(!letter(afterExplicit[0]).equals(letter(afterExplicit[1])),"Players received same letter identity");
          recordState(report,"after-explicit-B",afterExplicit[1]);
        }
      }
      stage="graceful final server stop";
      graceful(http,endpoint,child,output.resolve("control-1.token"));child=null;
      try(var store=OnlineStore.fromEnvironment()) {
        var finalSaved=saved(store,first,second);
        stableReward(afterExplicit,finalSaved,"final graceful shutdown");
        recordState(report,"final-A",finalSaved[0]);recordState(report,"final-B",finalSaved[1]);
      }
      append(report,"PASS: two native accounts/adapters, real owned PostgreSQL immediate stop/recovery, single B outage request without replay, preserved committed A state, explicit B post-recovery grant, graceful server exit 0.\n");
      System.out.println("PASS T4C-0208; sanitized report: "+report);
    } catch(Exception | Error failure) {
      String safeDetail=failure instanceof AssertionError && failure.getMessage()!=null
          && failure.getMessage().matches("[A-Za-z0-9 .,;()/-]{1,120}")
          ? " "+failure.getMessage() : "";
      append(report,"FAIL at "+stage+": "+failure.getClass().getSimpleName()+safeDetail
          +" (private child/cluster logs retained).\n");
      throw new AssertionError("T4C-0208 failed at "+stage+" (inspect private evidence)");
    } finally {
      if(child!=null && child.isAlive()) { child.destroyForcibly();child.waitFor(10,TimeUnit.SECONDS); }
      try { control(helper,runId,"stop",output); }
      catch(Exception cleanup) { append(report,"WARNING: owned cluster stop did not complete; inspect marker and private cluster logs.\n"); }
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
  private static String control(String helper,String runId,String action,Path output) throws Exception {
    require(List.of("status","crash","start","stop").contains(action),"Unsupported cluster action");
    var process=new ProcessBuilder("wsl.exe","-d","Ubuntu","--","/bin/bash",helper,action,runId)
        .redirectError(output.resolve("cluster-"+action+".err.log").toFile()).start();
    if(!process.waitFor(45,TimeUnit.SECONDS)) {
      process.destroyForcibly();process.waitFor(5,TimeUnit.SECONDS);
      throw new IllegalStateException("Owned cluster action exceeded deadline");
    }
    String result=new String(process.getInputStream().readAllBytes(),StandardCharsets.UTF_8).trim();
    require(process.exitValue()==0,"Owned cluster action failed: "+action);
    require(result.matches("(?:UP pid=[0-9]+ started=[0-9]+|DOWN|CRASH_OUTAGE_OK|RECOVERY_OK pid=[0-9]+ started=[0-9]+|STOP_OK)"),
        "Owned cluster action returned unexpected status");
    return result;
  }
  private static RawSession rawJoin(HttpClient http,URI endpoint,Player b,OnlineStore.CharacterState state) throws Exception {
    String login=b.key().secret()+" "+b.created().id();
    var request=HttpRequest.newBuilder(endpoint.resolve("/join")).timeout(Duration.ofSeconds(3))
        .header(OnlineProtocol.VERSION_HEADER,Integer.toString(OnlineProtocol.VERSION))
        .header("Origin",endpoint.toString()).header("Content-Type","text/plain")
        .POST(HttpRequest.BodyPublishers.ofString(login)).build();
    var pending=http.sendAsync(request,HttpResponse.BodyHandlers.ofString());
    HttpResponse<String> response;
    try { response=pending.get(4,TimeUnit.SECONDS); }
    catch(Exception timeout) { pending.cancel(true);throw timeout; }
    require(response.statusCode()==200,"Private second player session could not join");
    var body=JsonParser.parseString(response.body()).getAsJsonObject();
    String token=body.get("token").getAsString();
    long epoch=body.getAsJsonObject("region").get("locationEpoch").getAsLong();
    require(token.matches("[0-9a-f]{64}") && epoch>=0 && state.revision()>=0,"Invalid private join response");
    var ready=HttpRequest.newBuilder(endpoint.resolve("/region-ready")).timeout(Duration.ofSeconds(3))
        .header("Authorization","Bearer "+token).header("Origin",endpoint.toString())
        .header("Content-Type","text/plain")
        .POST(HttpRequest.BodyPublishers.ofString(Long.toString(epoch))).build();
    require(http.send(ready,HttpResponse.BodyHandlers.discarding()).statusCode()==200,
        "Private second player region was not ready");
    var characterRequest=HttpRequest.newBuilder(endpoint.resolve("/character"))
        .timeout(Duration.ofSeconds(2)).header("Authorization","Bearer "+token).GET().build();
    var characterReply=http.send(characterRequest,HttpResponse.BodyHandlers.ofString());
    require(characterReply.statusCode()==200,"Private second player state unavailable before outage");
    var privateState=JsonParser.parseString(characterReply.body()).getAsJsonObject();
    require(privateState.get("id").getAsString().equals(state.id().toString()),
        "Private second player identity differs from baseline");
    require(privateState.get("revision").getAsLong()==state.revision(),
        "Private second player revision changed during preflight");
    require(privateState.get("locationEpoch").getAsLong()==epoch,
        "Private second player location epoch differs from joined region epoch");
    var stateRequest=HttpRequest.newBuilder(endpoint.resolve("/state"))
        .timeout(Duration.ofSeconds(2)).header("Authorization","Bearer "+token).GET().build();
    var stateReply=http.send(stateRequest,HttpResponse.BodyHandlers.ofString());
    require(stateReply.statusCode()==200,"Private second player snapshot unavailable before outage");
    var snapshot=JsonParser.parseString(stateReply.body()).getAsJsonObject();
    require(snapshot.get("characterRevision").getAsLong()==state.revision(),
        "Private second player snapshot revision is stale");
    long actorId=body.get("id").getAsLong();
    var actors=snapshot.getAsJsonArray("players");
    var npcs=snapshot.getAsJsonArray("npcs");
    double actorX=Double.NaN,actorY=Double.NaN,npcX=Double.NaN,npcY=Double.NaN;
    for(var actor:actors) {
      var value=actor.getAsJsonObject();
      if(value.get("id").getAsLong()==actorId) { actorX=value.get("x").getAsDouble();actorY=value.get("y").getAsDouble(); }
    }
    for(var npc:npcs) {
      var value=npc.getAsJsonObject();
      if(value.get("id").getAsString().equals(CrimsonscaleLetterRules.NPC)) {
        npcX=value.get("x").getAsDouble();npcY=value.get("y").getAsDouble();
      }
    }
    double dx=actorX-npcX,dy=actorY-npcY;
    require(Double.isFinite(dx) && Double.isFinite(dy)
        && dx*dx+dy*dy<GameConstants.NPC_INTERACTION_RANGE_TILES_SQUARED,
        "Private second player is not in authored NPC interaction range");
    return new RawSession(token,epoch);
  }
  private static String rawInteractDuringOutage(HttpClient http,URI endpoint,RawSession session,
      OnlineStore.CharacterState before,String npc) throws Exception {
    var request=HttpRequest.newBuilder(endpoint.resolve("/interact")).timeout(Duration.ofSeconds(2))
        .header("Authorization","Bearer "+session.token()).header("Origin",endpoint.toString())
        .header("Content-Type","text/plain")
        .POST(HttpRequest.BodyPublishers.ofString(before.revision()+" "+session.epoch()+" "+npc)).build();
    var pending=http.sendAsync(request,HttpResponse.BodyHandlers.ofString());
    try {
      var response=pending.get(3,TimeUnit.SECONDS);
      require(response.statusCode()==503,"Outage interaction did not fail as persistence unavailable");
      var error=JsonParser.parseString(response.body()).getAsJsonObject().get("error").getAsString();
      require(error.equals("busy") || error.equals("account_busy"),"Unexpected outage refusal category");
      return "HTTP_503_"+error;
    } catch(java.util.concurrent.TimeoutException timeout) {
      pending.cancel(true);return "TIMEOUT_UNCERTAIN";
    } catch(java.util.concurrent.ExecutionException failed) {
      return "TRANSPORT_UNCERTAIN";
    }
  }
  private static void noLetter(OnlineStore.CharacterState state) {
    require(state.progress().inventory().stream().noneMatch(i->i.key().equals(CrimsonscaleLetterRules.ITEM))
        && state.progress().questFlags().getOrDefault(CrimsonscaleLetterRules.LETTER_FLAG,0)==0,
        "Unacknowledged second player letter appeared");
  }
  private static void stableState(OnlineStore.CharacterState before,OnlineStore.CharacterState after,String phase) {
    require(before.id().equals(after.id()) && before.accountId().equals(after.accountId())
        && before.name().equals(after.name()) && before.progress().equals(after.progress())
        && before.gold()==after.gold() && before.experience()==after.experience()
        && before.world()==after.world() && Double.compare(before.x(),after.x())==0
        && Double.compare(before.y(),after.y())==0 && before.locationEpoch()==after.locationEpoch()
        && before.portalCooldownUntil()==after.portalCooldownUntil() && after.revision()>=before.revision(),
        "Committed identity, progress, position or balances changed during "+phase);
  }
  private static void recordState(Path report,String phase,OnlineStore.CharacterState state) throws Exception {
    var letters=state.progress().inventory().stream().filter(i->i.key().equals(CrimsonscaleLetterRules.ITEM)).toList();
    append(report,phase+" id="+state.id()+" revision="+state.revision()
        +" progressVersion="+state.progress().version()+" world="+state.world()
        +" x="+state.x()+" y="+state.y()+" gold="+state.gold()+" xp="+state.experience()
        +" letter="+(letters.isEmpty()?"none":letters.getFirst().id())
        +" flag="+state.progress().questFlags().getOrDefault(CrimsonscaleLetterRules.LETTER_FLAG,0)
        +" progressSha256="+progressHash(state)+"\n");
  }
  private static String fileHash(Path file) throws Exception {
    var digest=MessageDigest.getInstance("SHA-256");
    try(var stream=Files.newInputStream(file)) {
      byte[] block=new byte[65536];int count;
      while((count=stream.read(block))!=-1) digest.update(block,0,count);
    }
    return java.util.HexFormat.of().formatHex(digest.digest());
  }
  private static int readSchema() throws Exception {
    try(var connection=java.sql.DriverManager.getConnection(System.getenv("T4C_DB_URL")+"?connectTimeout=2&socketTimeout=3",
            System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"));
        var statement=connection.createStatement();
        var rows=statement.executeQuery("SELECT max(version) FROM online_schema")) {
      require(rows.next(),"Owned database has no installed schema");
      return rows.getInt(1);
    }
  }
  private static int readSchemaAfterRecovery() throws Exception {
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
    do {
      try { return readSchema(); }
      catch(java.sql.SQLException temporarilyUnavailable) {
        if(System.nanoTime()>=until) throw new IllegalStateException("Recovered database did not accept bounded JDBC connection");
        Thread.sleep(200);
      }
    } while(true);
  }
  private static void available(OnlineStore store,Player player,int seconds) throws Exception {
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(seconds);
    while(System.nanoTime()<until) {
      if(free(store,player)) return;
      Thread.sleep(250);
    }
    throw new AssertionError("Second player lease did not release before private join");
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
