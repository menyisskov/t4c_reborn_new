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
  /**
   * Authored creature spawns that sit where no creature can stand under the hosted collision rules
   * (three inside solid rock, two on a tile with a wall directly above). Persisted creatures must
   * be standable, so these are left out of the hosted world; the list is exact, never a pattern,
   * and HostedKillRegionTest proves each entry is unstandable and nothing else is unstandable.
   */
  final Set<String> unhostableSpawnKeys;
  private HostedRegionPlan(List<Region> regions,PortalCatalog portals,boolean transitions) {
    this(regions,portals,transitions,List.of());
  }
  private HostedRegionPlan(List<Region> regions,PortalCatalog portals,boolean transitions,
      List<PortalContent.Portal> refusedPortals) {
    this(regions,portals,transitions,refusedPortals,Set.of());
  }
  private HostedRegionPlan(List<Region> regions,PortalCatalog portals,boolean transitions,
      List<PortalContent.Portal> refusedPortals,Set<String> unhostableSpawnKeys) {
    this.unhostableSpawnKeys=Set.copyOf(unhostableSpawnKeys);
    this.regions=List.copyOf(regions);this.portals=portals;this.transitions=transitions;
    this.refusedPortals=List.copyOf(refusedPortals);
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
  /**
   * The skeleton cave: the closed 111 x 111 dungeon chamber behind the three authored cave mouths
   * (portals 12, 13, 14) at 2770,1094, inside the renewed_wards kill circle. Its only exit is
   * portal 15 back to the same spot, so the tight bounding box of its walkable tiles is the whole
   * chamber (HostedKillRegionTest proves nothing walkable leaves it).
   */
  static final String CAVE_KEY="skeleton-cave";
  static final int CAVE_X=38,CAVE_Y=582,CAVE_SIZE=111;
  /**
   * The Lighthaven interior, grown from its original 80 x 60 crop to the tight bounding box of its
   * whole walkable component (HostedKillRegionTest floods it): the sewer stair 0 sits at the east
   * end, 71 tiles beyond the old crop. Still entered by door 16 and left by door 17.
   */
  static final String INTERIOR_KEY="lighthaven-interior";
  static final int INTERIOR_X=278,INTERIOR_Y=267,INTERIOR_WIDTH=165,INTERIOR_HEIGHT=166;
  /**
   * The three sewer levels below the Lighthaven interior, chained by portals 0/1, 2/3 and 4/5.
   * Their tight bounding boxes overlap, and one region may not overlap another, so two are
   * cropped where the crop costs nothing or little: the middle level loses its 2,709-tile north
   * west lobe (8 residents, above row 243, where the upper level owns the rectangle) and the lower
   * level loses a 30-tile nub with no residents east of column 277. Everything else is whole.
   */
  static final String SEWER_UPPER_KEY="lighthaven-sewer-upper",SEWER_MIDDLE_KEY="lighthaven-sewer-middle",SEWER_LOWER_KEY="lighthaven-sewer-lower";
  static final int SEWER_UPPER_X=148,SEWER_UPPER_Y=17,SEWER_UPPER_WIDTH=240,SEWER_UPPER_HEIGHT=226;
  static final int SEWER_MIDDLE_X=8,SEWER_MIDDLE_Y=243,SEWER_MIDDLE_WIDTH=210,SEWER_MIDDLE_HEIGHT=169;
  static final int SEWER_LOWER_X=138,SEWER_LOWER_Y=412,SEWER_LOWER_WIDTH=140,SEWER_LOWER_HEIGHT=146;
  /** Sewer stairs (0-5), skeleton cave mouths and exit (12-15) and the Lighthaven door pair (16/17). */
  static final Set<Integer> ENABLED_PORTALS=Set.of(0,1,2,3,4,5,12,13,14,15,16,17);
  /** Lighthaven interior: a Bat under a wall (323,415). Upper sewer: a Brown Rat in rock (291,62) and one under a wall (194,195). Middle: a Goblin (74,257) and a Giant Bat (85,362), both in rock. */
  static final Set<String> UNHOSTABLE_SPAWN_KEYS=Set.of(
      "authored-v1:40bb5f21b99c0b40a71d5188c41483d058486fb97f1b06466e93672e175e12e2",
      "authored-v1:bd1a5bd92418c7a0b6450550290f14cc9496870dbaf0fad49ef58cb76f9aa5ef",
      "authored-v1:4dbbff00c6b46d245b075165a623ffa700b8eb36b4ae20e3be7f7ba24c3c51fd",
      "authored-v1:1450067994ba4d586fca10b92e5f79a89634dc52156b2b9b547cd122df246def",
      "authored-v1:cf0bd2eb81001880a0b396cbe727595908d905c49908bd832e656b1701875918");
  /** World 0 is 6144 x 3072 tiles; the streamed region is the whole world at origin 0, so local equals absolute. */
  static final int WORLD0_WIDTH=6144,WORLD0_HEIGHT=3072;
  /** The cavern is world 2; its streamed region covers the whole world at origin 0. */
  static final String WORLD2_KEY="cavern";
  /** Collision shared by every region of a world, present only for streamed plans (worlds without one use arrays). */
  Map<Integer,SharedCollision> sharedCollision=Map.of();
  /** The routes this plan enables (the shipped list, plus the derived ones when world 0 streams). */
  List<PortalContent.Portal> enabledPortals=List.of();
  /** True when some region is streamed (whole-world, windowed); false for the shipped six-region plan. */
  public boolean streamed() { return regions.stream().anyMatch(r->r.spec().streamed()); }
  public static HostedRegionPlan startingSlice(Path worldCollision) throws IOException {
    return startingSlice(worldCollision,false);
  }
  /**
   * {@code streamWorld0} (server option {@code --stream-world0}, default off) replaces the 249 x 117
   * {@code start} crop by the whole of world 0 as one streamed region with the same key and the
   * same spawn. Everything else (the five world-1 pieces, their portals) stays as shipped; portals
   * from the streamed region to a different hosted region are derived rather than listed, and
   * portals that stay inside the streamed region wait for stage B (same-region transfers).
   */
  public static HostedRegionPlan startingSlice(Path worldCollision,boolean streamWorld0) throws IOException {
    return startingSlice(worldCollision,streamWorld0,false);
  }
  /**
   * {@code streamWorld2} (server option {@code --stream-world2}, needs streamWorld0, default off) also hosts the whole
   * cavern (world 2) as one streamed region. Portals between world 0 and world 2 are then derived like any other
   * cross-region portal; portals inside world 2 wait for same-region transfers.
   */
  public static HostedRegionPlan startingSlice(Path worldCollision,boolean streamWorld0,boolean streamWorld2) throws IOException {
    if(streamWorld2 && !streamWorld0) throw new IllegalArgumentException("World 2 streaming needs world 0 streaming");
    PortalContent content;
    try(var in=HostedRegionPlan.class.getResourceAsStream("/content/portals-v1.json")) {
      if(in==null) throw new IOException("Missing portal content");
      var bytes=in.readNBytes(4*1024*1024+1);if(bytes.length>4*1024*1024) throw new IOException("Portal content too large");
      try { content=new Gson().fromJson(new String(bytes,StandardCharsets.UTF_8),PortalContent.class); }
      catch(RuntimeException invalid) { throw new IOException("Invalid portal content",invalid); }
    }
    if(content==null) throw new IOException("Empty portal content");
    var listed=content.portals().stream().filter(p->ENABLED_PORTALS.contains(p.id())).toList();
    if(listed.size()!=ENABLED_PORTALS.size()) throw new IOException("Missing initial portal route");
    var startSpec=streamWorld0
        ? new RegionMapLoader.Spec(0,"start",0,0,WORLD0_WIDTH,WORLD0_HEIGHT,2944,1059,true)
        : new RegionMapLoader.Spec(0,"start",START_X,START_Y,START_WIDTH,START_HEIGHT,2944,1059);
    var primary=worldCollision.toAbsolutePath().normalize();
    var maps=new HashMap<Integer,Path>();
    maps.put(0,primary);maps.put(1,primary.getParent().getParent().resolve("dungeon/DungeonMap.colbin"));
    if(streamWorld2) maps.put(2,primary.getParent().getParent().resolve("cavern/CavernMap.colbin")); // the server package ships worlds 0 and 1 only
    var destinations=PortalDestinations.load(maps);
    var specList=new ArrayList<RegionMapLoader.Spec>();
    specList.add(startSpec);
    if(streamWorld2) {
      // Spawn: the first authored portal target in world 2 a character can stand on (a fallback only; arrivals use portals).
      var spawn=content.portals().stream().filter(p->p.targetWorld()==2 && destinations.canStand(new CharacterProgress.Respawn(2,p.targetX(),p.targetY()))).findFirst()
          .orElseThrow(()->new IOException("World 2 has no standable portal target"));
      specList.add(new RegionMapLoader.Spec(2,WORLD2_KEY,0,0,destinations.width(2),destinations.height(2),spawn.targetX(),spawn.targetY(),true));
    }
    var specs0=List.of(
        new RegionMapLoader.Spec(1,INTERIOR_KEY,INTERIOR_X,INTERIOR_Y,INTERIOR_WIDTH,INTERIOR_HEIGHT,304,383),
        new RegionMapLoader.Spec(1,CAVE_KEY,CAVE_X,CAVE_Y,CAVE_SIZE,CAVE_SIZE,134,667),
        new RegionMapLoader.Spec(1,SEWER_UPPER_KEY,SEWER_UPPER_X,SEWER_UPPER_Y,SEWER_UPPER_WIDTH,SEWER_UPPER_HEIGHT,168,201),
        new RegionMapLoader.Spec(1,SEWER_MIDDLE_KEY,SEWER_MIDDLE_X,SEWER_MIDDLE_Y,SEWER_MIDDLE_WIDTH,SEWER_MIDDLE_HEIGHT,121,332),
        new RegionMapLoader.Spec(1,SEWER_LOWER_KEY,SEWER_LOWER_X,SEWER_LOWER_Y,SEWER_LOWER_WIDTH,SEWER_LOWER_HEIGHT,154,529));
    specList.addAll(specs0);
    var specs=List.copyOf(specList);
    for(var portal:listed) {
      if(specs.stream().filter(s->s.contains(portal.sourceWorld(),portal.sourceX(),portal.sourceY())).count()!=1
          || specs.stream().filter(s->s.contains(portal.targetWorld(),portal.targetX(),portal.targetY())).count()!=1
          || !destinations.canStand(new CharacterProgress.Respawn(portal.targetWorld(),portal.targetX(),portal.targetY())))
        throw new IOException("Initial portal route has an unavailable destination or source");
    }
    var enabled=new ArrayList<>(listed);
    if(streamWorld0) {
      // Derived rule: an authored portal whose source and target are both hosted, in different
      // regions, onto a tile a character can stand on. Same-region portals (world 0 to world 0)
      // stay refused until stage B proves same-region transfers.
      for(var portal:content.portals()) {
        if(listed.stream().anyMatch(e->e.id()==portal.id())) continue;
        var source=specs.stream().filter(s->s.contains(portal.sourceWorld(),portal.sourceX(),portal.sourceY())).toList();
        var target=specs.stream().filter(s->s.contains(portal.targetWorld(),portal.targetX(),portal.targetY())).toList();
        if(source.size()==1 && target.size()==1 && source.getFirst()!=target.getFirst()
            && destinations.canStand(new CharacterProgress.Respawn(portal.targetWorld(),portal.targetX(),portal.targetY())))
          enabled.add(portal);
      }
    }
    var regions=new ArrayList<Region>();
    var shared=new HashMap<Integer,SharedCollision>();
    for(var spec:specs) {
      if(spec.streamed()) {
        var grid=shared.get(spec.world());
        if(grid==null) {
          grid=SharedCollision.load(maps.get(spec.world()),spec.world(),enabled);
          shared.put(spec.world(),grid);
        }
        if(spec.world()==0 && (grid.width()!=WORLD0_WIDTH || grid.height()!=WORLD0_HEIGHT))
          throw new IOException("World 0 collision is not "+WORLD0_WIDTH+" x "+WORLD0_HEIGHT);
        regions.add(new Region(spec,RegionMapLoader.loadStreamed(grid,spec)));
      } else regions.add(new Region(spec,RegionMapLoader.load(maps.get(spec.world()),spec,enabled)));
    }
    // Authored sources inside a hosted region that stay disabled (their targets are not hosted).
    var refused=content.portals().stream().filter(p->enabled.stream().noneMatch(e->e.id()==p.id()))
        .filter(p->specs.stream().anyMatch(s->s.contains(p.sourceWorld(),p.sourceX(),p.sourceY()))).toList();
    for(var portal:refused) {
      var sourceRegion=specs.stream().filter(s->s.contains(portal.sourceWorld(),portal.sourceX(),portal.sourceY())).findFirst().orElseThrow();
      var hostedTarget=specs.stream().filter(s->s.contains(portal.targetWorld(),portal.targetX(),portal.targetY())).findFirst();
      // A refused route may have a hosted destination only inside its own streamed region (stage B)
      // or onto a tile nobody can stand on; anything else is a route that should have been enabled.
      boolean explained=streamWorld0 && hostedTarget.isPresent() && (hostedTarget.get()==sourceRegion
          || !destinations.canStand(new CharacterProgress.Respawn(portal.targetWorld(),portal.targetX(),portal.targetY())));
      if(hostedTarget.isPresent() && !explained)
        throw new IOException("Refused portal "+portal.id()+" unexpectedly has a hosted destination");
    }
    var unhostable=new HashSet<>(UNHOSTABLE_SPAWN_KEYS);
    if(streamWorld2) {
      var spawn=specs.stream().filter(s->s.world()==2).findFirst().orElseThrow();
      for(var e:UnhostableSpawnAudit.audit(maps.get(2),2,spawn.spawnX(),spawn.spawnY()).entries()) unhostable.add(e.key());
    }
    if(streamWorld0) unhostable.addAll(UnhostableSpawnAudit.bundledKeys()); // world 0 hosts every authored spawn a creature can stand on
    var plan=new HostedRegionPlan(regions,new PortalCatalog(new PortalContent(1,enabled)),true,refused,unhostable);
    plan.sharedCollision=Map.copyOf(shared);
    plan.enabledPortals=List.copyOf(enabled);
    return plan;
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
