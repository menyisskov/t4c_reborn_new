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
    long stunnedUntilMillis;
    long lastCheckpointTick;
    long lastRecoveryAttemptNanos;
    long cacheGeneration;
    Entry(PreviewWorld.CreatureSpawn spawn,OnlineStore.SpawnState saved) { this.spawn=spawn;this.saved=saved; }
  }
  private record Completion(long id,OnlineStore.SpawnState saved,boolean checkpoint) {}
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
    var clanTargets=clanViews();
    for(var entry:entries.values()) {
      entry.attackReady=false;
      if(entry.controller==null || entry.pending || entry.recovering || entry.reserved
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
      if(positionChanged || transition) entry.dirty=true;
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
    Completion result;
    while((result=completed.poll())!=null) {
      var entry=entries.get(result.id());entry.pending=false;
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
          if(saved.diedAt()!=null) {
            try { saved=store.respawn(saved.key(),saved.eventId(),saved.stateRevision(),
                Math.max(0,entry.spawn.definition().getRespawnTime())); }
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
