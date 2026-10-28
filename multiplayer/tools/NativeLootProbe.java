package com.perso.T4C.server;

import com.google.gson.GsonBuilder;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.ItemContent;
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
import java.sql.DriverManager;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Isolated two-adapter ground-loot QA (T4C-0204 L12). It starts its own {@code ServerMain --online}
 * child on a caller-selected loopback port, provisions disposable characters in a dedicated
 * {@code t4c_loot_*} PostgreSQL database and drives them only through {@link OnlineConnection}.
 * It never uses a pre-existing server, never clears a lease and never replays a pickup.
 */
public final class NativeLootProbe {
  /** The hosted starter region lives on world 0; the start tile is derived, never hard-coded (see {@link #resolveStart}). */
  private static final int WORLD=0;
  private static final String QUEST_ID="renewed_wards";
  private static final int MIN_DROPS=3, MAX_KILLS=30, START_SECONDS=30, STOP_SECONDS=100;
  private static final long TOTAL_MS=TimeUnit.MINUTES.toMillis(20);
  private static final String[] REQUIRED_DB={"T4C_DB_URL","T4C_DB_USER","T4C_DB_PASSWORD"};
  private static final Set<String> RACE_REFUSALS=Set.of("loot_unavailable","busy");

  private record Player(String label,OnlineStore.AccountKey key,OnlineStore.CharacterState created,
      OnlineConnection.Credentials credentials) {}
  private record Outcome(boolean accepted,String messageKey,String error) {}
  /** Start tile (tile centre) beside an authored Skeleton anchor, both in world-0 tile coordinates. */
  private record Start(double x,double y,int anchorX,int anchorY,int circleSkeletons) {}

  /**
   * Derives the characters' start from the same sources {@code HostedKillRegionTest} uses: the real
   * {@code ServerMain --online} region plan ({@link HostedRegionPlan#startingSlice}), the
   * {@code renewed_wards} quest circle and the authored Skeleton spawns inside it. Picks the first
   * anchor (by x, then y) that has a standable, non-portal tile within melee reach with combat line of
   * sight, and re-checks that the tile is hosted. Fails with a clear message otherwise. It only reads
   * authored data and the collision map; it never weakens a server guard.
   */
  private static Start resolveStart(Path worldCollision) throws Exception {
    HostedRegionPlan plan=HostedRegionPlan.startingSlice(worldCollision);
    var region=plan.regions().stream().filter(r->r.spec().world()==WORLD).findFirst()
        .orElseThrow(()->new IllegalArgumentException("Precondition failed: the starting slice hosts no world-"+WORLD+" region"));
    PreviewMap map=region.map();
    var quest=QuestCatalog.bundled().definition(QUEST_ID);
    var skeletons=MonsterCatalog.loadBundled().spawns().stream()
        .filter(s->"Skeleton".equals(s.definition()) && s.world()==WORLD
            && Math.abs(s.x()-quest.getAreaCenterX())<=quest.getAreaRadiusTiles()
            && Math.abs(s.y()-quest.getAreaCenterY())<=quest.getAreaRadiusTiles())
        .sorted(java.util.Comparator.comparingInt((com.perso.T4C.online.model.MonsterContent.Spawn s)->s.x()).thenComparingInt(s->s.y()))
        .toList();
    if(skeletons.isEmpty()) throw new IllegalArgumentException(
        "Precondition failed: no authored Skeleton spawn inside the "+QUEST_ID+" circle");
    for(var anchor:skeletons) {
      Start best=null; double bestDistance=Double.MAX_VALUE;
      for(int dy=-2;dy<=2;dy++) for(int dx=-2;dx<=2;dx++) {
        if(dx==0 && dy==0) continue;
        int tileX=anchor.x()+dx,tileY=anchor.y()+dy;
        double wx=tileX+.5,wy=tileY+.5;
        if(!plan.canStand(new CharacterProgress.Respawn(WORLD,wx,wy))) continue;       // hosted AND standable
        if(plan.isRefusedPortalSource(WORLD,wx,wy)
            || plan.portals.resolve(WORLD,wx,wy,plan::canStand).isPresent()) continue;  // never start on a portal source
        double rx=wx-map.originX,ry=wy-map.originY;
        double distance=Math.hypot(dx*(double)GameConstants.GRID_W,dy*(double)GameConstants.GRID_H);
        if(distance>GameConstants.PLAYER_MELEE_ATTACK_RANGE) continue;
        if(!map.allowsCombat(rx,ry,anchor.x()-map.originX+.5,anchor.y()-map.originY+.5,false)) continue;
        if(distance<bestDistance) { bestDistance=distance; best=new Start(wx,wy,anchor.x(),anchor.y(),skeletons.size()); }
      }
      if(best!=null) return best;
    }
    throw new IllegalArgumentException("Precondition failed: none of the "+skeletons.size()
        +" authored Skeleton anchors has a standable, hosted, melee-reachable adjacent tile");
  }

  /** Sanitized evidence: no account IDs, secrets, tokens, database credentials or paths beyond the output dir. */
  private static final class Report {
    final Path file;
    final Map<String,Object> values=new LinkedHashMap<>();
    final List<String> stages=new ArrayList<>();
    Report(Path file) { this.file=file; values.put("tool","NativeLootProbe"); values.put("task","T4C-0204 L12");
      values.put("startedAt",Instant.now().toString()); values.put("status","RUNNING"); }
    void stage(String name) { stages.add(name); }
    void put(String key,Object value) { values.put(key,value); }
    void finish(String status) throws Exception {
      values.put("status",status); values.put("stages",stages); values.put("finishedAt",Instant.now().toString());
      values.put("evidenceScope","two actual native adapters; not rendered-client evidence");
      String json=new GsonBuilder().setPrettyPrinting().create().toJson(values);
      Files.writeString(file,json+"\n",StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW);
      System.out.println(json);
    }
  }

  public static void main(String[] args) throws Exception {
    if(args.length!=4) throw new IllegalArgumentException(
        "Usage: NativeLootProbe RUNTIME_DIR JAVA_EXE UNUSED_LOOPBACK_PORT NEW_OUTPUT_DIR");
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
    if(!databaseUrl.matches("jdbc:postgresql://[^/?#]+/t4c_loot_[a-z0-9_]+(?:\\?.*)?"))
      throw new IllegalArgumentException("Use an explicitly named, dedicated t4c_loot_* PostgreSQL database");
    for(String name:List.of("T4C_TLS_KEYSTORE","T4C_TLS_PASSWORD_FILE","T4C_PUBLIC_ORIGIN"))
      if(System.getenv(name)!=null && !System.getenv(name).isBlank())
        throw new IllegalArgumentException("This probe requires local HTTP server transport");
    String bind=System.getenv("T4C_BIND_ADDRESS");
    if(bind!=null && !bind.isBlank() && !bind.equals("127.0.0.1"))
      throw new IllegalArgumentException("Server bind must be loopback");
    for(String entry:System.getProperty("java.class.path").split(java.io.File.pathSeparator)) {
      String literal=entry.endsWith("*") ? entry.substring(0,entry.length()-1) : entry;
      if(!Path.of(literal).toAbsolutePath().normalize().startsWith(runtime))
        throw new IllegalArgumentException("Probe classpath must be entirely inside the runtime snapshot");
    }
    // Offline precondition: the start must be hosted, standable and beside a real Skeleton anchor.
    final Start start=resolveStart(map);
    try(var socket=new ServerSocket()) {
      socket.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),port));
    } catch(Exception occupied) { throw new IllegalArgumentException("Dedicated port is already in use",occupied); }
    Files.createDirectory(output);
    Report report=new Report(output.resolve("report.json"));
    report.put("port",port);
    report.put("startTile",List.of(start.x(),start.y()));
    report.put("skeletonAnchorTile",List.of(start.anchorX(),start.anchorY()));
    report.put("circleSkeletonAnchors",start.circleSkeletons());
    URI endpoint=URI.create("http://127.0.0.1:"+port);
    Process child=null;
    String stage="startup";
    boolean failed=false;
    ExecutorService pool=Executors.newFixedThreadPool(2);
    try(var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        var store=OnlineStore.fromEnvironment()) {
      long deadline=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(TOTAL_MS);
      report.stage(stage="start owned server");
      child=start(javaExe,jar,map,output,port);
      healthy(http,endpoint,child);
      report.put("serverHealth","local-persistent");
      report.stage(stage="provision three disposable characters");
      ItemContent content=ItemCatalog.loadBundled();
      var killerKit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(212));
      var racerKit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(213));
      var weightKit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(214));
      // All three keep the full starter kit equipped. The weighted character only gets an extra stack,
      // sized from the shared carry rules and its effective strength, so it is strictly over the limit.
      var fixture=OnlineOverweightFixture.size(weightKit.progress(),content);
      CharacterProgress heavy=OnlineOverweightFixture.apply(weightKit.progress(),fixture);
      report.put("overweightFixtureItemKey",fixture.itemKey());
      report.put("overweightFixtureQuantity",fixture.quantity());
      report.put("carryMaximum",fixture.maximum());
      report.put("carriedBeforeFixture",fixture.carriedBefore());
      report.put("carriedWithFixture",fixture.carriedAfter());
      require(fixture.carriedAfter()>fixture.maximum(),"Overweight fixture does not exceed the carry maximum");
      var killer=provision(store,"Loot probe killer","killer",start.x(),start.y(),killerKit.progress(),killerKit.gold());
      var racer=provision(store,"Loot probe racer","racer",start.x(),start.y(),racerKit.progress(),racerKit.gold());
      var weighted=provision(store,"Loot probe weighted","weighted",start.x(),start.y(),heavy,weightKit.gold());

      report.stage(stage="join native adapters and check protocol");
      // /join admits at most two concurrent public requests (HttpRequestAdmission.MAX_PUBLIC); join one adapter at a time.
      try(var a=joined(endpoint,killer);
          var b=joined(endpoint,racer);
          var w=joined(endpoint,weighted)) {
        int protocol=a.view().map().protocolVersion();
        require(protocol==OnlineProtocol.VERSION && protocol==5,"Server protocol is not version 5");
        report.put("protocolVersion",protocol);

        report.stage(stage="kill skeletons until drops appear and observer sees them");
        Map<UUID,String> drops=new LinkedHashMap<>();
        Set<UUID> observed=new HashSet<>();
        int kills=0;
        while(drops.size()<MIN_DROPS && kills<MAX_KILLS) {
          require(System.nanoTime()<deadline,"Deadline reached before enough loot dropped");
          collect(a,b,drops,observed);
          CreatureView skeleton=nearestSkeleton(a);
          if(skeleton==null) { Thread.sleep(500); continue; }
          kill(a,skeleton.id(),deadline);
          kills++;
          Thread.sleep(500);
          collect(a,b,drops,observed);
        }
        require(drops.size()>=MIN_DROPS,"Skeletons did not produce enough loot drops");
        for(int i=0;i<50 && !observed.containsAll(drops.keySet());i++) { Thread.sleep(100); collect(a,b,drops,observed); }
        require(observed.containsAll(drops.keySet()),"Observer did not see every committed drop");
        report.put("skeletonKills",kills);
        report.put("dropsObservedByKiller",drops.size());
        report.put("dropsObservedByObserver",observed.size());

        List<UUID> ids=new ArrayList<>(drops.keySet());
        UUID raceLoot=ids.get(0), weightLoot=ids.get(1), lostLoot=ids.get(2);

        report.stage(stage="simultaneous claim race after owner window");
        settle(a,killer,store);settle(b,racer,store);
        waitOwnerExpired(b,raceLoot,deadline);
        CountDownLatch go=new CountDownLatch(1);
        Future<Outcome> fa=pool.submit(() -> { go.await(); return pick(a,raceLoot); });
        Future<Outcome> fb=pool.submit(() -> { go.await(); return pick(b,raceLoot); });
        go.countDown();
        Outcome oa=fa.get(30,TimeUnit.SECONDS), ob=fb.get(30,TimeUnit.SECONDS);
        int wins=(oa.accepted()?1:0)+(ob.accepted()?1:0);
        require(wins==1,"Race did not produce exactly one winner: "+oa+" / "+ob);
        Outcome loser=oa.accepted()?ob:oa;
        require(loser.error()==null && RACE_REFUSALS.contains(loser.messageKey()),
            "Race loser was not refused as loot_unavailable/busy: "+loser);
        report.put("raceWinner",oa.accepted()?"killer":"racer");
        report.put("raceLoserRefusal",loser.messageKey());

        report.stage(stage="over-weight refusal leaves the loot");
        settle(w,weighted,store);
        waitOwnerExpired(w,weightLoot,deadline);
        Outcome heavyOutcome=pickSettled(w,weightLoot);
        require(!heavyOutcome.accepted() && "too_heavy".equals(heavyOutcome.messageKey()),
            "Over-weight pickup was not refused as too_heavy: "+heavyOutcome);
        require(visible(b,weightLoot),"Refused loot disappeared from the observer snapshot");
        report.put("overweightRefusal",heavyOutcome.messageKey());

        report.stage(stage="lost-response reconnect does not replay");
        settle(b,racer,store);
        waitOwnerExpired(b,lostLoot,deadline);
        var inFlight=b.pickUp(lostLoot);
        b.close();
        boolean answeredBeforeClose=inFlight.toCompletableFuture().isDone();
        report.put("lostResponseAnsweredBeforeClose",answeredBeforeClose);
        Thread.sleep(500);
        try(var b2=rejoin(endpoint,racer,deadline)) {
          settle(b2,racer,store);
          report.put("reconnectJoined",true);
          // No replay: the probe issues no second pickup; the database check below decides the outcome.
          stopAll(a,b2,w);
          report.stage(stage="graceful shutdown and database verification");
          graceful(http,endpoint,child,output.resolve("control-1.token"));
          child=null;
          verify(store,databaseUrl,report,drops.keySet(),
              List.of(killer,racer,weighted),raceLoot,weightLoot,lostLoot,oa.accepted()?killer:racer);
        }
      }
      report.finish("PASS");
      System.out.println("PASS native loot probe; sanitized report: "+output.resolve("report.json"));
    } catch(Throwable failure) {
      failed=true;
      report.put("failureStage",stage);
      report.put("failureClass",failure.getClass().getSimpleName());
      report.put("failureMessage",String.valueOf(failure.getMessage()));
      report.finish("FAIL");
    } finally {
      pool.shutdownNow();
      if(child!=null && child.isAlive()) { child.destroyForcibly(); child.waitFor(10,TimeUnit.SECONDS); }
    }
    if(failed) System.exit(1);
  }

  private static void stopAll(OnlineConnection... connections) {
    for(var c:connections) if(c!=null) c.close();
  }

  private static Player provision(OnlineStore store,String name,String label,double x,double y,
      CharacterProgress progress,long gold) throws Exception {
    var key=store.provisionAccount();
    var created=store.create(key.accountId(),name,WORLD,x,y,progress,gold);
    return new Player(label,key,created,new OnlineConnection.Credentials(key.secret(),created.id()));
  }

  private static CreatureView nearestSkeleton(OnlineConnection a) {
    for(var c:a.view().snapshot().creatures())
      if(c.definition().toLowerCase(Locale.ROOT).contains("skeleton") && c.health()>0) return c;
    return null;
  }

  private static void kill(OnlineConnection a,long creature,long deadline) throws Exception {
    while(System.nanoTime()<deadline) {
      CreatureView current=null;
      for(var c:a.view().snapshot().creatures()) if(c.id()==creature) current=c;
      if(current==null || current.health()<=0) return;
      try { a.attack(creature).toCompletableFuture().get(5,TimeUnit.SECONDS); }
      catch(java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException ignored) { }
      Thread.sleep(200);
    }
    throw new AssertionError("Skeleton was not killed before the deadline");
  }

  private static void collect(OnlineConnection a,OnlineConnection b,Map<UUID,String> drops,Set<UUID> observed) {
    for(var loot:a.view().snapshot().loot()) drops.putIfAbsent(loot.id(),loot.key());
    for(var loot:b.view().snapshot().loot()) if(drops.containsKey(loot.id())) observed.add(loot.id());
  }

  private static boolean visible(OnlineConnection c,UUID loot) {
    return c.view().snapshot().loot().stream().anyMatch(l->l.id().equals(loot));
  }

  private static void waitOwnerExpired(OnlineConnection observer,UUID loot,long deadline) throws Exception {
    while(System.nanoTime()<deadline) {
      var found=observer.view().snapshot().loot().stream().filter(l->l.id().equals(loot)).findFirst();
      require(found.isPresent(),"Loot vanished before its claim window ended");
      if(found.get().ownerRemainingMs()==0) return;
      Thread.sleep(500);
    }
    throw new AssertionError("Owner window did not expire before the deadline");
  }

  /** Waits until the adapter's private revision matches the committed record, so a claim is not refused as stale. */
  private static void settle(OnlineConnection c,Player p,OnlineStore store) throws Exception {
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
    while(System.nanoTime()<until) {
      var view=c.view();
      if(view!=null && view.character()!=null
          && view.character().revision()==store.load(p.key().accountId(),p.created().id()).revision()) return;
      Thread.sleep(150);
    }
    throw new AssertionError("Adapter private state did not settle to the committed revision");
  }

  private static Outcome pick(OnlineConnection c,UUID loot) {
    try {
      var r=c.pickUp(loot).toCompletableFuture().get(10,TimeUnit.SECONDS);
      return new Outcome(r.accepted(),r.messageKey(),null);
    } catch(Exception e) {
      return new Outcome(false,null,e.getClass().getSimpleName());
    }
  }

  /** Retries only a transient busy/revision refusal; a ground-state refusal is final and never replayed. */
  private static Outcome pickSettled(OnlineConnection c,UUID loot) throws Exception {
    Outcome last=null;
    for(int i=0;i<10;i++) {
      last=pick(c,loot);
      if(last.accepted() || last.messageKey()==null
          || !(last.messageKey().equals("busy") || last.messageKey().equals("revision"))) return last;
      Thread.sleep(300);
    }
    return last;
  }

  private static OnlineConnection rejoin(URI endpoint,Player p,long deadline) throws Exception {
    while(System.nanoTime()<deadline) {
      var candidate=new OnlineConnection(endpoint,p.credentials());
      long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(12);
      while(candidate.view()==null && !candidate.status().equals("disconnected") && System.nanoTime()<until)
        Thread.sleep(50);
      if(candidate.view()!=null && candidate.view().character()!=null
          && candidate.view().character().id().equals(p.created().id())) {
        candidate.confirmRegionReady(candidate.view().map().locationEpoch());
        return candidate;
      }
      candidate.close();
      Thread.sleep(1000);
    }
    throw new AssertionError("Lost-response character did not rejoin before the deadline");
  }

  private static OnlineConnection joined(URI endpoint,Player player) throws Exception {
    var connection=new OnlineConnection(endpoint,player.credentials());
    try { connected(connection,player);return connection; }
    catch(Throwable failure) { connection.close();throw failure; }
  }

  private static void connected(OnlineConnection connection,Player player) throws Exception {
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(12);
    while(connection.view()==null && !connection.status().equals("disconnected") && System.nanoTime()<until)
      Thread.sleep(25);
    require(connection.view()!=null && connection.view().character().id().equals(player.created().id()),
        "Native adapter did not join selected character");
    connection.confirmRegionReady(connection.view().map().locationEpoch());
  }

  /** Reads committed state directly; every loot row must match its holders and claim receipts exactly. */
  private static void verify(OnlineStore store,String url,Report report,Set<UUID> loot,List<Player> players,
      UUID raceLoot,UUID weightLoot,UUID lostLoot,Player raceWinner) throws Exception {
    Map<UUID,Player> byCharacter=new LinkedHashMap<>();
    Map<UUID,OnlineStore.CharacterState> saved=new LinkedHashMap<>();
    for(var p:players) {
      var state=store.load(p.key().accountId(),p.created().id());
      byCharacter.put(p.created().id(),p);
      saved.put(p.created().id(),state);
    }
    var rows=new ArrayList<Map<String,Object>>();
    String user=System.getenv("T4C_DB_USER"), password=System.getenv("T4C_DB_PASSWORD");
    try(var db=DriverManager.getConnection(url,user,password)) {
      for(UUID id:loot) {
        String state=null; UUID claimedBy=null; int claims;
        try(var ps=db.prepareStatement("SELECT state,claimed_by FROM online_ground_loot WHERE id=?")) {
          ps.setObject(1,id);
          try(var rs=ps.executeQuery()) {
            require(rs.next(),"Loot row missing from database");
            state=rs.getString(1); claimedBy=rs.getObject(2,UUID.class);
          }
        }
        try(var ps=db.prepareStatement("SELECT COUNT(*) FROM online_loot_claims WHERE loot_id=?")) {
          ps.setObject(1,id);
          try(var rs=ps.executeQuery()) { rs.next(); claims=rs.getInt(1); }
        }
        List<String> holders=new ArrayList<>();
        for(var entry:saved.entrySet()) {
          var progress=entry.getValue().progress();
          boolean held=progress.inventory().stream().anyMatch(i->i.id().equals(id))
              || progress.storage().stream().anyMatch(i->i.id().equals(id))
              || progress.equipment().containsValue(id);
          if(held) holders.add(byCharacter.get(entry.getKey()).label());
        }
        boolean claimed="CLAIMED".equals(state);
        require(holders.size()==(claimed?1:0),"Loot "+id+" is in "+holders.size()+" places for state "+state);
        require(claims==(claimed?1:0),"Loot "+id+" has "+claims+" claim receipts for state "+state);
        if(claimed) require(claimedBy!=null && byCharacter.containsKey(claimedBy)
            && holders.equals(List.of(byCharacter.get(claimedBy).label())),
            "Claimed loot is not held exactly by its recorded claimant");
        if(!claimed) require(holders.isEmpty(),"Unclaimed loot is held by a character");
        var row=new LinkedHashMap<String,Object>();
        row.put("lootId",id.toString()); row.put("state",state); row.put("holders",holders); row.put("claimReceipts",claims);
        if(id.equals(raceLoot)) row.put("role","race");
        if(id.equals(weightLoot)) row.put("role","over-weight");
        if(id.equals(lostLoot)) row.put("role","lost-response");
        rows.add(row);
      }
    }
    require(rowState(rows,raceLoot).equals("CLAIMED"),"Race loot not claimed exactly once");
    require(rowState(rows,weightLoot).equals("ON_GROUND"),"Over-weight loot did not stay on the ground");
    report.put("databaseCheck",rows);
    report.put("raceWinnerLabel",raceWinner.label());
    report.put("loot",loot.size());
  }

  private static String rowState(List<Map<String,Object>> rows,UUID id) {
    return rows.stream().filter(r->r.get("lootId").equals(id.toString())).findFirst()
        .map(r->(String)r.get("state")).orElse("");
  }

  private static Process start(Path java,Path jar,Path map,Path output,int port) throws Exception {
    Path control=output.resolve("control-1.token");
    var command=new ProcessBuilder(java.toString(),"-Djava.awt.headless=true","-jar",jar.toString(),
        "--online",map.toString(),Integer.toString(port));
    command.directory(jar.getParent().toFile());
    command.environment().put("T4C_CONTROL_FILE",control.toString());
    command.environment().put("T4C_BIND_ADDRESS","127.0.0.1");
    command.redirectOutput(output.resolve("server.out.log").toFile());
    command.redirectError(output.resolve("server.err.log").toFile());
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

  private static void require(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
}
