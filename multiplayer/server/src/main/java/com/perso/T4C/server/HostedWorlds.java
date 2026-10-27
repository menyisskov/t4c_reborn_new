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
    OnlineRetaliation retaliation;
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
    this.plan=plan;this.items=items;
    regions=plan.regions.stream().map(r->new Region(r.spec(),r.map(),new PreviewWorld(r.map(),monsters,r.spec().world()))).toList();
    for(var region:regions) region.npcs=new RegionNpcs(catalog,items,region.spec);
    sessions=store==null ? null : new PersistentSessions(store,this);
    try {
      for(var region:regions) if(store!=null) {
        // The authored Goblin -> Horse hostility needs a separate monster-vs-monster authority.
        // Refuse a hosted crop containing both until that transaction path exists.
        boolean goblins=region.world.creatureSpawns().stream().anyMatch(s->s.definition().getName().toLowerCase(Locale.ROOT).contains("goblin"));
        boolean horses=region.world.creatureSpawns().stream().anyMatch(s->s.definition().getName().toLowerCase(Locale.ROOT).contains("horse"));
        if(goblins && horses) throw new IllegalArgumentException("Unsupported clan combat in hosted region");
        region.creatures=new PersistentCreatures(store,region.world,region.map,region.spec.key(),sessions,items);
        region.melee=new OnlineMelee(region.map,region.world,sessions,region.creatures,items,new java.security.SecureRandom(),System::currentTimeMillis);
        region.retaliation=new OnlineRetaliation(region.map,region.world,sessions,region.creatures,items,new java.security.SecureRandom(),System::currentTimeMillis);
        long unsupported=region.world.creatureSpawns().stream().filter(s->s.definition().getSourceEvents()!=null && !s.definition().getSourceEvents().isEmpty()
            || !OnlineLootDrafts.supports(s.definition(),items) || !OnlineRetaliation.supported(s.definition())).count();
        if(unsupported>0) System.out.println("Region "+region.spec.key()+": "+unsupported+" creatures retain unsupported combat/script restrictions.");
      }
    } catch(SQLException|RuntimeException failure) { close();throw failure; }
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
        queue.add(new Pending(region,id,regionIndex*256L+id));
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
