package com.perso.T4C.server;

import com.perso.T4C.online.model.LootDropDraft;
import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.online.model.CharacterProgress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

/** Simulation-owned sessions; bounded workers perform all blocking checkpoints and lease renewal. */
final class PersistentSessions {
  private static final long CHECKPOINT_TICKS = PreviewWorld.TICK_RATE * 5L;
  // Stop simulation well before the database's 30-second lease can expire or be taken over.
  private static final long AUTHORITY_NANOS = TimeUnit.SECONDS.toNanos(20);
  /** Definite refusals from the claim transaction: nothing was written, so the actor stays connected. */
  private static final Set<String> PICKUP_REFUSALS=Set.of("revision","loot_unavailable","loot_reserved",
      "out_of_range","too_heavy","unsupported_item","unsupported_pickup");
  private final OnlineStore store;
  private final HostedWorlds hosted;
  private long nextPlayerId=1;
  private final Map<String, Binding> bindings = new HashMap<>();
  private final ConcurrentLinkedQueue<Completion> completions = new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<Runnable> reads = new ConcurrentLinkedQueue<>();
  private boolean sweepPending;
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
  /**
   * One gameplay write. A pickup carries {@code loot}; a lethal strike carries its committed {@code drops}.
   * Both are published to the region loot cache only after their transaction commits.
   */
  private record GameplayEvent(UUID id, long gold, long experience, CharacterProgress progress, boolean worldReward,
      OnlineStore.CreatureStrike strike, OnlineStore.CreatureProvocation provocation,
      OnlineStore.MonsterAttack monsterAttack, OnlineStore.DeathTransition death, UUID corpse,
      com.perso.T4C.server.persistence.PortalTransfer transfer, UUID loot, List<LootDropDraft> drops,
      CompletableFuture<OnlineStore.RewardResult> result) {
    GameplayEvent { drops=List.copyOf(drops); }
  }
  private record Completion(Binding binding, OnlineStore.CharacterState saved, boolean finalSave,
      long allowedUntil, OnlineStore.RewardResult reward, String refusal, Exception error) {}

  PersistentSessions(OnlineStore store, PreviewMap map, PreviewWorld world) throws SQLException {
    this(store,new HostedWorlds(map,world));
  }

  /** Loads every hosted region's live ground loot before any session can be admitted. */
  PersistentSessions(OnlineStore store,HostedWorlds hosted) throws SQLException {
    this.store=store;this.hosted=hosted;
    for (var region:hosted.regions) region.loot.replace(store.listGroundLoot(region.loot.world(),region.loot.area()));
  }

  PreviewWorld.Joined join(OnlineStore.Session session, long acquisitionStarted) {
    if (System.nanoTime()-acquisitionStarted>=AUTHORITY_NANOS) throw new PreviewWorld.Rejected(503,"busy");
    if (bindings.size()>=PreviewWorld.MAX_PLAYERS) throw new PreviewWorld.Rejected(409,"full");
    var saved=session.character();
    if(hosted.plan.transitions && saved.progress()!=null
        && (saved.progress().respawn()==null || !hosted.canStand(saved.progress().respawn())))
      throw new PreviewWorld.Rejected(409,"unavailable_respawn");
    var region=hosted.require(saved.world(),saved.x(),saved.y());var map=region.map;var world=region.world;
    if (!region.loot.loaded()) throw new PreviewWorld.Rejected(503,"busy");
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
      if (binding.saved.id().equals(id) && !binding.closing && !binding.awaitingRegionReady
          && binding.event==null && binding.region.world.position(binding.token)!=null
          && System.nanoTime()<binding.allowedUntil) return binding.token;
    return null;
  }

  /** Server-authored target eligibility; never uses a disconnected actor's last saved coordinates. */
  List<OnlineMonsterPursuit.TargetView> targetViews() {
    var views=new ArrayList<OnlineMonsterPursuit.TargetView>();
    long now=System.nanoTime();
    for(var binding:bindings.values()) {
      if(binding.closing) continue;
      var region=binding.region;
      var position=region.world.position(binding.token);
      if(position==null) continue;
      var progress=binding.saved.progress();
      boolean alive=progress!=null && progress.vitals().hp()>0;
      boolean supported=alive && progress.companion()==null
          && progress.skills().getOrDefault("stun_blow",0)==0
          && OnlineQuestFlags.supported(progress.questFlags(),QuestCatalog.bundled());
      if(supported) try {
        var loadout=OnlineCombatLoadout.resolve(progress,hosted.items,System.currentTimeMillis(),false);
        supported=loadout.profile().skill("stun_blow")==0
            && (loadout.primary()==null || !loadout.primary().bow())
            && (loadout.secondary()==null || !loadout.secondary().bow());
      } catch(IllegalArgumentException unsupported) { supported=false; }
      boolean ready=!binding.awaitingRegionReady && binding.event==null && supported;
      views.add(new OnlineMonsterPursuit.TargetView(binding.saved.id(),binding.saved.locationEpoch(),
          region.spec.world(),region.spec.key(),position.x(),position.y(),alive,ready,
          now<binding.allowedUntil));
    }
    return views;
  }

  /** Only the storage worker receives this private lease handle for target acquisition fencing. */
  OnlineStore.Session sessionForCharacter(UUID id) {
    var token=tokenForCharacter(id);
    if(token==null) return null;
    var binding=bindings.get(token);
    return binding.awaitingRegionReady || binding.event!=null ? null : binding.session;
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
    return interact(token,revision,npcId,OptionalInt.empty());
  }

  /**
   * Greeting (empty topic) or an authored topic of an NPC. A draft that changes state commits once with its
   * own gold and experience; a no-op returns the committed revision. An uncertain commit disconnects the actor
   * through the shared persistence path, and nothing here replays it.
   */
  CompletionStage<com.perso.T4C.online.model.NpcInteractionReply> interact(String token,long revision,String npcId,
      OptionalInt topic) {
    var binding=active(token);
    if(binding.event!=null || binding.awaitingRegionReady) throw new PreviewWorld.Rejected(409,"busy");
    if(binding.saved.revision()!=revision) throw new PreviewWorld.Rejected(409,"revision");
    if(binding.saved.progress()==null) throw new PreviewWorld.Rejected(409,"uninitialized_character");
    var position=binding.region.world.position(token);
    OnlineNpcInteractions.Result draft;
    try {
      if(binding.region.npcs==null) throw new IllegalArgumentException("No NPC catalog");
      draft=binding.region.npcs.prepare(binding.saved,binding.region.map,position.x(),position.y(),npcId,topic,
          System.currentTimeMillis(),ThreadLocalRandom.current());
    } catch(IllegalArgumentException unavailable) { throw new PreviewWorld.Rejected(409,"npc_unavailable"); }
    if(!draft.changed()) return CompletableFuture.completedFuture(
        new com.perso.T4C.online.model.NpcInteractionReply(binding.saved.revision(),draft.messageKey(),false,draft.args()));
    return commitGameplayEvent(token,revision,UUID.randomUUID(),draft.gold(),draft.experience(),draft.progress())
        .thenApply(committed->new com.perso.T4C.online.model.NpcInteractionReply(
            committed.character().revision(),draft.messageKey(),true,draft.args()));
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
    return commitCreatureStrike(token,expectedRevision,eventId,gold,experience,progress,strike,provocation,List.of());
  }

  /** A lethal strike may carry its rolled drops, which commit and publish together with the kill. */
  CompletionStage<OnlineStore.RewardResult> commitCreatureStrike(String token,long expectedRevision,
      UUID eventId,long gold,long experience,CharacterProgress progress,OnlineStore.CreatureStrike strike,
      OnlineStore.CreatureProvocation provocation,List<LootDropDraft> drops) {
    Objects.requireNonNull(strike);
    if ((strike.after()==0)!=Objects.requireNonNull(eventId).equals(strike.life()))
      throw new IllegalArgumentException("Invalid strike event identity");
    if (!Objects.requireNonNull(drops).isEmpty() && strike.after()!=0) throw new IllegalArgumentException("Loot requires a lethal strike");
    return commitEvent(token,expectedRevision,eventId,gold,experience,progress,false,strike,provocation,null,null,null,drops);
  }

  /**
   * Single-flight pickup of one committed drop. The database claim is authoritative; the simulation
   * only fences the epoch and rejects what its cache already knows is impossible.
   */
  CompletionStage<OnlineStore.RewardResult> pickup(String token,long revision,long epoch,UUID loot) {
    var binding=active(token);
    requireEpoch(token,epoch);
    if (binding.event!=null || binding.awaitingRegionReady) throw new PreviewWorld.Rejected(409,"busy");
    if (binding.saved.revision()!=revision) throw new PreviewWorld.Rejected(409,"revision");
    var progress=binding.saved.progress();
    if (progress==null) throw new PreviewWorld.Rejected(409,"uninitialized_character");
    if (progress.vitals().hp()<=0) throw new PreviewWorld.Rejected(409,"dead");
    if (hosted.items==null) throw new PreviewWorld.Rejected(409,"unsupported_pickup");
    var region=binding.region;
    if (!region.loot.loaded()) throw new PreviewWorld.Rejected(503,"busy");
    var found=region.loot.get(Objects.requireNonNull(loot));
    if (found==null) throw new PreviewWorld.Rejected(409,"loot_unavailable");
    var position=region.world.position(token);
    binding.position=position;
    double x=region.map.originX+position.x(),y=region.map.originY+position.y();
    if (Math.max(Math.abs(x-found.x()),Math.abs(y-found.y()))>com.perso.T4C.server.OnlinePickupRules.MAX_TILE_DISTANCE)
      throw new PreviewWorld.Rejected(409,"out_of_range");
    var result=new CompletableFuture<OnlineStore.RewardResult>();
    binding.event=new GameplayEvent(UUID.randomUUID(),0,0,progress,false,null,null,null,null,null,null,loot,List.of(),result);
    if (!binding.pending) submit(binding);
    return result.minimalCompletionStage();
  }

  CompletionStage<OnlineStore.RewardResult> commitMonsterAttack(String token,long revision,UUID event,
      CharacterProgress progress,OnlineStore.MonsterAttack attack,OnlineStore.DeathTransition death) {
    return commitEvent(token,revision,event,0,0,progress,false,null,null,Objects.requireNonNull(attack),death,null);
  }

  private CompletionStage<OnlineStore.RewardResult> commitEvent(String token, long expectedRevision,
      UUID eventId, long gold, long experience, CharacterProgress progress, boolean worldReward, OnlineStore.CreatureStrike strike,
      OnlineStore.CreatureProvocation provocation,OnlineStore.MonsterAttack attack,OnlineStore.DeathTransition death,UUID corpse) {
    return commitEvent(token,expectedRevision,eventId,gold,experience,progress,worldReward,strike,provocation,attack,death,corpse,List.of());
  }

  private CompletionStage<OnlineStore.RewardResult> commitEvent(String token, long expectedRevision,
      UUID eventId, long gold, long experience, CharacterProgress progress, boolean worldReward, OnlineStore.CreatureStrike strike,
      OnlineStore.CreatureProvocation provocation,OnlineStore.MonsterAttack attack,OnlineStore.DeathTransition death,UUID corpse,
      List<LootDropDraft> drops) {
    var binding=active(token);
    if (binding.event!=null || binding.awaitingRegionReady) throw new PreviewWorld.Rejected(409,"busy");
    if (binding.saved.revision()!=expectedRevision) throw new PreviewWorld.Rejected(409,"revision");
    if (gold<0 || experience<0) throw new IllegalArgumentException("Negative reward");
    var result=new CompletableFuture<OnlineStore.RewardResult>();
    binding.event=new GameplayEvent(Objects.requireNonNull(eventId),gold,experience,
        Objects.requireNonNull(progress),worldReward,strike,provocation,attack,death,corpse,null,null,drops,result);
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
    binding.event=new GameplayEvent(UUID.randomUUID(),0,0,binding.saved.progress(),false,null,null,null,null,null,draft,null,List.of(),new CompletableFuture<>());
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
    // Once per second: pending database loot reads and the expiry sweep, both off the tick thread.
    if (tick % PreviewWorld.TICK_RATE == 0) {
      for (var region:hosted.regions) if (region.loot.needsRead()) startLootRead(region);
      if (!sweepPending) startSweep();
    }
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
    var content=hosted.items;
    try {
      writes.execute(() -> {
        OnlineStore.CharacterState saved=null; Exception failure=null;
        OnlineStore.RewardResult reward=null; String refusal=null;
        long allowedUntil=System.nanoTime()+AUTHORITY_NANOS;
        try {
          store.renew(binding.session);
          if (event==null) {
            saved=store.checkpoint(binding.session,revision,region.spec.world(),map.originX+position.x(),map.originY+position.y());
          } else if (event.loot()!=null) {
            try {
              reward=store.claimGroundLoot(binding.session,revision,event.id(),event.loot(),region.spec.world(),
                  map.originX+position.x(),map.originY+position.y(),content);
              saved=reward.character();
            } catch (OnlineStore.Conflict conflict) {
              // Only the claim's own refusals are definite. Lease loss or a payload mismatch fails closed.
              if (!PICKUP_REFUSALS.contains(conflict.getMessage())) throw conflict;
              refusal=conflict.getMessage();
            }
          } else {
            reward=event.transfer()!=null
                ? store.commitTransfer(binding.session,revision,event.id(),event.transfer())
                : event.corpse()!=null
                ? store.recoverCorpse(binding.session,revision,event.corpse(),region.spec.world(),map.originX+position.x(),map.originY+position.y())
                : event.monsterAttack()!=null
                ? store.commitMonsterAttack(binding.session,revision,event.id(),event.progress(),event.monsterAttack(),event.death())
                : event.strike()!=null
                ? store.commitCreatureStrike(binding.session,revision,event.id(),event.gold(),event.experience(),event.progress(),event.strike(),event.provocation(),event.drops())
                : event.worldReward()
                ? store.commitWorldReward(binding.session,revision,event.id(),event.gold(),event.experience(),event.progress())
                : store.commitGameplayEvent(binding.session,revision,event.id(),event.gold(),event.experience(),event.progress(),event.provocation());
            saved=reward.character();
          }
          if (finalSave) store.release(binding.session);
        } catch (Exception error) { failure=error; }
        completions.add(new Completion(binding,saved,finalSave,allowedUntil,reward,refusal,failure));
      });
    } catch (RejectedExecutionException error) {
      completions.add(new Completion(binding,null,finalSave,0,null,null,error));
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
        // An uncertain pickup or drop may or may not have committed: rebuild the region from the database. Never replay.
        if(binding.event!=null && (binding.event.loot()!=null || !binding.event.drops().isEmpty())) refreshLoot(binding.region);
        if (binding.event!=null) binding.event.result().completeExceptionally(new IllegalStateException("Persistence unavailable"));
        binding.closed.completeExceptionally(new IllegalStateException("Persistence unavailable"));
        System.err.println("Online persistence failed ("+com.perso.T4C.server.persistence.PersistenceFailure.describe(completed.error())+"); character disconnected. Recover after lease expiry.");
      } else if (completed.refusal()!=null) {
        // A definite refusal rolled back before any write, so the character stays connected.
        binding.allowedUntil=completed.allowedUntil();
        var refused=binding.event;
        binding.event=null;
        // The database says this drop is no longer on the ground, so the cache was stale: drop it and re-sync.
        if ("loot_unavailable".equals(completed.refusal())) {
          binding.region.loot.remove(refused.loot());
          refreshLoot(binding.region);
        }
        refused.result().completeExceptionally(new PreviewWorld.Rejected(409,completed.refusal()));
      } else {
        binding.saved=completed.saved();
        publishHealth(binding);
        binding.allowedUntil=completed.allowedUntil();
        if (completed.reward()!=null) {
          var event=binding.event;
          var lootRegion=binding.region;
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
          publishLoot(binding,lootRegion,event,completed.reward());
          event.result().complete(completed.reward());
        }
        if (completed.finalSave()) { bindings.remove(binding.token);invalidateRead(binding);binding.closed.complete(null); }
      }
    }
  }

  /** Runs only after a commit. A claimed drop leaves the cache; committed kill drops join it. */
  private void publishLoot(Binding binding,HostedWorlds.Region region,GameplayEvent event,OnlineStore.RewardResult reward) {
    if (event.loot()!=null) region.loot.remove(event.loot());
    if (event.drops().isEmpty()) return;
    // A replayed kill's drops may already be claimed, so the database decides what this region holds.
    if (!reward.applied()) { refreshLoot(region); return; }
    boolean published=true;
    for (var drop:event.drops()) {
      var loot=new OnlineStore.GroundLoot(drop.id(),drop.world(),drop.x(),drop.y(),drop.key(),drop.quantity(),
          drop.charges(),binding.saved.id(),OnlineStore.LOOT_OWNER_WINDOW_MILLIS);
      if (region.loot.owns(loot)) region.loot.publish(loot); else published=false;
    }
    if (!published) refreshLoot(region);
  }

  /** Uncertain or unexplained loot state: the next database read replaces this region's cache. */
  private void refreshLoot(HostedWorlds.Region region) {
    region.loot.invalidate();
    if (!region.loot.reading()) startLootRead(region);
  }

  private void startLootRead(HostedWorlds.Region region) {
    var loot=region.loot;
    loot.beginRead();
    try { lookups.execute(() -> {
      java.util.List<OnlineStore.GroundLoot> found;
      try { found=store.listGroundLoot(loot.world(),loot.area()); }
      catch (SQLException | RuntimeException failure) { found=null; }
      var result=found;
      reads.add(() -> finishLootRead(region,result));
    }); } catch (RejectedExecutionException busy) { loot.abortRead(); }
  }

  private void finishLootRead(HostedWorlds.Region region,java.util.List<OnlineStore.GroundLoot> found) {
    region.loot.endRead(found);
    // A commit that landed while this read was running is not in it; read once more. Failures retry on cadence.
    if (found!=null && region.loot.needsRead()) startLootRead(region);
  }

  /** Bounded expiry sweep on the write queue, single-flight: one sweep at a time. */
  private void startSweep() {
    sweepPending=true;
    try { writes.execute(() -> {
      java.util.List<OnlineStore.ExpiredLoot> expired;
      try { expired=store.expireGroundLoot(OnlineStore.GROUND_LOOT_LIST_LIMIT); }
      catch (SQLException | RuntimeException failure) { expired=null; }
      var result=expired;
      reads.add(() -> finishSweep(result));
    }); } catch (RejectedExecutionException busy) { sweepPending=false; }
  }

  private void finishSweep(java.util.List<OnlineStore.ExpiredLoot> expired) {
    sweepPending=false;
    if (expired==null) { for (var region:hosted.regions) refreshLoot(region); return; }
    for (var loot:expired)
      for (var region:hosted.regions) if (region.loot.world()==loot.world()) region.loot.remove(loot.id());
  }

  /** Test and diagnostic seam: whether this actor's region currently publishes the drop. */
  boolean groundLootPublished(String token,UUID loot) {
    return active(token).region.loot.contains(loot);
  }

  private void publishHealth(Binding binding) {
    var progress=binding.saved.progress();
    if (progress!=null) binding.region.world.health(binding.token,progress.vitals().hp(),progress.vitals().maxHp());
  }

  /** Runs on the simulation owner after ticking stops. Bounded graceful flush; never invent a save. */
  void shutdown() {
    shutdown(TimeUnit.SECONDS.toNanos(12),TimeUnit.SECONDS.toNanos(12));
  }

  /** Shorter budgets are a test seam; production retains the twelve-second accepted-write drain. */
  void shutdown(long flushNanos,long terminationNanos) {
    var closing=new ArrayList<CompletableFuture<Void>>();
    for (var binding:new ArrayList<>(bindings.values())) closing.add(leave(binding.token));
    long deadline=System.nanoTime()+flushNanos;
    boolean interrupted=Thread.interrupted();
    boolean incomplete=false,workersStopped=false;
    try {
      while (!bindings.isEmpty() && System.nanoTime()<deadline) {
        step();
        try { Thread.sleep(10); } catch(InterruptedException signal) { interrupted=true; }
      }
    } finally {
      incomplete=!bindings.isEmpty() || closing.stream().anyMatch(CompletableFuture::isCompletedExceptionally);
      writes.shutdownNow();lookups.shutdownNow();
      for (var binding:bindings.values()) {
        invalidateRead(binding);
        var error=new IllegalStateException("Server stopped before persistence completed");
        binding.closed.completeExceptionally(error);
        if (binding.event!=null) binding.event.result().completeExceptionally(error);
      }
      bindings.clear();
      deadline=System.nanoTime()+terminationNanos;
      for(var executor:java.util.List.of(writes,lookups)) {
        while(!executor.isTerminated() && System.nanoTime()<deadline) {
          try { executor.awaitTermination(Math.max(1,deadline-System.nanoTime()),TimeUnit.NANOSECONDS); }
          catch(InterruptedException signal) { interrupted=true; }
        }
      }
      workersStopped=writes.isTerminated() && lookups.isTerminated();
      reads.clear();completions.clear();
      if(interrupted) Thread.currentThread().interrupt();
    }
    if(!workersStopped) throw new IllegalStateException("Persistence workers did not terminate; shutdown requires forced database abort");
    if(incomplete) throw new IllegalStateException("Persistence flush incomplete; recover from last committed state after lease expiry");
  }
}
