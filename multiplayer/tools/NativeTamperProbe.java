package com.perso.T4C.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.quest.QuestRules;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Tamper battery (T4C-0251): two legitimately provisioned characters on one real ServerMain --online child,
 * raw HTTP for the attacker (A) and the real native OnlineConnection adapter for the victim (B). Every case
 * records the exact refusal, whether A's or B's committed state changed, and (per group) that B still plays.
 * The probe never edits server code or weakens a guard; a FAIL row is a finding.
 */
public final class NativeTamperProbe {
  private static final int START_SECONDS=40, WORLD=0;
  private static final Gson GSON=new Gson();
  private static final String VERSION=Integer.toString(OnlineProtocol.VERSION);
  private static final String UUID_RE="[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
  private static final double SPEED_PER_TICK=com.perso.T4C.config.GameConstants.PLAYER_SPEED
      /(double)com.perso.T4C.config.GameConstants.GRID_W/(double)PreviewWorld.TICK_RATE;

  private record Acct(OnlineStore.AccountKey key,OnlineStore.CharacterState created) {
    UUID account() { return key.accountId(); }
    UUID id() { return created.id(); }
    String secret() { return key.secret(); }
  }
  private interface Check { boolean ok() throws Exception; }
  private record Reply(int status,String body,String io) {
    boolean is(int... codes) { if(io!=null) return false; for(int c:codes) if(status==c) return true; return false; }
    String code() {
      try { return JsonParser.parseString(body).getAsJsonObject().get("error").getAsString(); }
      catch(Exception none) { return null; }
    }
    boolean isRefusal(String code) { return is(409) && code.equals(code()); }
    String text() {
      if(io!=null) return "no HTTP status ("+io+")";
      if(status==200) return body.length()<110 && !body.contains("token") ? "200 "+body : "200 (body withheld)";
      String c=code();
      return status+(c!=null ? " "+c : body.isBlank() ? "" : " "+abbreviate(body));
    }
  }
  private record Row(String id,String what,String expected,String observed,boolean pass) {}
  private record Fp(long gold,long xp,CharacterProgress.Attributes at,CharacterProgress.Advancement ad,
      List<CharacterProgress.Item> inv,Map<String,UUID> eq,Map<String,Integer> skills,Set<String> spells,
      Map<String,Integer> flags,List<CharacterProgress.Item> storage,long storageGold,int world) {}
  private record Snap(Fp a,Fp b) {
    String diff(Snap other) {
      String x=diffOne("A",a,other.a),y=diffOne("B",b,other.b);
      return (x+" "+y).trim();
    }
    private static String diffOne(String who,Fp p,Fp q) {
      if(p.equals(q)) return "";
      var names=new ArrayList<String>();
      if(p.gold!=q.gold) names.add("gold "+p.gold+"->"+q.gold);
      if(p.xp!=q.xp) names.add("experience "+p.xp+"->"+q.xp);
      if(!p.at.equals(q.at)) names.add("attributes");
      if(!p.ad.equals(q.ad)) names.add("advancement");
      if(!p.inv.equals(q.inv)) names.add("inventory "+p.inv.size()+"->"+q.inv.size()+" stacks");
      if(!p.eq.equals(q.eq)) names.add("equipment");
      if(!p.skills.equals(q.skills)) names.add("skills");
      if(!p.spells.equals(q.spells)) names.add("spells");
      if(!p.flags.equals(q.flags)) names.add("questFlags");
      if(!p.storage.equals(q.storage) || p.storageGold!=q.storageGold) names.add("storage");
      if(p.world!=q.world) names.add("world "+p.world+"->"+q.world);
      return who+": "+String.join(", ",names);
    }
  }

  private static final List<Row> ROWS=new ArrayList<>();
  private static final List<String> NOT_COVERED=new ArrayList<>();
  private static Path report;
  private static HttpClient http;
  private static URI endpoint;
  private static int port;
  private static OnlineStore store;
  private static Acct A,B,C;
  private static String aToken;
  private static long aEpoch,aActor;
  private static long seq=0;
  private static OnlineConnection bConn;
  private static int originX,originY;
  
  public static void main(String[] args) throws Exception {
    if(args.length!=4) throw new IllegalArgumentException(
        "Usage: NativeTamperProbe RUNTIME_DIR JAVA_EXE UNUSED_LOOPBACK_PORT NEW_OUTPUT_DIR");
    Path runtime=Path.of(args[0]).toRealPath(),java=Path.of(args[1]).toRealPath();
    port=Integer.parseInt(args[2]);
    if(port<1024 || port>65535 || Set.of(18765,18766,18770,18771,55432,55433).contains(port))
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
        "jdbc:postgresql://(?:127\\.0\\.0\\.1|localhost):[0-9]{2,5}/t4c_(?:acc|tamper)_[0-9a-f]{12}(?:\\?[A-Za-z0-9=&]*)?"))
      throw new IllegalArgumentException("Use only a fresh disposable local t4c_acc_<id> or t4c_tamper_<id> database");
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
    report=output.resolve("report.txt");
    Files.writeString(report,"T4C-0251 native tamper battery started="+Instant.now()+" port="+port+"\n"
        +"No account key, session token, database password or control token is recorded here.\n",StandardOpenOption.CREATE_NEW);
    endpoint=URI.create("http://127.0.0.1:"+port);
    http=HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(3)).build();
    Process server=null;
    String stage="preflight";
    boolean failed=false;
    try(var s=OnlineStore.fromEnvironment()) {
      store=s;
      store.migrate();
      var plan=HostedRegionPlan.startingSlice(map);
      var npcs=NpcInteractionCatalog.loadBundled();
      var aelric=npcs.require("WardenAelric");var kilhiam=npcs.require("Kilhiam");
      double[] aTile=standableNear(plan,aelric.world(),aelric.x(),aelric.y());
      double[] bTile=standableNear(plan,kilhiam.world(),kilhiam.x(),kilhiam.y());
      var spawn=plan.regions().stream().filter(r->r.spec().world()==WORLD).findFirst().orElseThrow();
      originX=spawn.map().originX;originY=spawn.map().originY;
      var kitA=OnlineCharacterCreator.prepare(CharacterClass.PALADIN,"female",false,new Random(2511));
      var kitB=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(2512));
      var kitC=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(2513));
      // A starts at 1 hp so a healing potion has something to heal (no healing needed is a refusal, not a test).
      var pa=kitA.progress();
      var hurt=ProgressEdits.withVitals(pa,new CharacterProgress.Vitals(1,pa.vitals().maxHp(),pa.vitals().mana(),pa.vitals().maxMana()));
      stage="provisioning";
      A=provision("Tamper attacker A",aTile[0],aTile[1],hurt,kitA.gold());
      B=provision("Tamper victim B",bTile[0],bTile[1],kitB.progress(),kitB.gold());
      C=provision("Tamper unhosted C",3700.5,1450.5,kitC.progress(),kitC.gold());
      var bItem=B.created.progress().inventory().stream().filter(i->!B.created.progress().equipment().containsValue(i.id())).findFirst().orElseThrow();
      var bWornItem=B.created.progress().equipment().values().iterator().next();
      UUID corpseA=UUID.randomUUID(),corpseB=UUID.randomUUID(),lootB=UUID.randomUUID();
      UUID corpseAItem=UUID.randomUUID(),corpseBItem=UUID.randomUUID();
      String potion="item.light_healing_potion";
      stage="seeding fixtures";
      seed(corpseA,A,aTile,corpseAItem,potion,111,null,null);
      seed(corpseB,B,aTile,corpseBItem,potion,777,lootB,aTile);
      append("Fixtures: A at the WardenAelric tile, B at the Kilhiam tile, C at an unhosted tile; one corpse for A, one corpse and one "
          +"owner-window ground-loot row for B, both at A's tile (DB-seeded rows: the only route to a foreign corpse/loot without lethal combat).\n");
      var command=new ProcessBuilder(java.toString(),"-Djava.awt.headless=true","-jar",jar.toString(),
          "--online",map.toString(),Integer.toString(port));
      command.directory(runtime.toFile());
      command.environment().put("T4C_BIND_ADDRESS","127.0.0.1");
      command.environment().put("T4C_CONTROL_FILE",output.resolve("control.token").toString());
      command.redirectOutput(output.resolve("server.out.log").toFile());
      command.redirectError(output.resolve("server.err.log").toFile());
      server=command.start();
      stage="startup";
      healthy(server);
      Path control=output.resolve("control.token");

      stage="offline join tampering";
      groupOfflineJoin(corpseA,lootB);
      stage="legitimate joins";
      joinBoth();
      stage="info disclosure";
      groupDisclosure(corpseB,lootB);
      stage="authorization";
      groupAuth(control);
      stage="malformed";
      groupMalformed(bItem.id());
      bAlive("after malformed input");
      stage="stale revision / epoch";
      groupStale(potion);
      bAlive("after stale revision/epoch");
      stage="oversize";
      groupOversize();
      bAlive("after oversize bodies");
      stage="forged targets and foreign property";
      groupForged(corpseA,corpseB,lootB,bItem.id(),bWornItem,corpseBItem);
      bAlive("after forged targets");
      stage="idempotency";
      groupIdempotency(corpseA,corpseB,lootB,corpseBItem,potion);
      bAlive("after replay/idempotency");
      stage="stat/item/quest edits";
      groupEdits(bItem.id());
      bAlive("after stat/item/quest edit attempts");
      stage="movement";
      groupMovement(plan,spawn.map());
      bAlive("after movement tampering");
      stage="victim valid play";
      groupVictimPlay(kilhiam.id());
      stage="final database invariants";
      finalInvariants(lootB,corpseA,corpseB,kilhiam.id());
      notCovered();
      stage="graceful shutdown";
      aPost("/leave","");
      if(bConn!=null) { bConn.close();bConn=null; }
      Thread.sleep(2500);
      shutdown(server,control);
      server=null;
      append("Owned persistent server exited 0 after private admin stop.\n");
    } catch(Throwable failure) {
      failed=true;
      append("PROBE ABORTED at "+stage+": "+failure.getClass().getSimpleName()+": "+abbreviate(String.valueOf(failure.getMessage()))+"\n");
      failure.printStackTrace();
    } finally {
      if(bConn!=null) try { bConn.close(); } catch(Throwable ignored) { }
      if(server!=null && server.isAlive()) {
        boolean graceful=false;
        try {
          if(Files.isRegularFile(output.resolve("control.token"))) { shutdown(server,output.resolve("control.token"));graceful=true; }
        } catch(Throwable ignored) { /* Preserve original failure and logs. */ }
        if(!graceful && server.isAlive()) { server.destroyForcibly();append("Owned-server cleanup required forced fallback.\n"); }
      }
    }
    summary(output);
    long fails=ROWS.stream().filter(r->!r.pass).count();
    System.out.println((failed || fails>0 ? "FAIL" : "PASS")+" T4C-0251: "+ROWS.size()+" cases, "+fails+" FAIL; report "+report);
    if(failed || fails>0) System.exit(1);
  }

  // ---------------------------------------------------------------- fixtures

  private static Acct provision(String name,double x,double y,CharacterProgress progress,long gold) throws Exception {
    var key=store.provisionAccount();
    var created=store.create(key.accountId(),name,WORLD,x,y,progress,gold);
    return new Acct(key,created);
  }

  private static double[] standableNear(HostedRegionPlan plan,int world,int tx,int ty) {
    for(int r=0;r<=3;r++) for(int dy=-r;dy<=r;dy++) for(int dx=-r;dx<=r;dx++) {
      if(Math.max(Math.abs(dx),Math.abs(dy))!=r) continue;
      double x=tx+dx+.5,y=ty+dy+.5;
      if(standable(plan,world,x,y)) return new double[]{x,y};
    }
    throw new IllegalArgumentException("Precondition failed: no standable hosted tile beside the NPC at "+tx+","+ty);
  }
  private static boolean standable(HostedRegionPlan plan,int world,double x,double y) {
    if(!plan.canStand(new CharacterProgress.Respawn(world,x,y))) return false;
    return !(plan.isRefusedPortalSource(world,x,y) || plan.portals.resolve(world,x,y,plan::canStand).isPresent());
  }

  /** One corpse (for {@code owner}) and optionally one owner-window ground-loot row, both inserted before the server starts. */
  private static void seed(UUID corpse,Acct owner,double[] at,UUID corpseItem,String itemKey,long gold,UUID loot,double[] lootAt)
      throws Exception {
    String hash="0".repeat(63)+"1";
    try(Connection c=DriverManager.getConnection(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"))) {
      c.setAutoCommit(false);
      try {
        try(var q=c.prepareStatement("INSERT INTO online_combat_events(event_id,character_id,payload_hash) VALUES(?,?,?)")) {
          q.setObject(1,corpse);q.setObject(2,owner.id());q.setString(3,hash);q.executeUpdate();
        }
        String items=GSON.toJson(List.of(new CharacterProgress.Item(corpseItem,itemKey,1,1)));
        try(var q=c.prepareStatement("INSERT INTO online_corpses(id,character_id,world,x,y,items,gold) VALUES(?,?,?,?,?,?::jsonb,?)")) {
          q.setObject(1,corpse);q.setObject(2,owner.id());q.setInt(3,WORLD);q.setDouble(4,at[0]);q.setDouble(5,at[1]);
          q.setString(6,items);q.setLong(7,gold);q.executeUpdate();
        }
        if(loot!=null) {
          UUID source=UUID.randomUUID();
          try(var q=c.prepareStatement("INSERT INTO online_rewards(character_id,event_id,gold,experience,payload_hash) VALUES(?,?,0,0,?)")) {
            q.setObject(1,owner.id());q.setObject(2,source);q.setString(3,hash);q.executeUpdate();
          }
          try(var q=c.prepareStatement("INSERT INTO online_world_rewards(event_id,character_id) VALUES(?,?)")) {
            q.setObject(1,source);q.setObject(2,owner.id());q.executeUpdate();
          }
          try(var q=c.prepareStatement("""
              INSERT INTO online_ground_loot(id,source_event,drop_index,world,x,y,item_key,quantity,charges,owner_character,owner_until,expires_at)
              VALUES (?,?,0,?,?,?,?,1,1,?,clock_timestamp()+interval '30 minutes',clock_timestamp()+interval '40 minutes')
              """)) {
            q.setObject(1,loot);q.setObject(2,source);q.setInt(3,WORLD);q.setDouble(4,lootAt[0]);q.setDouble(5,lootAt[1]);
            q.setString(6,itemKey);q.setObject(7,owner.id());q.executeUpdate();
          }
        }
        c.commit();
      } catch(Exception failure) { c.rollback();throw failure; }
    }
  }

  private static String sql1(String sql,Object... params) throws Exception {
    try(Connection c=DriverManager.getConnection(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"));
        var q=c.prepareStatement(sql)) {
      for(int i=0;i<params.length;i++) q.setObject(i+1,params[i]);
      try(var rows=q.executeQuery()) {
        if(!rows.next()) return "<none>";
        var sb=new StringBuilder();
        for(int i=1;i<=rows.getMetaData().getColumnCount();i++) sb.append(i>1?"|":"").append(rows.getString(i));
        return sb.toString();
      }
    }
  }

  // ---------------------------------------------------------------- groups

  private static void groupOfflineJoin(UUID corpseA,UUID lootB) {
    var hdr=std(null);
    tamper("JOIN-1","A's account key + B's character UUID on /join (B offline, protocol header present)","409, B lease stays free, no session issued",
        r->r.is(409),()->post("/join",hdr,A.secret()+" "+B.id()));
    leaseFree("JOIN-1b","B lease still free after A's foreign join",B);
    tamper("JOIN-2","B's account key + A's character UUID on /join","409, A lease stays free",
        r->r.is(409),()->post("/join",hdr,B.secret()+" "+A.id()));
    leaseFree("JOIN-2b","A lease still free after B's foreign join",A);
    var noVersion=std(null);noVersion.remove(OnlineProtocol.VERSION_HEADER);
    tamper("JOIN-3","foreign /join without the protocol header","426",r->r.is(426),()->post("/join",noVersion,A.secret()+" "+B.id()));
    var oldVersion=std(null);oldVersion.put(OnlineProtocol.VERSION_HEADER,"4");
    tamper("JOIN-4","foreign /join with a stale protocol version (4)","426",r->r.is(426),()->post("/join",oldVersion,A.secret()+" "+B.id()));
    leaseFree("JOIN-4b","B lease still free after protocol-less/old-version foreign joins",B);
    tamper("JOIN-5","random well-formed key + A's UUID","409",r->r.is(409),
        ()->post("/join",hdr,randomHex(64)+" "+A.id()));
    tamper("JOIN-6","A's key + unknown character UUID","409",r->r.is(409),
        ()->post("/join",hdr,A.secret()+" "+UUID.randomUUID()));
    for(String[] bad:new String[][]{
        {"JOIN-7a","63-hex key",randomHex(63)+" "+A.id()},{"JOIN-7b","uppercase key",A.secret().toUpperCase()+" "+A.id()},
        {"JOIN-7c","uppercase UUID",A.secret()+" "+A.id().toString().toUpperCase()},{"JOIN-7d","missing UUID",A.secret()},
        {"JOIN-7e","extra token",A.secret()+" "+A.id()+" x"},{"JOIN-7f","empty body",""},
        {"JOIN-7g","newline separator",A.secret()+"\n"+A.id()},{"JOIN-7h","key and UUID swapped",A.id()+" "+A.secret()},
        {"JOIN-7i","B's UUID twice",B.id()+" "+B.id()}})
      tamper(bad[0],"malformed /join body: "+bad[1],"400 (413 when over the 101-byte cap)",r->r.is(400,413),()->post("/join",hdr,bad[2]));
    var evil=std(null);evil.put("Origin","http://evil.example");
    tamper("JOIN-8","/join from a foreign Origin","403",r->r.is(403),()->post("/join",evil,A.secret()+" "+A.id()));
    var noOrigin=std(null);noOrigin.remove("Origin");
    tamper("JOIN-9","/join without an Origin header","403",r->r.is(403),()->post("/join",noOrigin,A.secret()+" "+A.id()));
    var json=std(null);json.put("Content-Type","application/json");
    tamper("JOIN-10","/join with application/json content type","415",r->r.is(415),()->post("/join",json,A.secret()+" "+A.id()));
    tamper("JOIN-11","C (provisioned at an unhosted tile) /join","409 unhosted_region, no lease kept",
        r->r.isRefusal("unhosted_region"),()->post("/join",hdr,C.secret()+" "+C.id()));
    leaseFree("JOIN-11b","C lease released after the unhosted refusal",C);
    tamper("ROSTER-1","GET /account/characters with A's key lists only A's characters","200 without B's UUID",
        r->r.is(200) && !r.body.contains(B.id().toString()) && r.body.contains(A.id().toString()),
        ()->send("GET","/account/characters",account(A.secret()),null));
    tamper("ROSTER-2","GET /account/characters with a random key","401",r->r.is(401),
        ()->send("GET","/account/characters",account(randomHex(64)),null));
    var noProto=account(A.secret());noProto.remove(OnlineProtocol.VERSION_HEADER);
    tamper("ROSTER-3","GET /account/characters without the protocol header","426",r->r.is(426),
        ()->send("GET","/account/characters",noProto,null));
    tamper("ROSTER-4","GET /account/characters?after=<not a UUID>","400",r->r.is(400),
        ()->send("GET","/account/characters?after=1%27%20OR%201=1",account(A.secret()),null));
  }

  private static void joinBoth() throws Exception {
    // B native first (the adapter owns B's session); A raw. Sequential: the public admission is two wide.
    bConn=new OnlineConnection(endpoint,new OnlineConnection.Credentials(B.secret(),B.id()));
    waitFor(()->bConn.view()!=null || bConn.status().equals("disconnected"),15,"B adapter did not join");
    var view=bConn.view();
    require(view!=null && view.character()!=null && view.character().id().equals(B.id()),"B adapter failed its own join");
    bConn.confirmRegionReady(view.map().locationEpoch());
    waitFor(()->bConn.view().snapshot()!=null && bConn.view().snapshot().players().stream().anyMatch(p->p.id()==bConn.view().ownId()),8,"B own actor");
    Reply joined=null;
    for(int i=0;i<6;i++) {
      joined=post("/join",std(null),A.secret()+" "+A.id());
      if(joined.is(200)) break;
      Thread.sleep(400);
    }
    require(joined!=null && joined.is(200),"A raw join failed: "+(joined==null?"":joined.text()));
    var json=JsonParser.parseString(joined.body).getAsJsonObject();
    aToken=json.get("token").getAsString();aActor=json.get("id").getAsLong();
    aEpoch=json.getAsJsonObject("region").get("locationEpoch").getAsLong();
    var ready=aPost("/region-ready",Long.toString(aEpoch));
    require(ready.is(200),"A region-ready: "+ready.text());
    Thread.sleep(800);
    var st=state();
    boolean seesB=st!=null && st.getAsJsonArray("players").asList().stream().anyMatch(p->p.getAsJsonObject().get("id").getAsLong()==bConn.view().ownId());
    row("SETUP","A (raw HTTP) and B (native adapter) both joined and see each other","both connected, mutual visibility",
        "A session ok; B status "+bConn.status()+"; A sees B: "+seesB,seesB && bConn.status().equals("connected"));
  }

  private static void groupDisclosure(UUID corpseB,UUID lootB) {
    var secrets=new LinkedHashMap<String,String>();
    secrets.put("A account key",A.secret());secrets.put("B account key",B.secret());secrets.put("C account key",C.secret());
    secrets.put("A session token",aToken);secrets.put("B character UUID",B.id().toString());
    secrets.put("C character UUID",C.id().toString());secrets.put("B corpse UUID",corpseB.toString());
    for(String route:List.of("/state","/character","/corpses","/map")) {
      try {
        var r=aGet(route);
        String leaked=null;
        for(var e:secrets.entrySet()) if(r.body.contains(e.getValue())) { leaked=e.getKey();break; }
        row("LEAK"+route,"GET "+route+" with A's session carries no B UUID, account key, session token or B corpse id","200 without secrets",
            "HTTP "+r.status+" ("+r.body.length()+" bytes scanned)"+(leaked==null ? "; nothing leaked" : "; LEAKED "+leaked),r.is(200) && leaked==null);
      } catch(Exception e) { row("LEAK"+route,"disclosure scan","200",String.valueOf(e),false); }
    }
    var st=state();
    boolean seen=false;String owner="n/a";
    if(st!=null && st.has("loot")) for(var l:st.getAsJsonArray("loot")) {
      var o=l.getAsJsonObject();
      if(o.get("id").getAsString().equals(lootB.toString())) {
        seen=true;
        owner=o.has("ownerPlayerId") && !o.get("ownerPlayerId").isJsonNull() ? o.get("ownerPlayerId").getAsString() : "null";
      }
    }
    row("LEAK-LOOT","B's owner-window ground loot is visible to A without naming its owner","drop visible, ownerPlayerId null",
        "visible="+seen+", ownerPlayerId="+owner,seen && owner.equals("null"));
  }

  private static void groupAuth(Path control) throws Exception {
    List<String> routes=List.of("/input","/attack","/pickup","/cast","/use","/equip","/unequip","/recover","/interact","/region-ready","/leave");
    var noAuth=std(null);
    for(String p:routes) tamper("AUTH-NONE"+p,"POST "+p+" without Authorization","401 session",r->r.is(401),()->post(p,noAuth,sampleBody(p)));
    for(String[] g:new String[][]{{"bearer-lower","bearer "+aToken},{"basic","Basic "+aToken},{"short","Bearer "+aToken.substring(1)},
        {"upper-hex","Bearer "+aToken.toUpperCase()},{"padded","Bearer  "+aToken},
        {"empty","Bearer "},{"raw-token","Bearer "+"g".repeat(64)},{"token-only",aToken},{"prefix-junk","Bearer "+aToken+"zz"}}) {
      var h=std(null);h.put("Authorization",g[1]);
      // Sequence 0 keeps a (wrongly) accepted request from advancing A's input sequence.
      tamper("AUTH-GARBLED-"+g[0],"garbled Authorization ("+g[0]+") on /input","401",
          r->r.is(401),()->post("/input",h,aEpoch+" 0 0 0"));
    }
    // The JDK client strips trailing whitespace from header values, so this one is written by hand.
    tamper("AUTH-GARBLED-trailing-space","raw Authorization with a trailing space after the token","401, or 200 only if the HTTP layer strips trailing whitespace (RFC 9110); never another principal",
        r->r.is(401,200),()->raw("POST /input HTTP/1.1\r\nHost: 127.0.0.1:"+port+"\r\nOrigin: http://127.0.0.1:"+port+"\r\nContent-Type: text/plain\r\n"
            +"Authorization: Bearer "+aToken+" \r\nContent-Length: "+(aEpoch+" 0 0 0").length()+"\r\nConnection: close\r\n\r\n"+aEpoch+" 0 0 0"));
    for(String[] g:new String[][]{{"B-account-key",B.secret()},{"A-account-key",A.secret()},{"random",randomHex(64)}}) {
      var h=std(g[1]);
      tamper("AUTH-FOREIGN-"+g[0],"POST /attack with "+g[0]+" as a session token","401",r->r.is(401),()->post("/attack",h,"0 "+aEpoch+" 1"));
      tamper("AUTH-FOREIGN-GET-"+g[0],"GET /character with "+g[0]+" as a session token","401",r->r.is(401),
          ()->send("GET","/character",h,null));
    }
    for(String route:List.of("/state","/character","/corpses"))
      tamper("AUTH-GET-NONE"+route,"GET "+route+" without Authorization","401",r->r.is(401),()->send("GET",route,std(null),null));
    var h=std(aToken);
    tamper("ADMIN-1","POST /admin/shutdown with A's session token","403 admin, server keeps running",r->r.is(403),
        ()->post("/admin/shutdown",h,""));
    tamper("ADMIN-2","POST /admin/shutdown with A's account key","403 admin",r->r.is(403),()->post("/admin/shutdown",std(A.secret()),""));
    tamper("ADMIN-3","GET /admin/metrics with A's session token","403 admin",r->r.is(403),()->send("GET","/admin/metrics",h,null));
    tamper("ADMIN-4","GET /admin/metrics without Authorization","401 or 403",r->r.is(401,403),()->send("GET","/admin/metrics",std(null),null));
    row("ADMIN-5","server and both sessions still alive after the admin attempts","/health 200, B connected, A /state 200",
        send("GET","/health",std(null),null).text()+"; B "+bConn.status()+"; A /state "+aGet("/state").status,
        send("GET","/health",std(null),null).is(200) && bConn.status().equals("connected") && aGet("/state").is(200));
    tamper("FOREIGN-ACTIVE","A's key + B's UUID on /join while B is connected","409; B stays connected",
        r->r.is(409) && bConn.status().equals("connected"),()->post("/join",std(null),A.secret()+" "+B.id()));
    tamper("FOREIGN-ACTIVE-2","B's key + A's UUID on /join while A is connected","409",r->r.is(409),()->post("/join",std(null),B.secret()+" "+A.id()));
    tamper("FOREIGN-ACTIVE-3","A's own key + A's UUID second /join while A already connected","409 (one lease per character)",r->r.is(409),
        ()->post("/join",std(null),A.secret()+" "+A.id()));
    bAlive("after authorization attempts");
  }

  private static String sampleBody(String path) {
    return switch(path) {
      case "/input" -> aEpoch+" 99999 1 0";
      case "/attack" -> "0 "+aEpoch+" 1";
      case "/pickup","/use" -> "0 "+aEpoch+" "+UUID.randomUUID();
      case "/cast" -> "0 "+aEpoch+" spell.heal_light";
      case "/equip" -> "0 "+aEpoch+" "+UUID.randomUUID()+" WEAPON";
      case "/unequip" -> "0 "+aEpoch+" WEAPON";
      case "/recover" -> "0 "+aEpoch+" "+UUID.randomUUID();
      case "/interact" -> "0 "+aEpoch+" WardenAelric";
      case "/region-ready" -> Long.toString(aEpoch);
      default -> "";
    };
  }

  private static void groupMalformed(UUID bItem) {
    String u=UUID.randomUUID().toString();
    record M(String route,String why,String body) {}
    var list=new ArrayList<M>();
    for(String b:new String[]{"abc","","1 2 3","1  2 1 0","-1 0 1 0","0 -1 1 0","0 0 2 0","0 0 1 1 1","99999999999999999999 1 1 0",
        "9223372036854775808 1 0 0","\u0660 \u0660 1 0","+1 1 1 0","0 0 1.0 0","0x1 2 1 0","0 1 1 0\u0000","0 1 100 100","0 1 1 \u00e9"})
      list.add(new M("/input","/input body '"+vis(b)+"'",b));
    for(String b:new String[]{"abc","1 2","1 2 3 4","-1 0 1","0 0 -1","0 0 1.5","0 0 0x10","0 0 99999999999999999999","0 0 9223372036854775808",
        "9223372036854775808 0 1","0 9223372036854775808 1","0 0 1\u0000"})
      list.add(new M("/attack","/attack body '"+vis(b)+"'",b));
    for(String route:List.of("/pickup","/use"))
      for(String b:new String[]{"0 0 "+u.toUpperCase(),"0 0 "+u.substring(1),"0 0 {"+u+"}","0 0 ' OR 1=1 --","0 0 "+u+"x","0 0 ../../"+u,"-1 0 "+u,"0 -1 "+u,
          "0 0","abc","9223372036854775808 0 "+u})
        list.add(new M(route,route+" body '"+vis(b)+"'",b));
    for(String b:new String[]{"0 0 "+u.toUpperCase(),"0 0","0 0 "+u+" "+u+" "+u,"0 0 "+u+" nope","0 0 "+u.substring(2)})
      list.add(new M("/recover","/recover body '"+vis(b)+"'",b));
    for(String b:new String[]{"0 0 spell.Heal_Light","0 0 spell.","0 0 item.light_healing_potion","0 0 spell."+"a".repeat(65),"0 0 spell.heal light","0 0 spell.a;b","0 0 ../spell.x"})
      list.add(new M("/cast","/cast body '"+vis(b)+"'",b));
    for(String b:new String[]{"0 0 "+u+" NOSUCHSLOT","0 0 "+u+" WEAPONX9","0 0 "+u+" weapon","0 0 "+u+" WEAPON;","0 0 "+u+" "+"A".repeat(17),"0 0 "+u,"0 0 WEAPON "+u})
      list.add(new M("/equip","/equip body '"+vis(b)+"'",b));
    for(String b:new String[]{"0 0 weapon","0 0 WEAPONX9","0 0 "+"A".repeat(17),"0 0","0 0 NOSUCHSLOT"})
      list.add(new M("/unequip","/unequip body '"+vis(b)+"'",b));
    for(String b:new String[]{"0 0 ../../etc/passwd","0 0 "+"A".repeat(65),"0 0 WardenAelric 100","0 0 WardenAelric -1","0 0 WardenAelric 1 2",
        "0 0 1Aelric","0 0 Warden Aelric","0 0 WardenAelric gold=999999"})
      list.add(new M("/interact","/interact body '"+vis(b)+"'",b));
    for(String b:new String[]{"abc","-1","","1 2","99999999999999999999","9223372036854775808"}) list.add(new M("/region-ready","/region-ready body '"+vis(b)+"'",b));
    for(var m:list) {
      String body=m.body.replace("0 0 ","0 "+aEpoch+" ");
      tamper("MAL-"+m.route.substring(1)+"-"+Integer.toHexString(Objects.hash(m.route,m.body)&0xfffff),m.why,"400 (definite input refusal, never 2xx/5xx)",
          r->r.is(400),()->aPost(m.route,body));
    }
    // Syntactically valid numbers that name nothing: definite 409 refusals, never 5xx.
    tamper("MAL-nil-uuid-pickup","/pickup with the all-zero UUID","409 loot_unavailable (or stale revision)",
        r->r.is(409),()->aPost("/pickup",revBody("00000000-0000-0000-0000-000000000000")));
    tamper("MAL-unknown-npc","/interact with an unknown NPC id","409",r->r.is(409),()->aPost("/interact",revBody("Nobody")));
    tamper("MAL-unknown-spell","/cast spell.nonexistent","409 (not learned or unsupported)",r->r.is(409),()->aPost("/cast",revBody("spell.nonexistent")));
    tamper("MAL-method-put","PUT /input","405",r->r.is(405),()->send("PUT","/input",std(aToken),"0 1 1 0"));
    tamper("MAL-method-delete","DELETE /character","405",r->r.is(405),()->send("DELETE","/character",std(aToken),null));
    tamper("MAL-post-character","POST /character (GET-only route)","404",r->r.is(404),()->aPost("/character",""));
    tamper("MAL-unknown-route","POST /grant-gold","404",r->r.is(404),()->aPost("/grant-gold","gold 99999"));
    tamper("MAL-query-post","POST /input?x=1","400",r->r.is(400),()->send("POST","/input?x=1",std(aToken),aEpoch+" 5000 0 0"));
    tamper("MAL-query-get","GET /character?gold=99999","400",r->r.is(400),()->send("GET","/character?gold=99999",std(aToken),null));
    var json=std(aToken);json.put("Content-Type","application/json");
    for(String p:List.of("/input","/equip","/interact","/attack"))
      tamper("MAL-ctype"+p,"POST "+p+" with application/json","415",r->r.is(415),()->post(p,json,"{\"gold\":99999}"));
    var noCt=std(aToken);noCt.remove("Content-Type");
    tamper("MAL-no-ctype","POST /input without Content-Type","415",r->r.is(415),()->post("/input",noCt,aEpoch+" 5000 0 0"));
    var evil=std(aToken);evil.put("Origin","http://evil.example");
    tamper("MAL-origin-evil","POST /attack with foreign Origin","403",r->r.is(403),()->post("/attack",evil,revBody("1")));
    var noOrigin=std(aToken);noOrigin.remove("Origin");
    tamper("MAL-origin-none","POST /use without Origin","403",r->r.is(403),()->post("/use",noOrigin,revBody(UUID.randomUUID().toString())));
    for(String p:List.of("/pickup","/cast","/use")) {
      var noVer=std(aToken);noVer.remove(OnlineProtocol.VERSION_HEADER);
      tamper("MAL-proto"+p,"POST "+p+" without the protocol header","426",r->r.is(426),()->post(p,noVer,sampleBody(p)));
    }
  }

  private static void groupStale(String potion) throws Exception {
    // Current revision moves every ~5 s (periodic checkpoint); a wrong revision is wrong whenever it lands.
    String dummy=UUID.randomUUID().toString();
    Supplier<Long> wrong=()->{ long r=revA();return r+1_000_000L; };
    var weapon=A.created.progress().equipment().get("WEAPON");
    var potionItem=A.created.progress().inventory().stream().filter(i->i.key().equals(potion)).findFirst().orElseThrow();
    tamper("STALE-attack","/attack with a future revision against creature 1","409 revision (revision is checked before the target)",
        r->r.isRefusal("revision"),()->aPost("/attack",wrong.get()+" "+aEpoch+" 1"));
    tamper("STALE-attack-low","/attack with a past revision (0, or +1 when the revision is still 0)","409 revision",r->r.isRefusal("revision"),
        ()->aPost("/attack",(revA()>0 ? 0 : 1)+" "+aEpoch+" 1"));
    tamper("STALE-pickup","/pickup B's owner-window loot with a wrong revision","409 revision",r->r.isRefusal("revision"),
        ()->aPost("/pickup",wrong.get()+" "+aEpoch+" "+dummy));
    tamper("STALE-cast","/cast spell.heal_light with a wrong revision","409 revision",r->r.isRefusal("revision"),
        ()->aPost("/cast",wrong.get()+" "+aEpoch+" spell.heal_light"));
    tamper("STALE-use","/use A's own potion with a wrong revision","409 revision",r->r.isRefusal("revision"),
        ()->aPost("/use",wrong.get()+" "+aEpoch+" "+potionItem.id()));
    tamper("STALE-unequip","/unequip WEAPON (equipped) with a wrong revision","409 revision, weapon stays equipped",
        r->r.isRefusal("revision"),()->aPost("/unequip",wrong.get()+" "+aEpoch+" WEAPON"));
    tamper("STALE-interact","/interact WardenAelric with a wrong revision","409 revision",r->r.isRefusal("revision"),
        ()->aPost("/interact",wrong.get()+" "+aEpoch+" WardenAelric"));
    // Wrong location epoch on every route that carries one.
    long bad=aEpoch+1;
    for(String route:List.of("/attack","/pickup","/cast","/use","/equip","/unequip","/recover","/interact","/input","/region-ready")) {
      String body=switch(route) {
        case "/attack" -> revA()+" "+bad+" 1";
        case "/pickup","/use","/recover" -> revA()+" "+bad+" "+dummy;
        case "/cast" -> revA()+" "+bad+" spell.heal_light";
        case "/equip" -> revA()+" "+bad+" "+(weapon==null?dummy:weapon)+" WEAPON";
        case "/unequip" -> revA()+" "+bad+" WEAPON";
        case "/interact" -> revA()+" "+bad+" WardenAelric";
        case "/input" -> bad+" "+(100+(seq++))+" 0 0";
        default -> Long.toString(bad);
      };
      final String fixed=body;
      tamper("EPOCH"+route,"POST "+route+" with a stale/future location epoch","409 region_epoch",r->r.isRefusal("region_epoch"),()->aPost(route,fixed));
    }
    bAlive("after stale epoch");
  }

  private static void groupOversize() {
    record L(String route,int limit) {}
    var routes=List.of(new L("/input",64),new L("/attack",64),new L("/region-ready",64),new L("/leave",64),new L("/recover",128),
        new L("/cast",128),new L("/interact",128),new L("/use",96),new L("/equip",96),new L("/unequip",96),new L("/pickup",96));
    for(var l:routes) {
      String over="1".repeat(l.limit+1);
      tamper("BIG"+l.route,"POST "+l.route+" with "+(l.limit+1)+" bytes (cap "+l.limit+")","413",r->r.is(413),()->aPost(l.route,over));
    }
    tamper("BIG/join","/join with 102 bytes (cap 101)","413",r->r.is(413),
        ()->post("/join",std(null),"a".repeat(102)));
    var acct=account(A.secret());acct.put("Content-Type","application/json");acct.put("Origin",endpoint.toString());
    tamper("BIG/account","POST /account/characters with 513 bytes (cap 512)","413",r->r.is(413),
        ()->post("/account/characters",acct,"{\"name\":\""+"a".repeat(520)+"\"}"));
    tamper("BIG-huge-input","POST /input with 128 KiB","413 (or the connection closed without applying anything)",
        r->r.is(413) || r.io!=null,()->aPost("/input","9".repeat(128*1024)));
    tamper("BIG-chunked-input","POST /input as a chunked stream of 4096 bytes (no Content-Length)","413",r->r.is(413) || r.io!=null,
        ()->sendStream("/input",aToken,4096));
    tamper("BIG-lie-length","raw POST /input declaring Content-Length 1000000 with a 5-byte body","413",r->r.is(413),
        ()->raw("POST /input HTTP/1.1\r\nHost: 127.0.0.1:"+port+"\r\nOrigin: http://127.0.0.1:"+port+"\r\nContent-Type: text/plain\r\n"
            +"Authorization: Bearer "+aToken+"\r\nContent-Length: 1000000\r\nConnection: close\r\n\r\n12345"));
    tamper("BIG-dup-length","raw POST /input with two Content-Length headers","400",r->r.is(400),
        ()->raw("POST /input HTTP/1.1\r\nHost: 127.0.0.1:"+port+"\r\nOrigin: http://127.0.0.1:"+port+"\r\nContent-Type: text/plain\r\n"
            +"Authorization: Bearer "+aToken+"\r\nContent-Length: 5\r\nContent-Length: 6\r\nConnection: close\r\n\r\n12345"));
    tamper("BIG-neg-length","raw POST /input with Content-Length -1","400 (or the connection refused)",r->r.is(400) || r.io!=null || r.status>=400,
        ()->raw("POST /input HTTP/1.1\r\nHost: 127.0.0.1:"+port+"\r\nOrigin: http://127.0.0.1:"+port+"\r\nContent-Type: text/plain\r\n"
            +"Authorization: Bearer "+aToken+"\r\nContent-Length: -1\r\nConnection: close\r\n\r\n"));
    tamper("BIG-nan-length","raw POST /input with a non-numeric Content-Length","400 (or the connection refused)",r->r.is(400) || r.io!=null || r.status>=400,
        ()->raw("POST /input HTTP/1.1\r\nHost: 127.0.0.1:"+port+"\r\nOrigin: http://127.0.0.1:"+port+"\r\nContent-Type: text/plain\r\n"
            +"Authorization: Bearer "+aToken+"\r\nContent-Length: abc\r\nConnection: close\r\n\r\n"));
    tamper("BIG-bad-host","raw POST /input with a foreign Host header","403",r->r.is(403),
        ()->raw("POST /input HTTP/1.1\r\nHost: evil.example\r\nOrigin: http://127.0.0.1:"+port+"\r\nContent-Type: text/plain\r\n"
            +"Authorization: Bearer "+aToken+"\r\nContent-Length: 5\r\nConnection: close\r\n\r\n12345"));
    row("BIG-alive","A's session and the server still respond after the oversize volley","A /state 200, /health 200",
        aGet("/state").text()+"; "+send("GET","/health",std(null),null).text(),aGet("/state").is(200) && send("GET","/health",std(null),null).is(200));
  }

  private static void groupForged(UUID corpseA,UUID corpseB,UUID lootB,UUID bItem,UUID bWorn,UUID corpseBItem) throws Exception {
    var st=state();
    long bActor=bConn.view().ownId();
    long farCreature=-1;int farHealth=-1;
    if(st!=null) for(var c:st.getAsJsonArray("creatures")) { farCreature=c.getAsJsonObject().get("id").getAsLong();farHealth=c.getAsJsonObject().get("health").getAsInt();break; }
    long[] targets={0,1,aActor,bActor,4_000_000_000L,Long.MAX_VALUE};
    String[] names={"id 0","id 1","A's own actor id","B's player actor id","nonexistent id 4e9","Long.MAX_VALUE"};
    for(int i=0;i<targets.length;i++) {
      final long t=targets[i];
      tamper("FORGE-attack-"+i,"/attack "+names[i]+" with the current revision","409 (creature_unavailable/protected/out of range)",
          r->r.is(409),()->aPost("/attack",revA()+" "+aEpoch+" "+t));
    }
    final long fc=farCreature;
    if(fc>0) tamper("FORGE-attack-far","/attack a real creature (id "+fc+") that is outside melee range","409 out_of_range_or_blocked; creature health unchanged",
        r->r.is(409) && !r.isRefusal("revision"),()->aPost("/attack",revA()+" "+aEpoch+" "+fc));
    else NOT_COVERED.add("FORGE-attack-far: no creature in A's snapshot to target");
    if(fc>0) {
      var after=state();int health=-1;
      if(after!=null) for(var c:after.getAsJsonArray("creatures")) if(c.getAsJsonObject().get("id").getAsLong()==fc) health=c.getAsJsonObject().get("health").getAsInt();
      row("FORGE-attack-far-health","the out-of-range creature's health is unchanged","same health",farHealth+" -> "+health,farHealth==health);
    }
    tamper("FORGE-pickup-random","/pickup a random UUID","409 loot_unavailable",r->r.isRefusal("loot_unavailable"),
        ()->aPost("/pickup",revA()+" "+aEpoch+" "+UUID.randomUUID()));
    tamper("FORGE-pickup-B","/pickup B's ground loot inside B's owner window, standing on it","409 loot_reserved, drop stays on the ground",
        r->r.isRefusal("loot_reserved"),()->aPost("/pickup",revA()+" "+aEpoch+" "+lootB));
    String lootRow=sqlSafe("SELECT state||'|'||(owner_character=?)||'|'||(claimed_by IS NULL)||'|'||(SELECT count(*) FROM online_loot_claims WHERE loot_id=?) FROM online_ground_loot WHERE id=?",B.id(),lootB,lootB);
    row("FORGE-pickup-B-db","B's loot row after A's attempt (state|owner is B|unclaimed|receipts)","ON_GROUND|true|true|0",lootRow,lootRow.equals("ON_GROUND|true|true|0"));
    tamper("FORGE-recover-B-corpse","/recover B's corpse (it lies on A's tile)","409 corpse_unavailable",r->r.isRefusal("corpse_unavailable"),
        ()->aPost("/recover",revA()+" "+aEpoch+" "+corpseB));
    tamper("FORGE-recover-random","/recover a random corpse UUID","409 corpse_unavailable",r->r.isRefusal("corpse_unavailable"),
        ()->aPost("/recover",revA()+" "+aEpoch+" "+UUID.randomUUID()));
    tamper("FORGE-recover-B-item-on-A-corpse","/recover A's own corpse naming an item of B's corpse","409 refusal, A's corpse intact",
        r->r.is(409) && !r.isRefusal("revision"),()->aPost("/recover",revA()+" "+aEpoch+" "+corpseA+" "+corpseBItem));
    row("FORGE-recover-db","both corpses after the attempts","B corpse unrecovered with gold 777 and 1 item; A corpse unrecovered with gold 111 and 1 item",
        sqlSafe("SELECT recovered||'|'||gold||'|'||jsonb_array_length(items) FROM online_corpses WHERE id=?",corpseB)+" ; "
        +sqlSafe("SELECT recovered||'|'||gold||'|'||jsonb_array_length(items) FROM online_corpses WHERE id=?",corpseA),
        sqlSafe("SELECT recovered||'|'||gold||'|'||jsonb_array_length(items) FROM online_corpses WHERE id=?",corpseB).equals("false|777|1")
        && sqlSafe("SELECT recovered||'|'||gold||'|'||jsonb_array_length(items) FROM online_corpses WHERE id=?",corpseA).equals("false|111|1"));
    tamper("FORGE-use-B-item","/use B's inventory item instance","409 item_not_owned",r->r.isRefusal("item_not_owned"),
        ()->aPost("/use",revA()+" "+aEpoch+" "+bItem));
    tamper("FORGE-use-B-worn","/use B's worn item instance","409 item_not_owned",r->r.isRefusal("item_not_owned"),
        ()->aPost("/use",revA()+" "+aEpoch+" "+bWorn));
    tamper("FORGE-equip-B-item","/equip B's item instance into A's WEAPON slot","409 item_not_owned",r->r.isRefusal("item_not_owned"),
        ()->aPost("/equip",revA()+" "+aEpoch+" "+bWorn+" WEAPON"));
    tamper("FORGE-equip-random","/equip a random item UUID","409 item_not_owned",r->r.isRefusal("item_not_owned"),
        ()->aPost("/equip",revA()+" "+aEpoch+" "+UUID.randomUUID()+" HEAD"));
    tamper("FORGE-unequip-empty-slot","/unequip a slot A does not occupy (BELT)","409 item_not_equipped",
        r->r.is(409),()->aPost("/unequip",revA()+" "+aEpoch+" BELT"));
    tamper("FORGE-cast-unlearned","/cast spell.stone_shard (A is a Paladin, not learned)","409 not_learned",r->r.isRefusal("not_learned"),
        ()->aPost("/cast",revA()+" "+aEpoch+" spell.stone_shard"));
    tamper("FORGE-cast-item-spell","/cast spell.heal_critical (an item-only spell, not learned)","409 not_learned",r->r.isRefusal("not_learned"),
        ()->aPost("/cast",revA()+" "+aEpoch+" spell.heal_critical"));
    tamper("FORGE-use-heal-full","/use B-independent: unknown item UUID","409 item_not_owned",r->r.isRefusal("item_not_owned"),
        ()->aPost("/use",revA()+" "+aEpoch+" "+UUID.randomUUID()));
    // B's data may not be reachable by NPC range either: A stands at Aelric, B's NPC is Kilhiam.
    tamper("FORGE-interact-far-npc","/interact Kilhiam from A's tile (out of NPC range)","409 npc_unavailable, no letter granted",
        r->r.isRefusal("npc_unavailable"),()->aPost("/interact",revA()+" "+aEpoch+" Kilhiam"));
    tamper("FORGE-interact-topic-far-npc","/interact Kilhiam topic 0 from A's tile","409 npc_unavailable",
        r->r.isRefusal("npc_unavailable"),()->aPost("/interact",revA()+" "+aEpoch+" Kilhiam 0"));
  }

  private static void groupIdempotency(UUID corpseA,UUID corpseB,UUID lootB,UUID corpseBItem,String potion) throws Exception {
    // --- unequip / equip replay
    var weapon=A.created.progress().equipment().get("WEAPON");
    if(weapon==null) { NOT_COVERED.add("IDEM-equip: the paladin kit has no WEAPON slot item"); }
    else {
      var sent=validCommand("/unequip",r->r+" "+aEpoch+" WEAPON");
      var afterFirst=snapState();
      row("IDEM-unequip-1","/unequip WEAPON with the current revision (valid)","200",sent.reply.text(),sent.reply.is(200)
          && !afterFirst.a.eq.containsKey("WEAPON"));
      tamper("IDEM-unequip-replay","replay of the exact /unequip request","409 (applied at most once), equipment stays as after the first",
          r->r.is(409),()->aPost("/unequip",sent.body));
      tamper("STALE-equip","/equip the unequipped weapon with a wrong revision","409 revision, weapon stays unequipped",
          r->r.isRefusal("revision"),()->aPost("/equip",(revA()+1_000_000L)+" "+aEpoch+" "+weapon+" WEAPON"));
      var re=validCommand("/equip",r->r+" "+aEpoch+" "+weapon+" WEAPON");
      row("IDEM-equip-1","/equip the weapon back (valid)","200",re.reply.text(),re.reply.is(200) && snapState().a.eq.get("WEAPON")!=null);
      tamper("IDEM-equip-replay","replay of the exact /equip request","409 (applied at most once)",r->r.is(409),()->aPost("/equip",re.body));
    }
    Thread.sleep(300);
    // --- potion: parallel duplicate
    var potionItem=A.created.progress().inventory().stream().filter(i->i.key().equals(potion)).findFirst().orElseThrow();
    int potionsBefore=countKey(dbA().progress(),potion);
    long revNow=revA();
    String body=revNow+" "+aEpoch+" "+potionItem.id();
    var latch=new CountDownLatch(1);
    var f1=CompletableFuture.supplyAsync(()->{ await(latch);return aPost("/use",body); });
    var f2=CompletableFuture.supplyAsync(()->{ await(latch);return aPost("/use",body); });
    latch.countDown();
    Reply r1=f1.get(10,TimeUnit.SECONDS),r2=f2.get(10,TimeUnit.SECONDS);
    int oks=(r1.is(200)?1:0)+(r2.is(200)?1:0);
    Thread.sleep(300);
    int potionsAfter=countKey(dbA().progress(),potion);
    row("IDEM-use-parallel","two identical /use requests released together on one potion","exactly one 200, potion count -1",
        r1.text()+" / "+r2.text()+"; potions "+potionsBefore+"->"+potionsAfter,oks==1 && potionsAfter==potionsBefore-1);
    tamper("IDEM-use-replay","later replay of the same /use request","409 revision or item_not_owned, potion count unchanged",
        r->r.is(409),()->aPost("/use",body));
    Thread.sleep(2500);
    // --- cast replay
    var cast=validCommand("/cast",r->r+" "+aEpoch+" spell.heal_light");
    row("IDEM-cast-1","/cast spell.heal_light (valid, learned)","200",cast.reply.text(),cast.reply.is(200));
    tamper("IDEM-cast-replay","replay of the exact /cast request","409 (mana is not spent twice)",r->r.is(409),()->aPost("/cast",cast.body));
    Thread.sleep(2500);
    // --- corpse recovery replay
    long goldBefore=dbA().gold();int invBefore=dbA().progress().inventory().size();
    tamper("STALE-recover","/recover A's own corpse with a wrong revision","409 revision, corpse untouched",
        r->r.isRefusal("revision"),()->aPost("/recover",(revA()+1_000_000L)+" "+aEpoch+" "+corpseA));
    var rec=validCommand("/recover",r->r+" "+aEpoch+" "+corpseA);
    var after=dbA();
    row("IDEM-recover-1","/recover A's own corpse (valid)","200, gold +111, +1 item",
        rec.reply.text()+"; gold "+goldBefore+"->"+after.gold()+", stacks "+invBefore+"->"+after.progress().inventory().size(),
        rec.reply.is(200) && after.gold()==goldBefore+111 && after.progress().inventory().size()==invBefore+1);
    tamper("IDEM-recover-replay","replay of the exact /recover request","409 (nothing taken twice)",r->r.is(409),()->aPost("/recover",rec.body));
    tamper("IDEM-recover-again","/recover the same corpse again with a fresh revision","no further gold/items (409, or a 200 no-op)",
        r->r.is(409,200),()->aPost("/recover",revA()+" "+aEpoch+" "+corpseA));
    row("IDEM-recover-total","gold and stacks after the replays","exactly +111 gold and +1 item in total",
        "gold "+goldBefore+"->"+dbA().gold()+", stacks "+invBefore+"->"+dbA().progress().inventory().size(),
        dbA().gold()==goldBefore+111 && dbA().progress().inventory().size()==invBefore+1);
    // --- quest accept replay (WardenAelric)
    var giver=QuestCatalog.bundled().definitions().stream().filter(d->d.getGiverNpc().equals("WardenAelric")).findFirst().orElseThrow();
    var flagsBefore=dbA().progress().questFlags();
    var accepted=validCommand("/interact",r->r+" "+aEpoch+" WardenAelric 0");
    var flagsAfter=dbA().progress().questFlags();
    var expectedWrites=QuestRules.accept(k->flagsBefore.getOrDefault(k,0),giver);
    var expectedFlags=new HashMap<>(flagsBefore);expectedFlags.putAll(expectedWrites);
    row("IDEM-quest-accept","/interact WardenAelric topic 0 (accept the quest; valid)","200, flags = exactly the authored accept flags",
        accepted.reply.text()+"; flags "+new TreeMap<>(flagsAfter),accepted.reply.is(200) && flagsAfter.equals(expectedFlags));
    tamper("IDEM-quest-replay","replay of the exact accept request","409 revision, flags unchanged",r->r.is(409),()->aPost("/interact",accepted.body));
    tamper("IDEM-quest-again","accept again with a fresh revision","200 progress line, no flag/gold/xp change",
        r->r.is(200),()->aPost("/interact",revA()+" "+aEpoch+" WardenAelric 0"));
    tamper("IDEM-quest-greet","greeting (turn-in attempt) with the kill goal unmet","200 welcome/progress line, no reward",
        r->r.is(200),()->aPost("/interact",revA()+" "+aEpoch+" WardenAelric"));
    tamper("IDEM-quest-lore","topic 1 (lore)","200, no change",r->r.is(200),()->aPost("/interact",revA()+" "+aEpoch+" WardenAelric 1"));
    for(int topic:new int[]{2,3,9,10,50,99})
      tamper("IDEM-quest-topic"+topic,"topic "+topic+" (does not exist)","409 npc_unavailable, no change",
          r->r.isRefusal("npc_unavailable"),()->aPost("/interact",revA()+" "+aEpoch+" WardenAelric "+topic));
    var flags=dbA().progress().questFlags();
    row("IDEM-quest-allowlist","A's quest flags after every dialogue attempt are inside the online allowlist and equal the accept flags",
        "OnlineQuestFlags.supported == true; flags == pre-accept flags + authored accept writes",
        "flags "+new TreeMap<>(flags)+"; supported="+OnlineQuestFlags.supported(flags,QuestCatalog.bundled()),
        OnlineQuestFlags.supported(flags,QuestCatalog.bundled()) && flags.equals(expectedFlags));
    // --- input sequence replay / reorder
    long base=Math.max(seq,1000)+10;seq=base+100;
    var ok1=aPost("/input",aEpoch+" "+(base+2)+" 0 0");
    var reorder=aPost("/input",aEpoch+" "+(base+1)+" 0 0");
    var replay=aPost("/input",aEpoch+" "+(base+2)+" 0 0");
    var equal=aPost("/input",aEpoch+" "+(base+2)+" 1 0");
    var zero=aPost("/input",aEpoch+" 0 0 0");
    aPost("/input",aEpoch+" "+(seq++)+" 0 0");
    row("IDEM-input-seq","input sequence: send N+2, then N+1 (reordered), N+2 again (replay), N+2 with a different direction, and 0","200, then 409 sequence for each",
        ok1.text()+" / "+reorder.text()+" / "+replay.text()+" / "+equal.text()+" / "+zero.text(),
        ok1.is(200) && reorder.isRefusal("sequence") && replay.isRefusal("sequence") && equal.isRefusal("sequence") && zero.isRefusal("sequence"));
    // --- account creation replay
    String requestId=UUID.randomUUID().toString();
    String name="Tm"+randomLetters(6);
    int rosterBefore=rosterCount();
    var creation=creationHeaders();
    String create="{\"requestId\":\""+requestId+"\",\"name\":\""+name+"\",\"characterClass\":\"warrior\",\"gender\":\"male\",\"evil\":false}";
    var c1=post("/account/characters",creation,create);
    var c2=post("/account/characters",creation,create);
    var c3=post("/account/characters",creation,create.replace(name,"Tn"+randomLetters(6)));
    int rosterAfter=rosterCount();
    row("IDEM-create-replay","POST /account/characters twice with the same requestId, then again with a changed name","at most one character created",
        c1.text().replaceAll("\"id\":\"[0-9a-f-]+\"","\"id\":\"..\"")+" / "+(c2.is(200)?"200 replay":c2.text())+" / "+c3.text()+"; roster "+rosterBefore+"->"+rosterAfter,
        c1.is(200) && rosterAfter==rosterBefore+1);
  }

  private static void groupEdits(UUID bItem) {
    String w=A.created.progress().equipment().getOrDefault("WEAPON",UUID.randomUUID()).toString();
    record E(String id,String route,String what,String body) {}
    var list=List.of(
        new E("EDIT-equip-extra","/equip","extra trailing token on /equip","%REV% %EP% "+w+" WEAPON 9999"),
        new E("EDIT-use-qty","/use","a quantity token on /use","%REV% %EP% "+UUID.randomUUID()+" 100"),
        new E("EDIT-use-neg","/use","negative quantity","%REV% %EP% "+UUID.randomUUID()+" -1"),
        new E("EDIT-cast-power","/cast","a power/damage token on /cast","%REV% %EP% spell.heal_light 99999"),
        new E("EDIT-interact-gold","/interact","gold=999999 token on /interact","%REV% %EP% WardenAelric gold=999999"),
        new E("EDIT-interact-flag","/interact","a quest-flag assignment on /interact","%REV% %EP% WardenAelric ADDON_CRIMSONSCALE_LETTER=1"),
        new E("EDIT-attack-damage","/attack","a damage token on /attack","%REV% %EP% 1 99999"),
        new E("EDIT-recover-gold","/recover","a gold token on /recover","%REV% %EP% "+UUID.randomUUID()+" "+UUID.randomUUID()+" 999999"),
        new E("EDIT-input-speed","/input","a speed multiplier on /input","%EP% 7000001 1 0 5"),
        new E("EDIT-input-pos","/input","a position on /input","%EP% 7000002 1 0 2944.5 1059.5"),
        new E("EDIT-json-state","/equip","a JSON progress object in the body","{\"progress\":{\"gold\":999999},\"revision\":0}"),
        new E("EDIT-json-stats","/interact","a JSON stats object in the body","{\"attributes\":{\"strength\":99999}}"));
    for(var e:list) {
      long rev=revA();
      String body=e.body.replace("%REV%",Long.toString(rev)).replace("%EP%",Long.toString(aEpoch));
      tamper(e.id,e.what,"400 (the grammar carries no stat/gold/flag/item fields)",r->r.is(400),()->aPost(e.route,body));
    }
    var creation=creationHeaders();
    int rosterBefore=rosterCount();
    for(String[] extra:new String[][]{{"gold","99999"},{"level","50"},{"strength","999"},{"hp","99999"},{"x","1"},{"world","1"},{"progress","{}"},
        {"inventory","[]"},{"questFlags","{}"},{"experience","99999999"}}) {
      String json="{\"requestId\":\""+UUID.randomUUID()+"\",\"name\":\"Tm"+randomLetters(6)+"\",\"characterClass\":\"warrior\",\"gender\":\"male\",\"evil\":false,\""
          +extra[0]+"\":"+extra[1]+"}";
      tamper("EDIT-create-"+extra[0],"character creation with an extra '"+extra[0]+"' field","400 creation_input",
          r->r.is(400),()->post("/account/characters",creation,json));
    }
    String dupKey="{\"requestId\":\""+UUID.randomUUID()+"\",\"name\":\"Tm"+randomLetters(6)+"\",\"characterClass\":\"warrior\",\"gender\":\"male\",\"evil\":false,\"evil\":true}";
    tamper("EDIT-create-dup-field","character creation with a duplicated field","400",r->r.is(400),()->post("/account/characters",creation,dupKey));
    tamper("EDIT-create-class","character creation of an unsupported class (necromancer; mage became a supported class with caster creation)","400 unsupported_class",r->r.is(400),
        ()->post("/account/characters",creation,"{\"requestId\":\""+UUID.randomUUID()+"\",\"name\":\"Tm"+randomLetters(6)+"\",\"characterClass\":\"necromancer\",\"gender\":\"male\",\"evil\":false}"));
    row("EDIT-create-count","characters on A's account after the creation tampering","unchanged",
        "roster "+rosterBefore+"->"+rosterCount(),rosterBefore==rosterCount());
    var fin=dbA();
    var ref=A.created.progress();
    boolean statsOk=fin.progress().attributes().equals(ref.attributes()) && fin.progress().advancement().equals(ref.advancement())
        && fin.progress().skills().equals(ref.skills()) && fin.progress().spells().equals(ref.spells());
    row("EDIT-final-stats","A's attributes, level, skills and spells after the whole tamper battery","identical to provisioning",
        "attributes equal="+fin.progress().attributes().equals(ref.attributes())+", advancement equal="+fin.progress().advancement().equals(ref.advancement())
        +", skills equal="+fin.progress().skills().equals(ref.skills())+", spells equal="+fin.progress().spells().equals(ref.spells()),statsOk);
  }

  private static void groupMovement(HostedRegionPlan plan,PreviewMap map) throws Exception {
    var movementStart=snap();
    Thread.sleep(3000); // exhaustion from the potion and cast has expired
    // Absurd vectors never move the actor.
    for(String v:new String[]{"100 100","2 0","0 -2","-2 -2","1000000 0","1.5 0"}) {
      var r=aPost("/input",aEpoch+" "+(seq++)+" "+v);
      row("SPEED-vec-"+v.replace(' ','_'),"/input direction vector '"+v+"'","400",r.text(),r.is(400));
    }
    // Spam inputs faster than the tick and measure the displacement the server allows.
    var begin=pos();
    long stop=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(1500);
    int sent=0,accepted=0,limited=0;
    while(System.nanoTime()<stop) {
      var r=aPost("/input",aEpoch+" "+(seq++)+" 1 0");
      sent++;if(r.is(200)) accepted++;if(r.is(429)) limited++;
      Thread.sleep(15);
    }
    aPost("/input",aEpoch+" "+(seq++)+" 0 0");
    Thread.sleep(600);
    var end=pos();
    double dx=end[1]-begin[1],dy=end[2]-begin[2];
    double ticks=end[0]-begin[0];
    double allowed=SPEED_PER_TICK*(ticks+2);
    row("SPEED-spam","spam "+sent+" east inputs ("+accepted+" accepted, "+limited+" rate-limited) over ~1.5 s and measure the displacement",
        "moved at most "+String.format("%.2f",allowed)+" tiles per axis ("+ticks+" ticks x "+String.format("%.4f",SPEED_PER_TICK)+" + 2)",
        String.format("dx=%.2f dy=%.2f over %d ticks",dx,dy,(long)ticks),Math.abs(dx)<=allowed && Math.abs(dy)<=allowed);
    append("INFO SPEED-spam-moved: A moved dx="+String.format("%.2f",dx)+" dy="+String.format("%.2f",dy)+" (informational: a wall may stop it)"+"\n");
    // Walk in each direction toward walls and the region edge; every sampled position must be standable and hosted.
    var violations=new ArrayList<String>();
    int samples=0;
    double maxStep=0;double[] last=pos();
    for(int[] d:new int[][]{{1,0},{0,-1},{-1,0},{0,1},{1,1},{-1,-1}}) {
      long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(4);
      while(System.nanoTime()<until) {
        aPost("/input",aEpoch+" "+(seq++)+" "+d[0]+" "+d[1]);
        var p=pos();samples++;
        double relX=p[1]-map.originX,relY=p[2]-map.originY;
        if(!map.canStand(relX,relY)) violations.add(String.format("not standable at %.2f,%.2f",p[1],p[2]));
        if(!plan.canStand(new CharacterProgress.Respawn(WORLD,p[1],p[2]))) violations.add(String.format("outside hosted region at %.2f,%.2f",p[1],p[2]));
        double step=Math.max(Math.abs(p[1]-last[1]),Math.abs(p[2]-last[2]))/Math.max(1,p[0]-last[0]);
        maxStep=Math.max(maxStep,step);last=p;
        Thread.sleep(40);
      }
      aPost("/input",aEpoch+" "+(seq++)+" 0 0");
    }
    row("SPEED-walls","walk east/north/west/south/diagonals into walls and the region boundary for 24 s, sampling "+samples+" positions",
        "every sample standable and inside the hosted rectangle; per-tick step <= "+String.format("%.4f",SPEED_PER_TICK),
        violations.isEmpty() ? "no violations; max step "+String.format("%.4f",maxStep)+" tiles/tick" : violations.stream().limit(3).toList().toString(),
        violations.isEmpty() && maxStep<=SPEED_PER_TICK*1.05+0.01);
    row("SPEED-world","A's committed world and session after walking","still world 0, connected",
        "committed world "+dbA().world()+"; /state "+aGet("/state").status,dbA().world()==WORLD && aGet("/state").is(200));
    // Refused portal 947 (2964,1038): walk there, stand on it, nothing may happen.
    portalWalk(plan);
    // Last: a sequence jump. Legal-format, A-only effect.
    var jump=aPost("/input",aEpoch+" 9223372036854775807 0 0");
    var after=aPost("/input",aEpoch+" "+(seq++)+" 1 0");
    row("SEQ-jump","/input with sequence Long.MAX_VALUE, then a normal sequence","accepted once (own session only); later inputs 409 sequence; B unaffected",
        jump.text()+" / "+after.text(),(jump.is(200) && after.isRefusal("sequence")) || jump.is(400));
    NOT_COVERED.add("SEQ-jump follow-up: after a Long.MAX_VALUE sequence A cannot move again until it rejoins (own-session denial of its own movement; no other player affected). Reported as an observation, not a vulnerability.");
    String bDiff=movementStart.diff(snap());
    row("SPEED-B","B's committed record is unaffected by A's movement tampering","B fingerprint unchanged (A's own state is allowed to differ only by position)",
        bDiff.contains("B:") ? bDiff.substring(bDiff.indexOf("B:")) : "B unchanged",!bDiff.contains("B:"));
  }

  private static void portalWalk(HostedRegionPlan plan) throws Exception {
    int tx=2964,ty=1038;
    if(!plan.isRefusedPortalSource(WORLD,tx+.5,ty+.5)) { NOT_COVERED.add("PORTAL-refused: tile 2964,1038 is not a refused portal source in this build"); return; }
    boolean reached=false;
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(75);
    int[] cur=null;
    int lastX=0,lastY=0;
    while(System.nanoTime()<until) {
      var p=pos();
      // The server finishes a claimed tile (single-player's grid reservation rule), so plan from the tile the
      // player is ARRIVING on (the last direction sent), not from the floor of a mid-glide position.
      int cx=arrivingTile(p[1],lastX),cy=arrivingTile(p[2],lastY);
      cur=new int[]{cx,cy};
      if(cx==tx && cy==ty) { reached=true;break; }
      int[] next=nextStep(plan,cx,cy,tx,ty);
      if(next==null) break;
      lastX=Integer.signum(next[0]-cx);lastY=Integer.signum(next[1]-cy);
      aPost("/input",aEpoch+" "+(seq++)+" "+lastX+" "+lastY);
      Thread.sleep(25);
    }
    // Standing still on the tile: a transfer would be committed within a few ticks.
    aPost("/input",aEpoch+" "+(seq++)+" 0 0");
    Thread.sleep(2500);
    var fin=dbA();var st=state();
    String key=st==null ? "?" : st.getAsJsonObject("region").get("key").getAsString();
    if(!reached) {
      NOT_COVERED.add("PORTAL-refused: the probe's own tile BFS found no walkable route from A's tile to the refused portal tile 2964,1038 (A at "+Arrays.toString(cur)+"); the tile sits in a pocket walled off from the temple ground, so it cannot be stood on from A's start; not claimed");
      return;
    }
    var p=pos();
    row("PORTAL-refused","stand on the refused portal source tile 2964,1038 (target world 1, unhosted)","no transfer: region key 'start', world 0, same epoch, session alive",
        String.format("position %.1f,%.1f; region key %s; committed world %d; state %d",p[1],p[2],key,fin.world(),aGet("/state").status),
        key.equals("start") && fin.world()==WORLD && aGet("/state").is(200));
  }

  /** Tile index a glide is arriving on: the centre-rounded tile at rest, else the next tile in the last direction sent. Mirrors HostedTransferTest.walkPortal. */
  static int arrivingTile(double c,int last) {
    int tile=(int)Math.round(c-.5);
    if(Math.abs(c-Math.floor(c)-.5)>1e-6) tile=last>0 ? (int)Math.ceil(c-.5) : last<0 ? (int)Math.floor(c-.5) : tile;
    return tile;
  }

  private static int[] nextStep(HostedRegionPlan plan,int sx,int sy,int tx,int ty) {
    int w=300,h=200;int ox=2735,oy=1029;
    int[][] dist=new int[h][w];for(var row:dist) Arrays.fill(row,-1);
    var queue=new ArrayDeque<int[]>();
    dist[ty-oy][tx-ox]=0;queue.add(new int[]{tx,ty});
    int[][] dirs={{1,0},{-1,0},{0,1},{0,-1}};
    while(!queue.isEmpty()) {
      var c=queue.poll();
      for(var d:dirs) {
        int nx=c[0]+d[0],ny=c[1]+d[1];
        if(nx<ox || ny<oy || nx>=ox+249 || ny>=oy+117 || dist[ny-oy][nx-ox]>=0) continue;
        if(!plan.canStand(new CharacterProgress.Respawn(WORLD,nx+.5,ny+.5))) continue;
        if(plan.portals.resolve(WORLD,nx+.5,ny+.5,plan::canStand).isPresent()) continue;
        dist[ny-oy][nx-ox]=dist[c[1]-oy][c[0]-ox]+1;queue.add(new int[]{nx,ny});
      }
    }
    if(sx<ox || sy<oy || sx>=ox+249 || sy>=oy+117 || dist[sy-oy][sx-ox]<0) return null;
    for(var d:dirs) {
      int nx=sx+d[0],ny=sy+d[1];
      if(nx<ox || ny<oy || nx>=ox+249 || ny>=oy+117) continue;
      if(dist[ny-oy][nx-ox]>=0 && dist[ny-oy][nx-ox]<dist[sy-oy][sx-ox]) return new int[]{nx,ny};
    }
    return null;
  }

  private static void groupVictimPlay(String kilhiamId) throws Exception {
    // B plays normally through the native adapter after everything A tried: move, dialogue, committed result.
    require(bConn.status().equals("connected") && bConn.view()!=null,"B adapter disconnected");
    var before=dbB();
    var talk=talk(bConn,kilhiamId);
    Thread.sleep(500);
    var saved=dbB();
    var letters=saved.progress().inventory().stream().filter(i->i.key().equals(com.perso.T4C.quest.CrimsonscaleLetterRules.ITEM)).toList();
    row("B-PLAY-dialogue","B's native /interact Kilhiam after the whole battery","accepted, exactly one letter, story flag set once",
        "accepted="+talk.accepted()+", message "+talk.messageKey()+", letters "+letters.size()+" x"+(letters.isEmpty()?0:letters.getFirst().quantity()),
        talk.accepted() && letters.size()==1 && letters.getFirst().quantity()==1
        && saved.progress().questFlags().getOrDefault(com.perso.T4C.quest.CrimsonscaleLetterRules.LETTER_FLAG,0)==1 && saved.gold()==before.gold());
    double x0=own().x(),yStart=own().y();boolean moved=false;
    for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
      bConn.direction(d[0],d[1]);
      try { waitFor(()->Math.hypot(own().x()-x0,0)>.3 || Math.abs(own().y()-yStart)>.3,3,"B native movement stalled");moved=true; }
      catch(AssertionError blocked) { moved=false; }
      finally { bConn.stopInput(); }
      if(moved) break;
    }
    boolean committed=false;
    if(moved) {
      try { waitFor(()->Math.abs(dbB().x()-before.x())>.1 || Math.abs(dbB().y()-before.y())>.1,12,"B committed movement");committed=true; }
      catch(AssertionError none) { committed=false; }
    }
    row("B-PLAY-move","B moves through the native adapter and the move is committed","position changes and is persisted",
        String.format("native %.2f,%.2f->%.2f,%.2f; committed %.2f,%.2f->%.2f,%.2f",x0,yStart,own().x(),own().y(),before.x(),before.y(),dbB().x(),dbB().y()),moved && committed);
    var fp=fp(dbB());
    var initial=fp(B.created);
    row("B-PLAY-final","B's attributes, level, skills, spells, storage and gold versus provisioning","identical except the one authored letter and its flag",
        "gold "+initial.gold+"->"+fp.gold+", attributes equal="+initial.at.equals(fp.at)+", advancement equal="+initial.ad.equals(fp.ad)
        +", skills equal="+initial.skills.equals(fp.skills),
        initial.gold==fp.gold && initial.xp==fp.xp && initial.at.equals(fp.at) && initial.ad.equals(fp.ad) && initial.skills.equals(fp.skills)
        && initial.spells.equals(fp.spells) && initial.storage.equals(fp.storage));
  }

  private static OnlineConnection.CommandResult talk(OnlineConnection c,String npc) throws Exception {
    for(int i=0;i<12;i++) {
      var r=c.interact(npc).toCompletableFuture().get(6,TimeUnit.SECONDS);
      if(r.accepted()) return r;
      Thread.sleep(200);
    }
    return c.interact(npc).toCompletableFuture().get(6,TimeUnit.SECONDS);
  }

  private static void finalInvariants(UUID lootB,UUID corpseA,UUID corpseB,String kilhiam) throws Exception {
    row("DB-loot-B","B's seeded owner-window drop at the end of the run","ON_GROUND, owner B, never claimed, no receipt",
        sqlSafe("SELECT state||'|'||(claimed_by IS NULL)||'|'||(SELECT count(*) FROM online_loot_claims WHERE loot_id=?) FROM online_ground_loot WHERE id=?",lootB,lootB),
        sqlSafe("SELECT state||'|'||(claimed_by IS NULL)||'|'||(SELECT count(*) FROM online_loot_claims WHERE loot_id=?) FROM online_ground_loot WHERE id=?",lootB,lootB).equals("ON_GROUND|true|0"));
    row("DB-corpse-B","B's corpse at the end of the run","unrecovered, 777 gold, 1 item",
        sqlSafe("SELECT recovered||'|'||gold||'|'||jsonb_array_length(items) FROM online_corpses WHERE id=?",corpseB),
        sqlSafe("SELECT recovered||'|'||gold||'|'||jsonb_array_length(items) FROM online_corpses WHERE id=?",corpseB).equals("false|777|1"));
    row("DB-corpse-A","A's own corpse at the end of the run","recovered exactly once (gold 0, empty)",
        sqlSafe("SELECT recovered||'|'||gold||'|'||jsonb_array_length(items) FROM online_corpses WHERE id=?",corpseA),
        sqlSafe("SELECT recovered||'|'||gold||'|'||jsonb_array_length(items) FROM online_corpses WHERE id=?",corpseA).equals("true|0|0"));
  }

  private static void notCovered() {
    NOT_COVERED.add("Rendered-client behavior (a modified native client, input rejection UX): not reachable from an HTTP/adapter probe.");
    NOT_COVERED.add("Isolated /attack stale-revision fence AFTER target validation, and any damage/reward forging in combat: this probe does no combat (starter ground is lethal, T4C-0248); /attack revision is checked first and is covered.");
    NOT_COVERED.add("B's own successful pickup of its owner-window loot and B's own corpse recovery: both fixtures lie on A's tile, 17+ tiles from B (out of pickup/recover range).");
    NOT_COVERED.add("Wrong-region creature/loot ids (an id that belongs to another hosted region): no second region contains a creature or loot reachable by id without portal travel; random ids cover the refusal path.");
    NOT_COVERED.add("Enabled portal transfer tampering (routes 16/17) and teleport via transfer: only the refused portal 947 and unhosted joins are exercised.");
    NOT_COVERED.add("Account-key rotation fencing: covered separately by the T4C-0219 NativeKeyRotationProbe, not repeated here.");
    NOT_COVERED.add("Remote/TLS transport tampering: this run is plain loopback HTTP.");
  }

  // ---------------------------------------------------------------- B and A helpers

  private static void bAlive(String when) {
    try {
      long t0=bConn.view().snapshot().tick();
      Thread.sleep(700);
      long t1=bConn.view().snapshot().tick();
      boolean pass=bConn.status().equals("connected") && bConn.view().character()!=null && bConn.view().character().id().equals(B.id()) && t1>t0;
      row("B-ALIVE "+when,"B (native adapter) keeps playing "+when,"connected, own identity, snapshot ticks advancing",
          "status "+bConn.status()+", ticks "+t0+"->"+t1+", gold "+bConn.view().character().gold(),pass);
    } catch(Throwable e) { row("B-ALIVE "+when,"B keeps playing","connected",String.valueOf(e),false); }
  }

  private static OnlineConnection.Actor own() {
    var v=bConn.view();
    return v.snapshot().players().stream().filter(p->p.id()==v.ownId()).findFirst().orElseThrow();
  }

  private static OnlineStore.CharacterState dbA() { try { return store.load(A.account(),A.id()); } catch(Exception e) { throw new RuntimeException(e); } }
  private static OnlineStore.CharacterState dbB() { try { return store.load(B.account(),B.id()); } catch(Exception e) { throw new RuntimeException(e); } }
  private static Snap snapState() { return new Snap(fp(dbA()),fp(dbB())); }
  private static Snap snap() { return snapState(); }

  private static Fp fp(OnlineStore.CharacterState s) {
    var p=s.progress();
    return new Fp(s.gold(),s.experience(),p.attributes(),p.advancement(),p.inventory(),p.equipment(),p.skills(),p.spells(),
        p.questFlags(),p.storage(),p.storageGold(),s.world());
  }
  private static int countKey(CharacterProgress p,String key) { return (int)p.inventory().stream().filter(i->i.key().equals(key)).count(); }

  private static long revA() {
    var r=aGet("/character");
    if(!r.is(200)) return -1;
    return JsonParser.parseString(r.body).getAsJsonObject().get("revision").getAsLong();
  }
  private static String revBody(String tail) { return revA()+" "+aEpoch+" "+tail; }

  private record Sent(Reply reply,String body) {}
  /** A valid command carrying the live revision. The periodic checkpoint can move it between read and send, so retry a stale/busy answer. */
  private static Sent validCommand(String path,Function<Long,String> body) {
    Reply last=null;String sentBody="";
    for(int i=0;i<10;i++) {
      sentBody=body.apply(revA());
      last=aPost(path,sentBody);
      if(!(last.isRefusal("revision") || last.isRefusal("busy"))) break;
      sleep(120);
    }
    return new Sent(last,sentBody);
  }

  private static JsonObject state() {
    var r=aGet("/state");
    if(!r.is(200)) return null;
    return JsonParser.parseString(r.body).getAsJsonObject();
  }
  /** tick, x, y of A's own actor. */
  private static double[] pos() {
    var st=state();
    if(st==null) throw new AssertionError("A /state unavailable");
    for(var p:st.getAsJsonArray("players")) {
      var o=p.getAsJsonObject();
      if(o.get("id").getAsLong()==aActor) return new double[]{st.get("tick").getAsDouble(),originX+o.get("x").getAsDouble(),originY+o.get("y").getAsDouble()};
    }
    throw new AssertionError("A actor missing from its own snapshot");
  }
  private static int rosterCount() {
    var r=send("GET","/account/characters",account(A.secret()),null);
    if(!r.is(200)) return -1;
    var o=JsonParser.parseString(r.body);
    JsonArray arr=o.isJsonArray()?o.getAsJsonArray():o.getAsJsonObject().has("characters")?o.getAsJsonObject().getAsJsonArray("characters"):new JsonArray();
    return arr.size();
  }

  // ---------------------------------------------------------------- cases

  private static void tamper(String id,String what,String expected,Predicate<Reply> ok,Supplier<Reply> act) {
    try {
      var before=snap();
      if(aToken!=null) aGet("/state");
      Reply r=act.get();
      // The periodic checkpoint moves the revision every ~5 s; re-send once if a freshly read one was overtaken.
      if(!ok.test(r) && (r.isRefusal("revision") || r.isRefusal("busy")) && !expected.contains("revision") && !expected.contains("busy")) {
        Thread.sleep(150);r=act.get();
      }
      Thread.sleep(80);
      var after=snap();
      String diff=before.diff(after);
      boolean pass=ok.test(r) && diff.isEmpty() && !(r.status>=500);
      row(id,what,expected,r.text()+(diff.isEmpty() ? "; A and B committed state unchanged" : "; COMMITTED STATE CHANGED: "+diff),pass);
    } catch(Throwable t) { row(id,what,expected,"probe exception "+t,false); }
  }

  private static void leaseFree(String id,String what,Acct who) {
    try {
      boolean inUse=store.roster(who.secret(),null).stream().filter(e->e.character().id().equals(who.id())).findFirst().orElseThrow().inUse();
      row(id,what,"lease free",inUse?"lease HELD":"lease free",!inUse);
    } catch(Exception e) { row(id,what,"lease free",String.valueOf(e),false); }
  }

  private static void row(String id,String what,String expected,String observed,boolean pass) {
    var r=new Row(id,what,expected,observed,pass);
    ROWS.add(r);
    append((pass?"PASS ":"FAIL ")+id+" | "+what+" | expected "+expected+" | observed "+observed+"\n");
  }

  private static void summary(Path output) throws Exception {
    var sb=new StringBuilder();
    sb.append("| # | Case | Expected | Observed | Result |\n|---|---|---|---|---|\n");
    int i=1;
    for(var r:ROWS)
      sb.append("| ").append(i++).append(" | `").append(r.id).append("` ").append(md(r.what)).append(" | ").append(md(r.expected)).append(" | ")
          .append(md(r.observed)).append(" | ").append(r.pass?"PASS":"**FAIL**").append(" |\n");
    sb.append("\nNOT COVERED:\n");
    for(var n:NOT_COVERED) sb.append("- ").append(n).append('\n');
    Files.writeString(output.resolve("cases.md"),sb.toString(),StandardCharsets.UTF_8);
    long fails=ROWS.stream().filter(r->!r.pass).count();
    append("SUMMARY: "+ROWS.size()+" cases, "+(ROWS.size()-fails)+" PASS, "+fails+" FAIL. Table in cases.md.\n");
  }

  // ---------------------------------------------------------------- HTTP

  private static Map<String,String> std(String bearer) {
    var h=new LinkedHashMap<String,String>();
    h.put("Origin",endpoint.toString());h.put("Content-Type","text/plain");h.put(OnlineProtocol.VERSION_HEADER,VERSION);
    if(bearer!=null) h.put("Authorization","Bearer "+bearer);
    return h;
  }
  private static Map<String,String> account(String key) {
    var h=new LinkedHashMap<String,String>();
    h.put(OnlineProtocol.VERSION_HEADER,VERSION);h.put("Authorization","Bearer "+key);
    return h;
  }
  private static Map<String,String> creationHeaders() {
    var h=std(null);h.put("Authorization","Bearer "+A.secret());h.put("Content-Type","application/json");return h;
  }
  private static Reply aPost(String path,String body) { return send("POST",path,std(aToken),body); }
  private static Reply aGet(String path) {
    Reply r=null;
    for(int i=0;i<6;i++) {
      r=send("GET",path,std(aToken),null);
      if(!r.is(429,503)) return r;
      sleep(60);
    }
    return r;
  }
  private static Reply post(String path,Map<String,String> h,String body) { return send("POST",path,h,body); }

  private static Reply send(String method,String path,Map<String,String> headers,String body) {
    try {
      var b=HttpRequest.newBuilder(endpoint.resolve(path)).timeout(Duration.ofSeconds(8));
      headers.forEach(b::header);
      b.method(method,body==null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body,StandardCharsets.UTF_8));
      var r=http.send(b.build(),HttpResponse.BodyHandlers.ofString());
      return new Reply(r.statusCode(),r.body(),null);
    } catch(Exception e) { return new Reply(-1,"",e.getClass().getSimpleName()+": "+abbreviate(String.valueOf(e.getMessage()))); }
  }
  private static Reply sendStream(String path,String token,int bytes) {
    try {
      var b=HttpRequest.newBuilder(endpoint.resolve(path)).timeout(Duration.ofSeconds(8));
      std(token).forEach(b::header);
      b.POST(HttpRequest.BodyPublishers.ofInputStream(()->new java.io.ByteArrayInputStream("7".repeat(bytes).getBytes(StandardCharsets.US_ASCII))));
      var r=http.send(b.build(),HttpResponse.BodyHandlers.ofString());
      return new Reply(r.statusCode(),r.body(),null);
    } catch(Exception e) { return new Reply(-1,"",e.getClass().getSimpleName()+": "+abbreviate(String.valueOf(e.getMessage()))); }
  }
  /** Hand-written request for header cases the JDK client will not produce. */
  private static Reply raw(String request) {
    try(var s=new Socket()) {
      s.connect(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),port),3000);
      s.setSoTimeout(5000);
      s.getOutputStream().write(request.getBytes(StandardCharsets.ISO_8859_1));
      s.getOutputStream().flush();
      var out=new ByteArrayOutputStream();
      byte[] buf=new byte[2048];
      try { int n;while((n=s.getInputStream().read(buf))>0) { out.write(buf,0,n);if(out.size()>8192) break; } }
      catch(java.net.SocketTimeoutException ignored) { }
      String text=out.toString(StandardCharsets.ISO_8859_1);
      if(text.isEmpty()) return new Reply(-1,"","connection closed without a response");
      String first=text.lines().findFirst().orElse("");
      String[] parts=first.split(" ");
      int status=parts.length>1 ? Integer.parseInt(parts[1]) : -1;
      int bodyAt=text.indexOf("\r\n\r\n");
      return new Reply(status,bodyAt<0 ? "" : text.substring(bodyAt+4),null);
    } catch(Exception e) { return new Reply(-1,"",e.getClass().getSimpleName()+": "+abbreviate(String.valueOf(e.getMessage()))); }
  }

  private static void healthy(Process child) throws Exception {
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(START_SECONDS);
    while(System.nanoTime()<until) {
      require(child.isAlive(),"Owned server exited before health");
      var r=send("GET","/health",new LinkedHashMap<>(),null);
      if(r.is(200) && r.body.contains("local-persistent")) return;
      Thread.sleep(200);
    }
    throw new AssertionError("Owned persistent server did not become healthy");
  }
  private static void shutdown(Process child,Path control) throws Exception {
    require(child.isAlive() && Files.isRegularFile(control),"Owned server/control unavailable");
    String token=Files.readString(control).trim();
    require(token.matches("[0-9a-f]{64}"),"Private control token malformed");
    var r=post("/admin/shutdown",std(token),"");
    require(r.is(200),"Private admin stop rejected: "+r.text());
    require(child.waitFor(100,TimeUnit.SECONDS) && child.exitValue()==0,"Owned server did not exit cleanly");
  }

  // ---------------------------------------------------------------- misc

  private static String sqlSafe(String sql,Object... params) { try { return sql1(sql,params); } catch(Exception e) { return "sql error "+e.getClass().getSimpleName(); } }
  private static String randomHex(int n) {
    var r=new Random();var sb=new StringBuilder();
    while(sb.length()<n) sb.append("0123456789abcdef".charAt(r.nextInt(16)));
    return sb.toString();
  }
  private static String randomLetters(int n) {
    var r=new Random();var sb=new StringBuilder();
    for(int i=0;i<n;i++) sb.append((char)('a'+r.nextInt(26)));
    return sb.toString();
  }
  private static String vis(String s) { return s.replace("\u0000","\\0").replaceAll("[^\\x20-\\x7e]","?"); }
  private static String abbreviate(String s) { s=s==null?"":s.replaceAll("\\s+"," ").trim();return s.length()>110 ? s.substring(0,107)+"..." : s; }
  private static String md(String s) { return s.replace("|","\\|").replace("\n"," "); }
  private static void sleep(long ms) { try { Thread.sleep(ms); } catch(InterruptedException e) { Thread.currentThread().interrupt(); } }
  private static void await(CountDownLatch l) { try { l.await(); } catch(InterruptedException e) { Thread.currentThread().interrupt(); } }
  private static void waitFor(Check condition,int seconds,String message) throws Exception {
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(seconds);
    while(System.nanoTime()<until) { if(condition.ok()) return; Thread.sleep(25); }
    throw new AssertionError(message);
  }
  private static void append(String value) {
    try { Files.writeString(report,value,StandardCharsets.UTF_8,StandardOpenOption.APPEND); } catch(Exception ignored) { }
  }
  private static void require(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
}
