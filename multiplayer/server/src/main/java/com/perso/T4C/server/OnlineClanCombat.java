package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.perso.T4C.combat.CombatResolver;
import com.perso.T4C.combat.CombatProfile;
import com.perso.T4C.combat.MonsterCombatRules;
import com.perso.T4C.combat.PhysicalAttackRequest;
import com.perso.T4C.monster.core.MonsterPursuitRules;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.LongSupplier;

/** Simulation-owned physical clan exchanges; persistence and publication remain in PersistentCreatures. */
final class OnlineClanCombat {
  private final PreviewMap map;
  private final PreviewWorld world;
  private final PersistentCreatures creatures;
  private final MonsterContent.Classified classified;
  private final Random random;
  private final LongSupplier clock;
  private final Map<Long,PreviewWorld.CreatureSpawn> spawns=new HashMap<>();

  OnlineClanCombat(PreviewMap map,PreviewWorld world,PersistentCreatures creatures,
      MonsterContent.Classified classified,Random random,LongSupplier clock) {
    this.map=map;this.world=world;this.creatures=creatures;this.classified=classified;
    this.random=random;this.clock=clock;
    for(var spawn:world.creatureSpawns()) spawns.put(spawn.id(),spawn);
  }

  void step() {
    long now=clock.getAsLong();
    for(long sourceId:creatures.readyClanAttackers()) {
      try { attack(sourceId,now); }
      catch(PreviewWorld.Rejected unavailable) { /* A reserved or reloading life waits. */ }
    }
  }

  private void attack(long sourceId,long now) {
    var sourceSpawn=spawns.get(sourceId);
    var sourceLife=creatures.life(sourceId);
    if(sourceLife.diedAt()!=null || sourceLife.nextAttackAt()>now
        || !"CLAN".equals(sourceLife.mode()) || sourceLife.targetSpawnKey()==null) return;
    Long targetId=creatures.idForKey(sourceLife.targetSpawnKey());
    if(targetId==null || targetId==sourceId) return;
    var targetSpawn=spawns.get(targetId);
    var targetLife=creatures.life(targetId);
    if(targetLife.diedAt()!=null || !targetLife.eventId().equals(sourceLife.targetLife())
        || sourceSpawn.spawn().world()!=targetSpawn.spawn().world()) return;
    var sourceAuthority=classified.authorities().get(sourceSpawn.definition().getName());
    var targetAuthority=classified.authorities().get(targetSpawn.definition().getName());
    if(sourceAuthority==null || targetAuthority==null || !sourceSpawn.spawn().aggressive()
        || sourceSpawn.spawn().stationary() || !sourceSpawn.definition().isCanAttack()
        || sourceAuthority.attackFrameEvidence()!=MonsterContent.AttackFrameEvidence.FRAME_NAME_PRESENT
        || classified.relations().stream().noneMatch(r->r.source()==sourceAuthority.clan()
            && r.target()==targetAuthority.clan())
        || !OnlineRetaliation.supported(sourceSpawn.definition())
        || !OnlineRetaliation.supported(targetSpawn.definition())) return;
    var source=creatures.capture(sourceId);var target=creatures.capture(targetId);
    if(!"CLAN".equals(source.mode()) || !target.key().equals(source.targetSpawnKey())
        || !target.life().equals(source.targetLife())) return;
    double sx=source.x()-map.originX,sy=source.y()-map.originY;
    double tx=target.x()-map.originX,ty=target.y()-map.originY;
    float pixels=(float)Math.hypot((sx-tx)*GRID_W,(sy-ty)*GRID_H);
    if(!MonsterPursuitRules.withinAttackReach(pixels,sourceSpawn.definition().getAttacks())
        || MonsterPursuitRules.tileRange(pixels)>=2 || !map.hasLineOfSight(sx,sy,tx,ty)) return;
    int before=targetLife.health()==null ? targetSpawn.definition().getHealth() : targetLife.health();
    if(before<=0) return;
    var sourceStats=MonsterCombatRules.resolve(sourceSpawn.definition());
    var targetStats=MonsterCombatRules.resolve(targetSpawn.definition());
    var authored=MonsterCombatRules.chooseAttack(sourceSpawn.definition().getAttacks(),random);
    int attackSkill=authored==null ? sourceStats.attack() : Math.max(1,authored.getValue1());
    int raw=authored==null ? sourceSpawn.definition().getHitDamageMin()
        + (sourceSpawn.definition().getHitDamageMax()>sourceSpawn.definition().getHitDamageMin()
            ? random.nextInt(sourceSpawn.definition().getHitDamageMax()-sourceSpawn.definition().getHitDamageMin()+1) : 0)
        : MonsterCombatRules.rollDamage(authored.getName(),random);
    var attacker=new CombatProfile(sourceStats.level(),sourceStats.strength(),sourceStats.endurance(),
        sourceStats.agility(),attackSkill,attackSkill,sourceStats.dodge(),
        MonsterCombatRules.rollArmor(sourceStats.armorMin(),sourceStats.armorMax(),random),
        false,false,true,Map.of());
    var resolved=CombatResolver.resolve(new PhysicalAttackRequest(
        attacker,targetStats.profile(false,random),raw,0,false),random);
    int after=Math.max(0,before-resolved.damage());
    // A defender may acquire its attacker only when the authored relation points back.
    boolean retaliates=target.targetCharacter()==null && target.targetSpawnKey()==null
        && targetSpawn.spawn().aggressive() && !targetSpawn.spawn().stationary()
        && targetSpawn.definition().isCanAttack()
        && targetAuthority.attackFrameEvidence()==MonsterContent.AttackFrameEvidence.FRAME_NAME_PRESENT
        && classified.relations().stream().anyMatch(r->r.source()==targetAuthority.clan()
            && r.target()==sourceAuthority.clan());
    if(after>0 && resolved.damage()>0 && retaliates)
      target=new OnlineStore.SpawnMotion(target.key(),target.life(),target.revision(),target.x(),target.y(),
          "CLAN",null,null,source.key(),source.life());
    var draft=new OnlineStore.ClanAttack(source,target,targetSpawn.definition().getHealth(),
        before,after,sourceLife.nextAttackAt(),Math.addExact(now,OnlineRetaliation.COOLDOWN_MILLIS));
    creatures.submitClanAttack(sourceId,targetId,draft);
  }
}
