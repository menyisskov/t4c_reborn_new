package com.perso.T4C.server;

import com.perso.T4C.server.persistence.OnlineStore;
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
    boolean pending,recovering,reserved,reloadAfterPending,dirty,needsReplan;
    long lastCheckpointTick;
    long lastRecoveryAttemptNanos;
    Entry(PreviewWorld.CreatureSpawn spawn,OnlineStore.SpawnState saved) { this.spawn=spawn;this.saved=saved; }
  }
  private record Completion(long id,OnlineStore.SpawnState saved,boolean checkpoint) {}
  private final OnlineStore store;
  private final PreviewWorld world;
  private final PreviewMap map;
  private final String region;
  private final PersistentSessions sessions;
  private final com.perso.T4C.online.model.ItemContent items;
  private final Map<Long,Entry> entries=new LinkedHashMap<>();
  private final ConcurrentLinkedQueue<Completion> completed=new ConcurrentLinkedQueue<>();
  private final ThreadPoolExecutor worker;
  private long tick;

  PersistentCreatures(OnlineStore store,PreviewWorld world) throws SQLException {
    this(store,world,world.map(),"single",null,null);
  }

  PersistentCreatures(OnlineStore store,PreviewWorld world,PreviewMap map,String region,
      PersistentSessions sessions,com.perso.T4C.online.model.ItemContent items) throws SQLException {
    this.store=store;this.world=world;this.map=map;this.region=region;this.sessions=sessions;this.items=items;
    // Startup precedes the simulation and HTTP listener.
    for(var spawn:world.creatureSpawns()) {
      var entry=new Entry(spawn,store.loadOrCreateSpawn(spawn.spawn().persistenceKey()));
      entries.put(spawn.id(),entry);
      publish(entry,true);
    }
    worker=new ThreadPoolExecutor(1,1,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(256),
        Thread.ofPlatform().name("creature-persistence-",0).factory(),new ThreadPoolExecutor.AbortPolicy());
  }

  private void publish(Entry entry,boolean reconstruct) {
    var saved=entry.saved;var spawn=entry.spawn;
    double x=saved.currentX()==null ? spawn.spawn().x() : saved.currentX();
    double y=saved.currentY()==null ? spawn.spawn().y() : saved.currentY();
    double localX=x-map.originX,localY=y-map.originY;
    if (!map.canStand(localX,localY)) throw new IllegalStateException("Saved creature position is blocked: "+saved.key());
    world.creaturePosition(spawn.id(),localX,localY);
    world.creatureHealth(spawn.id(),saved.health());
    world.creatureActive(spawn.id(),saved.recipient()==null);
    if (!reconstruct) return;
    if (items==null || spawn.spawn().stationary() || !OnlineRetaliation.supported(spawn.definition())
        || !OnlineLootDrafts.supports(spawn.definition(),items)
        || spawn.definition().getSourceEvents()!=null && !spawn.definition().getSourceEvents().isEmpty()) {
      entry.controller=null;entry.needsReplan=false;return;
    }
    var mode=OnlineMonsterPursuit.Mode.valueOf(saved.mode());
    long epoch=saved.targetEpoch()==null ? -1 : saved.targetEpoch();
    entry.controller=new OnlineMonsterPursuit(map,spawn.spawn(),spawn.definition(),region,
        new OnlineMonsterPursuit.Seed(saved.eventId(),localX,localY,mode,saved.targetCharacter(),epoch));
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
    var view=entry.controller==null ? null : entry.controller.current();
    var position=world.creaturePosition(id);
    UUID target=view==null ? entry.saved.targetCharacter() : view.target();
    Long epoch=view==null ? entry.saved.targetEpoch() : view.targetEpoch()<0 ? null : view.targetEpoch();
    String mode=view==null ? entry.saved.mode() : view.mode().name();
    return new OnlineStore.SpawnMotion(entry.saved.key(),entry.saved.eventId(),entry.saved.stateRevision(),
        map.originX+position.x(),map.originY+position.y(),mode,target,epoch);
  }

  void reserve(long id) { life(id);entries.get(id).reserved=true; }
  void release(long id) { var entry=entries.get(id);if(entry!=null) entry.reserved=false; }
  void rewardCommitted(long id) { reload(id,true); }
  void damageCommitted(long id) { reload(id,false); }

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
    entry.recovering=true;entry.dirty=false;entry.needsReplan=false;
    if(!entry.pending) submitRead(entry);else entry.reloadAfterPending=true;
  }

  /** IDs awaiting a bounded path solve. The host grants one globally per tick. */
  List<Long> pendingReplans() {
    var ids=new ArrayList<Long>();
    for(var entry:entries.values()) if(entry.needsReplan && !entry.pending && !entry.recovering
        && !entry.reserved && entry.saved.recipient()==null) ids.add(entry.spawn.id());
    return ids;
  }

  void step(List<OnlineMonsterPursuit.TargetView> targets,Long permittedId) {
    drain();tick++;
    for(var entry:entries.values()) {
      if(entry.controller==null || entry.pending || entry.recovering || entry.reserved
          || entry.saved.recipient()!=null) continue;
      var before=entry.controller.current();
      var decision=entry.controller.step(targets,1.0/PreviewWorld.TICK_RATE,
          permittedId!=null && permittedId==entry.spawn.id(),false);
      entry.needsReplan=decision.needsReplan();
      boolean positionChanged=Double.compare(before.x(),decision.x())!=0 || Double.compare(before.y(),decision.y())!=0;
      boolean transition=before.mode()!=decision.mode() || !Objects.equals(before.target(),decision.target())
          || before.targetEpoch()!=decision.targetEpoch();
      if(positionChanged) world.creaturePosition(entry.spawn.id(),decision.x(),decision.y());
      if(positionChanged || transition) entry.dirty=true;
      if(entry.dirty && (transition || tick-entry.lastCheckpointTick>=CHECKPOINT_TICKS))
        submitCheckpoint(entry);
    }
    if(tick%PreviewWorld.TICK_RATE==0)
      for(var entry:entries.values()) if(!entry.pending && (entry.saved.recipient()!=null || entry.recovering))
        submitRead(entry);
  }

  void step() { step(List.of(),null); }

  private void drain() {
    Completion result;
    while((result=completed.poll())!=null) {
      var entry=entries.get(result.id());entry.pending=false;
      if(entry.reloadAfterPending) { entry.reloadAfterPending=false;submitRead(entry);continue; }
      if(result.saved()==null) {
        entry.recovering=true;entry.dirty=false;world.creatureActive(result.id(),false);
        continue; // SQL ambiguity: no guessed success or automatic replay.
      }
      entry.saved=result.saved();entry.recovering=false;
      publish(entry,true);
    }
  }

  private void submitCheckpoint(Entry entry) {
    var motion=capture(entry.spawn.id());
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
    try {
      worker.execute(() -> {
        OnlineStore.SpawnState saved=null;
        try {
          saved=store.loadOrCreateSpawn(entry.spawn.spawn().persistenceKey());
          if(saved.recipient()!=null) {
            try { saved=store.respawn(saved.key(),saved.eventId(),Math.max(0,entry.spawn.definition().getRespawnTime())); }
            catch(OnlineStore.Conflict notReady) { saved=store.loadOrCreateSpawn(saved.key()); }
          }
        } catch(SQLException failure) { saved=null; }
        finally { completed.add(new Completion(entry.spawn.id(),saved,false)); }
      });
    } catch(RejectedExecutionException busy) { entry.pending=false; }
  }

  @Override public void close() {
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
