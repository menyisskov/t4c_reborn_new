package com.perso.T4C.server;

import com.perso.T4C.server.persistence.OnlineStore;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

/** Startup recovery plus bounded worker-driven respawn; published state is simulation-thread owned. */
final class PersistentCreatures implements AutoCloseable {
  private static final class Entry {
    final PreviewWorld.CreatureSpawn spawn;
    OnlineStore.SpawnState saved;
    boolean pending, recovering, reserved, reloadAfterPending;
    Entry(PreviewWorld.CreatureSpawn spawn, OnlineStore.SpawnState saved) { this.spawn=spawn; this.saved=saved; }
  }
  private record Completion(long id, OnlineStore.SpawnState saved) {}
  private final OnlineStore store;
  private final PreviewWorld world;
  private final Map<Long,Entry> entries=new LinkedHashMap<>();
  private final ConcurrentLinkedQueue<Completion> completed=new ConcurrentLinkedQueue<>();
  private final ThreadPoolExecutor worker;
  private long tick;

  PersistentCreatures(OnlineStore store, PreviewWorld world) throws SQLException {
    this.store=store; this.world=world;
    // Startup precedes both simulation and HTTP listening; never recover synchronously on a tick.
    for (var spawn:world.creatureSpawns()) {
      var saved=store.loadOrCreateSpawn(spawn.spawn().persistenceKey());
      entries.put(spawn.id(),new Entry(spawn,saved));
      world.creatureHealth(spawn.id(),saved.health());
      world.creatureActive(spawn.id(),saved.recipient()==null);
    }
    worker=new ThreadPoolExecutor(1,1,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(256),
        Thread.ofPlatform().name("creature-persistence-",0).factory(),new ThreadPoolExecutor.AbortPolicy());
  }

  OnlineStore.SpawnState life(long id) {
    var entry=entries.get(id);
    if (entry==null || entry.pending || entry.recovering || entry.reserved) throw new PreviewWorld.Rejected(409,"creature_unavailable");
    return entry.saved;
  }

  void reserve(long id) { life(id); entries.get(id).reserved=true; }
  void release(long id) { var entry=entries.get(id); if (entry!=null) entry.reserved=false; }

  /** Called only after a world-reward transaction commits. */
  void rewardCommitted(long id) {
    reload(id,true);
  }

  /** Refresh committed partial damage without briefly removing the still-living actor. */
  void damageCommitted(long id) {
    reload(id,false);
  }

  void characterDied(java.util.UUID character) {
    for (var entry:entries.values())
      // An in-flight read/provocation can already contain this target while the published cache
      // still names somebody else. Fence that result before it can resurrect cleared aggro.
      if (character.equals(entry.saved.targetCharacter()) || entry.pending || entry.recovering || entry.reserved)
        reload(entry.spawn.id(),false);
  }

  private void reload(long id, boolean hide) {
    var entry=entries.get(id);
    if (entry==null) throw new IllegalArgumentException("Unknown creature");
    if (hide) world.creatureActive(id,false);
    entry.recovering=true;
    if (!entry.pending) submit(entry); else entry.reloadAfterPending=true;
  }

  void step() {
    Completion result;
    while ((result=completed.poll())!=null) {
      var entry=entries.get(result.id()); entry.pending=false;
      if (entry.reloadAfterPending) {
        entry.reloadAfterPending=false;
        submit(entry);
        continue; // This read may have begun before the newest committed mutation.
      }
      if (result.saved()!=null) {
        entry.saved=result.saved(); entry.recovering=false;
        world.creatureHealth(result.id(),entry.saved.health());
        world.creatureActive(result.id(),entry.saved.recipient()==null);
      }
    }
    if (++tick % PreviewWorld.TICK_RATE != 0) return;
    for (var entry:entries.values())
      if (!entry.pending && (entry.saved.recipient()!=null || entry.recovering)) submit(entry);
  }

  private void submit(Entry entry) {
    entry.pending=true;
    try {
      worker.execute(() -> {
        OnlineStore.SpawnState saved=null;
        try {
          saved=store.loadOrCreateSpawn(entry.spawn.spawn().persistenceKey());
          if (saved.recipient()!=null) {
            try { saved=store.respawn(saved.key(),saved.eventId(),Math.max(0,entry.spawn.definition().getRespawnTime())); }
            catch (OnlineStore.Conflict notReady) { saved=store.loadOrCreateSpawn(saved.key()); }
          }
        } catch (SQLException failure) {
          // Reload uncertain writes on retry. Never guess a fresh life or publish success.
          saved=null;
        } finally { completed.add(new Completion(entry.spawn.id(),saved)); }
      });
    } catch (RejectedExecutionException busy) { entry.pending=false; }
  }

  @Override public void close() {
    worker.shutdown();
    try { if (!worker.awaitTermination(12,TimeUnit.SECONDS)) worker.shutdownNow(); }
    catch (InterruptedException interrupted) { worker.shutdownNow(); Thread.currentThread().interrupt(); }
  }
}
