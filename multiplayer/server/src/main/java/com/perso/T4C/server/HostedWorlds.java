package com.perso.T4C.server;

import com.perso.T4C.online.model.*;
import com.perso.T4C.server.persistence.OnlineStore;
import java.sql.SQLException;
import java.util.*;

/** One process and simulation owner; region actors are isolated, session capacity is global. */
final class HostedWorlds implements AutoCloseable {
  static final class Region {
    final RegionMapLoader.Spec spec;
    final PreviewMap map;
    final PreviewWorld world;
    PersistentCreatures creatures;
    OnlineMelee melee;
    OnlineSpellStrikes spells;
    OnlineRetaliation retaliation;
    OnlineClanCombat clanCombat;
    RegionNpcs npcs;
    final RegionGroundLoot loot;
    Region(RegionMapLoader.Spec spec,PreviewMap map,PreviewWorld world) { this.spec=spec;this.map=map;this.world=world;this.loot=new RegionGroundLoot(spec); }
    OnlineRegion descriptor(long epoch) { return new OnlineRegion(spec.world(),spec.key(),map.originX,map.originY,map.width,map.height,epoch); }
  }
  final HostedRegionPlan plan;
  final List<Region> regions;
  final PersistentSessions sessions;
  /** Null only for the in-memory single-map seam; pickups need the bundled item content. */
  final ItemContent items;
  private long lastPathPermit=-1;
  HostedWorlds(PreviewMap map,PreviewWorld world) {
    plan=HostedRegionPlan.single(map);regions=List.of(new Region(plan.regions.getFirst().spec(),map,world));sessions=null;items=null;
  }
  HostedWorlds(HostedRegionPlan plan,OnlineStore store,ItemContent items,MonsterContent monsters) throws SQLException,java.io.IOException {
    var catalog=NpcInteractionCatalog.loadBundled();
    var appearances=plan.transitions ? NpcAppearanceCatalog.loadBundled() : null;
    this.plan=plan;this.items=items;
    // The v2 inventory is checked against the bundled v1 export. Supplied subsets remain valid
    // for isolated worlds while their authored definitions and spawn checks stay in PreviewWorld.
    var classified=monsters==null ? null : MonsterCatalog.loadClassifiedBundled(MonsterCatalog.loadBundled());
    var npcFiltered=withoutNpcOwnedSpawns(monsters,classified);
    var hostedMonsters=npcFiltered==null || plan.unhostableSpawnKeys.isEmpty() ? npcFiltered
        : new MonsterContent(npcFiltered.version(),npcFiltered.monsters(),
            npcFiltered.spawns().stream().filter(s->!plan.unhostableSpawnKeys.contains(s.persistenceKey())).toList());
    regions=plan.regions.stream().map(r->new Region(r.spec(),r.map(),new PreviewWorld(r.map(),hostedMonsters,r.spec().world()))).toList();
    for(var region:regions) region.npcs=appearances==null
        ? new RegionNpcs(catalog,items,region.spec)
        : new RegionNpcs(catalog,items,region.spec,appearances);
    sessions=store==null ? null : new PersistentSessions(store,this);
    try {
      for(var region:regions) if(store!=null) {
        validateClanAuthority(region,classifedRequired(classified));
        region.creatures=new PersistentCreatures(store,region.world,region.map,region.spec.key(),sessions,items,classified);
        region.melee=new OnlineMelee(region.map,region.world,sessions,region.creatures,items,new java.security.SecureRandom(),System::currentTimeMillis);
        region.spells=new OnlineSpellStrikes(region.map,region.world,sessions,region.creatures,items,SpellCatalogV2.bundled(),new java.security.SecureRandom(),System::currentTimeMillis);
        region.retaliation=new OnlineRetaliation(region.map,region.world,sessions,region.creatures,items,new java.security.SecureRandom(),System::currentTimeMillis);
        region.clanCombat=new OnlineClanCombat(region.map,region.world,region.creatures,classified,
            new java.security.SecureRandom(),System::currentTimeMillis);
        long unsupported=region.world.creatureSpawns().stream().filter(s->s.definition().getSourceEvents()!=null && !s.definition().getSourceEvents().isEmpty()
            || !OnlineLootDrafts.supports(s.definition(),items) || !OnlineRetaliation.supported(s.definition())).count();
        if(unsupported>0) System.out.println("Region "+region.spec.key()+": "+unsupported+" creatures retain unsupported combat/script restrictions.");
      }
    } catch(SQLException|RuntimeException failure) { close();throw failure; }
  }
  /**
   * Removes authored records the offline game hands to its NPC system (for example the SUNDIAL at
   * 2881,1070). They are not monsters, so the creature world never sees them and no combat
   * definition is invented. Ordinary monster records with no definition still fail PreviewWorld.
   */
  static MonsterContent withoutNpcOwnedSpawns(MonsterContent monsters) throws java.io.IOException {
    if(monsters==null) return null;
    return withoutNpcOwnedSpawns(monsters,MonsterCatalog.loadClassifiedBundled(MonsterCatalog.loadBundled()));
  }
  private static MonsterContent withoutNpcOwnedSpawns(MonsterContent monsters,
      MonsterContent.Classified classified) {
    if(monsters==null) return null;
    var npcOwned=classified.spawns().stream().filter(s->s.ownership()==MonsterContent.Ownership.NPC)
        .map(MonsterContent.ClassifiedSpawn::persistenceKey).collect(java.util.stream.Collectors.toSet());
    var kept=monsters.spawns().stream().filter(s->s.definition()!=null || !npcOwned.contains(s.persistenceKey())).toList();
    return new MonsterContent(monsters.version(),monsters.monsters(),kept);
  }
  private static MonsterContent.Classified classifedRequired(MonsterContent.Classified content) {
    return Objects.requireNonNull(content,"Missing classified authority");
  }
  private static void validateClanAuthority(Region region,MonsterContent.Classified classified) {
    for(var source:region.world.creatureSpawns()) {
      var authority=classified.authorities().get(source.definition().getName());
      if(authority==null) throw new IllegalArgumentException("Missing hosted creature authority");
      if(!source.spawn().aggressive() || source.spawn().stationary()) continue;
      boolean hasTarget=region.world.creatureSpawns().stream().anyMatch(target -> {
        var other=classified.authorities().get(target.definition().getName());
        return other!=null && classified.relations().stream().anyMatch(r->r.source()==authority.clan()
            && r.target()==other.clan());
      });
      if(hasTarget && (authority.attackFrameEvidence()!=MonsterContent.AttackFrameEvidence.FRAME_NAME_PRESENT
          || !source.definition().isCanAttack() || !OnlineRetaliation.supported(source.definition())))
        throw new IllegalArgumentException("Unsupported hosted clan attacker");
    }
  }
  Region require(int world,double x,double y) {
    return regions.stream().filter(r->r.spec.contains(world,x,y)).findFirst().orElseThrow(()->new PreviewWorld.Rejected(409,"unhosted_region"));
  }
  Region actor(String token) {
    return regions.stream().filter(r->r.world.position(token)!=null).findFirst().orElseThrow(()->new PreviewWorld.Rejected(401,"session"));
  }
  boolean canStand(CharacterProgress.Respawn destination) { return plan.canStand(destination); }
  void reloadTargets(UUID character) { for(var region:regions) if(region.creatures!=null) region.creatures.characterDied(character); }
  void step() {
    for(var region:regions) region.world.step();
    if(sessions!=null) sessions.step();
    record Pending(Region region,long id,long ordinal) {}
    var queue=new ArrayList<Pending>();
    for(int regionIndex=0;regionIndex<regions.size();regionIndex++) {
      var region=regions.get(regionIndex);
      if(region.creatures!=null) for(long id:region.creatures.pendingReplans())
        queue.add(new Pending(region,id,((long)regionIndex<<32)|id)); // ids are not bounded by 255 once a region streams
    }
    Pending permit=null;
    if(!queue.isEmpty()) {
      permit=queue.getFirst();
      for(var waiting:queue) if(waiting.ordinal()>lastPathPermit) { permit=waiting;break; }
      lastPathPermit=permit.ordinal();
    }
    var targets=sessions==null ? List.<OnlineMonsterPursuit.TargetView>of() : sessions.targetViews();
    for(var region:regions) {
      if(region.creatures!=null) region.creatures.step(targets,
          permit!=null && permit.region()==region ? permit.id() : null);
      if(region.clanCombat!=null) region.clanCombat.step();
      if(region.retaliation!=null) region.retaliation.step();
    }
  }
  @Override public void close() {
    RuntimeException failure=null;
    try { if(sessions!=null) sessions.shutdown(); } catch(RuntimeException error) { failure=error; }
    for(var region:regions) if(region.creatures!=null) {
      try { region.creatures.close(); }
      catch(RuntimeException error) { if(failure==null) failure=error;else failure.addSuppressed(error); }
    }
    if(failure!=null) throw failure;
  }
}
