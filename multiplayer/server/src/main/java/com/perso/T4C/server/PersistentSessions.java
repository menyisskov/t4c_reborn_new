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
  private static final Set<String> CORPSE_REFUSALS=Set.of(OnlineStore.CORPSE_TOO_HEAVY,OnlineStore.CORPSE_ITEM_UNAVAILABLE);
  private static final Set<String> SHOP_REFUSALS=OnlineStore.SHOP_REFUSALS;
  private static final Set<String> PICKUP_REFUSALS=Set.of("revision","loot_unavailable","loot_reserved",
      "out_of_range","too_heavy","unsupported_item","unsupported_pickup",OnlinePickupRules.LOOT_TAKEN);
  record FriendTarget(long actorId,String token,long revision,double x,double y,boolean alive,CharacterProgress progress) {}
  private final OnlineStore store;
  private final HostedWorlds hosted;
  /** Wall clock for absolute expiries and regeneration anchors; tests replace it. */
  java.util.function.LongSupplier wallClock=System::currentTimeMillis;
  private SpellCatalog spellCatalog;
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
    /** Earliest tick a portal transfer refused for a cooldown the database clock has not reached may be proposed again. */
    long transferRetryTick;
    /** In-memory progress (regeneration, join anchor) the next checkpoint must persist. */
    boolean progressUnsaved;
    /** Wall-clock join time: the earliest instant natural regeneration may count from. */
    long joinedAt;
    HostedWorlds.Region region;
    GameplayEvent event;
    java.util.List<OnlineStore.CorpseState> corpses=java.util.List.of();
    CompletableFuture<java.util.List<OnlineStore.CorpseState>> corpseRead;
    Binding(String token, OnlineStore.Session session, PreviewWorld.PlayerView position) {
      this.token=token; this.session=session; this.saved=session.character(); this.position=position;
    }
  }
  private record PlayerHeal(Binding recipient,long revision,int amount) {}
  /**
   * One gameplay write. A pickup carries {@code loot}; a lethal strike carries its committed {@code drops}.
   * Both are published to the region loot cache only after their transaction commits.
   */
  private record GameplayEvent(UUID id, long gold, long experience, CharacterProgress progress, boolean worldReward,
      OnlineStore.CreatureStrike strike, OnlineStore.CreatureProvocation provocation,
      OnlineStore.MonsterAttack monsterAttack, OnlineStore.DeathTransition death, UUID corpse, UUID corpseItem,
      com.perso.T4C.server.persistence.PortalTransfer transfer, UUID loot, List<LootDropDraft> drops,
      List<OnlineStore.SpellStrike> spellStrikes, OnlineStore.MonsterHeal monsterHeal,
      CompletableFuture<OnlineStore.RewardResult> result, OnlineShopTrades.Trade shop,PlayerHeal playerHeal) {
    GameplayEvent { drops=List.copyOf(drops); spellStrikes=spellStrikes==null ? List.of() : List.copyOf(spellStrikes); }
    GameplayEvent(UUID id, long gold, long experience, CharacterProgress progress, boolean worldReward,
        OnlineStore.CreatureStrike strike, OnlineStore.CreatureProvocation provocation,
        OnlineStore.MonsterAttack monsterAttack, OnlineStore.DeathTransition death, UUID corpse, UUID corpseItem,
        com.perso.T4C.server.persistence.PortalTransfer transfer, UUID loot, List<LootDropDraft> drops,
        List<OnlineStore.SpellStrike> spellStrikes, CompletableFuture<OnlineStore.RewardResult> result) {
      this(id,gold,experience,progress,worldReward,strike,provocation,monsterAttack,death,corpse,corpseItem,transfer,
          loot,drops,spellStrikes,null,result,null,null);
    }
    GameplayEvent(UUID id,long gold,long experience,CharacterProgress progress,boolean worldReward,
        OnlineStore.CreatureStrike strike,OnlineStore.CreatureProvocation provocation,
        OnlineStore.MonsterAttack monsterAttack,OnlineStore.DeathTransition death,UUID corpse,UUID corpseItem,
        com.perso.T4C.server.persistence.PortalTransfer transfer,UUID loot,List<LootDropDraft> drops,
        List<OnlineStore.SpellStrike> spellStrikes,OnlineStore.MonsterHeal monsterHeal,
        CompletableFuture<OnlineStore.RewardResult> result,OnlineShopTrades.Trade shop) {
      this(id,gold,experience,progress,worldReward,strike,provocation,monsterAttack,death,corpse,corpseItem,
          transfer,loot,drops,spellStrikes,monsterHeal,result,shop,null);
    }
  }
  private record Completion(Binding binding, OnlineStore.CharacterState saved, boolean finalSave,
      long allowedUntil, OnlineStore.RewardResult reward, String refusal, Exception error,
      com.perso.T4C.server.persistence.PersistenceFailure.Operation operation,OnlineStore.CharacterState partnerSaved) {}

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
    // Time spent logged out never regenerates: settling never counts time before this join.
    binding.joinedAt=wallClock.getAsLong();
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
    var binding=active(token);
    settleRegeneration(binding);
    return binding.saved;
  }

  /**
   * Brings the in-memory committed state up to date with natural regeneration. Every producer that
   * reads or changes vitals (cast, potion, melee, retaliation, equipment, quests) reads through here,
   * so its draft already contains the settled vitals and anchor; the checkpoint persists changed vitals.
   * A persisted anchor older than this join is raised to the join time, so logged-out time never counts.
   * Settled state is kept only when it matters: vitals changed, or ticks were consumed while below maximum
   * (dropping those would let a later call roll the same ticks again). A full or dead character keeps its
   * committed state untouched.
   */
  private void settleRegeneration(Binding binding) {
    var saved=binding.saved;var progress=saved.progress();
    if(progress==null) return;
    long now=wallClock.getAsLong();
    // Logged-out time never runs a buff down (shifted at join); expired buffs give their maximum health back.
    var effects=OnlineBuffs.shiftForJoin(progress,binding.joinedAt);
    boolean effectsChanged=effects!=progress;
    var base=effects.cooldowns().getOrDefault(OnlineRegeneration.ANCHOR,-1L)<binding.joinedAt
        ? OnlineRegeneration.reanchor(effects,binding.joinedAt) : effects;
    CharacterProgress settled;
    try {
      settled=hosted.items==null ? OnlineRegeneration.settle(base,now,ThreadLocalRandom.current(),Map.of())
          : OnlineRegeneration.settle(base,now,ThreadLocalRandom.current(),hosted.items);
    } catch (IllegalArgumentException unknownEquipment) { return; }
    effectsChanged|=!settled.buffs().equals(progress.buffs());
    var vitals=settled.vitals();
    boolean changed=!vitals.equals(progress.vitals());
    boolean idle=vitals.hp()==0 || (vitals.hp()==vitals.maxHp() && vitals.mana()==vitals.maxMana());
    long anchorBefore=base.cooldowns().getOrDefault(OnlineRegeneration.ANCHOR,-1L);
    boolean consumedTicks=settled.cooldowns().getOrDefault(OnlineRegeneration.ANCHOR,-1L)!=anchorBefore;
    if(!changed && !effectsChanged && (idle || !consumedTicks)) return;
    if(changed || effectsChanged) binding.progressUnsaved=true;
    binding.saved=withProgress(saved,settled);
  }

  private static OnlineStore.CharacterState withProgress(OnlineStore.CharacterState s,CharacterProgress progress) {
    return new OnlineStore.CharacterState(s.id(),s.accountId(),s.name(),s.world(),s.x(),s.y(),s.gold(),s.experience(),
        s.revision(),progress,s.locationEpoch(),s.portalCooldownUntil());
  }

  /** True while physical exhaustion from a cast or potion still forbids movement. */
  boolean movementExhausted(String token) {
    var progress=active(token).saved.progress();
    return progress!=null && progress.cooldowns().getOrDefault(OnlineCasting.PHYSICAL_EXHAUSTION,0L)>wallClock.getAsLong();
  }

  /** What a committed self-cast or potion did, for the acknowledgement. */
  record AbilityReply(long revision,int healthGained,int manaSpent,int manaGained) {}

  /**
   * Self-cast of a learned spell. The draft is rolled once from committed state and committed once through
   * the shared single-flight event path; a refusal commits nothing and keeps the player connected, an ambiguous
   * commit disconnects through the shared persistence path and is never replayed.
   */
  CompletionStage<AbilityReply> cast(String token,long revision,String spellKey) {
    var saved=usableCharacter(token,revision);
    var draft=OnlineCasting.prepareSelfCast(saved,spellKey,wallClock.getAsLong(),ThreadLocalRandom.current(),hosted.items,spells());
    return commitAbility(token,revision,draft);
  }

  /** True when the live spells-v1 allowlist casts this key as a self-cast; those casts keep the original path. */
  boolean legacySelfCast(String spellKey) {
    var spell=spells().spells().get(spellKey);
    return spell!=null && spell.use()==com.perso.T4C.online.model.SpellContent.Use.CAST;
  }

  /** Uses one loose consumable instance on its owner; same commit rules as {@link #cast}. */
  CompletionStage<AbilityReply> useItem(String token,long revision,UUID instance) {
    var saved=usableCharacter(token,revision);
    var draft=OnlineItemUse.prepareUse(saved,Objects.requireNonNull(instance),wallClock.getAsLong(),
        ThreadLocalRandom.current(),hosted.items,spells());
    return commitAbility(token,revision,draft);
  }

  private CompletionStage<AbilityReply> commitAbility(String token,long revision,OnlineCasting.Result draft) {
    return commitGameplayEvent(token,revision,UUID.randomUUID(),0,0,draft.progress())
        .thenApply(committed->new AbilityReply(committed.character().revision(),draft.healthGained(),
            draft.manaSpent(),draft.manaGained()));
  }

  OnlineStore.CharacterState usableCharacter(String token,long revision) {
    return usableCharacter(token,revision,true);
  }

  /** As above; a caller that brings its own item content (the region spell path) does not need the hosted one. */
  OnlineStore.CharacterState usableCharacter(String token,long revision,boolean requireHostedItems) {
    var binding=active(token);
    if(binding.event!=null || binding.awaitingRegionReady) throw new PreviewWorld.Rejected(409,"busy");
    if(binding.saved.revision()!=revision) throw new PreviewWorld.Rejected(409,"revision");
    var saved=character(token);
    if(saved.progress()==null) throw new PreviewWorld.Rejected(409,"uninitialized_character");
    if(requireHostedItems && hosted.items==null) throw new PreviewWorld.Rejected(409,"unsupported_ability");
    return saved;
  }

  private SpellCatalog spells() {
    if(spellCatalog==null) {
      try { spellCatalog=SpellCatalog.loadBundled(); }
      catch (java.io.IOException unavailable) { throw new java.io.UncheckedIOException(unavailable); }
    }
    return spellCatalog;
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
    return recoverCorpse(token,revision,corpse,null);
  }

  /** {@code item} names one corpse stack to take; null takes every stack that fits, in order. */
  CompletionStage<OnlineStore.RewardResult> recoverCorpse(String token,long revision,UUID corpse,UUID item) {
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
        Objects.requireNonNull(corpse),List.of(),item);
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
   * One spell cast that damaged creatures: the caster's draft, every strike, every kill's life claim and its
   * drops commit in one transaction under a fresh cast id. Same single-flight, revision-fenced path as a melee
   * strike; the kills' drops are published to the region cache only after the commit.
   */
  CompletionStage<OnlineStore.RewardResult> commitSpellStrikes(String token,long expectedRevision,UUID eventId,
      long gold,long experience,CharacterProgress progress,List<OnlineStore.SpellStrike> strikes) {
    Objects.requireNonNull(eventId);Objects.requireNonNull(progress);
    if (strikes.isEmpty()) throw new IllegalArgumentException("A spell commit needs at least one strike");
    if (gold<0 || experience<0) throw new IllegalArgumentException("Negative reward");
    var binding=active(token);
    if (binding.event!=null || binding.awaitingRegionReady) throw new PreviewWorld.Rejected(409,"busy");
    if (binding.saved.revision()!=expectedRevision) throw new PreviewWorld.Rejected(409,"revision");
    var drops=strikes.stream().flatMap(hit -> hit.drops().stream()).toList();
    var result=new CompletableFuture<OnlineStore.RewardResult>();
    binding.event=new GameplayEvent(eventId,gold,experience,progress,false,null,null,null,null,null,null,null,null,
        drops,strikes,null,result,null);
    if (!binding.pending) submit(binding);
    return result.minimalCompletionStage();
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
    if (found==null) throw new PreviewWorld.Rejected(409,
        region.loot.takenByOther(loot,binding.saved.id()) ? OnlinePickupRules.LOOT_TAKEN : "loot_unavailable");
    var position=region.world.position(token);
    binding.position=position;
    double x=region.map.originX+position.x(),y=region.map.originY+position.y();
    if (Math.max(Math.abs(x-found.x()),Math.abs(y-found.y()))>com.perso.T4C.server.OnlinePickupRules.MAX_TILE_DISTANCE)
      throw new PreviewWorld.Rejected(409,"out_of_range");
    var result=new CompletableFuture<OnlineStore.RewardResult>();
    binding.event=new GameplayEvent(UUID.randomUUID(),0,0,progress,false,null,null,null,null,null,null,null,loot,List.of(),List.of(),null,result,null);
    if (!binding.pending) submit(binding);
    return result.minimalCompletionStage();
  }

  /** The shopkeeper's listing and this character's sellable goods. A read: nothing is committed. */
  com.perso.T4C.online.model.ShopOffersReply shopOffers(String token,long epoch,String npcId) {
    var binding=active(token);
    requireEpoch(token,epoch);
    var vendor=shopkeeper(binding,token,npcId);
    return OnlineShops.offers(binding.saved.revision(),binding.saved.gold(),npcId,vendor,binding.saved.progress(),hosted.items);
  }

  /** What a committed trade did, from the server's own numbers. */
  record ShopReceipt(long revision,int quantity,long total) {}

  /**
   * Single-flight vendor trade. The server prices it from its own catalog, pre-checks it against the
   * committed state, then commits gold and inventory together in one revision-fenced, receipt-bound
   * transaction that re-validates it. An uncertain outcome disconnects the actor; nothing replays it.
   */
  CompletionStage<ShopReceipt> shopTrade(String token,long revision,long epoch,String npcId,
      OnlineShopTrades.Side side,String itemKey,int quantity) {
    var binding=active(token);
    requireEpoch(token,epoch);
    if (binding.event!=null || binding.awaitingRegionReady) throw new PreviewWorld.Rejected(409,"busy");
    if (binding.saved.revision()!=revision) throw new PreviewWorld.Rejected(409,"revision");
    var vendor=shopkeeper(binding,token,npcId);
    var definition=hosted.items.items().get(itemKey);
    if (definition==null) throw new PreviewWorld.Rejected(409,"unsupported_item");
    long price;
    if (side==OnlineShopTrades.Side.BUY) {
      price=OnlineShops.buyPrice(vendor,itemKey).orElseThrow(()->new PreviewWorld.Rejected(409,"not_for_sale"));
    } else {
      price=OnlineShops.sellPrice(definition);
      if (!com.perso.T4C.item.ShopRules.sellable(price)) throw new PreviewWorld.Rejected(409,"not_sellable");
    }
    OnlineShopTrades.Trade trade;
    try { trade=new OnlineShopTrades.Trade(side,itemKey,quantity,price); }
    catch (IllegalArgumentException invalid) { throw new PreviewWorld.Rejected(400,"input"); }
    var progress=binding.saved.progress();
    long goldBefore=binding.saved.gold();
    try { OnlineShopTrades.apply(progress,goldBefore,trade,hosted.items,carryRules(hosted.items).maximumWeight(progress),UUID.randomUUID()); }
    catch (OnlineShopTrades.Refused refused) { throw new PreviewWorld.Rejected(409,refused.refusal().code()); }
    var result=new CompletableFuture<OnlineStore.RewardResult>();
    binding.event=new GameplayEvent(UUID.randomUUID(),0,0,progress,false,null,null,null,null,null,null,null,null,
        List.of(),List.of(),null,result,trade);
    if (!binding.pending) submit(binding);
    return result.minimalCompletionStage().thenApply(committed->new ShopReceipt(committed.character().revision(),
        quantity,Math.abs(committed.character().gold()-goldBefore)));
  }

  /**
   * The vendor this character may trade with right now. Position and sight come from the simulation
   * and use the same conversation rule as {@link #interact}; a reachable NPC without a shop is refused.
   */
  private com.perso.T4C.online.model.NpcShopContent.Vendor shopkeeper(Binding binding,String token,String npcId) {
    if (binding.awaitingRegionReady) throw new PreviewWorld.Rejected(409,"busy");
    var progress=binding.saved.progress();
    if (progress==null) throw new PreviewWorld.Rejected(409,"uninitialized_character");
    if (progress.vitals().hp()<=0) throw new PreviewWorld.Rejected(409,"dead");
    if (hosted.items==null || binding.region.npcs==null) throw new PreviewWorld.Rejected(409,"npc_unavailable");
    var position=binding.region.world.position(token);
    binding.position=position;
    java.util.Optional<String> canonical;
    try { canonical=binding.region.npcs.reachableShopkeeper(binding.saved,binding.region.map,position.x(),position.y(),npcId); }
    catch (IllegalArgumentException unavailable) { throw new PreviewWorld.Rejected(409,"npc_unavailable"); }
    if (canonical.isEmpty()) throw new PreviewWorld.Rejected(409,"not_a_shop");
    return OnlineShops.vendor(canonical.get()).orElseThrow(()->new PreviewWorld.Rejected(409,"not_a_shop"));
  }

  CompletionStage<OnlineStore.RewardResult> commitMonsterAttack(String token,long revision,UUID event,
      CharacterProgress progress,OnlineStore.MonsterAttack attack,OnlineStore.DeathTransition death) {
    return commitEvent(token,revision,event,0,0,progress,false,null,null,Objects.requireNonNull(attack),death,null);
  }

  CompletionStage<OnlineStore.RewardResult> commitMonsterHeal(String token,long revision,UUID event,
      CharacterProgress progress,OnlineStore.MonsterHeal heal) {
    var binding=active(token);
    if(binding.event!=null || binding.awaitingRegionReady) throw new PreviewWorld.Rejected(409,"busy");
    if(binding.saved.revision()!=revision) throw new PreviewWorld.Rejected(409,"revision");
    var result=new CompletableFuture<OnlineStore.RewardResult>();
    binding.event=new GameplayEvent(Objects.requireNonNull(event),0,0,Objects.requireNonNull(progress),false,
        null,null,null,null,null,null,null,null,List.of(),List.of(),Objects.requireNonNull(heal),result,null);
    if(!binding.pending) submit(binding);
    return result.minimalCompletionStage();
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
    return commitEvent(token,expectedRevision,eventId,gold,experience,progress,worldReward,strike,provocation,attack,death,corpse,drops,null);
  }

  private CompletionStage<OnlineStore.RewardResult> commitEvent(String token, long expectedRevision,
      UUID eventId, long gold, long experience, CharacterProgress progress, boolean worldReward, OnlineStore.CreatureStrike strike,
      OnlineStore.CreatureProvocation provocation,OnlineStore.MonsterAttack attack,OnlineStore.DeathTransition death,UUID corpse,
      List<LootDropDraft> drops,UUID corpseItem) {
    var binding=active(token);
    if (binding.event!=null || binding.awaitingRegionReady) throw new PreviewWorld.Rejected(409,"busy");
    if (binding.saved.revision()!=expectedRevision) throw new PreviewWorld.Rejected(409,"revision");
    if (gold<0 || experience<0) throw new IllegalArgumentException("Negative reward");
    var result=new CompletableFuture<OnlineStore.RewardResult>();
    binding.event=new GameplayEvent(Objects.requireNonNull(eventId),gold,experience,
        Objects.requireNonNull(progress),worldReward,strike,provocation,attack,death,corpse,corpseItem,null,null,drops,List.of(),result);
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
  /** Resolves a live friendly actor from this hosted region; actor ids never cross region instances. */
  FriendTarget friendTarget(String token,long actorId) {
    if(actorId<=0) throw new PreviewWorld.Rejected(409,"friend_unavailable");
    var caster=active(token);
    for(var candidate:bindings.values()) {
      if(candidate==caster || candidate.region.world!=caster.region.world) continue;
      var position=candidate.region.world.position(candidate.token);
      if(position==null || position.id()!=actorId) continue;
      if(candidate.closing || candidate.pending || candidate.event!=null || candidate.awaitingRegionReady
          || candidate.saved.progress()==null)
        throw new PreviewWorld.Rejected(409,"friend_unavailable");
      return new FriendTarget(actorId,candidate.token,candidate.saved.revision(),position.x(),position.y(),
          candidate.saved.progress().vitals().hp()>0,candidate.saved.progress());
    }
    throw new PreviewWorld.Rejected(409,"friend_unavailable");
  }

  /** Reserves both online characters and commits one server-calculated heal under a single receipt. */
  CompletionStage<OnlineStore.RewardResult> commitPlayerHeal(String token,long revision,UUID eventId,
      CharacterProgress casterProgress,FriendTarget target,int amount) {
    var caster=active(token);var recipient=active(target.token());
    if(caster.event!=null || caster.awaitingRegionReady || caster.pending || caster.saved.revision()!=revision)
      throw new PreviewWorld.Rejected(409,"busy");
    var position=recipient.region.world.position(recipient.token);
    if(recipient==caster || position==null || position.id()!=target.actorId()
        || recipient.region.world!=caster.region.world || recipient.saved.revision()!=target.revision()
        || recipient.event!=null || recipient.pending || recipient.awaitingRegionReady
        || recipient.saved.progress()==null || recipient.saved.progress().vitals().hp()<=0)
      throw new PreviewWorld.Rejected(409,"friend_unavailable");
    if(amount<=0) throw new IllegalArgumentException("Invalid friend heal amount");
    var vitals=recipient.saved.progress().vitals();
    int hp=(int)Math.min(vitals.maxHp(),(long)vitals.hp()+amount);
    if(hp==vitals.hp()) return commitGameplayEvent(token,revision,eventId,0,0,casterProgress);
    var result=new CompletableFuture<OnlineStore.RewardResult>();
    var linked=new PlayerHeal(recipient,target.revision(),amount);
    var event=new GameplayEvent(eventId,0,0,casterProgress,false,null,null,null,null,null,null,null,null,
        List.of(),List.of(),null,result,null,linked);
    caster.event=event;recipient.event=event;recipient.pending=true;
    if(!caster.pending) submit(caster);
    return result.minimalCompletionStage();
  }
  boolean belongs(String token,PreviewWorld world) {
    var binding=bindings.get(token);return binding!=null && !binding.closing && binding.region.world==world;
  }
  CharacterProgress.Respawn respawnDestination(CharacterProgress.Respawn destination) {
    if(destination==null || !hosted.canStand(destination)) throw new PreviewWorld.Rejected(409,"unavailable_respawn");
    return arrivalAtTileCentre(destination);
  }
  /**
   * Authored portal targets and respawn points are whole tile numbers. Placing a player exactly on that number puts
   * them on a tile corner; movement keeps the half-tile offset, so they later walk along a row boundary through
   * tiles the shared collision map calls blocked (the T4C-0308 soak caught three adapters at 2785.x,1096.0 after
   * leaving the skeleton cave). Arrivals use the centre of the authored tile, like every join and spawn.
   */
  static CharacterProgress.Respawn arrivalAtTileCentre(CharacterProgress.Respawn authored) {
    return new CharacterProgress.Respawn(authored.world(),Math.floor(authored.x())+.5,Math.floor(authored.y())+.5);
  }
  private void maybeTransfer(Binding binding) {
    if(!hosted.plan.transitions || binding.closing || binding.event!=null || binding.awaitingRegionReady
        || tick<binding.transferRetryTick || binding.saved.portalCooldownUntil()>wallClock.getAsLong()) return;
    var position=binding.region.world.position(binding.token);if(position==null) return;
    var map=binding.region.map;
    var selected=hosted.plan.portals.resolve(binding.region.spec.world(),map.originX+position.x(),map.originY+position.y(),hosted::canStand);
    if(selected.isEmpty()) return;
    var portal=selected.get();var target=hosted.require(portal.destination().world(),portal.destination().x(),portal.destination().y());
    var draft=new com.perso.T4C.server.persistence.PortalTransfer(portal.portalId(),binding.region.spec.key(),target.spec.key(),
        portal.source(),arrivalAtTileCentre(portal.destination()),binding.saved.locationEpoch(),Math.addExact(wallClock.getAsLong(),350));
    binding.position=position;binding.region.world.freeze(binding.token,true);
    binding.event=new GameplayEvent(UUID.randomUUID(),0,0,binding.saved.progress(),false,null,null,null,null,null,null,draft,null,List.of(),List.of(),null,new CompletableFuture<>(),null);
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

  /** Shared carry rule: capacity from effective strength, weights from the item catalog. */
  private static OnlineStore.CarryRules carryRules(com.perso.T4C.online.model.ItemContent items) {
    return new OnlineStore.CarryRules() {
      public long maximumWeight(CharacterProgress progress) {
        return com.perso.T4C.item.CarryCapacityRules.maximumWeight(progress.attributes().strength()
            +OnlineEquipmentBonuses.resolve(progress,items).getOrDefault(3,0));
      }
      public long unitWeight(String key) {
        var definition=items.items().get(key);return definition==null ? 0L : Math.max(0L,definition.weight());
      }
    };
  }

  private void submit(Binding binding) {
    binding.pending=true;
    var position=binding.position;
    var region=binding.region;var map=region.map;
    long revision=binding.saved.revision();
    // Frequent rewards must not postpone movement checkpoints indefinitely. Logout drains its
    // accepted event first, then performs one final checkpoint using the latest position.
    var event=!binding.closing && tick-binding.checkpointSubmitted>=CHECKPOINT_TICKS ? null : binding.event;
    var operation=persistenceOperation(event);
    if (event==null) binding.checkpointSubmitted=tick;
    boolean finalSave=binding.closing && event==null;
    var content=hosted.items;
    // A plain checkpoint also persists settled regeneration and the join anchor, in the same revision-fenced write.
    CharacterProgress checkpointProgress=null;
    if (event==null && binding.saved.progress()!=null) {
      settleRegeneration(binding);
      // A character holding buffs is re-anchored at every checkpoint, so a crash or logout loses little.
      var anchoredProgress=OnlineBuffs.anchored(binding.saved.progress(),wallClock.getAsLong());
      if (anchoredProgress!=binding.saved.progress()) { binding.saved=withProgress(binding.saved,anchoredProgress);binding.progressUnsaved=true; }
      if (binding.progressUnsaved) { checkpointProgress=binding.saved.progress();binding.progressUnsaved=false; }
    }
    final var persistProgress=checkpointProgress;
    try {
      writes.execute(() -> {
        OnlineStore.CharacterState saved=null; Exception failure=null;
        OnlineStore.RewardResult reward=null; String refusal=null;OnlineStore.CharacterState partnerSaved=null;
        long allowedUntil=System.nanoTime()+AUTHORITY_NANOS;
        try {
          store.renew(binding.session);
          if (event==null) {
            saved=persistProgress==null
                ? store.checkpoint(binding.session,revision,region.spec.world(),map.originX+position.x(),map.originY+position.y())
                : store.checkpointCharacter(binding.session,revision,region.spec.world(),map.originX+position.x(),map.originY+position.y(),persistProgress);
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
          } else if (event.shop()!=null) {
            try {
              reward=store.commitShopTrade(binding.session,revision,event.id(),event.shop(),content,carryRules(content));
              saved=reward.character();
            } catch (OnlineStore.Conflict conflict) {
              // The trade was re-validated on the locked row and refused before any write. Anything else fails closed.
              if (!SHOP_REFUSALS.contains(conflict.getMessage())) throw conflict;
              refusal=conflict.getMessage();
            }
          } else if (event.corpse()!=null) {
            try {
              reward=store.recoverCorpse(binding.session,revision,event.corpse(),event.corpseItem(),region.spec.world(),
                  map.originX+position.x(),map.originY+position.y(),carryRules(content));
              saved=reward.character();
            } catch (OnlineStore.Conflict conflict) {
              // A refused item rolled back before any write. Anything else fails closed.
              if (!CORPSE_REFUSALS.contains(conflict.getMessage())) throw conflict;
              refusal=conflict.getMessage();
            }
          } else {
            if(event.playerHeal()!=null) {
              var heal=event.playerHeal();
              store.renew(heal.recipient().session);
              var committed=store.commitPlayerHeal(binding.session,revision,event.id(),event.progress(),
                  heal.recipient().session,heal.revision(),heal.amount());
              reward=new OnlineStore.RewardResult(committed.applied(),committed.caster());
              saved=committed.caster();partnerSaved=committed.recipient();
            } else {
            if (event.transfer()!=null) {
              try {
                reward=store.commitTransfer(binding.session,revision,event.id(),event.transfer());
                saved=reward.character();
              } catch (OnlineStore.Conflict conflict) {
                // Rolled back before any write; only the database clock disagreed. Anything else fails closed.
                if (!OnlineStore.PORTAL_COOLDOWN.equals(conflict.getMessage())) throw conflict;
                refusal=conflict.getMessage();
              }
            } else {
            reward=event.monsterHeal()!=null
                ? store.commitMonsterHeal(binding.session,revision,event.id(),event.progress(),event.monsterHeal())
                : event.monsterAttack()!=null
                ? store.commitMonsterAttack(binding.session,revision,event.id(),event.progress(),event.monsterAttack(),event.death())
                : !event.spellStrikes().isEmpty()
                ? store.commitSpellStrikes(binding.session,revision,event.id(),event.gold(),event.experience(),event.progress(),event.spellStrikes())
                : event.strike()!=null
                ? store.commitCreatureStrike(binding.session,revision,event.id(),event.gold(),event.experience(),event.progress(),event.strike(),event.provocation(),event.drops())
                : event.worldReward()
                ? store.commitWorldReward(binding.session,revision,event.id(),event.gold(),event.experience(),event.progress())
                : store.commitGameplayEvent(binding.session,revision,event.id(),event.gold(),event.experience(),event.progress(),event.provocation());
            saved=reward.character();
            }
            }
          }
          if (finalSave) store.release(binding.session);
        } catch (Exception error) { failure=error; }
        completions.add(new Completion(binding,saved,finalSave,allowedUntil,reward,refusal,failure,operation,partnerSaved));
      });
    } catch (RejectedExecutionException error) {
      completions.add(new Completion(binding,null,finalSave,0,null,null,error,operation,null));
    }
  }

  private static com.perso.T4C.server.persistence.PersistenceFailure.Operation persistenceOperation(GameplayEvent event) {
    if (event==null) return com.perso.T4C.server.persistence.PersistenceFailure.Operation.CHECKPOINT;
    if (event.loot()!=null) return com.perso.T4C.server.persistence.PersistenceFailure.Operation.LOOT_CLAIM;
    if (event.shop()!=null) return com.perso.T4C.server.persistence.PersistenceFailure.Operation.SHOP;
    if (event.corpse()!=null) return com.perso.T4C.server.persistence.PersistenceFailure.Operation.CORPSE;
    if (event.transfer()!=null) return com.perso.T4C.server.persistence.PersistenceFailure.Operation.TRANSFER;
    if (event.monsterAttack()!=null) return com.perso.T4C.server.persistence.PersistenceFailure.Operation.MONSTER_ATTACK;
    if (event.monsterHeal()!=null) return com.perso.T4C.server.persistence.PersistenceFailure.Operation.MONSTER_ATTACK;
    if (!event.spellStrikes().isEmpty() || event.strike()!=null)
      return com.perso.T4C.server.persistence.PersistenceFailure.Operation.SPELL_CREATURE_STRIKE;
    if (event.worldReward()) return com.perso.T4C.server.persistence.PersistenceFailure.Operation.WORLD_REWARD;
    return com.perso.T4C.server.persistence.PersistenceFailure.Operation.GENERIC_EVENT;
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
        var playerHeal=binding.event==null ? null : binding.event.playerHeal();
        if(playerHeal!=null) {
          var partner=playerHeal.recipient();partner.pending=false;
          partner.region.world.remove(partner.token);bindings.remove(partner.token);invalidateRead(partner);
          partner.closed.completeExceptionally(new IllegalStateException("Persistence unavailable"));
        }
        binding.region.world.remove(binding.token); bindings.remove(binding.token);
        invalidateRead(binding);
        if(binding.event!=null && (binding.event.death()!=null || binding.event.transfer()!=null)) hosted.reloadTargets(binding.saved.id());
        // An uncertain pickup or drop may or may not have committed: rebuild the region from the database. Never replay.
        if(binding.event!=null && (binding.event.loot()!=null || !binding.event.drops().isEmpty())) refreshLoot(binding.region);
        if (binding.event!=null) binding.event.result().completeExceptionally(new IllegalStateException("Persistence unavailable"));
        binding.closed.completeExceptionally(new IllegalStateException("Persistence unavailable"));
        System.err.println("Online persistence failed ("+com.perso.T4C.server.persistence.PersistenceFailure.format(completed.operation(),completed.error())+"); character disconnected. Recover after lease expiry.");
      } else if (completed.refusal()!=null) {
        // A definite refusal rolled back before any write, so the character stays connected.
        binding.allowedUntil=completed.allowedUntil();
        var refused=binding.event;
        binding.event=null;
        if(refused.playerHeal()!=null) {
          var partner=refused.playerHeal().recipient();partner.pending=false;partner.event=null;
        }
        // The database says this drop is no longer on the ground, so the cache was stale: drop it and re-sync.
        if ("loot_unavailable".equals(completed.refusal()) || OnlinePickupRules.LOOT_TAKEN.equals(completed.refusal())) {
          binding.region.loot.remove(refused.loot());
          refreshLoot(binding.region);
        }
        // Corpse recovery froze the actor for the write; a refusal ends that freeze.
        if (refused.corpse()!=null) binding.region.world.freeze(binding.token,false);
        // A portal transfer froze the actor too; a cooldown the database clock has not reached is retried shortly.
        if (refused.transfer()!=null) {
          binding.region.world.freeze(binding.token,false);
          binding.transferRetryTick=tick+PreviewWorld.TICK_RATE/4;
        }
        refused.result().completeExceptionally(new PreviewWorld.Rejected(409,completed.refusal()));
      } else {
        binding.saved=completed.saved();
        publishHealth(binding);
        if(completed.partnerSaved()!=null && binding.event!=null && binding.event.playerHeal()!=null) {
          var partner=binding.event.playerHeal().recipient();
          partner.saved=completed.partnerSaved();partner.pending=false;partner.event=null;publishHealth(partner);
        }
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
    if (event.loot()!=null) region.loot.removeClaimed(event.loot(),binding.saved.id());
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
    if (progress==null) return;
    binding.region.world.health(binding.token,progress.vitals().hp(),progress.vitals().maxHp());
    // The public look (name, gender, visible worn items) follows the same committed state.
    binding.region.world.look(binding.token,binding.saved.name(),progress.gender(),
        com.perso.T4C.online.model.PublicLook.visibleEquipment(progress,hosted.items));
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
