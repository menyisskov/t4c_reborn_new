package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.*;
import com.perso.T4C.combat.*;
import com.perso.T4C.monster.core.MonsterExperience;
import com.perso.T4C.monster.loot.LootTable;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.LongSupplier;

/** Immediate-impact melee slice. All arguments except revision/target originate on the server. */
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
    if (life.recipient()!=null) throw rejected("creature_unavailable");
    var definition=spawn.definition();
    // Never silently omit authored script/quest/item-drop behavior from this first combat slice.
    if ((definition.getSourceEvents()!=null && !definition.getSourceEvents().isEmpty())
        || (definition.getLoot()!=null && !definition.getLoot().isEmpty())
        || !p.questFlags().isEmpty() || p.skills().getOrDefault("stun_blow",0)>0)
      throw rejected("unsupported_combat");
    var position=world.position(token);
    double x=spawn.spawn().x()-map.originX,y=spawn.spawn().y()-map.originY;
    if (Math.hypot((position.x()-x)*GRID_W,(position.y()-y)*GRID_H)>PLAYER_MELEE_ATTACK_RANGE
        || !map.allowsCombat(position.x(),position.y(),x,y,false)) throw rejected("out_of_range_or_blocked");
    OnlineCombatLoadout loadout;
    try { loadout=OnlineCombatLoadout.resolve(p,items,now,false); }
    catch (IllegalArgumentException unsupported) { throw rejected("unsupported_combat"); }
    if (loadout.profile().skill("stun_blow")>0
        || (loadout.primary()!=null && loadout.primary().bow()) || (loadout.secondary()!=null && loadout.secondary().bow()))
      throw rejected("unsupported_combat");
    var resolved=CombatResolver.resolve(new PhysicalAttackRequest(loadout.profile(),
        MonsterCombatRules.resolve(definition).profile(false,random),
        WeaponDamageRules.melee(loadout.stats(),loadout.primary(),loadout.secondary(),loadout.damageBonus()),
        WeaponDamageRules.offHand(loadout.stats(),loadout.primary(),loadout.secondary(),loadout.damageBonus()),false),random);
    int before=life.health()==null ? definition.getHealth() : life.health();
    if (before<=0) throw rejected("creature_unavailable");
    int damage=Math.min(before,resolved.damage()),after=before-damage;
    var progression=OnlineProgression.award(saved,MonsterExperience.damageReward(definition.getXpPerHit(),damage),now,random);
    long experience=progression.experience(),gold=0;
    if (after==0) {
      var intermediate=new OnlineStore.CharacterState(saved.id(),saved.accountId(),saved.name(),saved.world(),saved.x(),saved.y(),
          saved.gold(),saved.experience()+experience,saved.revision(),progression.progress());
      progression=OnlineProgression.award(intermediate,MonsterExperience.reward(definition.getXpOnDeath()),now,random);
      experience=Math.addExact(experience,progression.experience());
      gold=LootTable.builder().gold(definition.getGoldMin(),definition.getGoldMax()).build().roll(random).gold();
    }
    var cooldowns=new HashMap<>(progression.progress().cooldowns());
    cooldowns.put("attack",Math.addExact(now,Math.max(1,loadout.attackCooldownMillis())));
    var draft=withAttackState(progression.progress(),cooldowns);
    pendingTargets.add(target);
    try {
      CompletionStage<OnlineStore.RewardResult> pending=damage==0
          ? sessions.commitGameplayEvent(token,revision,UUID.randomUUID(),0,0,draft)
          : sessions.commitCreatureStrike(token,revision,after==0 ? life.eventId() : UUID.randomUUID(),gold,experience,draft,
              new OnlineStore.CreatureStrike(life.key(),life.eventId(),definition.getHealth(),before,after));
      return pending.whenComplete((result,error) -> {
        // Session completions run on the simulation owner, including its shutdown drain.
        pendingTargets.remove(target);
        if (damage>0) {
          if (error!=null || after==0) creatures.rewardCommitted(target); // Hide uncertain outcomes until reload.
          else creatures.damageCommitted(target);
        }
      });
    } catch (RuntimeException failure) { pendingTargets.remove(target); throw failure; }
  }

  private static CharacterProgress withAttackState(CharacterProgress p,Map<String,Long> cooldowns) {
    return new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),p.vitals(),p.advancement(),
        p.inventory(),p.equipment(),p.skills(),p.spells(),p.questFlags(),p.storage(),p.storageGold(),p.respawn(),
        p.buffs(),cooldowns,0,p.companion());
  }
  private static PreviewWorld.Rejected rejected(String code) { return new PreviewWorld.Rejected(409,code); }
}
