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
  private final HostedWorlds hosted;
  private long nextPlayerId=1;
  private final Map<String, Binding> bindings = new HashMap<>();
  private final ConcurrentLinkedQueue<Completion> completions = new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<Runnable> reads = new ConcurrentLinkedQueue<>();
  private final ThreadPoolExecutor writes = new ThreadPoolExecutor(4,4,0,TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(PreviewWorld.MAX_PLAYERS),Thread.ofPlatform().name("online-store-",0).factory(),
      new ThreadPoolExecutor.AbortPolicy());
  // Read bursts must never consume the one-write-per-character capacity or lease-renewal slots.
  private final ThreadPoolExecutor lookups = new ThreadPoolExecutor(2,2,0,TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(PreviewWorld.MAX_PLAYERS),Thread.ofPlatform().name("online-lookup-",0).factory(),
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
    boolean awaitingRegionReady;
    HostedWorlds.Region region;
    GameplayEvent event;
    java.util.List<OnlineStore.CorpseState> corpses=java.util.List.of();
    CompletableFuture<java.util.List<OnlineStore.CorpseState>> corpseRead;
    Binding(String token, OnlineStore.Session session, PreviewWorld.PlayerView position) {
      this.token=token; this.session=session; this.saved=session.character(); this.position=position;
    }
  }
  private record GameplayEvent(UUID id, long gold, long experience, CharacterProgress progress, boolean worldReward,
      OnlineStore.CreatureStrike strike, OnlineStore.CreatureProvocation provocation,
      OnlineStore.MonsterAttack monsterAttack, OnlineStore.DeathTransition death, UUID corpse,
      com.perso.T4C.server.persistence.PortalTransfer transfer,
      CompletableFuture<OnlineStore.RewardResult> result) {}
  private record Completion(Binding binding, OnlineStore.CharacterState saved, boolean finalSave,
      long allowedUntil, OnlineStore.RewardResult reward, Exception error) {}

  PersistentSessions(OnlineStore store, PreviewMap map, PreviewWorld world) {
    this(store,new HostedWorlds(map,world));
  }

  PersistentSessions(OnlineStore store,HostedWorlds hosted) {
    this.store=store;this.hosted=hosted;
  }

  PreviewWorld.Joined join(OnlineStore.Session session, long acquisitionStarted) {
    if (System.nanoTime()-acquisitionStarted>=AUTHORITY_NANOS) throw new PreviewWorld.Rejected(503,"busy");
    if (bindings.size()>=PreviewWorld.MAX_PLAYERS) throw new PreviewWorld.Rejected(409,"full");
    var saved=session.character();
    if(hosted.plan.transitions && saved.progress()!=null
        && (saved.progress().respawn()==null || !hosted.canStand(saved.progress().respawn())))
      throw new PreviewWorld.Rejected(409,"unavailable_respawn");
    var region=hosted.require(saved.world(),saved.x(),saved.y());var map=region.map;var world=region.world;
    long appearance=saved.progress()==null ? session.characterId().getLeastSignificantBits() & 1L
        : "female".equals(saved.progress().gender()) ? 0 : 1;
    var joined=world.joinAt(saved.x()-map.originX,saved.y()-map.originY,appearance,nextPlayerId++);
    var binding=new Binding(joined.token(),session,world.position(joined.token()));
    binding.region=region;binding.awaitingRegionReady=hosted.plan.transitions;
    if(binding.awaitingRegionReady) world.freeze(joined.token(),true);
    binding.checkpointSubmitted=tick;
    binding.allowedUntil=acquisitionStarted+AUTHORITY_NANOS;
    bindings.put(joined.token(),binding);
    publishHealth(binding);
    return joined;
  }

  CompletableFuture<Void> leave(String token) {
    var binding=bindings.get(token);
    if (binding==null) throw new PreviewWorld.Rejected(401,"session");
    var world=binding.region.world;var map=binding.region.map;
    var position=world.position(token);
    if (position!=null) binding.position=position;
    world.remove(token); binding.closing=true; invalidateRead(binding);
    if (!binding.pending) submit(binding);
    return binding.closed;
  }

  /** Committed state only, for server gameplay producers running on the simulation thread. */
  OnlineStore.CharacterState character(String token) {
    return active(token).saved;
  }

  String tokenForCharacter(UUID id) {
    for (var binding:bindings.values())
      if (binding.saved.id().equals(id) && !binding.closing && binding.region.world.position(binding.token)!=null
          && System.nanoTime()<binding.allowedUntil) return binding.token;
    return null;
  }

  CompletionStage<java.util.List<OnlineStore.CorpseState>> corpses(String token) {
    var binding=active(token);
    if(binding.corpseRead!=null) return binding.corpseRead.minimalCompletionStage();
    var result=new CompletableFuture<java.util.List<OnlineStore.CorpseState>>();
    binding.corpseRead=result;
    try { lookups.execute(() -> {
      try {
        var found=store.listCorpses(binding.session);
        reads.add(() -> completeRead(binding,result,found,null));
      } catch (Exception failure) {
        reads.add(() -> completeRead(binding,result,null,new IllegalStateException("Corpse lookup unavailable")));
      }
    }); } catch (RejectedExecutionException busy) {
      binding.corpseRead=null;
      var rejected=new PreviewWorld.Rejected(503,"busy");result.completeExceptionally(rejected);throw rejected;
    }
    return result.minimalCompletionStage();
  }

  private void completeRead(Binding binding,CompletableFuture<java.util.List<OnlineStore.CorpseState>> result,
      java.util.List<OnlineStore.CorpseState> found,Exception failure) {
    if(binding.corpseRead!=result) { result.completeExceptionally(new PreviewWorld.Rejected(401,"session"));return; }
    binding.corpseRead=null;
    try {
      if(active(binding.token)!=binding) throw new PreviewWorld.Rejected(401,"session");
      if(failure!=null) { result.completeExceptionally(failure);return; }
      binding.corpses=found;result.complete(found);
    } catch(RuntimeException expired) { result.completeExceptionally(expired); }
  }

  private static void invalidateRead(Binding binding) {
    var pending=binding.corpseRead;binding.corpseRead=null;
    if(pending!=null) pending.completeExceptionally(new PreviewWorld.Rejected(401,"session"));
  }

  CompletionStage<com.perso.T4C.online.model.NpcInteractionReply> interact(String token,long revision,String npcId) {
    var binding=active(token);
    if(binding.event!=null || binding.awaitingRegionReady) throw new PreviewWorld.Rejected(409,"busy");
    if(binding.saved.revision()!=revision) throw new PreviewWorld.Rejected(409,"revision");
    if(binding.saved.progress()==null) throw new PreviewWorld.Rejected(409,"uninitialized_character");
    var position=binding.region.world.position(token);
    OnlineNpcInteractions.Result draft;
    try {
      if(binding.region.npcs==null) throw new IllegalArgumentException("No NPC catalog");
      draft=binding.region.npcs.prepare(binding.saved.progress(),binding.region.map,position.x(),position.y(),npcId);
    } catch(IllegalArgumentException unavailable) { throw new PreviewWorld.Rejected(409,"npc_unavailable"); }
    if(!draft.changed()) return CompletableFuture.completedFuture(
        new com.perso.T4C.online.model.NpcInteractionReply(binding.saved.revision(),draft.messageKey(),false));
    return commitGameplayEvent(token,revision,UUID.randomUUID(),0,0,draft.progress())
        .thenApply(committed->new com.perso.T4C.online.model.NpcInteractionReply(
            committed.character().revision(),draft.messageKey(),true));
  }

  CompletionStage<OnlineStore.RewardResult> recoverCorpse(String token,long revision,UUID corpse) {
    var binding=active(token);
    if (binding.saved.progress()==null || binding.saved.progress().vitals().hp()<=0)
      throw new PreviewWorld.Rejected(409,"dead");
    var found=binding.corpses.stream().filter(c -> c.id().equals(corpse)).findFirst()
        .orElseThrow(() -> new PreviewWorld.Rejected(409,"corpse_unavailable"));
    var world=binding.region.world;var map=binding.region.map;
    var position=world.position(token);
    if (found.world()!=binding.region.spec.world() || Math.hypot((map.originX+position.x()-found.x())*com.perso.T4C.config.GameConstants.GRID_W,
        (map.originY+position.y()-found.y())*com.perso.T4C.config.GameConstants.GRID_H)>com.perso.T4C.config.GameConstants.PLAYER_MELEE_ATTACK_RANGE)
      throw new PreviewWorld.Rejected(409,"out_of_range");
    binding.position=position;
    return commitEvent(token,revision,UUID.randomUUID(),0,0,binding.saved.progress(),false,null,null,null,null,
        Objects.requireNonNull(corpse));
  }

  /**
   * Accept one immutable server-authored result per character. A checkpoint already in flight may
   * finish first; no second gameplay result can be based on state that has not committed yet.
   * This is deliberately not a network command accepting client-provided rewards or attributes.
   */
  CompletionStage<OnlineStore.RewardResult> commitGameplayEvent(String token, long expectedRevision,
      UUID eventId, long gold, long experience, CharacterProgress progress) {
    return commitGameplayEvent(token,expectedRevision,eventId,gold,experience,progress,null);
  }

  CompletionStage<OnlineStore.RewardResult> commitGameplayEvent(String token,long expectedRevision,
      UUID eventId,long gold,long experience,CharacterProgress progress,OnlineStore.CreatureProvocation provocation) {
    return commitEvent(token,expectedRevision,eventId,gold,experience,progress,false,null,provocation,null,null,null);
  }

  CompletionStage<OnlineStore.RewardResult> commitWorldReward(String token, long expectedRevision,
      UUID eventId, long gold, long experience, CharacterProgress progress) {
    return commitEvent(token,expectedRevision,eventId,gold,experience,progress,true,null,null,null,null,null);
  }

  CompletionStage<OnlineStore.RewardResult> commitCreatureStrike(String token, long expectedRevision,
      UUID eventId, long gold, long experience, CharacterProgress progress, OnlineStore.CreatureStrike strike) {
    return commitCreatureStrike(token,expectedRevision,eventId,gold,experience,progress,strike,null);
  }

  CompletionStage<OnlineStore.RewardResult> commitCreatureStrike(String token,long expectedRevision,
      UUID eventId,long gold,long experience,CharacterProgress progress,OnlineStore.CreatureStrike strike,
      OnlineStore.CreatureProvocation provocation) {
    Objects.requireNonNull(strike);
    if ((strike.after()==0)!=Objects.requireNonNull(eventId).equals(strike.life()))
      throw new IllegalArgumentException("Invalid strike event identity");
    return commitEvent(token,expectedRevision,eventId,gold,experience,progress,false,strike,provocation,null,null,null);
  }

  CompletionStage<OnlineStore.RewardResult> commitMonsterAttack(String token,long revision,UUID event,
      CharacterProgress progress,OnlineStore.MonsterAttack attack,OnlineStore.DeathTransition death) {
    return commitEvent(token,revision,event,0,0,progress,false,null,null,Objects.requireNonNull(attack),death,null);
  }

  private CompletionStage<OnlineStore.RewardResult> commitEvent(String token, long expectedRevision,
      UUID eventId, long gold, long experience, CharacterProgress progress, boolean worldReward, OnlineStore.CreatureStrike strike,
      OnlineStore.CreatureProvocation provocation,OnlineStore.MonsterAttack attack,OnlineStore.DeathTransition death,UUID corpse) {
    var binding=active(token);
    if (binding.event!=null || binding.awaitingRegionReady) throw new PreviewWorld.Rejected(409,"busy");
    if (binding.saved.revision()!=expectedRevision) throw new PreviewWorld.Rejected(409,"revision");
    if (gold<0 || experience<0) throw new IllegalArgumentException("Negative reward");
    var result=new CompletableFuture<OnlineStore.RewardResult>();
    binding.event=new GameplayEvent(Objects.requireNonNull(eventId),gold,experience,
        Objects.requireNonNull(progress),worldReward,strike,provocation,attack,death,corpse,null,result);
    if (death!=null || corpse!=null) binding.region.world.freeze(token,true);
    if (!binding.pending) submit(binding);
    return result.minimalCompletionStage();
  }

  com.perso.T4C.online.model.OnlineRegion region(String token) {
    var binding=active(token);return binding.region.descriptor(binding.saved.locationEpoch());
  }
  void requireEpoch(String token,long epoch) {
    var binding=active(token);if(epoch!=binding.saved.locationEpoch()) throw new PreviewWorld.Rejected(409,"region_epoch");
  }
  void regionReady(String token,long epoch) {
    requireEpoch(token,epoch);var binding=active(token);
    if(binding.event!=null) throw new PreviewWorld.Rejected(409,"busy");
    binding.awaitingRegionReady=false;binding.region.world.freeze(token,false);
  }
  boolean belongs(String token,PreviewWorld world) {
    var binding=bindings.get(token);return binding!=null && !binding.closing && binding.region.world==world;
  }
  CharacterProgress.Respawn respawnDestination(CharacterProgress.Respawn destination) {
    if(destination==null || !hosted.canStand(destination)) throw new PreviewWorld.Rejected(409,"unavailable_respawn");
    return destination;
  }
  private void maybeTransfer(Binding binding) {
    if(!hosted.plan.transitions || binding.closing || binding.event!=null || binding.awaitingRegionReady
        || binding.saved.portalCooldownUntil()>System.currentTimeMillis()) return;
    var position=binding.region.world.position(binding.token);if(position==null) return;
    var map=binding.region.map;
    var selected=hosted.plan.portals.resolve(binding.region.spec.world(),map.originX+position.x(),map.originY+position.y(),hosted::canStand);
    if(selected.isEmpty()) return;
    var portal=selected.get();var target=hosted.require(portal.destination().world(),portal.destination().x(),portal.destination().y());
    var draft=new com.perso.T4C.server.persistence.PortalTransfer(portal.portalId(),binding.region.spec.key(),target.spec.key(),
        portal.source(),portal.destination(),binding.saved.locationEpoch(),Math.addExact(System.currentTimeMillis(),350));
    binding.position=position;binding.region.world.freeze(binding.token,true);
    binding.event=new GameplayEvent(UUID.randomUUID(),0,0,binding.saved.progress(),false,null,null,null,null,null,draft,new CompletableFuture<>());
  }

  private Binding active(String token) {
    var binding=bindings.get(token);
    if (binding==null || binding.closing || binding.region.world.position(token)==null
        || System.nanoTime()>=binding.allowedUntil) throw new PreviewWorld.Rejected(401,"session");
    return binding;
  }

  void step() {
    tick++;
    drain();
    for (var binding:new ArrayList<>(bindings.values())) {
      var world=binding.region.world;
      var position=world.position(binding.token);
      if (position==null) { binding.closing=true;invalidateRead(binding); }
      else binding.position=position;
      if (System.nanoTime()>=binding.allowedUntil) { world.remove(binding.token); binding.closing=true;invalidateRead(binding); }
      maybeTransfer(binding);
      if (!binding.pending && (binding.event!=null || binding.closing || tick-binding.checkpointSubmitted>=CHECKPOINT_TICKS)) submit(binding);
    }
  }

  private void submit(Binding binding) {
    binding.pending=true;
    var position=binding.position;
    var region=binding.region;var map=region.map;
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
            saved=store.checkpoint(binding.session,revision,region.spec.world(),map.originX+position.x(),map.originY+position.y());
          } else {
            reward=event.transfer()!=null
                ? store.commitTransfer(binding.session,revision,event.id(),event.transfer())
                : event.corpse()!=null
                ? store.recoverCorpse(binding.session,revision,event.corpse(),region.spec.world(),map.originX+position.x(),map.originY+position.y())
                : event.monsterAttack()!=null
                ? store.commitMonsterAttack(binding.session,revision,event.id(),event.progress(),event.monsterAttack(),event.death())
                : event.strike()!=null
                ? store.commitCreatureStrike(binding.session,revision,event.id(),event.gold(),event.experience(),event.progress(),event.strike(),event.provocation())
                : event.worldReward()
                ? store.commitWorldReward(binding.session,revision,event.id(),event.gold(),event.experience(),event.progress())
                : store.commitGameplayEvent(binding.session,revision,event.id(),event.gold(),event.experience(),event.progress(),event.provocation());
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
    Runnable read;
    while ((read=reads.poll())!=null) read.run();
    Completion completed;
    while ((completed=completions.poll())!=null) {
      var binding=completed.binding();
      if (bindings.get(binding.token)!=binding) continue;
      binding.pending=false;
      if (completed.error()!=null) {
        // Fail closed on uncertain writes. Lease expiry permits recovery from the last DB commit.
        binding.region.world.remove(binding.token); bindings.remove(binding.token);
        invalidateRead(binding);
        if(binding.event!=null && (binding.event.death()!=null || binding.event.transfer()!=null)) hosted.reloadTargets(binding.saved.id());
        if (binding.event!=null) binding.event.result().completeExceptionally(new IllegalStateException("Persistence unavailable"));
        binding.closed.completeExceptionally(new IllegalStateException("Persistence unavailable"));
        System.err.println("Online persistence failed ("+failureCategory(completed.error())+"); character disconnected. Recover after lease expiry.");
      } else {
        binding.saved=completed.saved();
        publishHealth(binding);
        binding.allowedUntil=completed.allowedUntil();
        if (completed.reward()!=null) {
          var event=binding.event;
          binding.event=null;
          if (event.death()!=null || event.transfer()!=null) {
            var destination=hosted.require(binding.saved.world(),binding.saved.x(),binding.saved.y());
            var map=destination.map;
            binding.region.world.transferTo(destination.world,binding.token,binding.saved.x()-map.originX,binding.saved.y()-map.originY);
            binding.region=destination;binding.awaitingRegionReady=hosted.plan.transitions;
            hosted.reloadTargets(binding.saved.id());
            // Logout can remove the actor while death commits. Its final checkpoint must keep
            // the committed respawn destination, never the pre-death position.
            var before=binding.position;
            binding.position=new PreviewWorld.PlayerView(before.id(),binding.saved.x()-map.originX,
                binding.saved.y()-map.originY,before.sequence(),before.appearance(),
                binding.saved.progress().vitals().hp(),binding.saved.progress().vitals().maxHp());
          }
          if (event.death()!=null || event.corpse()!=null || event.transfer()!=null) binding.region.world.freeze(binding.token,binding.awaitingRegionReady);
          event.result().complete(completed.reward());
        }
        if (completed.finalSave()) { bindings.remove(binding.token);invalidateRead(binding);binding.closed.complete(null); }
      }
    }
  }

  private void publishHealth(Binding binding) {
    var progress=binding.saved.progress();
    if (progress!=null) binding.region.world.health(binding.token,progress.vitals().hp(),progress.vitals().maxHp());
  }

  /** Class/state only: exception messages can contain SQL values, private identifiers or credentials. */
  private static String failureCategory(Exception failure) {
    if(failure instanceof RejectedExecutionException) return "queue_rejected";
    String type=failure.getClass().getSimpleName();
    if(!type.matches("[A-Za-z0-9_$]{1,80}")) type="Exception";
    String state=failure instanceof java.sql.SQLException sql ? sql.getSQLState() : null;
    return "class="+type+", sqlstate="+(state!=null && state.matches("[A-Z0-9]{5}") ? state : "unavailable");
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
      lookups.shutdownNow();
      for (var binding:bindings.values()) {
        invalidateRead(binding);
        var error=new IllegalStateException("Server stopped before persistence completed");
        binding.closed.completeExceptionally(error);
        if (binding.event!=null) binding.event.result().completeExceptionally(error);
      }
      bindings.clear();
      try {
        long remaining=deadline-System.nanoTime();
        if(remaining>0) lookups.awaitTermination(remaining,TimeUnit.NANOSECONDS);
      } catch(InterruptedException interrupted) { Thread.currentThread().interrupt(); }
      reads.clear();
    }
  }
}
