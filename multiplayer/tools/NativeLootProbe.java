package com.perso.T4C.server;

import com.google.gson.GsonBuilder;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.GroundLootView;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.player.PlayerProgression;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
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
 *
 * <p>Flow: ONE killer is provisioned and joins alone. It walks to and kills Skeletons until a drop
 * appears. Each further character (racer, weighted, second racer) is provisioned only when the drop it
 * needs exists, on a standable hosted tile within pickup range of that drop, so no pickup needs a walk
 * and no weak character is parked next to patrolling Skeletons earlier than necessary.
 */
public final class NativeLootProbe {
  /** The hosted starter region lives on world 0; the start tile is derived, never hard-coded (see {@link #resolveStart}). */
  private static final int WORLD=0;
  private static final String QUEST_ID="renewed_wards", MONSTER="Skeleton";
  private static final int START_SECONDS=30, STOP_SECONDS=100;
  private static final long TOTAL_MS=TimeUnit.MINUTES.toMillis(40);
  /** Per drop; ground loot lives 15 minutes ({@code OnlineStore.LOOT_LIFETIME_MILLIS}), so a drop is never held longer. */
  private static final long DROP_WAIT_MS=TimeUnit.MINUTES.toMillis(14);
  private static final long TARGET_MS=TimeUnit.SECONDS.toMillis(75);        // one Skeleton must die within this
  private static final long NO_ACCEPT_MS=TimeUnit.SECONDS.toMillis(30);     // in reach with zero accepted attacks
  private static final long NO_TARGET_MS=TimeUnit.SECONDS.toMillis(90);     // no live Skeleton in the snapshot
  private static final long IGNORE_MS=TimeUnit.SECONDS.toMillis(30);
  private static final long FRESH_LOOT_MS=TimeUnit.MINUTES.toMillis(10);    // reuse only drops younger than this
  private static final double REACH_PX=GameConstants.PLAYER_MELEE_ATTACK_RANGE-12;
  private static final double PICKUP_TILES=7d;                              // server limit is 8 (OnlinePickupRules)
  private static final String[] REQUIRED_DB={"T4C_DB_URL","T4C_DB_USER","T4C_DB_PASSWORD"};
  /** The adapter maps server pickup codes to keys (OnlineCommandRefusals); a busy/revision race refusal maps to the generic key. */
  private static final Set<String> RACE_REFUSALS=Set.of("online.loot.unavailable","online.loot.refused");
  private static final String TOO_HEAVY="online.loot.too_heavy";
  private static final Diag DIAG=new Diag();

  private record Player(String label,OnlineStore.AccountKey key,OnlineStore.CharacterState created,
      OnlineConnection.Credentials credentials) {}
  private record Outcome(boolean accepted,String messageKey,String error) {}
  /** Start tile (tile centre) beside an authored Skeleton anchor, both in world-0 tile coordinates. */
  private record Start(double x,double y,int anchorX,int anchorY,int circleSkeletons) {}
  private record Geo(HostedRegionPlan plan,PreviewMap map,Start start) {}
  private record Leveled(CharacterProgress progress,long experience) {}
  private record Seen(UUID id,String key,long seenMs,double x,double y) {}

  /** Walking, fighting and refusal evidence. Labels, rounded numbers and message keys only. */
  private static final class Diag {
    final Map<String,Integer> counts=new TreeMap<>();
    final List<String> samples=new ArrayList<>();
    final Map<String,Integer> perKey=new HashMap<>();
    synchronized void note(String kind,String key,String detail) {
      String k=kind+":"+key;
      counts.merge(k,1,Integer::sum);
      if(perKey.merge(k,1,Integer::sum)<=6 && samples.size()<120) samples.add(k+" "+detail);
    }
    void event(String line) { if(samples.size()<120) samples.add(line); }
  }

  /** A connected adapter plus the small amount of fighting state the probe tracks for it. */
  private static final class Fighter {
    final String label; final Player player; final OnlineConnection c;
    double lastX=Double.NaN,lastY=Double.NaN;
    int kills,acceptedAttacks,lastDx,lastDy;
    long lastAttackMs,lastPotionMs,targetSince,firstReachMs=-1,noTargetSince=-1;
    long target=-1; int targetAccepted;
    final Map<Long,Long> ignoreUntil=new HashMap<>();
    Fighter(String label,Player player,OnlineConnection c) { this.label=label; this.player=player; this.c=c; }
  }

  private static long ms() { return System.nanoTime()/1_000_000L; }

  /**
   * Derives the characters' start from the same sources {@code HostedKillRegionTest} uses: the real
   * {@code ServerMain --online} region plan ({@link HostedRegionPlan#startingSlice}), the
   * {@code renewed_wards} quest circle and the authored Skeleton spawns inside it. Picks the first
   * anchor (by x, then y) that has a standable, non-portal tile within melee reach with combat line of
   * sight, and re-checks that the tile is hosted. Fails with a clear message otherwise. It only reads
   * authored data and the collision map; it never weakens a server guard.
   */
  private static Geo resolveStart(Path worldCollision) throws Exception {
    HostedRegionPlan plan=HostedRegionPlan.startingSlice(worldCollision);
    var region=plan.regions().stream().filter(r->r.spec().world()==WORLD).findFirst()
        .orElseThrow(()->new IllegalArgumentException("Precondition failed: the starting slice hosts no world-"+WORLD+" region"));
    PreviewMap map=region.map();
    var quest=QuestCatalog.bundled().definition(QUEST_ID);
    var skeletons=MonsterCatalog.loadBundled().spawns().stream()
        .filter(s->MONSTER.equals(s.definition()) && s.world()==WORLD
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
        if(!standable(plan,wx,wy)) continue;
        double rx=wx-map.originX,ry=wy-map.originY;
        double distance=Math.hypot(dx*(double)GameConstants.GRID_W,dy*(double)GameConstants.GRID_H);
        if(distance>GameConstants.PLAYER_MELEE_ATTACK_RANGE) continue;
        if(!map.allowsCombat(rx,ry,anchor.x()-map.originX+.5,anchor.y()-map.originY+.5,false)) continue;
        if(distance<bestDistance) { bestDistance=distance; best=new Start(wx,wy,anchor.x(),anchor.y(),skeletons.size()); }
      }
      if(best!=null) return new Geo(plan,map,best);
    }
    throw new IllegalArgumentException("Precondition failed: none of the "+skeletons.size()
        +" authored Skeleton anchors has a standable, hosted, melee-reachable adjacent tile");
  }

  /** Hosted, standable and never a portal source (enabled or refused). */
  private static boolean standable(HostedRegionPlan plan,double wx,double wy) {
    if(!plan.canStand(new CharacterProgress.Respawn(WORLD,wx,wy))) return false;
    return !(plan.isRefusedPortalSource(WORLD,wx,wy) || plan.portals.resolve(WORLD,wx,wy,plan::canStand).isPresent());
  }

  /** Sanitized evidence: no account IDs, secrets, tokens, database credentials or paths beyond the output dir. */
  private static final class Report {
    final Path file;
    final Map<String,Object> values=new LinkedHashMap<>();
    final List<String> stages=new ArrayList<>();
    Report(Path file) { this.file=file; values.put("tool","NativeLootProbe"); values.put("task","T4C-0204 L12");
      values.put("startedAt",Instant.now().toString()); values.put("status","RUNNING"); }
    void stage(String name) { stages.add(Instant.now()+" "+name); }
    void put(String key,Object value) { values.put(key,value); }
    void finish(String status) throws Exception {
      values.put("status",status); values.put("stages",stages); values.put("finishedAt",Instant.now().toString());
      values.put("attackAndCommandOutcomes",DIAG.counts); values.put("diagnosticSamples",DIAG.samples);
      values.put("evidenceScope","actual native adapters; not rendered-client evidence");
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
    final Geo geo=resolveStart(map);
    final Start start=geo.start();
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
    List<OnlineConnection> open=new ArrayList<>();
    try(var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        var store=OnlineStore.fromEnvironment()) {
      long deadline=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(TOTAL_MS);
      report.stage(stage="start owned server");
      child=start(javaExe,jar,map,output,port);
      healthy(http,endpoint,child);
      report.put("serverHealth","local-persistent");
      ItemContent content=ItemCatalog.loadBundled();

      // Level policy (documented in NATIVE_LOOT_PROBE.md): the quest has no minimum level, the Skeleton is
      // level N, and anchors sit a few tiles apart inside the 8-tile aggro range, so two can engage at once.
      // A killer of twice the monster level is the probe's headroom. It is reached through the server's own
      // XP award path (OnlineProgression), not by writing stats.
      int monsterLevel=MonsterCatalog.loadBundled().monsters().get(MONSTER).getLevel();
      int questMin=QuestCatalog.bundled().definition(QUEST_ID).getMinLevel();
      int level=Math.max(Math.max(1,questMin),monsterLevel*2);
      report.put("skeletonLevel",monsterLevel);
      report.put("questMinimumLevel",questMin);
      report.put("probeCharacterLevel",level);

      report.stage(stage="provision and join the killer alone");
      var killerKit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(212));
      var killerP=provisionLeveled(store,"Loot probe killer","killer",start.x(),start.y(),killerKit,level,false,content,report);
      Fighter killer=new Fighter("killer",killerP,joined(endpoint,killerP)); open.add(killer.c);
      int protocol=killer.c.view().map().protocolVersion();
      require(protocol==OnlineProtocol.VERSION && protocol==5,"Server protocol is not version 5");
      report.put("protocolVersion",protocol);
      report.put("killerHpAtJoin",self(killer).maxHp());

      Map<UUID,Seen> known=new LinkedHashMap<>();
      Set<UUID> used=new java.util.HashSet<>();
      Player racerP=null,weightedP=null,racer2P=null;

      // ---- Drop 1: simultaneous claim race (killer vs a racer provisioned beside the drop) ----
      report.stage(stage="kill skeletons until the first drop appears");
      Seen raceDrop=awaitLoot(killer,known,used,deadline,report);
      used.add(raceDrop.id());
      report.stage(stage="provision racer beside the first drop and wait out the owner window");
      var racerKit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(213));
      racerP=provisionNear(store,geo,killer,raceDrop,"Loot probe racer","racer",racerKit,level,false,content,report);
      Fighter racer=new Fighter("racer",racerP,joined(endpoint,racerP)); open.add(racer.c);
      require(visible(racer.c,raceDrop.id()),"Observer did not see the committed first drop");
      report.put("observerSawFirstDrop",true);
      holdUntilOwnerExpired(killer,racer,raceDrop,deadline);
      report.stage(stage="simultaneous claim race after owner window");
      settle(killer.c,killerP,store);settle(racer.c,racerP,store);
      requireInPickupRange(killer,raceDrop);requireInPickupRange(racer,raceDrop);
      CountDownLatch go=new CountDownLatch(1);
      Future<Outcome> fa=pool.submit(() -> { go.await(); return pick(killer,raceDrop.id()); });
      Future<Outcome> fb=pool.submit(() -> { go.await(); return pick(racer,raceDrop.id()); });
      go.countDown();
      Outcome oa=fa.get(30,TimeUnit.SECONDS), ob=fb.get(30,TimeUnit.SECONDS);
      int wins=(oa.accepted()?1:0)+(ob.accepted()?1:0);
      require(wins==1,"Race did not produce exactly one winner: "+oa+" / "+ob);
      Outcome loser=oa.accepted()?ob:oa;
      require(loser.error()==null && RACE_REFUSALS.contains(loser.messageKey()),
          "Race loser was not refused as unavailable/busy: "+loser);
      report.put("raceWinner",oa.accepted()?"killer":"racer");
      report.put("raceLoserRefusal",loser.messageKey());
      racer.c.close(); open.remove(racer.c);

      // ---- Drop 2: over-weight refusal leaves the loot ----
      report.stage(stage="kill skeletons until a second drop appears");
      Seen weightDrop=awaitLoot(killer,known,used,deadline,report);
      used.add(weightDrop.id());
      report.stage(stage="provision over-weight character beside the second drop");
      var weightKit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(214));
      weightedP=provisionNear(store,geo,killer,weightDrop,"Loot probe weighted","weighted",weightKit,level,true,content,report);
      Fighter weighted=new Fighter("weighted",weightedP,joined(endpoint,weightedP)); open.add(weighted.c);
      holdUntilOwnerExpired(killer,weighted,weightDrop,deadline);
      report.stage(stage="over-weight refusal leaves the loot");
      settle(weighted.c,weightedP,store);
      requireInPickupRange(weighted,weightDrop);
      Outcome heavyOutcome=pickSettled(weighted,weightDrop.id());
      require(!heavyOutcome.accepted() && TOO_HEAVY.equals(heavyOutcome.messageKey()),
          "Over-weight pickup was not refused as too_heavy: "+heavyOutcome);
      require(visible(killer.c,weightDrop.id()),"Refused loot disappeared from the observer snapshot");
      report.put("overweightRefusal",heavyOutcome.messageKey());
      weighted.c.close(); open.remove(weighted.c);

      // ---- Drop 3: lost response, reconnect, no replay ----
      report.stage(stage="kill skeletons until a third drop appears");
      Seen lostDrop=awaitLoot(killer,known,used,deadline,report);
      used.add(lostDrop.id());
      report.stage(stage="provision second racer beside the third drop");
      var racer2Kit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(215));
      racer2P=provisionNear(store,geo,killer,lostDrop,"Loot probe racer two","racer2",racer2Kit,level,false,content,report);
      Fighter racer2=new Fighter("racer2",racer2P,joined(endpoint,racer2P)); open.add(racer2.c);
      holdUntilOwnerExpired(killer,racer2,lostDrop,deadline);
      report.stage(stage="lost-response reconnect does not replay");
      settle(racer2.c,racer2P,store);
      requireInPickupRange(racer2,lostDrop);
      var inFlight=racer2.c.pickUp(lostDrop.id());
      racer2.c.close(); open.remove(racer2.c);
      boolean answeredBeforeClose=inFlight.toCompletableFuture().isDone();
      report.put("lostResponseAnsweredBeforeClose",answeredBeforeClose);
      Thread.sleep(500);
      try(var b2=rejoin(endpoint,racer2P,deadline)) {
        settle(b2,racer2P,store);
        report.put("reconnectJoined",true);
        // No replay: the probe issues no second pickup; the database check below decides the outcome.
      }

      collect(killer,known);
      report.put("skeletonKills",killer.kills);
      report.put("dropsObservedByKiller",known.size());
      report.put("killerExperienceAtEnd",store.load(killerP.key().accountId(),killerP.created().id()).experience());
      stopAll(killer.c);open.remove(killer.c);
      report.stage(stage="graceful shutdown and database verification");
      graceful(http,endpoint,child,output.resolve("control-1.token"));
      child=null;
      verify(store,databaseUrl,report,known.keySet(),List.of(killerP,racerP,weightedP,racer2P),
          raceDrop.id(),weightDrop.id(),lostDrop.id(),oa.accepted()?killerP:racerP);
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
      for(var c:open) { try { c.close(); } catch(RuntimeException ignored) { } }
      if(child!=null && child.isAlive()) { child.destroyForcibly(); child.waitFor(10,TimeUnit.SECONDS); }
    }
    if(failed) System.exit(1);
  }

  private static void stopAll(OnlineConnection... connections) {
    for(var c:connections) if(c!=null) c.close();
  }

  // ---------------------------------------------------------------- provisioning

  /**
   * Raises a fresh warrior to {@code level} by feeding the server's own award path
   * ({@link OnlineProgression#award}, the code that runs when a kill pays XP) in memory: HP, mana and
   * unspent points come out exactly as a player's would. The matching cumulative XP is returned so the
   * ledger can be stored beside the progress; both bounds the server checks on every award hold by construction.
   */
  private static Leveled levelUp(CharacterProgress start,int level,String name) {
    XpCurve curve=XpCurve.loadDefault();
    double rate=PlayerProgression.scaledAward(1_000_000L,0f,false)/1_000_000d;
    long total=0; CharacterProgress p=start; Random random=new Random(level*7919L+name.hashCode());
    for(int i=0;i<64 && p.advancement().level()<level;i++) {
      long remaining=curve.getTotalXp(level)-total;
      long base=Math.max(1L,(long)Math.ceil(remaining/rate));
      var state=new OnlineStore.CharacterState(UUID.randomUUID(),UUID.randomUUID(),name,WORLD,0,0,0,total,0,p);
      var award=OnlineProgression.award(state,base,0,random);
      if(award.experience()<=0) break;
      total+=award.experience(); p=award.progress();
    }
    int reached=p.advancement().level();
    require(reached>=level && total>=curve.getTotalXp(reached) && total<curve.getTotalXp(reached+1),
        "Could not reach a consistent level "+level+" (got "+reached+")");
    return new Leveled(p,total);
  }

  /**
   * Creates a leveled character through {@link OnlineStore#create} (the same path as before) and stores
   * the matching cumulative XP in the new row before the character ever joins, so the server's own two-sided
   * progression check accepts the first award. No server guard is bypassed at runtime.
   */
  private static Player provisionLeveled(OnlineStore store,String name,String label,double x,double y,
      OnlineCharacterCreator.Prepared kit,int level,boolean overweight,ItemContent content,Report report) throws Exception {
    Leveled leveled=levelUp(kit.progress(),level,name);
    CharacterProgress progress=leveled.progress();
    if(overweight) {
      // The weighted character keeps its full equipped kit; the fixture stack is appended (T4C-0245).
      var fixture=OnlineOverweightFixture.size(progress,content);
      progress=OnlineOverweightFixture.apply(progress,fixture);
      report.put("overweightFixtureItemKey",fixture.itemKey());
      report.put("overweightFixtureQuantity",fixture.quantity());
      report.put("carryMaximum",fixture.maximum());
      report.put("carriedBeforeFixture",fixture.carriedBefore());
      report.put("carriedWithFixture",fixture.carriedAfter());
      require(fixture.carriedAfter()>fixture.maximum(),"Overweight fixture does not exceed the carry maximum");
    }
    var key=store.provisionAccount();
    var created=store.create(key.accountId(),name,WORLD,x,y,progress,kit.gold());
    try(var db=DriverManager.getConnection(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"));
        var ps=db.prepareStatement("UPDATE online_characters SET experience=? WHERE id=? AND revision=0")) {
      ps.setLong(1,leveled.experience()); ps.setObject(2,created.id());
      require(ps.executeUpdate()==1,"Experience ledger seed did not apply to the new character");
    }
    var seeded=store.load(key.accountId(),created.id());
    require(seeded.experience()==leveled.experience() && seeded.progress().advancement().level()==level,
        "Provisioned character level/experience mismatch");
    report.put("level."+label,seeded.progress().advancement().level());
    report.put("experienceSeed."+label,leveled.experience());
    report.put("maxHp."+label,seeded.progress().vitals().maxHp());
    return new Player(label,key,seeded,new OnlineConnection.Credentials(key.secret(),seeded.id()));
  }

  /**
   * A later character is provisioned on a standable hosted tile within pickup range of its drop, as far as
   * possible from any live Skeleton (world coordinates), so the claim needs no walk. Same {@code create} path.
   */
  private static Player provisionNear(OnlineStore store,Geo geo,Fighter killer,Seen drop,String name,String label,
      OnlineCharacterCreator.Prepared kit,int level,boolean overweight,ItemContent content,Report report) throws Exception {
    double lootWx=drop.x()+geo.map().originX,lootWy=drop.y()+geo.map().originY;
    List<double[]> live=new ArrayList<>();
    for(var cr:killer.c.view().snapshot().creatures())
      if(isSkeleton(cr) && cr.health()>0) live.add(new double[]{cr.x()+geo.map().originX,cr.y()+geo.map().originY});
    double[] best=null; double bestScore=-1,bestLoot=Double.MAX_VALUE;
    int cx=(int)Math.floor(lootWx),cy=(int)Math.floor(lootWy);
    for(int dy=-6;dy<=6;dy++) for(int dx=-6;dx<=6;dx++) {
      double wx=cx+dx+.5,wy=cy+dy+.5;
      if(Math.max(Math.abs(wx-lootWx),Math.abs(wy-lootWy))>6.5 || !standable(geo.plan(),wx,wy)) continue;
      double score=Double.MAX_VALUE;
      for(var s:live) score=Math.min(score,Math.hypot(s[0]-wx,s[1]-wy));
      double toLoot=Math.hypot(wx-lootWx,wy-lootWy);
      if(score>bestScore+1e-9 || (Math.abs(score-bestScore)<=1e-9 && toLoot<bestLoot)) { best=new double[]{wx,wy}; bestScore=score; bestLoot=toLoot; }
    }
    require(best!=null,"No standable hosted tile within pickup range of the drop for "+label);
    report.put("spawnDistanceToNearestSkeleton."+label,Math.round(Math.min(bestScore,9999)*10)/10.0);
    return provisionLeveled(store,name,label,best[0],best[1],kit,level,overweight,content,report);
  }

  // ---------------------------------------------------------------- fighting

  private static boolean isSkeleton(CreatureView c) { return c.definition().toLowerCase(Locale.ROOT).contains("skeleton"); }

  private static OnlineConnection.Actor self(Fighter f) {
    var view=f.c.view();
    if(view==null) throw new AssertionError(f.label+": adapter has no view (status="+f.c.status()+")");
    for(var a:view.snapshot().players()) if(a.id()==view.ownId()) return a;
    throw new AssertionError(f.label+": own actor missing from the snapshot");
  }

  /** Stops with a clear message the moment a character is dead, respawned elsewhere or disconnected. */
  private static OnlineConnection.Actor observe(Fighter f) {
    if("disconnected".equals(f.c.status())) throw new AssertionError(f.label+" disconnected; "+summary(f));
    var me=self(f);
    boolean teleported=!Double.isNaN(f.lastX) && Math.max(Math.abs(me.x()-f.lastX),Math.abs(me.y()-f.lastY))>15;
    if(me.hp()<=0 || teleported)
      throw new AssertionError(f.label+" was "+(me.hp()<=0 ? "defeated (hp 0)" : "moved far away, presumed defeated and respawned")
          +" at local ("+Math.round(me.x())+","+Math.round(me.y())+") hp "+me.hp()+"/"+me.maxHp()+"; "+summary(f));
    f.lastX=me.x(); f.lastY=me.y();
    return me;
  }

  private static String summary(Fighter f) {
    return "kills="+f.kills+" acceptedAttacks="+f.acceptedAttacks+" outcomes="+DIAG.counts;
  }

  private static double distPx(OnlineConnection.Actor me,double x,double y) {
    return Math.hypot((me.x()-x)*GameConstants.GRID_W,(me.y()-y)*GameConstants.GRID_H);
  }

  /**
   * Holds the adapter's movement intent toward a local point. The server finishes a claimed tile
   * (single-player's grid reservation rule), so the intent is chosen relative to the tile the player is
   * ARRIVING on (the last direction sent), with a half-tile dead band so it cannot oscillate around a target.
   */
  private static void steer(Fighter f,OnlineConnection.Actor me,double tx,double ty) {
    double ax=arrivingTile(me.x(),f.lastDx)+.5,ay=arrivingTile(me.y(),f.lastDy)+.5;
    int dx=Math.abs(tx-ax)>=.5 ? (tx>ax?1:-1) : 0;
    int dy=Math.abs(ty-ay)>=.5 ? (ty>ay?1:-1) : 0;
    f.lastDx=dx; f.lastDy=dy;
    f.c.direction(dx,dy);
  }

  /** Tile a glide is arriving on (the last direction sent while off-centre); mirrors HostedTransferTest.walkPortal. */
  private static int arrivingTile(double c,int last) {
    int tile=(int)Math.round(c-.5);
    if(Math.abs(c-Math.floor(c)-.5)>1e-6) tile=last>0 ? (int)Math.ceil(c-.5) : last<0 ? (int)Math.floor(c-.5) : tile;
    return tile;
  }

  private enum Tick { ATTACKED, WALKING, IDLE, NO_TARGET }

  /** One bounded step: heal if hurt, retarget by distance, walk when out of reach (if allowed), else attack. */
  private static Tick fightTick(Fighter f,boolean walk) throws Exception {
    var me=observe(f);
    long now=ms();
    var view=f.c.view();
    // A target that is gone or at 0 health after at least one accepted attack counts as a kill.
    if(f.target>0) {
      CreatureView t=null;
      for(var c:view.snapshot().creatures()) if(c.id()==f.target) t=c;
      if(t==null || t.health()<=0) { if(f.targetAccepted>0) f.kills++; f.target=-1; f.targetAccepted=0; f.firstReachMs=-1; }
    }
    heal(f,me);
    CreatureView pick=null; double pickDist=Double.MAX_VALUE;
    for(var c:view.snapshot().creatures()) {
      if(!isSkeleton(c) || c.health()<=0) continue;
      Long until=f.ignoreUntil.get(c.id());
      if(until!=null && until>now) continue;
      double d=distPx(me,c.x(),c.y());
      if(d<pickDist) { pickDist=d; pick=c; }
    }
    if(pick==null) { f.c.stopInput(); if(f.noTargetSince<0) f.noTargetSince=now; return Tick.NO_TARGET; }
    f.noTargetSince=-1;
    if(f.target!=pick.id()) { f.target=pick.id(); f.targetSince=now; f.targetAccepted=0; f.firstReachMs=-1; }
    if(now-f.targetSince>TARGET_MS) {
      DIAG.note("target","timeout",f.label+" skeleton not dead after "+TARGET_MS/1000+"s dist="+Math.round(pickDist)+"px hp="+me.hp());
      f.ignoreUntil.put(pick.id(),now+IGNORE_MS); f.target=-1; f.c.stopInput(); return Tick.IDLE;
    }
    if(pickDist<=REACH_PX) {
      f.c.stopInput();
      if(f.firstReachMs<0) f.firstReachMs=now;
      if(f.acceptedAttacks==0 && now-f.firstReachMs>NO_ACCEPT_MS)
        throw new AssertionError(f.label+" has been in melee reach for "+NO_ACCEPT_MS/1000
            +"s with no accepted attack (hp "+me.hp()+"/"+me.maxHp()+", dist "+Math.round(pickDist)+"px); outcomes="+DIAG.counts);
      if(now-f.lastAttackMs>=300) { f.lastAttackMs=now; attack(f,pick,me,pickDist); }
      return Tick.ATTACKED;
    }
    if(!walk) { f.c.stopInput(); return Tick.IDLE; }
    steer(f,me,pick.x(),pick.y());
    return Tick.WALKING;
  }

  /** Logs every outcome: accepted, the adapter's refusal key (null means the adapter does not map attack refusals), or the exception. */
  private static void attack(Fighter f,CreatureView target,OnlineConnection.Actor me,double dist) {
    String key; String detail="dist="+Math.round(dist)+"px hp="+me.hp()+"/"+me.maxHp()+" inReach="+(dist<=GameConstants.PLAYER_MELEE_ATTACK_RANGE);
    try {
      var r=f.c.attack(target.id()).toCompletableFuture().get(5,TimeUnit.SECONDS);
      if(r.accepted()) { f.acceptedAttacks++; f.targetAccepted++; key="accepted"; }
      else key=r.messageKey()==null ? "refused_reason_not_exposed_by_adapter" : r.messageKey();
    } catch(java.util.concurrent.ExecutionException e) {
      key="exception:"+(e.getCause()==null?"?":e.getCause().getClass().getSimpleName());
      detail+=" "+truncate(String.valueOf(e.getCause()==null?e.getMessage():e.getCause().getMessage()));
    } catch(java.util.concurrent.TimeoutException e) { key="timeout";
    } catch(IllegalStateException e) { key="not_sent:"+truncate(e.getMessage());
    } catch(InterruptedException e) { Thread.currentThread().interrupt(); key="interrupted"; }
    DIAG.note("attack."+f.label,key,detail);
  }

  /** A hurt fighter drinks one carried healing potion through the normal use command (server-owned effect). */
  private static void heal(Fighter f,OnlineConnection.Actor me) {
    long now=ms();
    if(me.hp()*100>=me.maxHp()*50 || now-f.lastPotionMs<3000) return;
    var view=f.c.view();
    if(view.character()==null || view.character().progress()==null) return;
    for(var item:view.character().progress().inventory()) {
      if(!item.key().contains("healing_potion") || view.character().progress().equipment().containsValue(item.id())) continue;
      f.lastPotionMs=now;
      String key;
      try {
        var r=f.c.useItem(item.id()).toCompletableFuture().get(5,TimeUnit.SECONDS);
        key=r.accepted() ? "accepted" : (r.messageKey()==null ? "refused" : r.messageKey());
      } catch(Exception e) { key="exception:"+e.getClass().getSimpleName(); }
      DIAG.note("potion."+f.label,key,"hp="+me.hp()+"/"+me.maxHp());
      return;
    }
  }

  private static String truncate(String s) { return s==null ? "" : (s.length()>80 ? s.substring(0,80) : s).replaceAll("[\\r\\n]"," "); }

  private static void collect(Fighter f,Map<UUID,Seen> known) {
    for(GroundLootView l:f.c.view().snapshot().loot())
      known.putIfAbsent(l.id(),new Seen(l.id(),l.key(),ms(),l.x(),l.y()));
  }

  /**
   * Returns a drop the stage can use: an unused, still visible, recent one, else kills until one appears.
   * Fails with a clear message at the per-drop deadline instead of spinning.
   */
  private static Seen awaitLoot(Fighter killer,Map<UUID,Seen> known,Set<UUID> used,long deadline,Report report) throws Exception {
    long stageEnd=Math.min(deadline,System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(DROP_WAIT_MS));
    int killsAtStart=killer.kills;
    while(System.nanoTime()<stageEnd) {
      collect(killer,known);
      for(var seen:known.values())
        if(!used.contains(seen.id()) && ms()-seen.seenMs()<FRESH_LOOT_MS && visible(killer.c,seen.id())) return seen;
      Tick t=fightTick(killer,true);
      if(t==Tick.NO_TARGET && ms()-killer.noTargetSince>NO_TARGET_MS)
        throw new AssertionError("No live Skeleton in the snapshot for "+NO_TARGET_MS/1000+"s; "+summary(killer));
      Thread.sleep(120);
    }
    throw new AssertionError("No new loot drop within the stage budget after "+(killer.kills-killsAtStart)+" kills; "+summary(killer));
  }

  /** Keeps the killer near the drop (fighting only what is in reach) and the other adapter defending until the owner window ends. */
  private static void holdUntilOwnerExpired(Fighter killer,Fighter other,Seen drop,long deadline) throws Exception {
    long end=Math.min(deadline,System.nanoTime()+TimeUnit.SECONDS.toNanos(150));
    while(System.nanoTime()<end) {
      var found=other.c.view().snapshot().loot().stream().filter(l->l.id().equals(drop.id())).findFirst();
      require(found.isPresent(),"Loot vanished before its claim window ended");
      if(found.get().ownerRemainingMs()==0) return;
      var me=observe(killer);
      if(Math.max(Math.abs(me.x()-drop.x()),Math.abs(me.y()-drop.y()))>5) { steer(killer,me,drop.x(),drop.y()); }
      else fightTick(killer,false);
      fightTick(other,false);
      Thread.sleep(150);
    }
    throw new AssertionError("Owner window did not expire before the deadline");
  }

  private static void requireInPickupRange(Fighter f,Seen drop) {
    var me=observe(f);
    double cheb=Math.max(Math.abs(me.x()-drop.x()),Math.abs(me.y()-drop.y()));
    require(cheb<=PICKUP_TILES,f.label+" is "+Math.round(cheb*10)/10.0+" tiles from the drop, outside pickup range");
  }

  // ---------------------------------------------------------------- loot commands

  private static boolean visible(OnlineConnection c,UUID loot) {
    return c.view().snapshot().loot().stream().anyMatch(l->l.id().equals(loot));
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

  private static Outcome pick(Fighter f,UUID loot) {
    try {
      var r=f.c.pickUp(loot).toCompletableFuture().get(10,TimeUnit.SECONDS);
      DIAG.note("pickup."+f.label,r.accepted()?"accepted":String.valueOf(r.messageKey()),"");
      return new Outcome(r.accepted(),r.messageKey(),null);
    } catch(Exception e) {
      DIAG.note("pickup."+f.label,"exception:"+e.getClass().getSimpleName(),truncate(e.getMessage()));
      return new Outcome(false,null,e.getClass().getSimpleName());
    }
  }

  /** Retries only the generic busy/revision refusal; a ground-state refusal is final and never replayed. */
  private static Outcome pickSettled(Fighter f,UUID loot) throws Exception {
    Outcome last=null;
    for(int i=0;i<10;i++) {
      last=pick(f,loot);
      if(last.accepted() || last.messageKey()==null || !last.messageKey().equals("online.loot.refused")) return last;
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

  /** /join admits at most two concurrent public requests (HttpRequestAdmission.MAX_PUBLIC); adapters join one at a time. */
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
