package com.perso.T4C.server;

import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.online.model.MonsterContent;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

/** Simulation-owned creature lives, with one bounded persistence worker per region. */
final class PersistentCreatures implements AutoCloseable {
  private static final long CHECKPOINT_TICKS=PreviewWorld.TICK_RATE*5L;
  private static final class Entry {
    final PreviewWorld.CreatureSpawn spawn;
    OnlineStore.SpawnState saved;
    OnlineMonsterPursuit controller;
    boolean pending,recovering,reserved,reloadAfterPending,dirty,needsReplan,attackReady;
    /** Streamed regions: a database row may exist for this life (it was read, or something wrote or targeted it). */
    boolean maybeRow;
    /** Streamed regions: bumped whenever a write that needs the row is prepared, so a stale pristine delete is ignored. */
    long touch;
    /** Streamed regions: left every window; frozen while its last state is flushed. */
    boolean settling;
    int deleteFailures;
    long deleteRetryTick;
    long stunnedUntilMillis;
    long lastCheckpointTick;
    long lastRecoveryAttemptNanos;
    long cacheGeneration;
    Entry(PreviewWorld.CreatureSpawn spawn,OnlineStore.SpawnState saved) { this.spawn=spawn;this.saved=saved; }
  }
  private record Completion(long id,OnlineStore.SpawnState saved,boolean checkpoint,boolean persisted) {
    Completion(long id,OnlineStore.SpawnState saved,boolean checkpoint) { this(id,saved,checkpoint,saved!=null); }
  }
  private record ActivationResult(List<Integer> indices,Map<String,OnlineStore.SpawnState> rows) {}
  private record DeleteCompletion(long id,long cacheGeneration,long touch,boolean deleted) {}
  /** Streamed lifecycle states of one hosted spawn (WORLD_STREAMING_DESIGN.md 3.4); everything else in this class is the active state. */
  static final byte DORMANT=0,QUEUED=1,LOADING=2,ACTIVE=3,SETTLING=4;
  /** At most this many dormant creatures are read and built per tick, so a teleport into a dense block cannot stall the tick. */
  static final int DEFAULT_ACTIVATION_BATCH=64;
  /** Guard rail on live creatures in one streamed region. */
  static final int DEFAULT_MAX_ACTIVE=1024;
  /** Coverage is recomputed from player positions this often (4.25 Hz); the settle scan runs once a second. */
  static final int COVERAGE_TICKS=8;
  record Stats(int active,int loading,int queued,int settling,long activated,long deactivated,long pristineRowsDeleted,
      long capRefusals,long failedActivations,long batches,long maxBatchNanos,int coveredCells,int maxActiveSeen) {}
  private record PairCompletion(long sourceId,long targetId,long sourceGeneration,long targetGeneration,
      OnlineStore.ClanAttackResult result) {}
  private final OnlineStore store;
  private final PreviewWorld world;
  private final PreviewMap map;
  private final String region;
  private final PersistentSessions sessions;
  private final com.perso.T4C.online.model.ItemContent items;
  private final MonsterContent.Classified classified;
  private final Map<Long,Entry> entries=new LinkedHashMap<>();
  private final ConcurrentLinkedQueue<Completion> completed=new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<PairCompletion> completedPairs=new ConcurrentLinkedQueue<>();
  private final ThreadPoolExecutor worker;
  private long tick;
  // Streamed region only (world.streamedCreatures()); all of it is owned by the simulation thread.
  private final boolean streamed;
  private final List<PreviewWorld.CreatureSpawn> hosted;
  private final CreatureCellIndex cellIndex;
  private final byte[] lifecycle;
  private final AreaOfInterest.Coverage coverage=new AreaOfInterest.Coverage();
  private final ArrayDeque<Integer> urgent=new ArrayDeque<>(),queued=new ArrayDeque<>();
  private final Set<Integer> failedIndices=new HashSet<>();
  private final ConcurrentLinkedQueue<ActivationResult> activations=new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<DeleteCompletion> deletions=new ConcurrentLinkedQueue<>();
  private int activationBatch=DEFAULT_ACTIVATION_BATCH,maxActive=DEFAULT_MAX_ACTIVE;
  private int loadingCount,queuedCount,maxActiveSeen;
  private long activatedTotal,deactivatedTotal,pristineDeleted,capRefusals,failedActivations,batchesTotal,retryAfterTick;
  private volatile long maxBatchNanos;
  private boolean closing;

  PersistentCreatures(OnlineStore store,PreviewWorld world) throws SQLException {
    this(store,world,world.map(),"single",null,null);
  }

  PersistentCreatures(OnlineStore store,PreviewWorld world,PreviewMap map,String region,
      PersistentSessions sessions,com.perso.T4C.online.model.ItemContent items) throws SQLException {
    this(store,world,map,region,sessions,items,null);
  }

  PersistentCreatures(OnlineStore store,PreviewWorld world,PreviewMap map,String region,
      PersistentSessions sessions,com.perso.T4C.online.model.ItemContent items,
      MonsterContent.Classified classified) throws SQLException {
    this.store=store;this.world=world;this.map=map;this.region=region;this.sessions=sessions;this.items=items;
    this.classified=classified;
    this.streamed=world.streamedCreatures();
    if(streamed) {
      // Exception-only persistence: no per-spawn round trip. The only startup read is the rows that exist,
      // that is, the creatures players disturbed; everything else is described by the content files.
      hosted=world.hostedSpawns();
      int n=hosted.size();var tx=new int[n];var ty=new int[n];
      var keyIndex=new HashMap<String,Integer>(n*2);
      for(int i=0;i<n;i++) {
        var s=hosted.get(i).spawn();
        tx[i]=(int)Math.floor(s.x()-map.originX);ty[i]=(int)Math.floor(s.y()-map.originY);
        keyIndex.put(s.persistenceKey(),i);
      }
      cellIndex=new CreatureCellIndex(tx,ty);lifecycle=new byte[n];
      for(var row:store.listSpawnPositions()) {
        Integer index=keyIndex.get(row.key());
        if(index!=null && row.x()!=null && row.y()!=null) cellIndex.displace(index,row.x()-map.originX,row.y()-map.originY);
      }
    } else {
      hosted=null;cellIndex=null;lifecycle=null;
      // Startup precedes the simulation and HTTP listener.
      for(var spawn:world.creatureSpawns()) {
        var entry=new Entry(spawn,store.loadOrCreateSpawn(spawn.spawn().persistenceKey()));
        entries.put(spawn.id(),entry);
        publish(entry,true);
      }
    }
    worker=new ThreadPoolExecutor(1,1,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(256),
        Thread.ofPlatform().name("creature-persistence-",0).factory(),new ThreadPoolExecutor.AbortPolicy());
  }

  private void publish(Entry entry,boolean reconstruct) {
    var saved=entry.saved;var spawn=entry.spawn;
    double x=saved.currentX()==null ? spawn.spawn().x() : saved.currentX();
    double y=saved.currentY()==null ? spawn.spawn().y() : saved.currentY();
    double localX=x-map.originX,localY=y-map.originY;
    if (!Double.isFinite(localX) || !Double.isFinite(localY) || map.blocksFoot((int)Math.floor(localX),(int)Math.floor(localY))) throw new IllegalStateException("Saved creature position is blocked: "+saved.key());
    world.creaturePosition(spawn.id(),localX,localY);
    world.creatureHealth(spawn.id(),saved.health());
    world.creatureActive(spawn.id(),saved.diedAt()==null);
    if (!reconstruct) return;
    if (saved.diedAt()!=null || items==null || spawn.spawn().stationary() || !OnlineRetaliation.supported(spawn.definition())
        || !OnlineLootDrafts.supports(spawn.definition(),items)
        || spawn.definition().getSourceEvents()!=null && !spawn.definition().getSourceEvents().isEmpty()) {
      entry.controller=null;entry.needsReplan=false;return;
    }
    var mode=OnlineMonsterPursuit.Mode.valueOf(saved.mode());
    long epoch=saved.targetEpoch()==null ? -1 : saved.targetEpoch();
    var authority=classified==null ? null : classified.authorities().get(spawn.definition().getName());
    var clan=authority==null ? MonsterContent.Clan.NEUTRAL : authority.clan();
    var enemies=classified==null ? Set.<MonsterContent.Clan>of() : classified.relations().stream()
        .filter(r->r.source()==clan).map(MonsterContent.ClanRelation::target)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
    entry.controller=new OnlineMonsterPursuit(map,spawn.spawn(),spawn.definition(),region,
        new OnlineMonsterPursuit.Seed(saved.eventId(),localX,localY,mode,saved.targetCharacter(),epoch,
            saved.targetSpawnKey(),saved.targetLife()),clan,enemies,
        authority!=null && authority.attackFrameEvidence()==MonsterContent.AttackFrameEvidence.FRAME_NAME_PRESENT);
    entry.needsReplan=false;
  }

  OnlineStore.SpawnState life(long id) {
    var entry=entries.get(id);
    if (entry==null || entry.pending || entry.recovering || entry.reserved)
      throw new PreviewWorld.Rejected(409,"creature_unavailable");
    return entry.saved;
  }

  OnlineStore.SpawnMotion capture(long id) {
    var entry=entries.get(id);life(id);
    entry.maybeRow=true;entry.touch++; // every write built from this pose may create the row
    var view=entry.controller==null ? null : entry.controller.current();
    var position=world.creaturePosition(id);
    UUID target=view==null ? entry.saved.targetCharacter() : view.target();
    Long epoch=view==null ? entry.saved.targetEpoch() : view.targetEpoch()<0 ? null : view.targetEpoch();
    String mode=view==null ? entry.saved.mode() : view.mode().name();
    String targetSpawn=view==null ? entry.saved.targetSpawnKey() : view.targetSpawnKey();
    UUID targetLife=view==null ? entry.saved.targetLife() : view.targetLife();
    return new OnlineStore.SpawnMotion(entry.saved.key(),entry.saved.eventId(),entry.saved.stateRevision(),
        map.originX+position.x(),map.originY+position.y(),mode,target,epoch,targetSpawn,targetLife);
  }

  void reserve(long id) { life(id);entries.get(id).reserved=true; }
  void release(long id) { var entry=entries.get(id);if(entry!=null) entry.reserved=false; }
  void rewardCommitted(long id) { reload(id,true); }
  void damageCommitted(long id) { reload(id,false); }
  boolean stunned(long id,long now) {
    var entry=entries.get(id);
    return entry!=null && entry.stunnedUntilMillis>now;
  }
  void stunCommitted(long id,long now,long durationMillis) {
    var entry=entries.get(id);
    if(entry!=null && durationMillis>0)
      entry.stunnedUntilMillis=Math.max(entry.stunnedUntilMillis,Math.addExact(now,durationMillis));
  }

  void characterDied(UUID character) {
    for(var entry:entries.values()) {
      var current=entry.controller==null ? null : entry.controller.current();
      if(character.equals(entry.saved.targetCharacter()) || current!=null && character.equals(current.target())
          || entry.pending || entry.recovering || entry.reserved) reload(entry.spawn.id(),false);
    }
  }

  private void reload(long id,boolean hide) {
    var entry=entries.get(id);if(entry==null) throw new IllegalArgumentException("Unknown creature");
    if(hide) world.creatureActive(id,false);
    entry.cacheGeneration++;
    entry.recovering=true;entry.dirty=false;entry.needsReplan=false;
    if(!entry.pending) submitRead(entry);else entry.reloadAfterPending=true;
  }

  /** IDs awaiting a bounded path solve. The host grants one globally per tick. */
  List<Long> pendingReplans() {
    var ids=new ArrayList<Long>();
    for(var entry:entries.values()) if(entry.needsReplan && !entry.pending && !entry.recovering
        && !entry.reserved && entry.saved.diedAt()==null) ids.add(entry.spawn.id());
    return ids;
  }

  void step(List<OnlineMonsterPursuit.TargetView> targets,Long permittedId) {
    drain();tick++;
    if(streamed) streamedTick();
    var clanTargets=clanViews();
    for(var entry:entries.values()) {
      entry.attackReady=false;
      if(entry.controller==null || entry.pending || entry.recovering || entry.reserved || entry.settling
          || entry.saved.diedAt()!=null || entry.stunnedUntilMillis>System.currentTimeMillis()) continue;
      var before=entry.controller.current();
      var decision=entry.controller.step(targets,clanTargets,1.0/PreviewWorld.TICK_RATE,
          permittedId!=null && permittedId==entry.spawn.id(),false);
      entry.needsReplan=decision.needsReplan();
      entry.attackReady=decision.attackReady() && decision.mode()==OnlineMonsterPursuit.Mode.CLAN;
      boolean positionChanged=Double.compare(before.x(),decision.x())!=0 || Double.compare(before.y(),decision.y())!=0;
      boolean transition=before.mode()!=decision.mode() || !Objects.equals(before.target(),decision.target())
          || before.targetEpoch()!=decision.targetEpoch()
          || !Objects.equals(before.targetSpawnKey(),decision.targetSpawnKey())
          || !Objects.equals(before.targetLife(),decision.targetLife());
      if(positionChanged) world.creaturePosition(entry.spawn.id(),decision.x(),decision.y());
      // Exception-only persistence: a creature that is only patrolling its home with nothing written for
      // it has nothing worth a row; position, target and mode are persisted once it deviates.
      boolean deviated=!streamed || entry.maybeRow || decision.target()!=null || decision.targetSpawnKey()!=null
          || decision.mode()!=OnlineMonsterPursuit.Mode.IDLE && decision.mode()!=OnlineMonsterPursuit.Mode.PATROL;
      if(deviated && (positionChanged || transition)) entry.dirty=true;
      if(entry.dirty && (transition || tick-entry.lastCheckpointTick>=CHECKPOINT_TICKS))
        submitCheckpoint(entry);
    }
    if(tick%PreviewWorld.TICK_RATE==0)
      for(var entry:entries.values()) if(!entry.pending && (entry.saved.diedAt()!=null || entry.recovering))
        submitRead(entry);
  }

  void step() { step(List.of(),null); }

  private List<OnlineMonsterPursuit.ClanView> clanViews() {
    if(classified==null) return List.of();
    var views=new ArrayList<OnlineMonsterPursuit.ClanView>();
    for(var entry:entries.values()) {
      if(entry.controller==null || entry.saved.diedAt()!=null) continue;
      var authority=classified.authorities().get(entry.spawn.definition().getName());
      if(authority==null) continue;
      var position=world.creaturePosition(entry.spawn.id());
      views.add(new OnlineMonsterPursuit.ClanView(entry.saved.key(),entry.saved.eventId(),
          entry.spawn.spawn().world(),region,position.x(),position.y(),true,authority.clan()));
    }
    return List.copyOf(views);
  }

  List<Long> readyClanAttackers() {
    var ids=new ArrayList<Long>();
    for(var entry:entries.values()) if(entry.attackReady && !entry.pending && !entry.recovering
        && !entry.reserved && entry.saved.diedAt()==null) ids.add(entry.spawn.id());
    return ids;
  }

  Long idForKey(String key) {
    for(var entry:entries.values()) if(entry.saved.key().equals(key)) return entry.spawn.id();
    return null;
  }

  boolean submitClanAttack(long sourceId,long targetId,OnlineStore.ClanAttack draft) {
    var source=entries.get(sourceId);var target=entries.get(targetId);
    if(source==null || target==null || source==target || source.pending || target.pending
        || source.recovering || target.recovering || source.reserved || target.reserved
        || source.saved.diedAt()!=null || target.saved.diedAt()!=null
        || !source.saved.key().equals(draft.source().key())
        || !target.saved.key().equals(draft.target().key())
        || !source.saved.eventId().equals(draft.source().life())
        || !target.saved.eventId().equals(draft.target().life())
        || source.saved.stateRevision()!=draft.source().revision()
        || target.saved.stateRevision()!=draft.target().revision()) return false;
    // Both are simulation-thread owned. Reserve in key order before worker admission.
    var first=source.saved.key().compareTo(target.saved.key())<0 ? source : target;
    var second=first==source ? target : source;
    first.reserved=true;second.reserved=true;
    first.pending=true;second.pending=true;
    long sourceGeneration=source.cacheGeneration,targetGeneration=target.cacheGeneration;
    try {
      worker.execute(() -> {
        OnlineStore.ClanAttackResult committed=null;
        try { committed=store.commitClanAttack(draft); }
        catch(SQLException|RuntimeException uncertain) { /* Reload both; never reroll. */ }
        finally { completedPairs.add(new PairCompletion(sourceId,targetId,
            sourceGeneration,targetGeneration,committed)); }
      });
      return true;
    } catch(RejectedExecutionException busy) {
      first.pending=false;second.pending=false;first.reserved=false;second.reserved=false;
      return false;
    }
  }

  // ---- Streamed regions: creature activation and settling (WORLD_STREAMING_DESIGN.md 3.4 and 10) ----

  static OnlineStore.SpawnState pristine(String key,UUID life) {
    return new OnlineStore.SpawnState(key,0,life,null,null,null,null,0,null,null,"IDLE",null,0);
  }

  boolean streamed() { return streamed; }
  void activationBatch(int perTick) { if(perTick<1) throw new IllegalArgumentException("Invalid batch");activationBatch=perTick; }
  void maxActive(int limit) { if(limit<1) throw new IllegalArgumentException("Invalid cap");maxActive=limit; }

  Stats stats() {
    int settling=0;
    for(var entry:entries.values()) if(entry.settling) settling++;
    return new Stats(entries.size(),loadingCount,queuedCount,settling,activatedTotal,deactivatedTotal,pristineDeleted,
        capRefusals,failedActivations,batchesTotal,maxBatchNanos,coverage.size(),maxActiveSeen);
  }

  /** Lifecycle state of a hosted spawn by its id (index + 1). */
  byte lifecycleOf(long id) { return lifecycle[Math.toIntExact(id-1)]; }

  private long coverageVersion;
  /** Changes whenever a cell becomes covered, so per-cell loot is read again for it. */
  long coverageVersion() { return coverageVersion; }

  /** The covered cells as absolute-tile rectangles, runs of neighbouring cells merged, for the per-cell loot read. */
  List<OnlineStore.TileRect> coveredRects() {
    var rects=new ArrayList<OnlineStore.TileRect>();
    int runStart=0,runEnd=-1,runRow=Integer.MIN_VALUE;
    for(long key:coverage.keys()) { // ascending: row-major
      int cx=AreaOfInterest.cellX(key),cy=AreaOfInterest.cellY(key);
      if(cy==runRow && cx==runEnd+1) { runEnd=cx;continue; }
      if(runRow!=Integer.MIN_VALUE) rects.add(rect(runStart,runEnd,runRow));
      runStart=cx;runEnd=cx;runRow=cy;
    }
    if(runRow!=Integer.MIN_VALUE) rects.add(rect(runStart,runEnd,runRow));
    return rects;
  }

  private OnlineStore.TileRect rect(int fromCellX,int toCellX,int cellY) {
    int c=AreaOfInterest.CELL;
    return new OnlineStore.TileRect(map.originX+(double)fromCellX*c,map.originY+(double)cellY*c,
        map.originX+(double)(toCellX+1)*c,map.originY+(double)(cellY+1)*c);
  }

  private void streamedTick() {
    if(closing) return;
    if(tick%COVERAGE_TICKS==0) updateCoverage(world.simulationWindows());
    dispatchActivations();
    if(tick%PreviewWorld.TICK_RATE==17) settleScan();
  }

  private void updateCoverage(List<AreaOfInterest.Window> windows) {
    var fresh=new LinkedHashSet<Long>();
    for(var window:windows) for(long key:AreaOfInterest.cellsCovering(window)) if(!coverage.covered(key)) fresh.add(key);
    coverage.update(tick,windows);
    if(!fresh.isEmpty()) coverageVersion++;
    for(long key:fresh) for(int index:cellIndex.spawnsIn(key)) enqueue(index,false);
  }

  /**
   * An arrival (join, portal, respawn, recall) or any teleport: the window around the committed position
   * is covered at once, the snapshot window's creatures go to the front of the queue, and the rest of the
   * simulation window follows. The caller holds the arrival until {@link #windowActive} (section 10).
   */
  void requestWindow(double x,double y) {
    if(!streamed || closing) return;
    var simulation=AreaOfInterest.window(x,y,AreaOfInterest.SIMULATION_HALF_EXTENT,map.width,map.height);
    var snapshot=AreaOfInterest.window(x,y,AreaOfInterest.SNAPSHOT_HALF_EXTENT,map.width,map.height);
    for(long key:AreaOfInterest.cellsCovering(simulation)) if(!coverage.covered(key)) { coverageVersion++;break; }
    coverage.update(tick,List.of(simulation));
    for(long key:AreaOfInterest.cellsCovering(snapshot)) for(int index:cellIndex.spawnsIn(key))
      if(homeInside(index,snapshot)) enqueue(index,true);
    for(long key:AreaOfInterest.cellsCovering(simulation)) for(int index:cellIndex.spawnsIn(key)) enqueue(index,false);
  }

  /** True when every creature whose home is inside the snapshot window around (x, y) is live (or cannot be). */
  boolean windowActive(double x,double y) {
    if(!streamed) return true;
    return rectActive(AreaOfInterest.window(x,y,AreaOfInterest.SNAPSHOT_HALF_EXTENT,map.width,map.height));
  }

  /** {@link #windowActive} for any rectangle (the latency measurements use 384 x 384 windows). */
  boolean rectActive(AreaOfInterest.Window snapshot) {
    if(!streamed) return true;
    for(long key:AreaOfInterest.cellsCovering(snapshot)) for(int index:cellIndex.spawnsIn(key)) {
      if(!homeInside(index,snapshot)) continue;
      byte state=lifecycle[index];
      if(state==QUEUED || state==LOADING || state==DORMANT && !failedIndices.contains(index)) return false;
    }
    return true;
  }

  private boolean homeInside(int index,AreaOfInterest.Window window) {
    var s=hosted.get(index).spawn();
    return window.contains(s.x()-map.originX,s.y()-map.originY);
  }

  private void enqueue(int index,boolean front) {
    if(lifecycle[index]==DORMANT && !failedIndices.contains(index)) {
      lifecycle[index]=QUEUED;queuedCount++;
      (front ? urgent : queued).add(index);
    } else if(front && lifecycle[index]==QUEUED) urgent.add(index);
  }

  private boolean interested(int index,double x,double y) {
    return coverage.covered(cellIndex.homeCell(index))
        || coverage.covered(AreaOfInterest.cellKey(AreaOfInterest.cell((int)Math.floor(x)),AreaOfInterest.cell((int)Math.floor(y))));
  }

  private boolean interested(Entry entry) {
    var at=world.creaturePosition(entry.spawn.id());
    return interested((int)entry.spawn.id()-1,at.x(),at.y());
  }

  private void dispatchActivations() {
    if(tick<retryAfterTick) return;
    int budget=activationBatch;
    var batch=new ArrayList<Integer>(budget);
    while(budget>0) {
      Integer index=!urgent.isEmpty() ? urgent.poll() : queued.poll();
      if(index==null) break;
      if(lifecycle[index]!=QUEUED) continue; // already dispatched through the other queue
      var covering=coverage.covered(cellIndex.homeCell(index)) || coverage.covered(cellIndex.currentCell(index));
      if(!covering) { lifecycle[index]=DORMANT;queuedCount--;continue; } // left every window before it was read: nothing written
      if(entries.size()+loadingCount+batch.size()>=maxActive) {
        capRefusals++;urgent.addFirst(index);break; // explicit capacity condition, retried next tick
      }
      lifecycle[index]=LOADING;queuedCount--;loadingCount++;batch.add(index);budget--;
    }
    if(!batch.isEmpty()) submitActivationBatch(batch);
  }

  private void submitActivationBatch(List<Integer> indices) {
    var keys=new ArrayList<String>(indices.size());
    var delays=new ArrayList<Long>(indices.size());
    for(int index:indices) {
      var spawn=hosted.get(index);
      keys.add(spawn.spawn().persistenceKey());delays.add((long)Math.max(0,spawn.definition().getRespawnTime()));
    }
    try {
      worker.execute(() -> {
        long started=System.nanoTime();
        Map<String,OnlineStore.SpawnState> rows=null;
        try {
          rows=store.loadSpawns(keys);
          // A creature that died while dormant respawns on the first activation after its authored delay.
          for(int i=0;i<keys.size();i++) {
            var row=rows.get(keys.get(i));
            if(row!=null && row.diedAt()!=null) {
              try { rows.put(row.key(),store.respawn(row.key(),row.eventId(),row.stateRevision(),delays.get(i))); }
              catch(OnlineStore.Conflict notReady) { /* still waiting: it activates hidden and retries each second */ }
            }
          }
        } catch(SQLException|RuntimeException failure) { rows=null; }
        finally {
          maxBatchNanos=Math.max(maxBatchNanos,System.nanoTime()-started);
          activations.add(new ActivationResult(indices,rows));
        }
      });
      batchesTotal++;
    } catch(RejectedExecutionException busy) {
      for(int index:indices) { lifecycle[index]=QUEUED;queuedCount++;loadingCount--;urgent.addFirst(index); }
      retryAfterTick=tick+1;
    }
  }

  private void drainStreamed() {
    ActivationResult result;
    while((result=activations.poll())!=null) {
      if(result.rows()==null) { // the read failed: nothing was created; try again shortly
        for(int index:result.indices()) if(lifecycle[index]==LOADING) {
          loadingCount--;lifecycle[index]=DORMANT;
          if(!closing) enqueue(index,false);
        }
        retryAfterTick=tick+PreviewWorld.TICK_RATE;
        continue;
      }
      for(int index:result.indices()) {
        if(lifecycle[index]!=LOADING) continue;
        loadingCount--;
        var spawn=hosted.get(index);
        var row=result.rows().get(spawn.spawn().persistenceKey());
        var home=spawn.spawn();
        double x=row!=null && row.currentX()!=null ? row.currentX()-map.originX : home.x()-map.originX;
        double y=row!=null && row.currentY()!=null ? row.currentY()-map.originY : home.y()-map.originY;
        if(closing || !interested(index,x,y)) { lifecycle[index]=DORMANT;continue; } // nothing was written for it
        activate(index,row);
      }
    }
    DeleteCompletion deleted;
    while((deleted=deletions.poll())!=null) {
      var entry=entries.get(deleted.id());
      if(entry==null) continue;
      entry.pending=false;
      if(entry.reloadAfterPending) { entry.reloadAfterPending=false;submitRead(entry);continue; }
      if(deleted.deleted() && deleted.touch()==entry.touch && deleted.cacheGeneration()==entry.cacheGeneration) {
        entry.maybeRow=false;entry.dirty=false;entry.deleteFailures=0;pristineDeleted++;
        entry.saved=pristine(entry.saved.key(),entry.saved.eventId());
      } else if(!deleted.deleted()) { entry.deleteFailures++;entry.deleteRetryTick=tick+PreviewWorld.TICK_RATE*10L; }
    }
  }

  private void activate(int index,OnlineStore.SpawnState row) {
    var spawn=hosted.get(index);
    var saved=row!=null ? row : pristine(spawn.spawn().persistenceKey(),UUID.randomUUID());
    Entry entry=null;
    try {
      world.activateCreature(spawn);
      entry=new Entry(spawn,saved);entry.maybeRow=row!=null;entry.lastCheckpointTick=tick;
      entries.put(spawn.id(),entry);
      publish(entry,true);
      cellIndex.clearDisplaced(index);
      lifecycle[index]=ACTIVE;activatedTotal++;
      maxActiveSeen=Math.max(maxActiveSeen,entries.size());
    } catch(RuntimeException failure) {
      // One bad persisted position must not stop the server; the creature stays dormant and is counted.
      entries.remove(spawn.id());
      if(world.creatureSpawn(spawn.id())!=null) world.deactivateCreature(spawn.id());
      lifecycle[index]=DORMANT;failedIndices.add(index);failedActivations++;
      System.err.println("Creature "+spawn.spawn().persistenceKey()+" stays dormant: "+failure.getMessage());
    }
  }

  private int maximumHealth(Entry entry) { return entry.spawn.definition().getHealth(); }

  private static boolean benign(OnlineMonsterPursuit.Mode mode) {
    return mode==OnlineMonsterPursuit.Mode.IDLE || mode==OnlineMonsterPursuit.Mode.PATROL;
  }

  /** Nothing about this creature differs from what the content files say: alive, unhurt, home-bound, targeting nothing. */
  private boolean pristine(Entry entry) {
    var saved=entry.saved;
    if(saved.diedAt()!=null || saved.health()!=null && saved.health()<maximumHealth(entry)
        || saved.targetCharacter()!=null || saved.targetSpawnKey()!=null) return false;
    if(entry.controller==null) return saved.mode().equals("IDLE") || saved.mode().equals("PATROL") || saved.mode().equals("RETURN");
    var view=entry.controller.current();
    return view.target()==null && view.targetSpawnKey()==null && benign(view.mode());
  }

  private boolean calm(Entry entry) {
    return !entry.pending && !entry.recovering && !entry.reserved && !entry.reloadAfterPending
        && entry.stunnedUntilMillis<=System.currentTimeMillis();
  }

  /** Once a second: delete rows of creatures back to pristine, and settle creatures that left every window. */
  private void settleScan() {
    int writes=0;
    for(var entry:List.copyOf(entries.values())) {
      if(!calm(entry)) continue;
      int index=(int)entry.spawn.id()-1;
      boolean wanted=interested(entry);
      boolean controllerCalm=entry.controller==null || benign(entry.controller.current().mode())
          && entry.controller.current().target()==null && entry.controller.current().targetSpawnKey()==null;
      if(wanted) {
        if(entry.settling) { entry.settling=false;lifecycle[index]=ACTIVE; }
        if(entry.maybeRow && writes<16 && tick>=entry.deleteRetryTick && controllerCalm && pristine(entry)) {
          submitPristineDelete(entry);writes++;
        }
        continue;
      }
      if(!controllerCalm) continue; // a chase, return or clan fight is sticky: never cut off by a window
      if(!entry.settling) { entry.settling=true;lifecycle[index]=SETTLING; }
      if(pristine(entry)) {
        if(!entry.maybeRow || entry.deleteFailures>=3) drop(entry);
        else if(writes<16 && tick>=entry.deleteRetryTick) { submitPristineDelete(entry);writes++; }
      } else if(entry.dirty) { if(writes<16) { submitCheckpoint(entry);writes++; } }
      else drop(entry);
    }
  }

  private void submitPristineDelete(Entry entry) {
    var saved=entry.saved;long touch=entry.touch,generation=entry.cacheGeneration;int maximum=maximumHealth(entry);
    long id=entry.spawn.id();
    entry.pending=true;
    try {
      worker.execute(() -> {
        boolean deleted=false;
        try { deleted=store.deleteSpawnIfPristine(saved.key(),saved.eventId(),saved.stateRevision(),maximum); }
        catch(SQLException|RuntimeException failure) { /* kept: a stale pristine row is harmless */ }
        finally { deletions.add(new DeleteCompletion(id,generation,touch,deleted)); }
      });
    } catch(RejectedExecutionException busy) { entry.pending=false; }
  }

  private void drop(Entry entry) {
    long id=entry.spawn.id();int index=(int)id-1;
    var at=world.creaturePosition(id);
    entries.remove(id);world.deactivateCreature(id);
    lifecycle[index]=DORMANT;deactivatedTotal++;
    // A creature that keeps a row is found again where it stands, not only at home.
    if(entry.maybeRow) cellIndex.displace(index,at.x(),at.y());else cellIndex.clearDisplaced(index);
  }

  private void drain() {
    PairCompletion pair;
    while((pair=completedPairs.poll())!=null) {
      var source=entries.get(pair.sourceId());var target=entries.get(pair.targetId());
      source.pending=false;target.pending=false;source.reserved=false;target.reserved=false;
      source.attackReady=false;target.attackReady=false;
      if(pair.result()==null || source.cacheGeneration!=pair.sourceGeneration()
          || target.cacheGeneration!=pair.targetGeneration()
          || source.reloadAfterPending || target.reloadAfterPending) {
        source.reloadAfterPending=false;target.reloadAfterPending=false;
        reload(pair.sourceId(),false);reload(pair.targetId(),false);
        continue;
      }
      source.saved=pair.result().source();target.saved=pair.result().target();
      source.recovering=false;target.recovering=false;source.dirty=false;target.dirty=false;
      publish(source,true);publish(target,true);
    }
    if(streamed) drainStreamed();
    Completion result;
    while((result=completed.poll())!=null) {
      var entry=entries.get(result.id());
      if(entry==null) continue; // deactivated is only allowed with nothing pending, so this is defensive
      entry.pending=false;
      if(result.persisted()) entry.maybeRow=true;
      if(entry.reloadAfterPending) { entry.reloadAfterPending=false;submitRead(entry);continue; }
      if(result.saved()==null) {
        entry.recovering=true;entry.dirty=false;world.creatureActive(result.id(),false);
        continue; // SQL ambiguity: no guessed success or automatic replay.
      }
      if(!entry.saved.eventId().equals(result.saved().eventId())) entry.stunnedUntilMillis=0;
      entry.saved=result.saved();entry.recovering=false;
      publish(entry,true);
    }
  }

  private void submitCheckpoint(Entry entry) {
    var motion=capture(entry.spawn.id());
    if(streamed && motion.targetSpawnKey()!=null) { // the store creates the target's row in the same transaction
      Long other=idForKey(motion.targetSpawnKey());
      if(other!=null) { var target=entries.get(other);target.maybeRow=true;target.touch++; }
    }
    var targetSession=motion.targetCharacter()==null || sessions==null ? null
        : sessions.sessionForCharacter(motion.targetCharacter());
    entry.pending=true;entry.dirty=false;entry.lastCheckpointTick=tick;
    try {
      worker.execute(() -> {
        OnlineStore.SpawnState saved=null;
        try { saved=store.checkpointSpawn(motion,targetSession); }
        catch(SQLException | RuntimeException failure) { /* Drain reloads authoritative state. */ }
        finally { completed.add(new Completion(entry.spawn.id(),saved,true)); }
      });
    } catch(RejectedExecutionException busy) { entry.pending=false;entry.dirty=true; }
  }

  private void submitRead(Entry entry) {
    entry.pending=true;
    entry.lastRecoveryAttemptNanos=System.nanoTime();
    final UUID knownLife=entry.saved.eventId();
    try {
      worker.execute(() -> {
        OnlineStore.SpawnState saved=null;boolean persisted=true;
        try {
          if(streamed) {
            // Exception-only persistence: a missing row is a pristine creature, never created by a read.
            var key=entry.spawn.spawn().persistenceKey();
            saved=store.loadSpawns(List.of(key)).get(key);
            if(saved==null) { saved=pristine(key,knownLife);persisted=false; }
          } else saved=store.loadOrCreateSpawn(entry.spawn.spawn().persistenceKey());
          if(saved.diedAt()!=null) {
            try { saved=store.respawn(saved.key(),saved.eventId(),saved.stateRevision(),
                Math.max(0,entry.spawn.definition().getRespawnTime())); }
            catch(OnlineStore.Conflict notReady) { saved=streamed ? store.loadSpawns(List.of(saved.key())).get(saved.key())
                : store.loadOrCreateSpawn(saved.key()); }
          }
        } catch(SQLException failure) { saved=null; }
        finally { completed.add(new Completion(entry.spawn.id(),saved,false,persisted && saved!=null)); }
      });
    } catch(RejectedExecutionException busy) { entry.pending=false; }
  }

  @Override public void close() {
    closing=true;
    boolean interrupted=Thread.interrupted();
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(12);
    try {
      while(System.nanoTime()<deadline) {
        drain();
        // A failed checkpoint/read has no queued retry once normal ticks have stopped.
        // Reload committed state only; never replay the uncertain mutation.
        for(var entry:entries.values()) if(entry.recovering && !entry.pending
            && System.nanoTime()-entry.lastRecoveryAttemptNanos>=TimeUnit.MILLISECONDS.toNanos(100))
          submitRead(entry);
        for(var entry:entries.values()) if(entry.dirty && !entry.pending && !entry.recovering && !entry.reserved)
          submitCheckpoint(entry);
        if(entries.values().stream().noneMatch(e->e.pending || e.dirty || e.recovering || e.reserved)) break;
        try { Thread.sleep(10); } catch(InterruptedException signal) { interrupted=true; }
      }
      if(entries.values().stream().anyMatch(e->e.pending || e.dirty || e.recovering || e.reserved))
        throw new IllegalStateException("Creature persistence flush incomplete");
    } finally {
      worker.shutdownNow();
      long termination=System.nanoTime()+TimeUnit.SECONDS.toNanos(12);
      while(!worker.isTerminated() && System.nanoTime()<termination) {
        try { worker.awaitTermination(Math.max(1,termination-System.nanoTime()),TimeUnit.NANOSECONDS); }
        catch(InterruptedException signal) { interrupted=true; }
      }
      if(interrupted) Thread.currentThread().interrupt();
    }
    if(!worker.isTerminated()) throw new IllegalStateException("Creature persistence worker did not terminate");
  }
}
