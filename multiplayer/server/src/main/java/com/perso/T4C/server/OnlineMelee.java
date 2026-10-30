package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.*;
import com.perso.T4C.combat.*;
import com.perso.T4C.player.BodyPart;
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
    var spawn=world.creatureSpawn(target);
    if (spawn==null) throw rejected("creature_unavailable");
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
        || !OnlineQuestFlags.supported(p.questFlags(),QuestCatalog.bundled()) || p.companion()!=null
        || !OnlineRetaliation.supported(definition))
      throw rejected("unsupported_combat");
    var position=world.position(token);
    OnlineCombatLoadout loadout;
    try { loadout=OnlineCombatLoadout.resolve(p,items,now,false); }
    catch (IllegalArgumentException unsupported) { throw rejected("unsupported_combat"); }
    boolean ranged=isBow(loadout.primary()) || isBow(loadout.secondary());
    if (ranged && (loadout.secondary()==null || loadout.secondary().structure()!=QUIVER_STRUCTURE_ID))
      throw rejected("unsupported_combat");
    double x=captured.x()-map.originX,y=captured.y()-map.originY;
    double range=ranged ? BOW_ATTACK_RANGE : PLAYER_MELEE_ATTACK_RANGE;
    boolean visible=ranged ? map.hasSpellLineOfSight(position.x(),position.y(),x,y)
        : map.hasLineOfSight(position.x(),position.y(),x,y);
    if (Math.hypot((position.x()-x)*GRID_W,(position.y()-y)*GRID_H)>range
        || !map.combatEndpointsSafe(position.x(),position.y(),x,y,false) || !visible)
      throw rejected("out_of_range_or_blocked");
    var resolved=CombatResolver.resolve(new PhysicalAttackRequest(loadout.profile(),
        MonsterCombatRules.resolve(definition).profile(creatures.stunned(target,now),random),
        ranged ? WeaponDamageRules.bow(loadout.stats(),loadout.primary(),loadout.secondary(),loadout.damageBonus())
            : WeaponDamageRules.melee(loadout.stats(),loadout.primary(),loadout.secondary(),loadout.damageBonus()),
        ranged ? 0 : WeaponDamageRules.offHand(loadout.stats(),loadout.primary(),loadout.secondary(),loadout.damageBonus()),
        ranged),random);
    int before=life.health()==null ? definition.getHealth() : life.health();
    if (before<=0) throw rejected("creature_unavailable");
    int damage=Math.min(before,resolved.damage()),after=before-damage;
    long stunDuration=resolved.hit() ? resolved.stunDurationMillis() : 0;
    var splashItem=OnlineEquipmentBonuses.equipment(p,items).get(BodyPart.WEAPON2);
    var splash=splashItem==null || splashItem.quiverSplash()==null ? null
        : new QuiverSplash(splashItem.quiverSplash().radius(),splashItem.quiverSplash().targets(),
            splashItem.quiverSplash().damagePercent());
    var targets=new ArrayList<StrikeTarget>();
    targets.add(new StrikeTarget(spawn,life,captured,definition,before,damage,after));
    if (ranged && splash!=null && resolved.hit() && damage>0) {
      var candidates=world.creatureSpawns();
      var selected=splash.select(true,damage,candidates,spawn, candidate -> {
        var candidateDefinition=candidate.definition();
        if (candidate.spawn().stationary()
            || candidateDefinition.getSourceEvents()!=null && !candidateDefinition.getSourceEvents().isEmpty()
            || !OnlineLootDrafts.supports(candidateDefinition,items)
            || !OnlineRetaliation.supported(candidateDefinition)) return false;
        try {
          var candidateLife=creatures.life(candidate.id());
          int health=candidateLife.health()==null ? candidate.definition().getHealth() : candidateLife.health();
          var point=world.creaturePosition(candidate.id());
          double armor=MonsterCombatRules.resolve(candidateDefinition).profile(
              creatures.stunned(candidate.id(),now),random).armorClass();
          return candidateLife.diedAt()==null && health>0
              && !CombatResolver.isInvulnerableArmor(armor)
              && map.combatEndpointsSafe(position.x(),position.y(),point.x(),point.y(),false)
              && map.hasSpellLineOfSight(captured.x(),captured.y(),point.x(),point.y());
        } catch (PreviewWorld.Rejected unavailable) { return false; }
      },candidate -> {
        var point=world.creaturePosition(candidate.id());
        return Math.hypot(point.x()-captured.x(),point.y()-captured.y());
      });
      for (var candidate:selected) {
        var candidateLife=creatures.life(candidate.id());
        var candidateMotion=creatures.capture(candidate.id());
        int candidateBefore=candidateLife.health()==null ? candidate.definition().getHealth() : candidateLife.health();
        int candidateDamage=Math.min(candidateBefore,splash.damage(damage));
        targets.add(new StrikeTarget(candidate,candidateLife,candidateMotion,candidate.definition(),candidateBefore,
            candidateDamage,candidateBefore-candidateDamage));
      }
    }
    var progression=OnlineProgression.award(saved,MonsterExperience.damageReward(definition.getXpPerHit(),damage),now,random);
    long experience=progression.experience(),gold=0;
    var strikes=new ArrayList<OnlineStore.SpellStrike>();
    for (var hit:targets) {
      var targetSpawn=hit.spawn(); var targetLife=hit.life(); var targetMotion=hit.motion();
      var targetDefinition=hit.definition(); boolean lethal=hit.after()==0;
      if (hit.damage()<=0) continue;
      if (hit.damage()>0) {
        if (targetSpawn.id()!=target) {
          var award=OnlineProgression.award(state(saved,progression.progress(),experience),
              MonsterExperience.damageReward(targetDefinition.getXpPerHit(),hit.damage()),now,random);
          progression=award; experience=Math.addExact(experience,award.experience());
        }
      }
      List<LootDropDraft> drops=List.of();
      if (lethal) {
        var award=OnlineProgression.award(state(saved,progression.progress(),experience),
            MonsterExperience.reward(targetDefinition.getXpOnDeath()),now,random);
        progression=award; experience=Math.addExact(experience,award.experience());
        try {
          var roll=OnlineLootDrafts.roll(targetDefinition,items,random,sessions.region(token).world(),targetMotion.x(),targetMotion.y());
          gold=Math.addExact(gold,roll.gold()); drops=roll.drops();
        } catch (IllegalArgumentException unsupported) { throw rejected("unsupported_combat"); }
        progression=new OnlineProgression.Result(progression.experience(),withLethalQuestCredit(progression.progress(),
            targetDefinition.getName(),sessions.region(token).world(),targetMotion.x(),targetMotion.y()));
      }
      var targetMotionUpdate=lethal
          ? new OnlineStore.SpawnMotion(targetMotion.key(),targetMotion.life(),targetMotion.revision(),targetMotion.x(),targetMotion.y(),"IDLE",null,null)
          : targetDefinition.isCanAttack()
          ? new OnlineStore.SpawnMotion(targetMotion.key(),targetMotion.life(),targetMotion.revision(),targetMotion.x(),targetMotion.y(),"RETALIATION",saved.id(),saved.locationEpoch())
          : targetMotion;
      var targetProvocation=!lethal && targetDefinition.isCanAttack()
          ? new OnlineStore.CreatureProvocation(targetLife.key(),targetLife.eventId(),saved.id(),
              targetLife.targetCharacter()==null ? Math.addExact(now,OnlineRetaliation.COOLDOWN_MILLIS) : targetLife.nextAttackAt(),targetMotionUpdate) : null;
      strikes.add(new OnlineStore.SpellStrike(new OnlineStore.CreatureStrike(targetLife.key(),targetLife.eventId(),
          targetDefinition.getHealth(),hit.before(),hit.after(),targetMotionUpdate),targetProvocation,drops));
    }
    var cooldowns=new HashMap<>(progression.progress().cooldowns());
    cooldowns.put("attack",Math.addExact(now,Math.max(1,loadout.attackCooldownMillis())));
    var draft=withAttackState(progression.progress(),cooldowns);
    var missedMotion=definition.isCanAttack()
        ? new OnlineStore.SpawnMotion(captured.key(),captured.life(),captured.revision(),captured.x(),captured.y(),
            "RETALIATION",saved.id(),saved.locationEpoch()) : captured;
    var missedProvocation=after>0 && definition.isCanAttack()
        ? new OnlineStore.CreatureProvocation(life.key(),life.eventId(),saved.id(),
            life.targetCharacter()==null ? Math.addExact(now,OnlineRetaliation.COOLDOWN_MILLIS) : life.nextAttackAt(),missedMotion) : null;
    var reserved=new ArrayList<Long>();
    try {
      for (var hit:targets) { creatures.reserve(hit.spawn().id()); reserved.add(hit.spawn().id()); }
      pendingTargets.addAll(reserved);
      CompletionStage<OnlineStore.RewardResult> pending=strikes.isEmpty()
          ? sessions.commitGameplayEvent(token,revision,UUID.randomUUID(),0,0,draft,missedProvocation)
          : sessions.commitSpellStrikes(token,revision,UUID.randomUUID(),gold,experience,draft,strikes);
      return pending.whenComplete((result,error) -> {
        // Session completions run on the simulation owner, including its shutdown drain.
        for (var hit:targets) {
          long id=hit.spawn().id(); pendingTargets.remove(id); creatures.release(id);
          if (id==target && error==null && result!=null && stunDuration>0) creatures.stunCommitted(id,clock.getAsLong(),stunDuration);
          if (hit.damage()>0 || hit.after()==0) {
            if (error!=null || hit.after()==0) creatures.rewardCommitted(id); // Hide uncertain outcomes until reload.
            else creatures.damageCommitted(id);
          }
        }
      });
    } catch (RuntimeException failure) { for(long id:reserved){pendingTargets.remove(id);creatures.release(id);} throw failure; }
  }

  private static OnlineStore.CharacterState state(OnlineStore.CharacterState original,CharacterProgress progress,long experience) {
    return new OnlineStore.CharacterState(original.id(),original.accountId(),original.name(),original.world(),original.x(),original.y(),
        original.gold(),original.experience()+experience,original.revision(),progress);
  }
  private record StrikeTarget(PreviewWorld.CreatureSpawn spawn,OnlineStore.SpawnState life,OnlineStore.SpawnMotion motion,
      com.perso.T4C.monster.core.MonsterDef definition,int before,int damage,int after) {}

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
