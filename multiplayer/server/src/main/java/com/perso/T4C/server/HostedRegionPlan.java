package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.online.model.PortalContent;
import com.perso.T4C.online.model.CharacterProgress;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/** Explicit same-process region ownership, prepared and validated before listening. */
public final class HostedRegionPlan {
  public record Region(RegionMapLoader.Spec spec,PreviewMap map) {}
  final List<Region> regions;
  final PortalCatalog portals;
  final boolean transitions;
  /** Authored portal sources inside a hosted region whose route is deliberately not enabled. */
  final List<PortalContent.Portal> refusedPortals;
  /** Accepted checkpoint exclusion: Goblin-to-Horse hostility is not simulated in this plan. */
  final boolean clanHostilityNotSimulated;
  private HostedRegionPlan(List<Region> regions,PortalCatalog portals,boolean transitions) {
    this(regions,portals,transitions,List.of(),false);
  }
  private HostedRegionPlan(List<Region> regions,PortalCatalog portals,boolean transitions,
      List<PortalContent.Portal> refusedPortals,boolean clanHostilityNotSimulated) {
    this.regions=List.copyOf(regions);this.portals=portals;this.transitions=transitions;
    this.refusedPortals=List.copyOf(refusedPortals);this.clanHostilityNotSimulated=clanHostilityNotSimulated;
    var keys=new HashSet<String>();
    for(var region:regions) if(!keys.add(region.spec().key())) throw new IllegalArgumentException("Duplicate region key");
    for(int i=0;i<regions.size();i++) for(int j=i+1;j<regions.size();j++) {
      var a=regions.get(i).spec();var b=regions.get(j).spec();
      if(a.world()==b.world() && a.originX()<(long)b.originX()+b.width() && b.originX()<(long)a.originX()+a.width()
          && a.originY()<(long)b.originY()+b.height() && b.originY()<(long)a.originY()+a.height())
        throw new IllegalArgumentException("Overlapping region ownership");
    }
  }
  static HostedRegionPlan single(PreviewMap map) {
    var spec=new RegionMapLoader.Spec(0,"start",map.originX,map.originY,map.width,map.height,
        map.originX+(int)map.spawnX,map.originY+(int)map.spawnY);
    return new HostedRegionPlan(List.of(new Region(spec,map)),new PortalCatalog(new PortalContent(1,List.of())),false);
  }
  /**
   * World-0 starter region: the original temple crop plus every tile of the renewed_wards kill
   * circle (radius 50 around 2785,1095), the smallest rectangle holding both. Same-world regions
   * cannot be walked between, so the circle must live in the same region as the temple.
   */
  static final int START_X=2735,START_Y=1029,START_WIDTH=249,START_HEIGHT=117;
  public static HostedRegionPlan startingSlice(Path worldCollision) throws IOException {
    PortalContent content;
    try(var in=HostedRegionPlan.class.getResourceAsStream("/content/portals-v1.json")) {
      if(in==null) throw new IOException("Missing portal content");
      var bytes=in.readNBytes(4*1024*1024+1);if(bytes.length>4*1024*1024) throw new IOException("Portal content too large");
      try { content=new Gson().fromJson(new String(bytes,StandardCharsets.UTF_8),PortalContent.class); }
      catch(RuntimeException invalid) { throw new IOException("Invalid portal content",invalid); }
    }
    if(content==null) throw new IOException("Empty portal content");
    var enabled=content.portals().stream().filter(p->p.id()==16 || p.id()==17).toList();
    if(enabled.size()!=2) throw new IOException("Missing initial portal route");
    var specs=List.of(new RegionMapLoader.Spec(0,"start",START_X,START_Y,START_WIDTH,START_HEIGHT,2944,1059),
        new RegionMapLoader.Spec(1,"lighthaven-interior",264,353,80,60,304,383));
    var primary=worldCollision.toAbsolutePath().normalize();
    var maps=Map.of(0,primary,1,primary.getParent().getParent().resolve("dungeon/DungeonMap.colbin"));
    var destinations=PortalDestinations.load(maps);
    for(var portal:enabled) {
      if(specs.stream().filter(s->s.contains(portal.sourceWorld(),portal.sourceX(),portal.sourceY())).count()!=1
          || specs.stream().filter(s->s.contains(portal.targetWorld(),portal.targetX(),portal.targetY())).count()!=1
          || !destinations.canStand(new CharacterProgress.Respawn(portal.targetWorld(),portal.targetX(),portal.targetY())))
        throw new IOException("Initial portal route has an unavailable destination or source");
    }
    var regions=new ArrayList<Region>();
    for(var spec:specs) regions.add(new Region(spec,RegionMapLoader.load(maps.get(spec.world()),spec,enabled)));
    // Authored sources inside a hosted region that stay disabled (their targets are not hosted).
    var refused=content.portals().stream().filter(p->enabled.stream().noneMatch(e->e.id()==p.id()))
        .filter(p->specs.stream().anyMatch(s->s.contains(p.sourceWorld(),p.sourceX(),p.sourceY()))).toList();
    for(var portal:refused) if(specs.stream().anyMatch(s->s.contains(portal.targetWorld(),portal.targetX(),portal.targetY())))
      throw new IOException("Refused portal "+portal.id()+" unexpectedly has a hosted destination");
    return new HostedRegionPlan(regions,new PortalCatalog(new PortalContent(1,enabled)),true,refused,true);
  }
  /** True when the tile is an authored portal source whose route is deliberately unavailable. */
  public boolean isRefusedPortalSource(int world,double x,double y) {
    if(!Double.isFinite(x) || !Double.isFinite(y)) return false;
    return refusedPortals.stream().anyMatch(p->p.sourceWorld()==world && p.sourceX()==(int)Math.floor(x) && p.sourceY()==(int)Math.floor(y));
  }
  public Region require(int world,double x,double y) {
    return regions.stream().filter(r->r.spec().contains(world,x,y)).findFirst()
        .orElseThrow(()->new PreviewWorld.Rejected(409,"unhosted_region"));
  }
  public List<Region> regions() { return regions; }
  public boolean canStand(CharacterProgress.Respawn position) {
    try { var region=require(position.world(),position.x(),position.y());
      return region.map().canStand(position.x()-region.map().originX,position.y()-region.map().originY);
    } catch(PreviewWorld.Rejected unavailable) { return false; }
  }
}
