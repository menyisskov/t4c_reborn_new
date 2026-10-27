import com.google.gson.*;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.server.PreviewMap;
import com.perso.T4C.server.HostedRegionPlan;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/** Standalone QA driver using the desktop client's real network adapter, without a renderer. */
class NativeAdapterSoak {
  static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
  static final int[][] DIRECTIONS={{1,0},{-1,0},{0,1},{0,-1}};
  static final class Player {
    final OnlineConnection.Credentials credentials;
    OnlineConnection connection;
    CompletableFuture<OnlineConnection.CommandResult> command;
    long commandStarted,nextCommand,reconnectAt,reconnectStarted,lastTick=-1,lastRevision=-1,lastFresh;
    long attacks,recoveries,accepted,rejected,failures,reconnects,observations,moved,hpChanges;
    long locationEpoch=-1,epochChanges,worldTransfers;
    int world=-1;
    String regionKey;
    double x=Double.NaN,y; int hp=-1;
    boolean expectedReconnect;
    final long[] latencyBuckets=new long[8];
    final Set<String> errors=new TreeSet<>();
    Player(OnlineConnection.Credentials c) { credentials=c; }
  }
  static long ms() { return System.nanoTime()/1_000_000; }
  static void error(Player p,String text) { p.failures++; p.errors.add(text); }
  public static void main(String[] args) throws Exception {
    if(args.length!=6) throw new IllegalArgumentException("Expected endpoint profiles-directory seconds collision-file report-file server-pid-or-0");
    URI endpoint=URI.create(args[0]); Path profiles=Path.of(args[1]),report=Path.of(args[4]);
    int duration=Integer.parseInt(args[2]); if(duration<10 || duration>86400) throw new IllegalArgumentException("Duration must be 10..86400 seconds");
    long serverPid=Long.parseLong(args[5]);
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
    Files.createDirectories(report.toAbsolutePath().getParent());
    if(Files.exists(report)) throw new IllegalArgumentException("Report already exists");
    long start=ms(),end=start+duration*1000L,lastSample=start,nextReport=start;
    long[] connectedMillis=new long[21];
    long firstTick=-1,lastTick=-1,firstTickTime=0,lastTickTime=0,healthFailures=0,healthSamples=0,healthNanos=0;
    int maxObserved=0;
    Instant startedAt=Instant.now();
    long cpuStart=cpu(serverPid);
    Map<String,Object> databaseBefore=database();
    boolean completed=false;
    for(int i=0;i<players.size();i++) {
      Player p=players.get(i); p.connection=new OnlineConnection(endpoint,p.credentials);
      p.reconnectAt=start+120_000+i*6_000L; p.lastFresh=start;
    }
    try(var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()) {
      while(ms()<end) {
        long now=ms(); int connected=0;
        for(int i=0;i<players.size();i++) {
          Player p=players.get(i);
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
            if(now-p.reconnectStarted<3000) continue;
            p.connection=new OnlineConnection(endpoint,p.credentials); p.expectedReconnect=false;
            p.lastTick=-1; p.lastFresh=now; p.reconnectAt=now+120_000; p.reconnects++;
          }
          var view=p.connection.view();
          if(!p.connection.status().equals("connected") || view==null) {
            if(now-p.lastFresh>15_000) {
              error(p,"connection_or_reconnect_timeout");
              p.connection.close(); p.expectedReconnect=true; p.reconnectStarted=now+30_000;
            }
            continue;
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
          if(!map.canStand(self.x(),self.y())) error(p,"position_not_walkable");
          if(self.hp()<0 || self.hp()>self.maxHp()) error(p,"invalid_health");
          if(!Double.isNaN(p.x) && Math.hypot(p.x-self.x(),p.y-self.y())>.01) p.moved++;
          if(p.hp>=0 && p.hp!=self.hp()) p.hpChanges++;
          p.x=self.x(); p.y=self.y(); p.hp=self.hp();
          if(now>=p.reconnectAt && p.command==null) {
            p.connection.close(); p.expectedReconnect=true; p.reconnectStarted=now; continue;
          }
          if(p.command!=null) continue;
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
            double[] next=path(map,self.x(),self.y(),tx,ty);
            if(next==null) p.connection.stopInput();
            else p.connection.direction(direction(next[0]-self.x()),direction(next[1]-self.y()));
          }
        }
        connectedMillis[connected]+=now-lastSample; lastSample=now;
        if(now>=nextReport) {
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
              healthFailures,healthSamples,healthNanos,cpuStart,cpu(serverPid),databaseBefore,Map.of(),false);
          nextReport=now+5000;
        }
        Thread.sleep(100);
      }
      completed=true;
    } finally {
      for(var p:players) p.connection.close();
      write(report,players,startedAt,start,connectedMillis,maxObserved,firstTick,lastTick,firstTickTime,lastTickTime,
          healthFailures,healthSamples,healthNanos,cpuStart,cpu(serverPid),databaseBefore,database(),completed);
    }
    System.out.println("Native network-adapter soak finished; report: "+report.toAbsolutePath());
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
      Map<String,Object> dbBefore,Map<String,Object> dbAfter,boolean finished) throws Exception {
    var report=new LinkedHashMap<String,Object>();
    report.put("kind","20 actual native OnlineConnection adapters; no rendered clients");
    report.put("protocolVersion",com.perso.T4C.online.model.OnlineProtocol.VERSION);
    report.put("startedAt",started.toString()); report.put("finished",finished); report.put("elapsedMillis",ms()-start);
    report.put("connectedMillisByCount",connected); report.put("maximumSnapshotPlayers",maxObserved);
    report.put("healthFailures",failures); report.put("healthMeanMillis",samples==0?0:nanos/1e6/samples);
    report.put("observedTickRate",lastTime>firstTime?(last-first)*1000.0/(lastTime-firstTime):0);
    report.put("tickDeficitProxy",lastTime>firstTime?Math.max(0,(lastTime-firstTime)*34.0/1000-(last-first)):0);
    report.put("serverCpuNanos",cpuStart>=0 && cpuEnd>=cpuStart?cpuEnd-cpuStart:-1);
    report.put("databaseBefore",dbBefore); report.put("databaseAfter",dbAfter);
    report.put("latencyBucketsMillis",List.of("<=50","<=100","<=250","<=500","<=1000","<=2000","<=5000",">5000"));
    var results=new ArrayList<Map<String,Object>>();
    for(int i=0;i<players.size();i++) {
      var p=players.get(i); var row=new LinkedHashMap<String,Object>(); row.put("player",i+1);
      row.put("attacks",p.attacks); row.put("recoveries",p.recoveries); row.put("accepted",p.accepted); row.put("rejected",p.rejected);
      row.put("failures",p.failures); row.put("errors",p.errors); row.put("reconnects",p.reconnects);
      row.put("locationEpochChanges",p.epochChanges); row.put("worldTransfers",p.worldTransfers);
      row.put("movementObservations",p.moved); row.put("hpChanges",p.hpChanges); row.put("latencyBuckets",p.latencyBuckets); results.add(row);
      row.put("commandPendingAtReport",p.command!=null);
    }
    report.put("players",results);
    Path temporary=file.resolveSibling(file.getFileName()+".tmp");
    Files.writeString(temporary,JSON.toJson(report),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
    try { Files.move(temporary,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
    catch(AtomicMoveNotSupportedException unsupported) { Files.move(temporary,file,StandardCopyOption.REPLACE_EXISTING); }
  }
}
