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
    return OnlineMonsterSpellAttacks.supported(definition);
  }

  void step() {
    long now=clock.getAsLong();
    for (var spawn:world.creatureSpawns()) {
      if (spawn.spawn().stationary() || !spawn.definition().isCanAttack() || !supported(spawn.definition())) continue;
      OnlineStore.SpawnState life;
      try { life=creatures.life(spawn.id()); }
      catch (PreviewWorld.Rejected busy) { continue; }
      if (life.diedAt()!=null || life.targetCharacter()==null || life.nextAttackAt()>now) continue;
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
    int range = MonsterPursuitRules.tileRange(distance);
    List<com.perso.T4C.monster.core.MonsterDef.Attack> attacks = spawn.definition().getAttacks();
    boolean inRange = attacks == null || attacks.isEmpty()
        ? range < 2
        : attacks.stream().anyMatch(attack -> attack != null && attack.isInRange(range));
    return inRange && map.allowsCombat(creature.x(),creature.y(),player.x(),player.y(),false);
  }

  private void attack(String token,PreviewWorld.CreatureSpawn spawn,OnlineStore.SpawnState life,
      PreviewWorld.PlayerView position,long now) {
    var saved=sessions.character(token); var progress=saved.progress();
    if (progress==null || progress.vitals().hp()<=0) return;
    OnlineCombatLoadout defender;
    try { defender=OnlineCombatLoadout.resolve(progress,items,now,progress.cooldowns().getOrDefault("stun",0L)>now); }
    catch (IllegalArgumentException unsupported) { return; }
    var definition=spawn.definition(); var stats=MonsterCombatRules.resolve(definition);
    var attacks=definition.getAttacks();
    boolean spellSet=attacks!=null && attacks.stream().anyMatch(a -> a!=null && a.isSpell());
    if (spellSet) {
      double distance=Math.hypot((position.x()-world.creaturePosition(spawn.id()).x())*GRID_W,
          (position.y()-world.creaturePosition(spawn.id()).y())*GRID_H);
      int tiles=MonsterPursuitRules.tileRange((float)distance);
      attacks=attacks.stream().filter(a -> a!=null && a.isInRange(tiles)).toList();
    }
    var attack=MonsterCombatRules.chooseAttack(attacks,random);
    boolean selfHeal=attack!=null && attack.isSpell() && attack.getSpellId()==10616
        && OnlineMonsterSpellAttacks.supportedHeal(attack.getSpellId())!=null;
    if(attack!=null && attack.isSpell() && OnlineMonsterSpellAttacks.supportedSpell(attack.getSpellId())==null && !selfHeal) return;
    int attackSkill=attack==null ? stats.attack() : Math.max(1,attack.getValue1());
    int raw=attack==null ? definition.getHitDamageMin()+
        (definition.getHitDamageMax()>definition.getHitDamageMin()
          ? random.nextInt(definition.getHitDamageMax()-definition.getHitDamageMin()+1) : 0)
        : attack.isSpell() ? OnlineMonsterSpellAttacks.rawDamage(definition,attack.getSpellId(),random)
        : MonsterCombatRules.rollDamage(attack.getName(),random);
    if(selfHeal) {
      int maximum=definition.getHealth();
      int before=life.health()==null ? maximum : Math.min(maximum,life.health());
      int after=(int)Math.min(maximum,(long)before+Math.max(0,raw));
      if(before<=0) return;
      var motion=creatures.capture(spawn.id());
      var heal=new OnlineStore.MonsterHeal(life.key(),life.eventId(),saved.id(),life.nextAttackAt(),
          Math.addExact(now,COOLDOWN_MILLIS),life.stateRevision(),before,after,maximum,motion);
      creatures.reserve(spawn.id());
      try {
        sessions.commitMonsterHeal(token,saved.revision(),UUID.randomUUID(),progress,heal)
            .whenComplete((result,error)->{creatures.release(spawn.id());creatures.damageCommitted(spawn.id());});
      } catch(RuntimeException failure) { creatures.release(spawn.id());throw failure; }
      return;
    }
    var profile=new CombatProfile(stats.level(),stats.strength(),stats.endurance(),stats.agility(),
        attackSkill,attackSkill,stats.dodge(),MonsterCombatRules.rollArmor(stats.armorMin(),stats.armorMax(),random),
        false,false,true,Map.of());
    int damage;
    if (attack!=null && attack.isSpell()) {
      var spell=OnlineMonsterSpellAttacks.supportedSpell(attack.getSpellId());
      if (spell==null) return;
      damage=switch (spell.behavior()) {
        case ELEMENTAL_DAMAGE -> OnlineMonsterSpellAttacks.elementalDamage(
            spell,definition,raw,progress,items,now);
        case ARMOR_DAMAGE -> CombatResolver.resolve(
            new PhysicalAttackRequest(profile,defender.profile(),raw,0,false),random).damage();
        default -> 0;
      };
    } else {
      damage=CombatResolver.resolve(new PhysicalAttackRequest(profile,defender.profile(),raw,0,false),random).damage();
    }
    int hp=Math.max(0,progress.vitals().hp()-damage);
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
            // A deferred deadline leaves aggro intact; a failed acknowledgement still reloads all targets.
            if (fatal && (error!=null || result!=null && result.applied())) creatures.characterDied(saved.id());
          });
    } catch (RuntimeException failure) { creatures.release(spawn.id()); throw failure; }
  }
}
