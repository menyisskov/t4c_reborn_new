import com.google.gson.*;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.server.PreviewMap;
import com.perso.T4C.server.HostedRegionPlan;
import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/** Standalone QA driver using the desktop client's real network adapter, without a renderer. */
class NativeAdapterSoak {
  static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
  static final int METRICS_RESPONSE_LIMIT=1024*1024;
  static final class BoundedBody implements HttpResponse.BodySubscriber<byte[]> {
    private final HttpResponse.BodySubscriber<byte[]> delegate=HttpResponse.BodySubscribers.ofByteArray();
    private Flow.Subscription subscription;
    private long received;
    @Override public CompletionStage<byte[]> getBody() { return delegate.getBody(); }
    @Override public void onSubscribe(Flow.Subscription value) { subscription=value; delegate.onSubscribe(value); }
    @Override public void onNext(List<java.nio.ByteBuffer> buffers) {
      for(var buffer:buffers) received+=buffer.remaining();
      if(received>METRICS_RESPONSE_LIMIT) {
        subscription.cancel();delegate.onError(new java.io.IOException("Operator metrics response exceeded limit"));
      } else delegate.onNext(buffers);
    }
    @Override public void onError(Throwable failure) { delegate.onError(failure); }
    @Override public void onComplete() { delegate.onComplete(); }
  }
  static final class RuntimeSamples {
    final String controlToken;
    JsonObject initial,latest,max;
    long samples,failures;
    RuntimeSamples(String controlToken) { this.controlToken=controlToken; }
    void sample(HttpClient http,URI endpoint) {
      if(controlToken==null) return;
      CompletableFuture<HttpResponse<byte[]>> pending=null;
      try {
        pending=http.sendAsync(HttpRequest.newBuilder(endpoint.resolve("/admin/metrics"))
            .header("Authorization","Bearer "+controlToken).timeout(Duration.ofSeconds(2)).GET().build(),
            ignored -> new BoundedBody());
        var response=pending.get(2,TimeUnit.SECONDS);
        if(response.statusCode()!=200) throw new IllegalStateException("operator metrics unavailable");
        var value=JsonParser.parseString(new String(response.body(),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        if(initial==null) { initial=value.deepCopy(); max=value.deepCopy(); }
        latest=value.deepCopy(); mergeMax(max,value); samples++;
      } catch(Exception unavailable) {
        if(pending!=null) pending.cancel(true);
        if(unavailable instanceof InterruptedException) Thread.currentThread().interrupt();
        failures++;
      }
    }
    Map<String,Object> report() {
      var result=new LinkedHashMap<String,Object>();
      result.put("available",samples>0); result.put("samples",samples); result.put("failures",failures);
      if(samples>0) { result.put("initial",initial); result.put("latest",latest); result.put("max",max); }
      return result;
    }
  }
  static void mergeMax(JsonObject max,JsonObject current) {
    for(var entry:current.entrySet()) {
      if(entry.getValue().isJsonObject()) mergeMax(max.getAsJsonObject(entry.getKey()),entry.getValue().getAsJsonObject());
      else if(entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()
          && (!max.has(entry.getKey()) || max.get(entry.getKey()).isJsonNull()
              || entry.getValue().getAsBigDecimal().compareTo(max.get(entry.getKey()).getAsBigDecimal())>0))
        max.add(entry.getKey(),entry.getValue().deepCopy());
    }
  }
  static final int[][] DIRECTIONS={{1,0},{-1,0},{0,1},{0,-1}};
  /** Ideal cardinal walks hub-to-cave-mouth take up to ~165 s (PortalWalkTimingTest pins this with headroom); 150 s timed legs out. */
  static final int DEFAULT_LEG_TIMEOUT_SECONDS=300;
  /** Concurrent joins allowed and the minimum gap between opens; the server admits only two public requests. */
  static final int MAX_JOINING=1;
  static final long OPEN_GAP_MILLIS=500;
  /**
   * Pure pacing of adapter opens (initial and reconnect) so a login burst never exceeds the server's
   * two-wide public admission. {@code joining} counts adapters opened but not yet connected.
   */
  static final class OpenGate {
    final int maxJoining; final long gapMillis; long nextOpenAt=Long.MIN_VALUE;
    OpenGate(int maxJoining,long gapMillis) {
      if(maxJoining<1 || gapMillis<0) throw new IllegalArgumentException("Invalid open gate");
      this.maxJoining=maxJoining; this.gapMillis=gapMillis;
    }
    boolean mayOpen(int joining,long now) { return joining<maxJoining && now>=nextOpenAt; }
    void opened(long now) { nextOpenAt=now+gapMillis; }
  }
  /** One enabled authored portal: the hosted region it leaves, the region it enters, and its absolute source tile. */
  record PortalRoute(int id,String fromKey,String toKey,int sourceX,int sourceY) {}
  /**
   * Routes derived from the trusted hosted plan: an authored portal is enabled exactly when its source and its
   * target both lie in hosted regions (the plan refuses a hosted target for any disabled source). Duplicate source
   * tiles keep the first, like the server's catalog. Only routes that also have a way back are kept.
   */
  static List<PortalRoute> portalRoutes(List<HostedRegionPlan.Region> regions,List<com.perso.T4C.online.model.PortalContent.Portal> portals) {
    var seen=new HashSet<String>(); var all=new ArrayList<PortalRoute>();
    for(var p:portals) {
      String from=regionAt(regions,p.sourceWorld(),p.sourceX(),p.sourceY()),to=regionAt(regions,p.targetWorld(),p.targetX(),p.targetY());
      if(from==null || to==null || !seen.add(p.sourceWorld()+":"+p.sourceX()+":"+p.sourceY())) continue;
      all.add(new PortalRoute(p.id(),from,to,p.sourceX(),p.sourceY()));
    }
    return all.stream().filter(r -> all.stream().anyMatch(b -> b.fromKey().equals(r.toKey()) && b.toKey().equals(r.fromKey()))).toList();
  }
  static String regionAt(List<HostedRegionPlan.Region> regions,int world,int x,int y) {
    for(var r:regions) { var s=r.spec();
      if(s.world()==world && x>=s.originX() && y>=s.originY() && x<s.originX()+s.width() && y<s.originY()+s.height()) return s.key(); }
    return null;
  }
  /** True for exactly {@code walkers} of {@code total} adapters, spread evenly through the profile order. */
  static boolean isPortalWalker(int index,int total,int walkers) { return (long)(index+1)*walkers/total>(long)index*walkers/total; }
  /**
   * Pure per-adapter trip state machine: IDLE, then walk OUT through a portal, DWELL in the destination, optionally
   * go deeper, then walk BACK through the paired portals to the home region. Any region that is not the expected
   * one (death respawn, reconnect elsewhere) aborts the trip. The caller supplies time and the region key; this
   * class never touches the network, so the self-test can drive it.
   */
  static final class PortalTrip {
    enum Phase { IDLE,OUT,DWELL,BACK }
    static final int MAX_DEPTH=3;
    final int index; final List<PortalRoute> routes; final long intervalMillis,dwellMillis,legTimeoutMillis;
    Phase phase=Phase.IDLE; long nextTripAt,dwellUntil,legStartedAt;
    final ArrayDeque<String> trail=new ArrayDeque<>();
    PortalRoute route; int depthTarget;
    long started,completed,aborted,timeouts,outbound,inbound;
    final Set<String> notes=new TreeSet<>();
    /** Caller-maintained last known position, appended to abort notes so a stuck walker can be located. */
    String where="";
    PortalTrip(int index,List<PortalRoute> routes,long firstAt,long intervalMillis,long dwellMillis,long legTimeoutMillis) {
      this.index=index; this.routes=routes; this.nextTripAt=firstAt; this.intervalMillis=intervalMillis;
      this.dwellMillis=dwellMillis; this.legTimeoutMillis=legTimeoutMillis;
    }
    boolean walking() { return phase==Phase.OUT || phase==Phase.BACK; }
    private List<PortalRoute> forward(String key,String avoid) {
      return routes.stream().filter(r -> r.fromKey().equals(key) && !r.toKey().equals(avoid)).toList();
    }
    private void abort(long now,String note,boolean timeout) {
      if(timeout) timeouts++; else aborted++;
      if(notes.size()<6) notes.add(note+"@"+where);
      phase=Phase.IDLE; route=null; trail.clear(); nextTripAt=now+intervalMillis;
    }
    /** Advance timers; call each loop for a connected adapter in region {@code key}. */
    void tick(long now,String key) {
      switch(phase) {
        case IDLE -> {
          if(now<nextTripAt) return;
          var options=forward(key,null);
          if(options.isEmpty()) { nextTripAt=now+intervalMillis; return; }
          route=options.get((int)((index+started)%options.size())); started++;
          depthTarget=1+(int)((index+started)%MAX_DEPTH); trail.clear(); phase=Phase.OUT; legStartedAt=now;
        }
        case DWELL -> {
          if(now<dwellUntil) return;
          var deeper=trail.size()+1<depthTarget?forward(key,trail.peek()):List.<PortalRoute>of();
          if(!deeper.isEmpty()) { route=deeper.get((int)((index+started)%deeper.size())); phase=Phase.OUT; legStartedAt=now; }
          else { route=backRoute(key); if(route==null) abort(now,"no_way_back_from_"+key,false); else { phase=Phase.BACK; legStartedAt=now; } }
        }
        case OUT,BACK -> {
          if(now-legStartedAt>legTimeoutMillis) abort(now,phase+"_timeout_portal_"+route.id()+"_in_"+key,true);
        }
      }
    }
    private PortalRoute backRoute(String key) {
      String home=trail.peek(); if(home==null) return null;
      return routes.stream().filter(r -> r.fromKey().equals(key) && r.toKey().equals(home)).findFirst().orElse(null);
    }
    /** The region the adapter is now in changed (committed location epoch). */
    void onRegion(long now,String key) {
      if(phase==Phase.IDLE) return;
      if(phase==Phase.DWELL) { abort(now,"region_changed_during_dwell_to_"+key,false); return; }
      if(!key.equals(route.toKey())) { abort(now,phase+"_unexpected_region_"+key,false); return; }
      if(phase==Phase.OUT) {
        trail.push(route.fromKey()); outbound++; phase=Phase.DWELL; dwellUntil=now+dwellMillis; route=null;
      } else {
        trail.pop(); inbound++;
        if(trail.isEmpty()) { completed++; phase=Phase.IDLE; route=null; nextTripAt=now+intervalMillis; }
        else { route=backRoute(key); if(route==null) abort(now,"no_way_back_from_"+key,false); else legStartedAt=now; }
      }
    }
  }
  /** Cardinal breadth-first step over the collision map to an exact goal tile; null when unreachable or already there. */
  static int[] pathToTile(PreviewMap map,int sx,int sy,int gx,int gy) {
    if(sx==gx && sy==gy) return null;
    if(sx<0 || sy<0 || sx>=map.width || sy>=map.height || gx<0 || gy<0 || gx>=map.width || gy>=map.height) return null;
    int[] parent=new int[map.width*map.height]; Arrays.fill(parent,-1);
    int[] queue=new int[parent.length]; int head=0,tail=0,start=sy*map.width+sx,goal=gy*map.width+gx; queue[tail++]=start; parent[start]=start;
    while(head<tail && parent[goal]<0) {
      int cell=queue[head++],cx=cell%map.width,cy=cell/map.width;
      for(int[] d:DIRECTIONS) {
        int nx=cx+d[0],ny=cy+d[1],id=ny*map.width+nx;
        if(nx<0 || ny<0 || nx>=map.width || ny>=map.height || parent[id]>=0 || !map.canStand(nx+.5,ny+.5)) continue;
        parent[id]=cell; queue[tail++]=id;
      }
    }
    if(parent[goal]<0) return null;
    int cell=goal; while(parent[cell]!=start) cell=parent[cell];
    return new int[]{cell%map.width,cell/map.width};
  }
  static void require(boolean ok,String what) { if(!ok) throw new AssertionError("Self-test failed: "+what); }
  /** Headless check of the pacing helper: {@code NativeAdapterSoak.java --self-test}. */
  static void selfTest() {
    var gate=new OpenGate(2,500);
    require(gate.mayOpen(0,0),"first open allowed");
    gate.opened(0);
    require(!gate.mayOpen(0,499),"gap enforced");
    require(gate.mayOpen(1,500),"second concurrent join allowed after gap");
    gate.opened(500);
    require(!gate.mayOpen(2,1000),"concurrency bound enforced");
    require(gate.mayOpen(1,1000),"slot reopens when a join completes");
    // Twenty adapters through a one-wide gate with a 3-tick join each never exceed the bound and all open.
    var one=new OpenGate(1,500); int opened=0,maxJoining=0; long[] doneAt=new long[20]; boolean[] joining=new boolean[20];
    for(long t=0;t<200_000 && opened<20;t+=50) {
      int j=0; for(int i=0;i<20;i++) { if(joining[i] && t>=doneAt[i]) joining[i]=false; if(joining[i]) j++; }
      maxJoining=Math.max(maxJoining,j);
      if(one.mayOpen(j,t)) { one.opened(t); joining[opened]=true; doneAt[opened]=t+1200; opened++; }
    }
    require(opened==20 && maxJoining<=1,"twenty paced opens, bounded joining");
    boolean rejected=false; try { new OpenGate(0,0); } catch(IllegalArgumentException expected) { rejected=true; }
    require(rejected,"invalid gate rejected");
    // Portal walkers: evenly spread, and the trip machine goes out, dwells, deepens, returns and aborts on surprises.
    for(int n=0;n<=20;n++) { int c=0; for(int i=0;i<20;i++) if(isPortalWalker(i,20,n)) c++; require(c==n,"exactly "+n+" walkers"); }
    var routes=List.of(new PortalRoute(16,"start","hall",5,5),new PortalRoute(17,"hall","start",6,6),
        new PortalRoute(0,"hall","sewer",7,7),new PortalRoute(1,"sewer","hall",8,8),new PortalRoute(99,"start","gone",9,9));
    var trip=new PortalTrip(0,routes,1000,5000,2000,60_000);
    trip.tick(999,"start"); require(trip.phase==PortalTrip.Phase.IDLE,"trip waits for its first time");
    trip.tick(1000,"start"); require(trip.walking() && trip.route.id()==16,"trip starts out through portal 16");
    trip.onRegion(1500,"hall"); require(trip.phase==PortalTrip.Phase.DWELL && trip.outbound==1,"arrival starts the dwell");
    trip.tick(3499,"hall"); require(trip.phase==PortalTrip.Phase.DWELL,"dwell lasts");
    trip.tick(3500,"hall");
    if(trip.phase==PortalTrip.Phase.OUT) {
      require(trip.route.id()==0,"deeper step uses the forward portal"); trip.onRegion(4000,"sewer");
      trip.tick(6000,"sewer"); require(trip.route.id()==1,"back from the sewer");
      trip.onRegion(6500,"hall"); require(trip.phase==PortalTrip.Phase.BACK,"continues back");
    }
    require(trip.phase==PortalTrip.Phase.BACK && trip.route.id()==17,"returns through portal 17");
    trip.onRegion(7000,"start"); require(trip.phase==PortalTrip.Phase.IDLE && trip.completed==1 && trip.nextTripAt==12000,"trip completes and rests");
    trip.tick(12000,"start"); trip.onRegion(12500,"elsewhere"); require(trip.phase==PortalTrip.Phase.IDLE && trip.aborted==1,"surprise region aborts");
    var slow=new PortalTrip(1,routes,0,5000,2000,60_000); slow.tick(0,"start"); slow.tick(60_001,"start");
    require(slow.phase==PortalTrip.Phase.IDLE && slow.timeouts==1,"stalled leg times out");
    require(portalRoutes(List.of(),List.of()).isEmpty(),"no regions, no routes");
    System.out.println("NativeAdapterSoak self-test passed");
  }
  static final class Player {
    final OnlineConnection.Credentials credentials;
    OnlineConnection connection;
    CompletableFuture<OnlineConnection.CommandResult> command;
    long commandStarted,nextCommand,reconnectAt,reconnectStarted,lastTick=-1,lastRevision=-1,lastFresh;
    long openedAt=-1,firstConnectedMillis=-1;
    boolean connectedSinceOpen;
    long attacks,recoveries,accepted,rejected,failures,reconnects,observations,moved,hpChanges;
    long locationEpoch=-1,epochChanges,worldTransfers,regionTransfers;
    PortalTrip trip;
    int world=-1;
    String regionKey;
    double x=Double.NaN,y; int hp=-1; int lastDx,lastDy;
    boolean expectedReconnect;
    final long[] latencyBuckets=new long[8];
    final Set<String> errors=new TreeSet<>();
    /** Bounded context for an error (region and position), reported beside the unchanged error names. */
    final Set<String> errorDetails=new TreeSet<>();
    Player(OnlineConnection.Credentials c) { credentials=c; }
  }
  static long ms() { return System.nanoTime()/1_000_000; }
  /** Report section for the optional portal walk; null (omitted) when it is off. */
  static Map<String,Object> portalConfig;
  static int reconnectSeconds=120;
  static int envInt(String name,int fallback,int min,int max) {
    String text=System.getenv(name); if(text==null || text.isBlank()) return fallback;
    int value; try { value=Integer.parseInt(text.trim()); } catch(NumberFormatException bad) { throw new IllegalArgumentException(name+" must be an integer"); }
    if(value<min || value>max) throw new IllegalArgumentException(name+" must be "+min+".."+max);
    return value;
  }
  static void error(Player p,String text) { p.failures++; p.errors.add(text); }
  public static void main(String[] args) throws Exception {
    if(args.length==1 && "--self-test".equals(args[0])) { selfTest(); return; }
    if(args.length!=6) throw new IllegalArgumentException("Expected endpoint profiles-directory seconds collision-file report-file server-pid-or-0");
    URI endpoint=URI.create(args[0]); Path profiles=Path.of(args[1]),report=Path.of(args[4]);
    String controlFile=System.getenv("T4C_SOAK_CONTROL_FILE");
    String controlToken=null;
    if(controlFile!=null && !controlFile.isBlank()) {
      if(!"127.0.0.1".equals(endpoint.getHost()) && !"::1".equals(endpoint.getHost()))
        throw new IllegalArgumentException("Operator metrics require a loopback endpoint");
      try(var input=Files.newInputStream(Path.of(controlFile))) {
        byte[] content=input.readNBytes(69);
        if(content.length>68) throw new IllegalArgumentException("Invalid operator metrics credential");
        controlToken=new String(content,java.nio.charset.StandardCharsets.US_ASCII).trim();
      } catch(java.io.IOException|java.nio.file.InvalidPathException unreadable) {
        throw new IllegalArgumentException("Operator metrics credential unreadable");
      }
      if(!controlToken.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("Invalid operator metrics credential");
    }
    var runtimeSamples=new RuntimeSamples(controlToken);
    int duration=Integer.parseInt(args[2]); if(duration<10 || duration>86400) throw new IllegalArgumentException("Duration must be 10..86400 seconds");
    long serverPid=Long.parseLong(args[5]);
    String stopFileSetting=System.getenv("T4C_SOAK_STOP_FILE");
    Path stopFile=stopFileSetting==null || stopFileSetting.isBlank()?null:Path.of(stopFileSetting);
    // Use the server's trusted hosted graph and exact portal-source collision exceptions.
    // Never turn server-provided region keys into filesystem paths.
    var hosted=HostedRegionPlan.startingSlice(Path.of(args[3]));
    var maps=new HashMap<String,HostedRegionPlan.Region>();
    for(var region:hosted.regions()) maps.put(region.spec().key(),region);
    List<Player> players=new ArrayList<>();
    try(var files=Files.list(profiles)) {
      for(var file:files.filter(p -> p.getFileName().toString().matches("player-[0-9]{2}\\.json")).sorted().toList()) {
        var profile=JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        players.add(new Player(new OnlineConnection.Credentials(profile.get("secret").getAsString(),UUID.fromString(profile.get("characterId").getAsString()))));
      }
    }
    if(players.size()!=20 || players.stream().map(p -> p.credentials.characterId()).distinct().count()!=20)
      throw new IllegalArgumentException("Exactly twenty distinct player-NN.json QA profiles are required");
    // Optional paced portal walking (off unless T4C_SOAK_PORTAL_WALKERS is 1..20): that many evenly spread adapters
    // periodically walk an authored portal, dwell in the destination, optionally go deeper, and walk back.
    int walkers=envInt("T4C_SOAK_PORTAL_WALKERS",0,0,20);
    long tripIntervalMillis=envInt("T4C_SOAK_PORTAL_INTERVAL_SECONDS",60,5,3600)*1000L;
    long tripDwellMillis=envInt("T4C_SOAK_PORTAL_DWELL_SECONDS",20,1,600)*1000L;
    long tripLegTimeoutMillis=envInt("T4C_SOAK_PORTAL_LEG_TIMEOUT_SECONDS",DEFAULT_LEG_TIMEOUT_SECONDS,30,900)*1000L;
    // Optional steadier mode: seconds between each adapter's scheduled reconnects (default 120 = the original churn).
    reconnectSeconds=envInt("T4C_SOAK_RECONNECT_INTERVAL_SECONDS",120,30,86400);
    long reconnectMillis=reconnectSeconds*1000L;
    List<PortalRoute> routes=List.of();
    if(walkers>0) {
      try(var in=NativeAdapterSoak.class.getResourceAsStream("/content/portals-v1.json")) {
        if(in==null) throw new IllegalArgumentException("Portal content unavailable");
        var content=new Gson().fromJson(new String(in.readNBytes(4*1024*1024),java.nio.charset.StandardCharsets.UTF_8),com.perso.T4C.online.model.PortalContent.class);
        routes=portalRoutes(hosted.regions(),content.portals());
      }
      if(routes.isEmpty()) throw new IllegalArgumentException("No enabled hosted portal route to walk");
    }
    Files.createDirectories(report.toAbsolutePath().getParent());
    if(Files.exists(report)) throw new IllegalArgumentException("Report already exists");
    long start=ms(),end=start+duration*1000L,lastSample=start,nextReport=start;
    if(walkers>0) {
      for(int i=0;i<players.size();i++) if(isPortalWalker(i,players.size(),walkers))
        players.get(i).trip=new PortalTrip(i,routes,start+30_000+i*4_000L,tripIntervalMillis,tripDwellMillis,tripLegTimeoutMillis);
      portalConfig=new LinkedHashMap<>();
      portalConfig.put("walkers",walkers); portalConfig.put("intervalSeconds",tripIntervalMillis/1000);
      portalConfig.put("dwellSeconds",tripDwellMillis/1000); portalConfig.put("legTimeoutSeconds",tripLegTimeoutMillis/1000);
      portalConfig.put("routes",routes.stream().map(r -> r.id()+":"+r.fromKey()+"->"+r.toKey()).toList());
    }
    long[] connectedMillis=new long[21];
    long firstTick=-1,lastTick=-1,firstTickTime=0,lastTickTime=0,healthFailures=0,healthSamples=0,healthNanos=0;
    int maxObserved=0;
    Instant startedAt=Instant.now();
    long cpuStart=cpu(serverPid);
    Map<String,Object> databaseBefore=database();
    boolean completed=false;
    // Adapters open one at a time through the gate (initial opens and reconnects alike), see OpenGate.
    var gate=new OpenGate(MAX_JOINING,OPEN_GAP_MILLIS);
    try(var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()) {
      runtimeSamples.sample(http,endpoint);
      while(ms()<end && (stopFile==null || !Files.exists(stopFile))) {
        long now=ms(); int connected=0;
        int joining=0;
        for(var q:players)
          if(q.connection!=null && !q.expectedReconnect && !q.connectedSinceOpen && !q.connection.status().equals("connected")
              && !q.connection.status().equals("disconnected")) joining++;
        for(int i=0;i<players.size();i++) {
          Player p=players.get(i);
          if(p.connection==null) {
            // Never opened yet: open through the gate, in profile order.
            if(gate.mayOpen(joining,now)) {
              p.connection=new OnlineConnection(endpoint,p.credentials); gate.opened(now); joining++;
              p.openedAt=now; p.connectedSinceOpen=false; p.lastFresh=now; p.reconnectAt=now+reconnectMillis+i*6_000L;
            }
            continue;
          }
          if(p.command!=null && p.command.isDone()) {
            long elapsed=now-p.commandStarted;
            int bucket=0; long[] limits={50,100,250,500,1000,2000,5000};
            while(bucket<limits.length && elapsed>limits[bucket]) bucket++;
            p.latencyBuckets[bucket]++;
            try { if(p.command.join().accepted()) p.accepted++; else p.rejected++; }
            catch(RuntimeException failed) { error(p,"gameplay_result_unavailable"); }
            p.command=null;
          }
          if(p.expectedReconnect) {
            if(now-p.reconnectStarted<3000 || !gate.mayOpen(joining,now)) continue;
            p.connection=new OnlineConnection(endpoint,p.credentials); p.expectedReconnect=false; gate.opened(now); joining++;
            p.lastTick=-1; p.lastFresh=now; p.reconnectAt=now+reconnectMillis; p.reconnects++;
            p.openedAt=now; p.connectedSinceOpen=false;
            continue;
          }
          var view=p.connection.view();
          if(!p.connection.status().equals("connected") || view==null) {
            if(p.connection.status().equals("disconnected")) {
              // The adapter's worker ended (for example after its bounded busy retries ran out): reopen soon.
              error(p,p.connectedSinceOpen?"adapter_stopped_after_connect":"adapter_stopped_before_connect");
              p.connection.close(); p.expectedReconnect=true; p.reconnectStarted=now;
            } else if(now-p.lastFresh>15_000) {
              error(p,"connection_or_reconnect_timeout");
              p.connection.close(); p.expectedReconnect=true; p.reconnectStarted=now+30_000;
            }
            continue;
          }
          if(!p.connectedSinceOpen) {
            p.connectedSinceOpen=true;
            if(p.firstConnectedMillis<0) p.firstConnectedMillis=now-p.openedAt;
          }
          connected++; p.observations++;
          var snapshot=view.snapshot(); maxObserved=Math.max(maxObserved,snapshot.players().size());
          var region=view.map().region();
          var loaded=maps.get(region.key());
          if(loaded==null || loaded.spec().world()!=region.world() || loaded.map().originX!=region.originX()
              || loaded.map().originY!=region.originY() || loaded.map().width!=region.width() || loaded.map().height!=region.height()
              || !region.equals(snapshot.region())) {
            p.connection.stopInput(); error(p,"unknown_or_mismatched_region"); continue;
          }
          PreviewMap map=loaded.map();
          if(p.locationEpoch==region.locationEpoch() && (p.world!=region.world() || !Objects.equals(p.regionKey,region.key()))) {
            p.connection.stopInput(); error(p,"region_changed_without_epoch"); continue;
          }
          if(region.locationEpoch()!=p.locationEpoch) {
            if(p.locationEpoch>=0) {
              if(region.locationEpoch()<p.locationEpoch) error(p,"location_epoch_regressed");
              p.epochChanges++;
              if(p.world!=region.world()) p.worldTransfers++;
              if(!Objects.equals(p.regionKey,region.key())) p.regionTransfers++;
              p.lastDx=0; p.lastDy=0;
              if(p.trip!=null) p.trip.onRegion(now,region.key());
            }
            p.connection.stopInput(); p.lastTick=-1; p.lastFresh=now; p.x=Double.NaN;
            p.locationEpoch=region.locationEpoch(); p.world=region.world(); p.regionKey=region.key();
          }
          // Headless equivalent of preparing the matching rendered map before acknowledgement.
          p.connection.confirmRegionReady(region.locationEpoch());
          if(snapshot.tick()<p.lastTick) error(p,"tick_regressed");
          if(snapshot.tick()>p.lastTick) p.lastFresh=now;
          else if(now-p.lastFresh>5000) { error(p,"snapshot_stalled"); p.lastFresh=now; }
          p.lastTick=snapshot.tick();
          if(view.character()==null || !view.character().id().equals(p.credentials.characterId())) { error(p,"owner_identity"); continue; }
          if(view.character().locationEpoch()!=region.locationEpoch()) { error(p,"owner_region_epoch_mismatch"); continue; }
          if(view.character().revision()<p.lastRevision) error(p,"character_revision_regressed");
          p.lastRevision=view.character().revision();
          var self=snapshot.players().stream().filter(a -> a.id()==view.ownId()).findFirst().orElseThrow();
          // The server steps players with single-player's grid rule: positions are tile centres or points on a straight glide
          // between two adjacent tile centres (diagonals too) that the rule lets a player rest on. canStand's stricter
          // above-foot fold is not that rule (T4C-0314); this check still refuses anything off the lattice or in a wall.
          if(!map.onGridPath(self.x(),self.y())) {
            error(p,"position_not_walkable");
            if(p.errorDetails.size()<5) p.errorDetails.add("position_not_walkable@"+region.key()+" epoch "+region.locationEpoch()
                +" abs "+(map.originX+self.x())+","+(map.originY+self.y())+" last "+p.lastDx+","+p.lastDy
                +(p.trip!=null?" trip "+p.trip.phase:""));
          }
          if(p.trip!=null) p.trip.where=region.key()+" abs "+(map.originX+self.x())+","+(map.originY+self.y());
          if(self.hp()<0 || self.hp()>self.maxHp()) error(p,"invalid_health");
          if(!Double.isNaN(p.x) && Math.hypot(p.x-self.x(),p.y-self.y())>.01) p.moved++;
          if(p.hp>=0 && p.hp!=self.hp()) p.hpChanges++;
          p.x=self.x(); p.y=self.y(); p.hp=self.hp();
          if(now>=p.reconnectAt && p.command==null) {
            p.connection.close(); p.expectedReconnect=true; p.reconnectStarted=now; continue;
          }
          if(p.command!=null) continue;
          if(p.trip!=null) {
            p.trip.tick(now,region.key());
            if(p.trip.walking()) {
              // Same arriving-tile planning as the combat walker, but to the exact authored portal source tile.
              var goal=p.trip.route;
              int goalX=goal.sourceX()-map.originX,goalY=goal.sourceY()-map.originY;
              int arrivingX=arrivingTile(self.x(),p.lastDx),arrivingY=arrivingTile(self.y(),p.lastDy);
              int[] next=pathToTile(map,arrivingX,arrivingY,goalX,goalY);
              if(next==null) {
                p.connection.stopInput();
                if(arrivingX!=goalX || arrivingY!=goalY) p.trip.notes.add("portal_"+goal.id()+"_unreachable_from_"+arrivingX+","+arrivingY);
              } else {
                p.lastDx=Integer.signum(next[0]-arrivingX); p.lastDy=Integer.signum(next[1]-arrivingY);
                p.connection.direction(p.lastDx,p.lastDy);
              }
              continue;
            }
          }
          OnlineConnection.Corpse corpse=view.corpses().stream()
              .filter(c -> c.world()==region.world() && c.x()>=region.originX() && c.y()>=region.originY()
                  && c.x()<region.originX()+region.width() && c.y()<region.originY()+region.height())
              .findFirst().orElse(null);
          var creatures=snapshot.creatures();
          var target=creatures.isEmpty()?null:creatures.get((i+(int)((now-start)/30_000))%creatures.size());
          double tx=corpse!=null?corpse.x()-map.originX:target!=null?target.x():map.spawnX;
          double ty=corpse!=null?corpse.y()-map.originY:target!=null?target.y():map.spawnY;
          boolean reachable=distance(self.x(),self.y(),tx,ty)<48 && map.allowsCombat(self.x(),self.y(),tx,ty,false);
          if(reachable) {
            p.connection.stopInput();
            if(now>=p.nextCommand && (corpse!=null || target!=null)) {
              try {
                p.commandStarted=now;
                p.command=(corpse!=null?p.connection.recover(corpse.id()):p.connection.attack(target.id())).toCompletableFuture();
                if(corpse!=null) p.recoveries++; else p.attacks++;
              } catch(IllegalStateException unavailable) { error(p,"command_not_ready"); }
              p.nextCommand=now+1100+(i*37)%300;
            }
          } else {
            // The server finishes a claimed tile (single-player's grid reservation rule): route from the tile
            // the player is ARRIVING on and steer relative to that tile, not to the mid-glide position.
            int arrivingX=arrivingTile(self.x(),p.lastDx),arrivingY=arrivingTile(self.y(),p.lastDy);
            double[] next=path(map,arrivingX+.5,arrivingY+.5,tx,ty);
            if(next==null) p.connection.stopInput();
            else {
              p.lastDx=direction(next[0]-(arrivingX+.5)); p.lastDy=direction(next[1]-(arrivingY+.5));
              p.connection.direction(p.lastDx,p.lastDy);
            }
          }
        }
        connectedMillis[connected]+=now-lastSample; lastSample=now;
        if(now>=nextReport) {
          runtimeSamples.sample(http,endpoint);
          try {
            long before=System.nanoTime();
            var response=http.send(HttpRequest.newBuilder(endpoint.resolve("/health")).timeout(Duration.ofSeconds(2)).GET().build(),HttpResponse.BodyHandlers.ofString());
            healthNanos+=System.nanoTime()-before; healthSamples++;
            if(response.statusCode()!=200) throw new IllegalStateException();
            long tick=JsonParser.parseString(response.body()).getAsJsonObject().get("tick").getAsLong();
            if(firstTick<0) { firstTick=tick; firstTickTime=now; }
            lastTick=tick; lastTickTime=now;
          } catch(Exception unavailable) { healthFailures++; }
          write(report,players,startedAt,start,connectedMillis,maxObserved,firstTick,lastTick,firstTickTime,lastTickTime,
              healthFailures,healthSamples,healthNanos,cpuStart,cpu(serverPid),databaseBefore,Map.of(),runtimeSamples,false);
          nextReport=now+5000;
        }
        Thread.sleep(100);
      }
      completed=ms()>=end;
    } finally {
      for(var p:players) if(p.connection!=null) p.connection.close();
      write(report,players,startedAt,start,connectedMillis,maxObserved,firstTick,lastTick,firstTickTime,lastTickTime,
          healthFailures,healthSamples,healthNanos,cpuStart,cpu(serverPid),databaseBefore,database(),runtimeSamples,completed);
    }
    System.out.println("Native network-adapter soak finished; report: "+report.toAbsolutePath());
  }
  /** Tile a glide is arriving on (the last direction sent while off-centre); mirrors HostedTransferTest.walkPortal. */
  static int arrivingTile(double c,int last) {
    int tile=(int)Math.round(c-.5);
    if(Math.abs(c-Math.floor(c)-.5)>1e-6) tile=last>0 ? (int)Math.ceil(c-.5) : last<0 ? (int)Math.floor(c-.5) : tile;
    return tile;
  }
  static int direction(double delta) { return Math.abs(delta)<.12?0:delta>0?1:-1; }
  static double distance(double x,double y,double tx,double ty) {
    return Math.hypot((tx-x)*com.perso.T4C.config.GameConstants.GRID_W,(ty-y)*com.perso.T4C.config.GameConstants.GRID_H);
  }
  static double[] path(PreviewMap map,double x,double y,double tx,double ty) {
    // Cardinal breadth-first routing uses the same collision map. The server still decides movement.
    int start=(int)y*map.width+(int)x; int[] parent=new int[map.width*map.height]; Arrays.fill(parent,-1);
    int[] queue=new int[parent.length]; int head=0,tail=0; queue[tail++]=start; parent[start]=start;
    while(head<tail) {
      int cell=queue[head++],cx=cell%map.width,cy=cell/map.width;
      if(distance(cx+.5,cy+.5,tx,ty)<40 && map.allowsCombat(cx+.5,cy+.5,tx,ty,false)) {
        if(cell==start) return new double[]{cx+.5,cy+.5};
        while(parent[cell]!=start) cell=parent[cell];
        return new double[]{cell%map.width+.5,cell/map.width+.5};
      }
      for(int[] d:DIRECTIONS) {
        int nx=cx+d[0],ny=cy+d[1],id=ny*map.width+nx;
        if(nx<0 || ny<0 || nx>=map.width || ny>=map.height || parent[id]>=0 || !map.canStand(nx+.5,ny+.5)) continue;
        parent[id]=cell; queue[tail++]=id;
      }
    }
    return null;
  }
  static long cpu(long pid) {
    if(pid<=0) return -1;
    return ProcessHandle.of(pid).flatMap(p -> p.info().totalCpuDuration()).map(Duration::toNanos).orElse(-1L);
  }
  static Map<String,Object> database() {
    String url=System.getenv("T4C_DB_URL"); if(url==null) return Map.of("available",false);
    try(var c=java.sql.DriverManager.getConnection(url,System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"));
        var q=c.createStatement()) {
      q.setQueryTimeout(2);
      try(var r=q.executeQuery("SELECT pg_database_size(current_database()),numbackends,xact_commit,xact_rollback,deadlocks FROM pg_stat_database WHERE datname=current_database()")) {
        r.next(); return Map.of("available",true,"bytes",r.getLong(1),"connections",r.getLong(2),"commits",r.getLong(3),"rollbacks",r.getLong(4),"deadlocks",r.getLong(5));
      }
    } catch(Exception unavailable) { return Map.of("available",false); }
  }
  static void write(Path file,List<Player> players,Instant started,long start,long[] connected,int maxObserved,
      long first,long last,long firstTime,long lastTime,long failures,long samples,long nanos,long cpuStart,long cpuEnd,
      Map<String,Object> dbBefore,Map<String,Object> dbAfter,RuntimeSamples runtimeSamples,boolean finished) throws Exception {
    var report=new LinkedHashMap<String,Object>();
    report.put("kind","20 actual native OnlineConnection adapters; no rendered clients");
    report.put("protocolVersion",com.perso.T4C.online.model.OnlineProtocol.VERSION);
    report.put("startedAt",started.toString()); report.put("finished",finished); report.put("elapsedMillis",ms()-start);
    report.put("connectedMillisByCount",connected); report.put("maximumSnapshotPlayers",maxObserved);
    report.put("healthFailures",failures); report.put("healthMeanMillis",samples==0?0:nanos/1e6/samples);
    report.put("observedTickRate",lastTime>firstTime?(last-first)*1000.0/(lastTime-firstTime):0);
    report.put("tickDeficitProxy",lastTime>firstTime?Math.max(0,(lastTime-firstTime)*34.0/1000-(last-first)):0);
    report.put("serverCpuNanos",cpuStart>=0 && cpuEnd>=cpuStart?cpuEnd-cpuStart:-1);
    report.put("reconnectIntervalSeconds",reconnectSeconds);
    report.put("openPacing",Map.of("maxConcurrentJoins",MAX_JOINING,"minGapMillis",OPEN_GAP_MILLIS));
    report.put("databaseBefore",dbBefore); report.put("databaseAfter",dbAfter);
    report.put("serverRuntime",runtimeSamples.report());
    report.put("latencyBucketsMillis",List.of("<=50","<=100","<=250","<=500","<=1000","<=2000","<=5000",">5000"));
    var results=new ArrayList<Map<String,Object>>();
    for(int i=0;i<players.size();i++) {
      var p=players.get(i); var row=new LinkedHashMap<String,Object>(); row.put("player",i+1);
      row.put("attacks",p.attacks); row.put("recoveries",p.recoveries); row.put("accepted",p.accepted); row.put("rejected",p.rejected);
      row.put("failures",p.failures); row.put("errors",p.errors); row.put("errorDetails",p.errorDetails); row.put("reconnects",p.reconnects);
      row.put("locationEpochChanges",p.epochChanges); row.put("worldTransfers",p.worldTransfers);
      row.put("regionTransfers",p.regionTransfers);
      if(p.trip!=null) {
        var trip=new LinkedHashMap<String,Object>();
        trip.put("started",p.trip.started); trip.put("completed",p.trip.completed); trip.put("aborted",p.trip.aborted);
        trip.put("timeouts",p.trip.timeouts); trip.put("outbound",p.trip.outbound); trip.put("inbound",p.trip.inbound);
        trip.put("phase",p.trip.phase.toString()); trip.put("notes",p.trip.notes);
        row.put("portalTrip",trip);
      }
      row.put("movementObservations",p.moved); row.put("hpChanges",p.hpChanges); row.put("latencyBuckets",p.latencyBuckets); results.add(row);
      row.put("commandPendingAtReport",p.command!=null);
      row.put("firstConnectedMillis",p.firstConnectedMillis);
    }
    if(portalConfig!=null) {
      var totals=new LinkedHashMap<String,Object>(portalConfig);
      long tStarted=0,tCompleted=0,tAborted=0,tTimeouts=0,tOutbound=0,tInbound=0,tRegion=0;
      for(var p:players) {
        tRegion+=p.regionTransfers;
        if(p.trip==null) continue;
        tStarted+=p.trip.started; tCompleted+=p.trip.completed; tAborted+=p.trip.aborted; tTimeouts+=p.trip.timeouts;
        tOutbound+=p.trip.outbound; tInbound+=p.trip.inbound;
      }
      totals.put("tripsStarted",tStarted); totals.put("tripsCompleted",tCompleted); totals.put("tripsAborted",tAborted);
      totals.put("legTimeouts",tTimeouts); totals.put("outboundTransfers",tOutbound); totals.put("inboundTransfers",tInbound);
      totals.put("regionTransfersAllAdapters",tRegion);
      report.put("portalWalk",totals);
    }
    report.put("players",results);
    Path temporary=file.resolveSibling(file.getFileName()+".tmp");
    Files.writeString(temporary,JSON.toJson(report),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
    replaceReport(temporary,file);
  }

  /** Windows can briefly deny replacing a report that another process has open without delete sharing. */
  static void replaceReport(Path temporary,Path file) throws IOException {
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(2);
    while(true) {
      try {
        try { Files.move(temporary,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
        catch(AtomicMoveNotSupportedException unsupported) {
          Files.move(temporary,file,StandardCopyOption.REPLACE_EXISTING);
        }
        return;
      } catch(AccessDeniedException denied) {
        // Other access failures (including the temporary file or its directory) are not report-reader locks.
        boolean targetMove=(temporary.toString().equals(denied.getFile())
            && file.toString().equals(denied.getOtherFile()))
            || (file.toString().equals(denied.getFile()) && denied.getOtherFile()==null);
        long remaining=deadline-System.nanoTime();
        if(!targetMove || remaining<=0) throw denied;
        try { TimeUnit.NANOSECONDS.sleep(Math.min(remaining,TimeUnit.MILLISECONDS.toNanos(25))); }
        catch(InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          var failure=new IOException("Interrupted while replacing soak report",interrupted);
          failure.addSuppressed(denied);
          throw failure;
        }
      }
    }
  }
}
