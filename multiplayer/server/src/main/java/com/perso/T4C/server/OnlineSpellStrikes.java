package com.perso.T4C.server;

import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterExperience;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.LootDropDraft;
import com.perso.T4C.online.model.SpellContentV2;
import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.spell.ElementRules;
import com.perso.T4C.spell.SpellCastRules;
import com.perso.T4C.spell.SpellFormulaContexts;
import com.perso.T4C.spell.engine.SpellEngine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.LongSupplier;

/**
 * Server-authoritative casting of the engine-stage-1 spells (single-target attack spells with their
 * residual splash, drains, single heals): a cast is planned once from committed state by the shared
 * {@link SpellEngine}, then committed once, atomically, through the same single-flight
 * revision-fenced path as a melee strike. A refused or failed cast returns no plan and changes
 * nothing; an ambiguous commit is never replayed.
 *
 * <p>The plan is pure ({@link #plan}): it reads a character, a creature source and a map, and
 * returns the immutable draft. Kill rewards reuse the melee machinery: {@link OnlineProgression} for
 * per-hit and on-death experience, {@link OnlineLootDrafts} for the one gold-and-drops roll per kill,
 * and {@link OnlineMelee#withLethalQuestCredit} for quest credit.
 *
 * <p>Line of sight uses the shared sampled spell rule ({@link PreviewMap#SPELL_LINE_OF_SIGHT}),
 * so a spell is visible exactly where single-player's is; melee and creature attacks keep the
 * stricter server ray. Spells never hurt other players (no player is ever a candidate target).
 */
final class OnlineSpellStrikes {
  /** What a plan reads about the region's creatures. */
  interface CreatureSource {
    PreviewWorld.CreatureSpawn spawn(long id);

    List<PreviewWorld.CreatureSpawn> spawns();

    /** Local tile position. */
    PreviewWorld.CreaturePosition position(long id);

    /** Throws {@code creature_unavailable} while the creature is reserved, pending or recovering. */
    OnlineStore.SpawnState life(long id);

    OnlineStore.SpawnMotion capture(long id);
  }

  /** One creature the cast damaged, as the reply reports it. */
  record Hit(long target, int damage, int health, boolean killed) {}

  /** The cast's strikes ready to commit, with what the reply says. */
  record Plan(CharacterProgress progress, long gold, long experience, List<OnlineStore.SpellStrike> strikes,
      List<Long> struck, int healthGained, int manaSpent, List<Hit> hits, List<Laid> buffs,
      List<SpellEngine.Event> events) {}

  /** A buff the cast laid on the caster and when it ends (absolute UTC millis; {@link Long#MAX_VALUE} = never). */
  record Laid(String spell, long expiresAt) {}

  /** What the reply to the caster says. */
  record Reply(long revision, int healthGained, int manaSpent, List<Hit> hits, List<Laid> buffs) {}

  private final PreviewMap map;
  private final PreviewWorld world;
  private final PersistentSessions sessions;
  private final PersistentCreatures creatures;
  private final ItemContent items;
  private final SpellCatalogV2 spells;
  private final Random random;
  private final LongSupplier clock;

  OnlineSpellStrikes(PreviewMap map, PreviewWorld world, PersistentSessions sessions, PersistentCreatures creatures,
      ItemContent items, SpellCatalogV2 spells, Random random, LongSupplier clock) {
    this.map = map;
    this.world = world;
    this.sessions = sessions;
    this.creatures = creatures;
    this.items = items;
    this.spells = spells;
    this.random = random;
    this.clock = clock;
  }

  /**
   * Casts a learned stage-1 spell at a target (self, a creature id, or a tile position the rules refuse
   * for these spells). Everything else originates on the server.
   */
  CompletionStage<Reply> cast(String token, long revision, String spellKey, SpellEngine.Target target) {
    if (!sessions.belongs(token, world)) throw rejected("region");
    var saved = sessions.usableCharacter(token, revision, false);
    long now = clock.getAsLong();
    var position = world.position(token);
    PersistentSessions.FriendTarget playerTarget=target instanceof SpellEngine.Target.Friend friend
        ? sessions.friendTarget(token,friend.id()) : null;
    SpellEngine.FriendView friendView=null;
    if(playerTarget!=null) {
      SpellFormulaContexts.Caster friendCaster;
      try { friendCaster=OnlineSpellContexts.caster(playerTarget.progress(),items,now); }
      catch(IllegalArgumentException unsupported) { throw rejected("unsupported_ability"); }
      friendView=new SpellEngine.FriendView(playerTarget.actorId(),playerTarget.x(),playerTarget.y(),playerTarget.alive(),friendCaster);
    }
    var source = new CreatureSource() {
      @Override public PreviewWorld.CreatureSpawn spawn(long id) {
        return world.creatureSpawns().stream().filter(s -> s.id() == id).findFirst().orElse(null);
      }
      @Override public List<PreviewWorld.CreatureSpawn> spawns() { return world.creatureSpawns(); }
      @Override public PreviewWorld.CreaturePosition position(long id) { return world.creaturePosition(id); }
      @Override public OnlineStore.SpawnState life(long id) { return OnlineSpellStrikes.this.creatures.life(id); }
      @Override public OnlineStore.SpawnMotion capture(long id) { return OnlineSpellStrikes.this.creatures.capture(id); }
    };
    var plan = plan(saved, spellKey, target, now, random, items, spells, map, position.x(), position.y(),
        sessions.region(token).world(), source,friendView);
    if(playerTarget!=null) {
      var friendHeal=plan.events().stream().filter(SpellEngine.Event.FriendHeal.class::isInstance)
          .map(SpellEngine.Event.FriendHeal.class::cast).findFirst().orElseThrow(() -> rejected("wrong_target"));
      return sessions.commitPlayerHeal(token,revision,UUID.randomUUID(),plan.progress(),playerTarget,friendHeal.amount())
          .thenApply(committed -> new Reply(committed.character().revision(),0,plan.manaSpent(),List.of(),plan.buffs()));
    }
    var reserved = new ArrayList<Long>();
    try {
      for (long id : plan.struck()) {
        creatures.reserve(id);
        reserved.add(id);
      }
      CompletionStage<OnlineStore.RewardResult> pending = plan.strikes().isEmpty()
          ? sessions.commitGameplayEvent(token, revision, UUID.randomUUID(), 0, 0, plan.progress())
          : sessions.commitSpellStrikes(token, revision, UUID.randomUUID(), plan.gold(), plan.experience(),
              plan.progress(), plan.strikes());
      return pending.whenComplete((result, error) -> {
        // Session completions run on the simulation owner, including its shutdown drain.
        for (long id : reserved) {
          creatures.release(id);
          // Hide uncertain outcomes (and kills) until the database state is reloaded.
          boolean killed = plan.hits().stream().anyMatch(hit -> hit.target() == id && hit.killed());
          if (error != null || killed) creatures.rewardCommitted(id);
          else creatures.damageCommitted(id);
        }
      }).thenApply(committed -> new Reply(committed.character().revision(), plan.healthGained(),
          plan.manaSpent(), plan.hits(), plan.buffs()));
    } catch (RuntimeException failure) {
      for (long id : reserved) creatures.release(id);
      throw failure;
    }
  }

  /** The pure draft. Throws {@link PreviewWorld.Rejected} (409) with the refusal code and changes nothing. */
  static Plan plan(OnlineStore.CharacterState saved, String spellKey, SpellEngine.Target target, long now, Random random,
      ItemContent items, SpellCatalogV2 spells, PreviewMap map, double casterX, double casterY, int regionWorld,
      CreatureSource source) {
    return plan(saved,spellKey,target,now,random,items,spells,map,casterX,casterY,regionWorld,source,null);
  }

  private static Plan plan(OnlineStore.CharacterState saved, String spellKey, SpellEngine.Target target, long now,
      Random random, ItemContent items, SpellCatalogV2 spells, PreviewMap map, double casterX, double casterY,
      int regionWorld, CreatureSource source,SpellEngine.FriendView friend) {
    var progress = OnlineCasting.aliveProgress(saved, now);
    if (!progress.spells().contains(spellKey)) throw rejected("not_learned");
    SpellContentV2.Spell spell;
    try {
      spell = spells.castable(spellKey);
    } catch (IllegalArgumentException unsupported) {
      throw rejected("unsupported_spell");
    }
    boolean attack = spell.kind() == SpellContentV2.Kind.NUKE || spell.kind() == SpellContentV2.Kind.DRAIN
        || spell.kind() == SpellContentV2.Kind.DEBUFF || spell.kind() == SpellContentV2.Kind.DOT
        || spell.kind() == SpellContentV2.Kind.AREA_DAMAGE;
    // An area spell can hurt creatures without aiming at one, so it is held to the same combat support.
    if ((target instanceof SpellEngine.Target.Unit || spell.kind() == SpellContentV2.Kind.AREA_DAMAGE) && attack
        && (progress.companion() != null || !OnlineQuestFlags.supported(progress.questFlags(), QuestCatalog.bundled())))
      throw rejected("unsupported_combat");
    SpellFormulaContexts.Caster caster;
    try {
      caster = OnlineSpellContexts.caster(progress, items, now);
    } catch (IllegalArgumentException unknownEquipment) {
      throw rejected("unsupported_ability");
    }
    var cooldowns = progress.cooldowns();
    var view = new View(source, map, casterX, casterY,
        target instanceof SpellEngine.Target.Unit unit ? Long.valueOf(unit.id()) : null,friend);
    var request = new SpellEngine.CastRequest(spell, caster, casterX, casterY, target,
        true, progress.vitals().mana(), false,
        SpellCastRules.isActive(cooldowns.getOrDefault(spell.key(), 0L), now),
        SpellCastRules.isActive(cooldowns.getOrDefault(OnlineCasting.MENTAL_EXHAUSTION, 0L), now)
            || SpellCastRules.isActive(cooldowns.getOrDefault(OnlineCasting.STUN, 0L), now),
        components(progress, spells), OnlineBuffs.activeBySpell(progress, now));
    SpellEngine.CastResolution resolved;
    try {
      resolved = SpellEngine.cast(request, view, random);
    } catch (IllegalArgumentException unsupported) {
      throw rejected("unsupported_spell");
    }
    if (resolved.refusal() != null) throw rejected(resolved.refusal());
    if (!resolved.success()) {
      if (resolved.verdict() == SpellCastRules.Verdict.ACTIVATION_FAILED) throw rejected("spell_failed");
      throw rejected(resolved.verdict().name().toLowerCase(Locale.ROOT));
    }
    return apply(saved, progress, spell, resolved, now, random, items, regionWorld, view, source);
  }

  /** Applies the engine's events to the character in the order single-player applies them. */
  private static Plan apply(OnlineStore.CharacterState saved, CharacterProgress progress,
      SpellContentV2.Spell spell, SpellEngine.CastResolution resolved, long now, Random random, ItemContent items,
      int regionWorld, View view, CreatureSource source) {
    var costs = resolved.costs();
    var vitals = progress.vitals();
    int hpStart = vitals.hp();
    var cooldowns = new HashMap<>(progress.cooldowns());
    if (costs.cooldownSeconds() > 0)
      start(cooldowns, spell.key(), Math.addExact(now, costs.cooldownSeconds() * 1000L));
    start(cooldowns, OnlineCasting.MENTAL_EXHAUSTION, now, costs.mentalMillis());
    start(cooldowns, OnlineCasting.PHYSICAL_EXHAUSTION, now, costs.physicalMillis());
    start(cooldowns, OnlineCasting.ATTACK_EXHAUSTION, now, costs.attackMillis());
    // A successful cast always reveals a hidden caster, as single-player's disturb() does.
    var working = revealed(ProgressEdits.withCooldowns(ProgressEdits.withVitals(progress,
        new CharacterProgress.Vitals(vitals.hp(), vitals.maxHp(), vitals.mana() - costs.manaCost(), vitals.maxMana())),
        cooldowns));
    long gold = 0, experience = 0;
    var laid = new ArrayList<Laid>();
    var strikes = new ArrayList<OnlineStore.SpellStrike>();
    var struck = new ArrayList<Long>();
    var hits = new LinkedHashMap<Long, Hit>();
    for (var event : resolved.events()) {
      switch (event) {
        case SpellEngine.Event.SelfHeal heal -> working = healed(working, heal.amount());
        case SpellEngine.Event.FriendHeal ignored -> { /* Committed with the recipient in one two-character transaction. */ }
        case SpellEngine.Event.DrainHeal drain -> working = healed(working, drain.amount());
        case SpellEngine.Event.ApplyBuff buff -> {
          // Only the caster's own buffs are laid here; a friend's is committed on the friend's own character.
          if (buff.recipient() != null) throw new IllegalStateException("Friend buff reached the caster draft");
          working = OnlineBuffs.apply(working, buff, now);
          laid.add(new Laid(buff.spellKey(), working.buffs().stream()
              .filter(b -> b.spell().equals(buff.spellKey())).findFirst().orElseThrow().expiresAt()));
        }
        case SpellEngine.Event.Hide hide ->
            working = ProgressEdits.withHiddenUntil(working, Math.addExact(now, hide.durationMillis()));
        case SpellEngine.Event.DetectInvisible detect -> working = detecting(working, DETECT_INVISIBLE, now, detect.durationMillis());
        case SpellEngine.Event.DetectHidden detect -> working = detecting(working, DETECT_HIDDEN, now, detect.durationMillis());
        case SpellEngine.Event.Dispel dispel -> working = OnlineBuffs.dispel(working, dispel.spellKey(), now);
        case SpellEngine.Event.Damage damage -> {
          var fact = view.fact(damage.target());
          var definition = fact.spawn.definition();
          // Never silently omit authored script or loot behaviour for a creature the spell would hurt.
          if ((definition.getSourceEvents() != null && !definition.getSourceEvents().isEmpty())
              || !OnlineLootDrafts.supports(definition, items)
              || !OnlineRetaliation.supported(definition))
            throw rejected("unsupported_combat");
          int before = fact.health;
          int dealt = Math.min(before, damage.amount());
          if (dealt <= 0) continue;
          int after = before - dealt;
          var life = fact.life;
          var captured = source.capture(damage.target());
          var award = OnlineProgression.award(stateOf(saved, working, saved.experience() + experience),
              MonsterExperience.damageReward(definition.getXpPerHit(), dealt), now, random);
          working = award.progress();
          experience = Math.addExact(experience, award.experience());
          List<LootDropDraft> drops = List.of();
          if (after == 0) {
            award = OnlineProgression.award(stateOf(saved, working, saved.experience() + experience),
                MonsterExperience.reward(definition.getXpOnDeath()), now, random);
            working = award.progress();
            experience = Math.addExact(experience, award.experience());
            try {
              var roll = OnlineLootDrafts.roll(definition, items, random, regionWorld, captured.x(), captured.y());
              gold = Math.addExact(gold, roll.gold());
              drops = roll.drops();
            } catch (IllegalArgumentException unsupported) {
              throw rejected("unsupported_combat");
            }
            working = OnlineMelee.withLethalQuestCredit(working, definition.getName(), regionWorld, captured.x(), captured.y());
          }
          var motion = after == 0
              ? new OnlineStore.SpawnMotion(captured.key(), captured.life(), captured.revision(), captured.x(),
                  captured.y(), "IDLE", null, null)
              : definition.isCanAttack()
              ? new OnlineStore.SpawnMotion(captured.key(), captured.life(), captured.revision(), captured.x(),
                  captured.y(), "RETALIATION", saved.id(), saved.locationEpoch())
              : captured;
          var provocation = after > 0 && definition.isCanAttack()
              ? new OnlineStore.CreatureProvocation(life.key(), life.eventId(), saved.id(),
                  life.targetCharacter() == null ? Math.addExact(now, OnlineRetaliation.COOLDOWN_MILLIS) : life.nextAttackAt(),
                  motion)
              : null;
          strikes.add(new OnlineStore.SpellStrike(new OnlineStore.CreatureStrike(life.key(), life.eventId(),
              definition.getHealth(), before, after, motion), provocation, drops));
          struck.add(damage.target());
          fact.health = after;
          hits.put(damage.target(), new Hit(damage.target(), dealt, after, after == 0));
        }
      }
    }
    int healthGained = working.vitals().hp() - hpStart;
    // A character holding buffs records when it was last settled online, so logged-out time is never counted.
    working = OnlineBuffs.anchored(working, now);
    return new Plan(working, gold, experience, List.copyOf(strikes), List.copyOf(struck), healthGained,
        costs.manaCost(), List.copyOf(hits.values()), List.copyOf(laid), resolved.events());
  }

  /** The spell keys offered to Renew Armor: the components this character has learned. */
  private static Map<String, SpellContentV2.Spell> components(CharacterProgress progress, SpellCatalogV2 spells) {
    var related = new LinkedHashMap<String, SpellContentV2.Spell>();
    for (String key : SpellEngine.RENEW_ARMOR_COMPONENTS) {
      var component = spells.spells().get(key);
      if (component != null && progress.spells().contains(key)) related.put(key, component);
    }
    return related;
  }

  private static final String DETECT_INVISIBLE = "detect_invisible_until";
  private static final String DETECT_HIDDEN = "detect_hidden_until";

  /** Detection flags keep the later of the running and the new end, as single-player's setters do. */
  private static CharacterProgress detecting(CharacterProgress progress, String flag, long now, long durationMillis) {
    var cooldowns = new HashMap<>(progress.cooldowns());
    cooldowns.merge(flag, Math.addExact(now, Math.max(0L, durationMillis)), Math::max);
    return ProgressEdits.withCooldowns(progress, cooldowns);
  }

  private static OnlineStore.CharacterState stateOf(OnlineStore.CharacterState saved, CharacterProgress progress,
      long experience) {
    return new OnlineStore.CharacterState(saved.id(), saved.accountId(), saved.name(), saved.world(), saved.x(),
        saved.y(), saved.gold(), experience, saved.revision(), progress, saved.locationEpoch(),
        saved.portalCooldownUntil());
  }

  /** Health restored, clamped to the maximum, as the self-cast path and Player.applyHeal do. */
  private static CharacterProgress healed(CharacterProgress progress, int amount) {
    var vitals = progress.vitals();
    if (amount <= 0) return progress;
    int hp = Math.min(vitals.maxHp(), vitals.hp() + amount);
    return ProgressEdits.withVitals(progress,
        new CharacterProgress.Vitals(hp, vitals.maxHp(), vitals.mana(), vitals.maxMana()));
  }

  private static CharacterProgress revealed(CharacterProgress p) {
    return new CharacterProgress(p.version(), p.characterClass(), p.gender(), p.attributes(), p.vitals(),
        p.advancement(), p.inventory(), p.equipment(), p.skills(), p.spells(), p.questFlags(), p.storage(),
        p.storageGold(), p.respawn(), p.buffs(), p.cooldowns(), 0, p.companion());
  }

  /** Absolute expiries never shorten an existing timer, so reconnecting cannot reset one. */
  private static void start(Map<String, Long> cooldowns, String key, long now, long millis) {
    if (millis > 0) start(cooldowns, key, Math.addExact(now, millis));
  }

  private static void start(Map<String, Long> cooldowns, String key, long until) {
    cooldowns.merge(key, until, Math::max);
  }

  private static PreviewWorld.Rejected rejected(String code) {
    return new PreviewWorld.Rejected(409, code);
  }

  /** A creature the cast has read, with its committed life and its health as the cast applies damage. */
  private static final class Fact {
    final PreviewWorld.CreatureSpawn spawn;
    final OnlineStore.SpawnState life;
    int health;

    Fact(PreviewWorld.CreatureSpawn spawn, OnlineStore.SpawnState life) {
      this.spawn = spawn;
      this.life = life;
      this.health = life.diedAt() != null ? 0 : life.health() == null ? spawn.definition().getHealth() : life.health();
    }
  }

  /**
   * The engine's world for one cast: the primary target and every creature within two tiles of it
   * (a superset of the adjacent splash ring), read from committed life state. Sight is the shared
   * sampled spell rule; combat safety is the map's safe-haven check.
   */
  private static final class View implements SpellEngine.WorldView {
    private final CreatureSource source;
    private final PreviewMap map;
    private final double casterX, casterY;
    private final Long primary;
    private final SpellEngine.FriendView friend;
    private final Map<Long, Fact> facts = new LinkedHashMap<>();
    private List<SpellEngine.UnitView> near;

    View(CreatureSource source, PreviewMap map, double casterX, double casterY, Long primary,
        SpellEngine.FriendView friend) {
      this.source = source;
      this.map = map;
      this.casterX = casterX;
      this.casterY = casterY;
      this.primary = primary;
      this.friend=friend;
    }

    @Override public SpellEngine.FriendView friend(long id) {
      return friend!=null && friend.id()==id ? friend : null;
    }

    Fact fact(long id) {
      var fact = facts.get(id);
      if (fact == null) throw rejected("creature_unavailable");
      return fact;
    }

    private Fact read(long id) {
      var fact = facts.get(id);
      if (fact != null) return fact;
      var spawn = source.spawn(id);
      if (spawn == null) return null;
      fact = new Fact(spawn, source.life(id));
      facts.put(id, fact);
      return fact;
    }

    private SpellEngine.UnitView unitOf(Fact fact) {
      var position = source.position(fact.spawn.id());
      return new SpellEngine.UnitView(fact.spawn.id(), position.x(), position.y(), fact.health,
          !fact.spawn.spawn().stationary(), resistance(fact.spawn.definition()));
    }

    @Override public SpellEngine.UnitView unit(long id) {
      if (primary == null || id != primary) return null;
      var fact = read(id);
      return fact == null ? null : unitOf(fact);
    }

    @Override public List<SpellEngine.UnitView> units() {
      if (near != null) return near;
      var list = new ArrayList<SpellEngine.UnitView>();
      if (primary != null) {
        var center = source.position(primary);
        var spawns = new ArrayList<>(source.spawns());
        spawns.sort(java.util.Comparator.comparingLong(PreviewWorld.CreatureSpawn::id));
        for (var spawn : spawns) {
          var position = source.position(spawn.id());
          if (Math.abs(position.x() - center.x()) > 2 || Math.abs(position.y() - center.y()) > 2) continue;
          var fact = read(spawn.id());
          if (fact != null) list.add(unitOf(fact));
        }
      }
      near = List.copyOf(list);
      return near;
    }

    /**
     * Area spells: only the creatures whose tile lies in the box are read, so a reserved or recovering
     * creature far from the blast never refuses the cast. Ascending id, as the engine's ties require.
     */
    @Override public List<SpellEngine.UnitView> unitsNear(double centerX, double centerY, double tilesX, double tilesY) {
      var list = new ArrayList<SpellEngine.UnitView>();
      var spawns = new ArrayList<>(source.spawns());
      spawns.sort(java.util.Comparator.comparingLong(PreviewWorld.CreatureSpawn::id));
      for (var spawn : spawns) {
        var position = source.position(spawn.id());
        if (Math.abs(position.x() - centerX) > tilesX || Math.abs(position.y() - centerY) > tilesY) continue;
        try {
          var fact = read(spawn.id());
          if (fact != null) list.add(unitOf(fact));
        } catch (PreviewWorld.Rejected unavailable) {
          // A creature that is dead, respawning or mid-commit is not in the blast: it is left alone
          // (only the creature aimed at must be available, which unit() still enforces).
          if (!"creature_unavailable".equals(unavailable.getMessage())) throw unavailable;
        }
      }
      return list;
    }

    @Override public boolean combatSafe(double fromX, double fromY, double toX, double toY) {
      return map.combatEndpointsSafe(fromX, fromY, toX, toY, false);
    }

    @Override public boolean lineOfSight(double fromX, double fromY, double toX, double toY) {
      return map.hasLineOfSight(PreviewMap.SPELL_LINE_OF_SIGHT, fromX, fromY, toX, toY);
    }

    /** Monster resistances in element order; an unset (zero) value reads as the neutral 100. */
    private static SpellFormulaContexts.Elements resistance(MonsterDef definition) {
      int[] raw = definition.getResists() == null ? new int[12] : definition.getResists();
      return new SpellFormulaContexts.Elements(
          ElementRules.targetResistance(at(raw, 3)), ElementRules.targetResistance(at(raw, 1)),
          ElementRules.targetResistance(at(raw, 0)), ElementRules.targetResistance(at(raw, 2)),
          ElementRules.targetResistance(at(raw, 5)), ElementRules.targetResistance(at(raw, 4)));
    }

    private static int at(int[] values, int index) {
      return index < values.length ? Math.max(0, values[index]) : 0;
    }
  }

}
