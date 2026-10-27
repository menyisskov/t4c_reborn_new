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
  private HostedRegionPlan(List<Region> regions,PortalCatalog portals,boolean transitions) {
    this.regions=List.copyOf(regions);this.portals=portals;this.transitions=transitions;
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
    var specs=List.of(new RegionMapLoader.Spec(0,"start",2904,1029,80,60,2944,1059),
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
    return new HostedRegionPlan(regions,new PortalCatalog(new PortalContent(1,enabled)),true);
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
