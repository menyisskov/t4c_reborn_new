package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.*;
import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.MonsterContent;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;

/** No database: the world-0 starter region must hold the temple, Aelric and the whole kill circle. */
class HostedKillRegionTest {
  private static final Path WORLD=Path.of("../../assets/maps/worldmap/worldmap.colbin");

  @Test void regionIsTheSmallestRectangleHoldingTheTempleCropAndEveryObjectiveTile() throws Exception {
    var plan=HostedRegionPlan.startingSlice(WORLD);
    var quest=QuestCatalog.bundled().definition("renewed_wards");
    var spec=plan.require(0,2944.5,1059.5).spec();
    int circleWest=quest.getAreaCenterX()-quest.getAreaRadiusTiles(),circleEast=quest.getAreaCenterX()+quest.getAreaRadiusTiles();
    int circleNorth=quest.getAreaCenterY()-quest.getAreaRadiusTiles(),circleSouth=quest.getAreaCenterY()+quest.getAreaRadiusTiles();
    // The temple crop (2904..2983 x 1029..1088) supplies the north and east bounds.
    assertEquals(circleWest,spec.originX());
    assertEquals(circleSouth+1,spec.originY()+spec.height());
    assertEquals(1029,spec.originY());
    assertEquals(2984,spec.originX()+spec.width());
    for(int x:new int[]{circleWest,circleEast}) for(int y:new int[]{circleNorth,circleSouth})
      assertTrue(spec.contains(0,x,y),"circle corner "+x+","+y);
    assertTrue(spec.width()<=256 && spec.height()<=256,"OnlineRegion per-axis bound");
    assertEquals(1,plan.regions().stream().filter(r->r.spec().world()==0).count(),"One world-0 region, no seam walking");
  }

  @Test void sundialIsExcludedAsNpcOwnedWhileEverySkeletonAnchorIsHosted() throws Exception {
    var plan=HostedRegionPlan.startingSlice(WORLD);
    var monsters=MonsterCatalog.loadBundled();
    try(var hosted=new HostedWorlds(plan,null,ItemCatalog.loadBundled(),monsters)) {
      var start=hosted.require(0,2944.5,1059.5);
      var quest=QuestCatalog.bundled().definition("renewed_wards");
      var inCircle=monsters.spawns().stream().filter(s->"Skeleton".equals(s.definition()) && s.world()==0
          && Math.abs(s.x()-quest.getAreaCenterX())<=quest.getAreaRadiusTiles()
          && Math.abs(s.y()-quest.getAreaCenterY())<=quest.getAreaRadiusTiles()).toList();
      assertFalse(inCircle.isEmpty());
      var hostedKeys=new HashSet<String>();
      for(var spawn:start.world.creatureSpawns()) hostedKeys.add(spawn.spawn().persistenceKey());
      for(var skeleton:inCircle) assertTrue(hostedKeys.contains(skeleton.persistenceKey()),"Skeleton "+skeleton.x()+","+skeleton.y());
      assertTrue(start.world.creatureSpawns().stream().noneMatch(s->"SUNDIAL".equalsIgnoreCase(s.spawn().type())));
      assertTrue(monsters.region(0,start.spec.originX(),start.spec.originY(),start.spec.width(),start.spec.height()).stream()
          .anyMatch(s->"SUNDIAL".equalsIgnoreCase(s.type()) && s.x()==2881 && s.y()==1070),"The record exists in the export");
      assertTrue(start.world.creatureSpawns().stream().allMatch(s->s.definition()!=null));
    }
  }

  @Test void unknownOrdinaryMonsterInsideTheRegionStaysFatal() throws Exception {
    var plan=HostedRegionPlan.startingSlice(WORLD);
    var monsters=MonsterCatalog.loadBundled();
    var spawns=new ArrayList<>(monsters.spawns());
    spawns.add(new MonsterContent.Spawn("NOTAREALMONSTER",null,2800,1090,0,false,true));
    var broken=new MonsterContent(monsters.version(),monsters.monsters(),spawns);
    assertThrows(IllegalArgumentException.class,()->new HostedWorlds(plan,null,ItemCatalog.loadBundled(),broken));
  }

  @Test void disabledPortalsInsideTheRegionAreRefusedAndNeverTransfer() throws Exception {
    var plan=HostedRegionPlan.startingSlice(WORLD);
    assertEquals(Set.of(12,13,14,947),plan.refusedPortals.stream().map(p->p.id()).collect(java.util.stream.Collectors.toSet()));
    for(var portal:plan.refusedPortals) {
      double x=portal.sourceX()+.5,y=portal.sourceY()+.5;
      assertTrue(plan.isRefusedPortalSource(portal.sourceWorld(),x,y));
      assertTrue(plan.portals.resolve(portal.sourceWorld(),x,y,plan::canStand).isEmpty(),"Portal "+portal.id()+" cannot transfer");
      assertFalse(plan.canStand(new com.perso.T4C.online.model.CharacterProgress.Respawn(portal.targetWorld(),portal.targetX(),portal.targetY())),
          "Destination stays unhosted");
      assertThrows(PreviewWorld.Rejected.class,()->plan.require(portal.targetWorld(),portal.targetX(),portal.targetY()));
    }
    var door=plan.portals.resolve(0,2931.5,1056.5,plan::canStand).orElseThrow();
    assertEquals(16,door.portalId(),"The real lighthaven door still works");
    assertFalse(plan.isRefusedPortalSource(0,2931.5,1056.5));
  }

  @Test void temple_aelric_and_kill_circle_are_mutually_walkable_inside_the_region() throws Exception {
    var plan=HostedRegionPlan.startingSlice(WORLD);
    var region=plan.regions().stream().filter(r->r.spec().world()==0).findFirst().orElseThrow();
    var map=region.map();
    var monsters=MonsterCatalog.loadBundled();
    var quest=QuestCatalog.bundled().definition("renewed_wards");
    var npcs=NpcInteractionCatalog.loadBundled().npcs();
    var aelric=npcs.stream().filter(n->"WardenAelric".equals(n.id())).findFirst().orElseThrow();

    var from=reach(map,(int)map.spawnX,(int)map.spawnY,plan);
    // The quest giver is approached to talking range, not stood upon.
    assertTrue(near(map,from,aelric.x()-map.originX,aelric.y()-map.originY),"Aelric reachable from the temple spawn");
    var skeletons=monsters.spawns().stream().filter(s->"Skeleton".equals(s.definition()) && s.world()==0
        && Math.abs(s.x()-quest.getAreaCenterX())<=quest.getAreaRadiusTiles()
        && Math.abs(s.y()-quest.getAreaCenterY())<=quest.getAreaRadiusTiles()).toList();
    assertTrue(skeletons.size()>=quest.getRequiredKills(),"Enough authored targets exist");
    for(var skeleton:skeletons)
      assertTrue(near(map,from,skeleton.x()-map.originX,skeleton.y()-map.originY),"Skeleton "+skeleton.x()+","+skeleton.y()+" reachable");
    // Mutual: from a Skeleton approach the temple door and Aelric are reachable too (walking is symmetric).
    var first=skeletons.getFirst();
    int[] approach=approach(map,from,first.x()-map.originX,first.y()-map.originY);
    var back=reach(map,approach[0],approach[1],plan);
    assertTrue(back[approach[1]][approach[0]]);
    assertTrue(near(map,back,aelric.x()-map.originX,aelric.y()-map.originY));
    assertTrue(back[(int)map.spawnY][(int)map.spawnX],"Temple spawn reachable from the kill circle");
    assertTrue(back[1056-map.originY][2931-map.originX],"Portal 16 source reachable from the kill circle");
  }

  /** Legal melee approach: standable, reachable, within the player's reach with sight to the target. */
  private static boolean near(PreviewMap map,boolean[][] reachable,double tx,double ty) {
    return approach(map,reachable,tx,ty)!=null;
  }
  private static int[] approach(PreviewMap map,boolean[][] reachable,double tx,double ty) {
    for(int y=0;y<map.height;y++) for(int x=0;x<map.width;x++) {
      if(!reachable[y][x]) continue;
      if(Math.hypot((x+.5-tx-.5)*GRID_W,(y+.5-ty-.5)*GRID_H)<=PLAYER_MELEE_ATTACK_RANGE
          && map.allowsCombat(x+.5,y+.5,tx+.5,ty+.5,false)) return new int[]{x,y};
    }
    return null;
  }

  /** Eight-neighbour footprint-aware BFS over tile centres; diagonals need both orthogonal tiles free. */
  private static boolean[][] reach(PreviewMap map,int startX,int startY,HostedRegionPlan plan) {
    var seen=new boolean[map.height][map.width];
    assertTrue(map.canStand(startX+.5,startY+.5),"Start must be standable");
    var queue=new ArrayDeque<int[]>();queue.add(new int[]{startX,startY});seen[startY][startX]=true;
    while(!queue.isEmpty()) {
      var tile=queue.remove();
      for(int dy=-1;dy<=1;dy++) for(int dx=-1;dx<=1;dx++) {
        if(dx==0 && dy==0) continue;
        int x=tile[0]+dx,y=tile[1]+dy;
        if(x<0||y<0||x>=map.width||y>=map.height||seen[y][x]||!map.canStand(x+.5,y+.5)) continue;
        if(dx!=0 && dy!=0 && (!map.canStand(tile[0]+dx+.5,tile[1]+.5) || !map.canStand(tile[0]+.5,tile[1]+dy+.5))) continue;
        // Reached but never expanded: an enabled portal source starts a transfer.
        seen[y][x]=true;
        if(plan.portals.resolve(0,map.originX+x+.5,map.originY+y+.5,plan::canStand).isPresent()) continue;
        queue.add(new int[]{x,y});
      }
    }
    return seen;
  }
}
