package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.*;
import com.perso.T4C.combat.*;
import com.perso.T4C.monster.core.MonsterExperience;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.LootDropDraft;
import com.perso.T4C.quest.QuestRules;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.LongSupplier;

/** Server-authoritative physical attack resolver for melee and bow strikes. */
final class OnlineMelee {
  private final PreviewMap map;
  private final PreviewWorld world;
  private final PersistentSessions sessions;
  private final PersistentCreatures creatures;
  private final ItemContent items;
  private final Random random;
  private final LongSupplier clock;
  private final Set<Long> pendingTargets=new HashSet<>();

  OnlineMelee(PreviewMap map,PreviewWorld world,PersistentSessions sessions,PersistentCreatures creatures,
      ItemContent items,Random random,LongSupplier clock) {
    this.map=map; this.world=world; this.sessions=sessions; this.creatures=creatures;
    this.items=items; this.random=random; this.clock=clock;
  }

  CompletionStage<OnlineStore.RewardResult> attack(String token,long revision,long target) {
    if(!sessions.belongs(token,world)) throw rejected("region");
    var saved=sessions.character(token);
    if (saved.revision()!=revision) throw rejected("revision");
    var p=saved.progress(); long now=clock.getAsLong();
    if (p==null) throw rejected("uninitialized_character");
    if (p.vitals().hp()==0) throw rejected("dead");
    if (p.cooldowns().getOrDefault("attack",0L)>now || p.cooldowns().getOrDefault("stun",0L)>now
        || p.cooldowns().getOrDefault("attack_exhaustion",0L)>now) throw rejected("cooldown");
    if (pendingTargets.contains(target)) throw rejected("creature_busy");
    var spawn=world.creatureSpawns().stream().filter(s -> s.id()==target).findFirst()
        .orElseThrow(() -> rejected("creature_unavailable"));
    // Authored stationary actors are protected, matching BaseMonster.canBeAttackedByPlayer.
    // The preview's lack of AI movement does not change this authored flag.
    if (spawn.spawn().stationary()) throw rejected("protected_target");
    var life=creatures.life(target);
    if (life.diedAt()!=null) throw rejected("creature_unavailable");
    var captured=creatures.capture(target);
    var definition=spawn.definition();
    // Never silently omit authored script/quest behavior. Loot is accepted only for tables the online roll can grant.
    if ((definition.getSourceEvents()!=null && !definition.getSourceEvents().isEmpty())
        || !OnlineLootDrafts.supports(definition,items)
        || !OnlineQuestFlags.supported(p.questFlags(),QuestCatalog.bundled()) || p.companion()!=null || p.skills().getOrDefault("stun_blow",0)>0
        || !OnlineRetaliation.supported(definition))
      throw rejected("unsupported_combat");
    var position=world.position(token);
    OnlineCombatLoadout loadout;
    try { loadout=OnlineCombatLoadout.resolve(p,items,now,false); }
    catch (IllegalArgumentException unsupported) { throw rejected("unsupported_combat"); }
    boolean ranged=isBow(loadout.primary()) || isBow(loadout.secondary());
    if (loadout.profile().skill("stun_blow")>0
        || (ranged && (loadout.secondary()==null || loadout.secondary().structure()!=QUIVER_STRUCTURE_ID)))
      throw rejected("unsupported_combat");
    double x=captured.x()-map.originX,y=captured.y()-map.originY;
    double range=ranged ? BOW_ATTACK_RANGE : PLAYER_MELEE_ATTACK_RANGE;
    boolean visible=ranged ? map.hasSpellLineOfSight(position.x(),position.y(),x,y)
        : map.hasLineOfSight(position.x(),position.y(),x,y);
    if (Math.hypot((position.x()-x)*GRID_W,(position.y()-y)*GRID_H)>range
        || !map.combatEndpointsSafe(position.x(),position.y(),x,y,false) || !visible)
      throw rejected("out_of_range_or_blocked");
    var resolved=CombatResolver.resolve(new PhysicalAttackRequest(loadout.profile(),
        MonsterCombatRules.resolve(definition).profile(false,random),
        ranged ? WeaponDamageRules.bow(loadout.stats(),loadout.primary(),loadout.secondary(),loadout.damageBonus())
            : WeaponDamageRules.melee(loadout.stats(),loadout.primary(),loadout.secondary(),loadout.damageBonus()),
        ranged ? 0 : WeaponDamageRules.offHand(loadout.stats(),loadout.primary(),loadout.secondary(),loadout.damageBonus()),
        ranged),random);
    int before=life.health()==null ? definition.getHealth() : life.health();
    if (before<=0) throw rejected("creature_unavailable");
    int damage=Math.min(before,resolved.damage()),after=before-damage;
    var progression=OnlineProgression.award(saved,MonsterExperience.damageReward(definition.getXpPerHit(),damage),now,random);
    long experience=progression.experience(),gold=0;
    List<LootDropDraft> drops=List.of();
    if (after==0) {
      var intermediate=new OnlineStore.CharacterState(saved.id(),saved.accountId(),saved.name(),saved.world(),saved.x(),saved.y(),
          saved.gold(),saved.experience()+experience,saved.revision(),progression.progress());
      progression=OnlineProgression.award(intermediate,MonsterExperience.reward(definition.getXpOnDeath()),now,random);
      experience=Math.addExact(experience,progression.experience());
      // One roll supplies both gold and drops, exactly as the offline kill does. Drops sit on the creature's tile.
      try {
        var roll=OnlineLootDrafts.roll(definition,items,random,sessions.region(token).world(),captured.x(),captured.y());
        gold=roll.gold();
        drops=roll.drops();
      } catch (IllegalArgumentException unsupported) { throw rejected("unsupported_combat"); }
    }
    var cooldowns=new HashMap<>(progression.progress().cooldowns());
    cooldowns.put("attack",Math.addExact(now,Math.max(1,loadout.attackCooldownMillis())));
    var credited=after==0
        ? withLethalQuestCredit(progression.progress(),definition.getName(),sessions.region(token).world(),captured.x(),captured.y())
        : progression.progress();
    var draft=withAttackState(credited,cooldowns);
    var motion=after==0
        ? new OnlineStore.SpawnMotion(captured.key(),captured.life(),captured.revision(),captured.x(),captured.y(),
            "IDLE",null,null)
        : definition.isCanAttack()
        ? new OnlineStore.SpawnMotion(captured.key(),captured.life(),captured.revision(),captured.x(),captured.y(),
            "RETALIATION",saved.id(),saved.locationEpoch()) : captured;
    var provocation=after>0 && definition.isCanAttack()
        ? new OnlineStore.CreatureProvocation(life.key(),life.eventId(),saved.id(),
            life.targetCharacter()==null ? Math.addExact(now,OnlineRetaliation.COOLDOWN_MILLIS) : life.nextAttackAt(),motion) : null;
    creatures.reserve(target);
    pendingTargets.add(target);
    try {
      CompletionStage<OnlineStore.RewardResult> pending=damage==0
          ? sessions.commitGameplayEvent(token,revision,UUID.randomUUID(),0,0,draft,provocation)
          : sessions.commitCreatureStrike(token,revision,after==0 ? life.eventId() : UUID.randomUUID(),gold,experience,draft,
              new OnlineStore.CreatureStrike(life.key(),life.eventId(),definition.getHealth(),before,after,motion),provocation,drops);
      return pending.whenComplete((result,error) -> {
        // Session completions run on the simulation owner, including its shutdown drain.
        pendingTargets.remove(target);
        creatures.release(target);
        if (damage>0 || provocation!=null) {
          if (error!=null || after==0) creatures.rewardCommitted(target); // Hide uncertain outcomes until reload.
          else creatures.damageCommitted(target);
        }
      });
    } catch (RuntimeException failure) { pendingTargets.remove(target); creatures.release(target); throw failure; }
  }

  private static CharacterProgress withAttackState(CharacterProgress p,Map<String,Long> cooldowns) {
    return new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),p.vitals(),p.advancement(),
        p.inventory(),p.equipment(),p.skills(),p.spells(),p.questFlags(),p.storage(),p.storageGold(),p.respawn(),
        p.buffs(),cooldowns,0,p.companion());
  }
  private static boolean isBow(WeaponDamageRules.Weapon weapon) { return weapon!=null && weapon.bow(); }
  /** Count only the authoritative lethal claimant; these flags share the strike's receipt and transaction. */
  static CharacterProgress withLethalQuestCredit(CharacterProgress progress,String monster,int world,double x,double y) {
    var flags=new HashMap<>(progress.questFlags());
    String log=QuestRules.killLogFlag(monster);
    // A lifetime kill log cannot wrap negative and make later online combat unsupported.
    if(flags.getOrDefault(log,0)<Integer.MAX_VALUE)
      flags.putAll(QuestRules.recordKillLog(key->flags.getOrDefault(key,0),log));
    int tileX=(int)Math.floor(x),tileY=(int)Math.floor(y);
    for(var quest:QuestCatalog.bundled().definitions())
      flags.putAll(QuestRules.creditObjective(key->flags.getOrDefault(key,0),quest,
          quest.getTargetMonster().equalsIgnoreCase(monster),world,tileX,tileY));
    return ProgressEdits.withFlags(progress,flags);
  }
  private static PreviewWorld.Rejected rejected(String code) { return new PreviewWorld.Rejected(409,code); }
}
