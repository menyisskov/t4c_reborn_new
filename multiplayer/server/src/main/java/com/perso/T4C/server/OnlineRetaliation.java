package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.*;

import com.perso.T4C.combat.*;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterPursuitRules;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.*;
import java.util.function.LongSupplier;

/** Simulation-owned, persisted retaliation for the currently supported physical creature attacks. */
final class OnlineRetaliation {
  static final long COOLDOWN_MILLIS=Math.round(MONSTER_ATTACK_COOLDOWN*1000);
  private final PreviewMap map;
  private final PreviewWorld world;
  private final PersistentSessions sessions;
  private final PersistentCreatures creatures;
  private final ItemContent items;
  private final Random random;
  private final LongSupplier clock;

  OnlineRetaliation(PreviewMap map,PreviewWorld world,PersistentSessions sessions,PersistentCreatures creatures,
      ItemContent items,Random random,LongSupplier clock) {
    this.map=map; this.world=world; this.sessions=sessions; this.creatures=creatures;
    this.items=items; this.random=random; this.clock=clock;
  }

  static boolean supported(MonsterDef definition) {
    if (!definition.isCanAttack()) return true;
    return definition.getAttacks().stream().allMatch(a -> a!=null && !a.isSpell()
        && a.getName()!=null && a.getName().matches("(?i)[1-9][0-9]*d[1-9][0-9]*([+-][0-9]+)?"));
  }

  void step() {
    long now=clock.getAsLong();
    for (var spawn:world.creatureSpawns()) {
      if (spawn.spawn().stationary() || !spawn.definition().isCanAttack() || !supported(spawn.definition())) continue;
      OnlineStore.SpawnState life;
      try { life=creatures.life(spawn.id()); }
      catch (PreviewWorld.Rejected busy) { continue; }
      if (life.recipient()!=null || life.targetCharacter()==null || life.nextAttackAt()>now) continue;
      String token=sessions.tokenForCharacter(life.targetCharacter());
      if (token==null || !sessions.belongs(token,world)) continue; // Persisted target survives reconnect; no attacks against absent actors.
      if (life.targetEpoch()==null || life.targetEpoch()!=sessions.character(token).locationEpoch()) continue;
      var position=world.position(token);
      if (!canStrike(map,world,spawn,position)) continue;
      try { attack(token,spawn,life,position,now); }
      catch (PreviewWorld.Rejected busyOrDisconnected) { /* Retry from committed state on a later tick. */ }
    }
  }

  static boolean canStrike(PreviewMap map, PreviewWorld world, PreviewWorld.CreatureSpawn spawn,
      PreviewWorld.PlayerView player) {
    if (player==null) return false;
    var creature=world.creaturePosition(spawn.id());
    float distance=(float)Math.hypot((player.x()-creature.x())*GRID_W,
        (player.y()-creature.y())*GRID_H);
    return MonsterPursuitRules.withinAttackReach(distance,spawn.definition().getAttacks())
        && MonsterPursuitRules.tileRange(distance)<2
        && map.allowsCombat(creature.x(),creature.y(),player.x(),player.y(),false);
  }

  private void attack(String token,PreviewWorld.CreatureSpawn spawn,OnlineStore.SpawnState life,
      PreviewWorld.PlayerView position,long now) {
    var saved=sessions.character(token); var progress=saved.progress();
    if (progress==null || progress.vitals().hp()<=0) return;
    OnlineCombatLoadout defender;
    try { defender=OnlineCombatLoadout.resolve(progress,items,now,progress.cooldowns().getOrDefault("stun",0L)>now); }
    catch (IllegalArgumentException unsupported) { return; }
    var definition=spawn.definition(); var stats=MonsterCombatRules.resolve(definition);
    var attack=MonsterCombatRules.chooseAttack(definition.getAttacks(),random);
    int attackSkill=attack==null ? stats.attack() : Math.max(1,attack.getValue1());
    int raw=attack==null ? definition.getHitDamageMin()+
        (definition.getHitDamageMax()>definition.getHitDamageMin()
          ? random.nextInt(definition.getHitDamageMax()-definition.getHitDamageMin()+1) : 0)
        : MonsterCombatRules.rollDamage(attack.getName(),random);
    var profile=new CombatProfile(stats.level(),stats.strength(),stats.endurance(),stats.agility(),
        attackSkill,attackSkill,stats.dodge(),MonsterCombatRules.rollArmor(stats.armorMin(),stats.armorMax(),random),
        false,false,true,Map.of());
    var hit=CombatResolver.resolve(new PhysicalAttackRequest(profile,defender.profile(),raw,0,false),random);
    int hp=Math.max(0,progress.vitals().hp()-hit.damage());
    CharacterProgress draft;
    OnlineStore.DeathTransition death=null;
    if (hp==0) {
      var destination=sessions.respawnDestination(progress.respawn());
      var prepared=OnlineDeath.prepare(saved,items,
          new CharacterProgress.Respawn(world.worldId,map.originX+position.x(),map.originY+position.y()),destination,random);
      draft=prepared.progress(); death=prepared.transition();
    } else {
      var vitals=new CharacterProgress.Vitals(hp,progress.vitals().maxHp(),progress.vitals().mana(),progress.vitals().maxMana());
      draft=new CharacterProgress(progress.version(),progress.characterClass(),progress.gender(),progress.attributes(),vitals,
          progress.advancement(),progress.inventory(),progress.equipment(),progress.skills(),progress.spells(),progress.questFlags(),
          progress.storage(),progress.storageGold(),progress.respawn(),progress.buffs(),progress.cooldowns(),progress.hiddenUntil(),progress.companion());
    }
    var motion=creatures.capture(spawn.id());
    creatures.reserve(spawn.id());
    boolean fatal=death!=null;
    try {
      sessions.commitMonsterAttack(token,saved.revision(),UUID.randomUUID(),draft,
          new OnlineStore.MonsterAttack(life.key(),life.eventId(),saved.id(),life.nextAttackAt(),Math.addExact(now,COOLDOWN_MILLIS),motion),death)
          .whenComplete((result,error) -> {
            creatures.release(spawn.id());
            // Reload authoritative next attack and life after both known and uncertain outcomes.
            creatures.damageCommitted(spawn.id());
            // A lost commit acknowledgement may still have cleared every persisted target.
            if (fatal) creatures.characterDied(saved.id());
          });
    } catch (RuntimeException failure) { creatures.release(spawn.id()); throw failure; }
  }
}
