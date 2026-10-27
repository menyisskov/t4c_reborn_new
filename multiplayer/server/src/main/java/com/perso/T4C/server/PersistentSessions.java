package com.perso.T4C.server;

import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.online.model.CharacterProgress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;

/** Simulation-owned sessions; bounded workers perform all blocking checkpoints and lease renewal. */
final class PersistentSessions {
  private static final long CHECKPOINT_TICKS = PreviewWorld.TICK_RATE * 5L;
  // Stop simulation well before the database's 30-second lease can expire or be taken over.
  private static final long AUTHORITY_NANOS = TimeUnit.SECONDS.toNanos(20);
  private final OnlineStore store;
  private final PreviewMap map;
  private final PreviewWorld world;
  private final Map<String, Binding> bindings = new HashMap<>();
  private final ConcurrentLinkedQueue<Completion> completions = new ConcurrentLinkedQueue<>();
  private final ThreadPoolExecutor writes = new ThreadPoolExecutor(4,4,0,TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(PreviewWorld.MAX_PLAYERS),Thread.ofPlatform().name("online-store-",0).factory(),
      new ThreadPoolExecutor.AbortPolicy());
  private long tick;
  private static final class Binding {
    final String token;
    final OnlineStore.Session session;
    final CompletableFuture<Void> closed = new CompletableFuture<>();
    OnlineStore.CharacterState saved;
    PreviewWorld.PlayerView position;
    long checkpointSubmitted, allowedUntil;
    boolean pending, closing;
    GameplayEvent event;
    Binding(String token, OnlineStore.Session session, PreviewWorld.PlayerView position) {
      this.token=token; this.session=session; this.saved=session.character(); this.position=position;
    }
  }
  private record GameplayEvent(UUID id, long gold, long experience, CharacterProgress progress, boolean worldReward,
      CompletableFuture<OnlineStore.RewardResult> result) {}
  private record Completion(Binding binding, OnlineStore.CharacterState saved, boolean finalSave,
      long allowedUntil, OnlineStore.RewardResult reward, Exception error) {}

  PersistentSessions(OnlineStore store, PreviewMap map, PreviewWorld world) {
    this.store=store; this.map=map; this.world=world;
  }

  PreviewWorld.Joined join(OnlineStore.Session session, long acquisitionStarted) {
    if (System.nanoTime()-acquisitionStarted>=AUTHORITY_NANOS) throw new PreviewWorld.Rejected(503,"busy");
    if (bindings.size()>=PreviewWorld.MAX_PLAYERS) throw new PreviewWorld.Rejected(409,"full");
    var saved=session.character();
    if (saved.world()!=0) throw new PreviewWorld.Rejected(409,"world");
    long appearance=saved.progress()==null ? session.characterId().getLeastSignificantBits() & 1L
        : "female".equals(saved.progress().gender()) ? 0 : 1;
    var joined=world.joinAt(saved.x()-map.originX,saved.y()-map.originY,appearance);
    var binding=new Binding(joined.token(),session,world.position(joined.token()));
    binding.checkpointSubmitted=tick;
    binding.allowedUntil=acquisitionStarted+AUTHORITY_NANOS;
    bindings.put(joined.token(),binding);
    return joined;
  }

  CompletableFuture<Void> leave(String token) {
    var binding=bindings.get(token);
    if (binding==null) throw new PreviewWorld.Rejected(401,"session");
    var position=world.position(token);
    if (position!=null) binding.position=position;
    world.remove(token); binding.closing=true;
    if (!binding.pending) submit(binding);
    return binding.closed;
  }

  /** Committed state only, for server gameplay producers running on the simulation thread. */
  OnlineStore.CharacterState character(String token) {
    return active(token).saved;
  }

  /**
   * Accept one immutable server-authored result per character. A checkpoint already in flight may
   * finish first; no second gameplay result can be based on state that has not committed yet.
   * This is deliberately not a network command accepting client-provided rewards or attributes.
   */
  CompletionStage<OnlineStore.RewardResult> commitGameplayEvent(String token, long expectedRevision,
      UUID eventId, long gold, long experience, CharacterProgress progress) {
    return commitEvent(token,expectedRevision,eventId,gold,experience,progress,false);
  }

  CompletionStage<OnlineStore.RewardResult> commitWorldReward(String token, long expectedRevision,
      UUID eventId, long gold, long experience, CharacterProgress progress) {
    return commitEvent(token,expectedRevision,eventId,gold,experience,progress,true);
  }

  private CompletionStage<OnlineStore.RewardResult> commitEvent(String token, long expectedRevision,
      UUID eventId, long gold, long experience, CharacterProgress progress, boolean worldReward) {
    var binding=active(token);
    if (binding.event!=null) throw new PreviewWorld.Rejected(409,"busy");
    if (binding.saved.revision()!=expectedRevision) throw new PreviewWorld.Rejected(409,"revision");
    if (gold<0 || experience<0) throw new IllegalArgumentException("Negative reward");
    var result=new CompletableFuture<OnlineStore.RewardResult>();
    binding.event=new GameplayEvent(Objects.requireNonNull(eventId),gold,experience,
        Objects.requireNonNull(progress),worldReward,result);
    if (!binding.pending) submit(binding);
    return result.minimalCompletionStage();
  }

  private Binding active(String token) {
    var binding=bindings.get(token);
    if (binding==null || binding.closing || world.position(token)==null
        || System.nanoTime()>=binding.allowedUntil) throw new PreviewWorld.Rejected(401,"session");
    return binding;
  }

  void step() {
    tick++;
    drain();
    for (var binding:new ArrayList<>(bindings.values())) {
      var position=world.position(binding.token);
      if (position==null) binding.closing=true;
      else binding.position=position;
      if (System.nanoTime()>=binding.allowedUntil) { world.remove(binding.token); binding.closing=true; }
      if (!binding.pending && (binding.event!=null || binding.closing || tick-binding.checkpointSubmitted>=CHECKPOINT_TICKS)) submit(binding);
    }
  }

  private void submit(Binding binding) {
    binding.pending=true;
    var position=binding.position;
    long revision=binding.saved.revision();
    // Frequent rewards must not postpone movement checkpoints indefinitely. Logout drains its
    // accepted event first, then performs one final checkpoint using the latest position.
    var event=!binding.closing && tick-binding.checkpointSubmitted>=CHECKPOINT_TICKS ? null : binding.event;
    if (event==null) binding.checkpointSubmitted=tick;
    boolean finalSave=binding.closing && event==null;
    try {
      writes.execute(() -> {
        OnlineStore.CharacterState saved=null; Exception failure=null;
        OnlineStore.RewardResult reward=null;
        long allowedUntil=System.nanoTime()+AUTHORITY_NANOS;
        try {
          store.renew(binding.session);
          if (event==null) {
            saved=store.checkpoint(binding.session,revision,0,map.originX+position.x(),map.originY+position.y());
          } else {
            reward=event.worldReward()
                ? store.commitWorldReward(binding.session,revision,event.id(),event.gold(),event.experience(),event.progress())
                : store.commitGameplayEvent(binding.session,revision,event.id(),event.gold(),event.experience(),event.progress());
            saved=reward.character();
          }
          if (finalSave) store.release(binding.session);
        } catch (Exception error) { failure=error; }
        completions.add(new Completion(binding,saved,finalSave,allowedUntil,reward,failure));
      });
    } catch (RejectedExecutionException error) {
      completions.add(new Completion(binding,null,finalSave,0,null,error));
    }
  }

  private void drain() {
    Completion completed;
    while ((completed=completions.poll())!=null) {
      var binding=completed.binding();
      if (bindings.get(binding.token)!=binding) continue;
      binding.pending=false;
      if (completed.error()!=null) {
        // Fail closed on uncertain writes. Lease expiry permits recovery from the last DB commit.
        world.remove(binding.token); bindings.remove(binding.token);
        if (binding.event!=null) binding.event.result().completeExceptionally(new IllegalStateException("Persistence unavailable"));
        binding.closed.completeExceptionally(new IllegalStateException("Persistence unavailable"));
        System.err.println("Online persistence failed; character disconnected. Recover after lease expiry.");
      } else {
        binding.saved=completed.saved();
        binding.allowedUntil=completed.allowedUntil();
        if (completed.reward()!=null) {
          var event=binding.event;
          binding.event=null;
          event.result().complete(completed.reward());
        }
        if (completed.finalSave()) { bindings.remove(binding.token); binding.closed.complete(null); }
      }
    }
  }

  /** Runs on the simulation owner after ticking stops. Bounded graceful flush; never invent a save. */
  void shutdown() {
    for (var binding:new ArrayList<>(bindings.values())) leave(binding.token);
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(12);
    try {
      while (!bindings.isEmpty() && System.nanoTime()<deadline) {
        step(); Thread.sleep(10);
      }
    } catch (InterruptedException error) { Thread.currentThread().interrupt(); }
    finally {
      writes.shutdownNow();
      for (var binding:bindings.values()) {
        var error=new IllegalStateException("Server stopped before persistence completed");
        binding.closed.completeExceptionally(error);
        if (binding.event!=null) binding.event.result().completeExceptionally(error);
      }
      bindings.clear();
    }
  }
}
