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
    Region(RegionMapLoader.Spec spec,PreviewMap map,PreviewWorld world) { this.spec=spec;this.map=map;this.world=world; }
    OnlineRegion descriptor(long epoch) { return new OnlineRegion(spec.world(),spec.key(),map.originX,map.originY,map.width,map.height,epoch); }
  }
  final HostedRegionPlan plan;
  final List<Region> regions;
  final PersistentSessions sessions;
  HostedWorlds(PreviewMap map,PreviewWorld world) {
    plan=HostedRegionPlan.single(map);regions=List.of(new Region(plan.regions.getFirst().spec(),map,world));sessions=null;
  }
  HostedWorlds(HostedRegionPlan plan,OnlineStore store,ItemContent items,MonsterContent monsters) throws SQLException {
    this.plan=plan;
    regions=plan.regions.stream().map(r->new Region(r.spec(),r.map(),new PreviewWorld(r.map(),monsters,r.spec().world()))).toList();
    sessions=store==null ? null : new PersistentSessions(store,this);
    try {
      for(var region:regions) if(store!=null) {
        region.creatures=new PersistentCreatures(store,region.world);
        region.melee=new OnlineMelee(region.map,region.world,sessions,region.creatures,items,new java.security.SecureRandom(),System::currentTimeMillis);
        region.retaliation=new OnlineRetaliation(region.map,region.world,sessions,region.creatures,items,new java.security.SecureRandom(),System::currentTimeMillis);
        long unsupported=region.world.creatureSpawns().stream().filter(s->s.definition().getSourceEvents()!=null && !s.definition().getSourceEvents().isEmpty()
            || s.definition().getLoot()!=null && !s.definition().getLoot().isEmpty() || !OnlineRetaliation.supported(s.definition())).count();
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
    for(var region:regions) { if(region.creatures!=null) region.creatures.step();if(region.retaliation!=null) region.retaliation.step(); }
  }
  @Override public void close() {
    if(sessions!=null) sessions.shutdown();
    for(var region:regions) if(region.creatures!=null) region.creatures.close();
  }
}
