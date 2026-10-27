package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.perso.T4C.online.model.PortalContent;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;

class RegionMapLoaderTest {
  @Test void hostedPlanResolvesBothDirectionsWithoutUnknownWorldFallback() throws Exception {
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    var enter=plan.portals.resolve(0,2931.5,1056.5,plan::canStand).orElseThrow();
    var dungeon=plan.require(enter.destination().world(),enter.destination().x(),enter.destination().y());
    assertEquals(1,dungeon.spec().world());
    var exit=plan.portals.resolve(1,306,381,plan::canStand).orElseThrow();
    assertEquals(0,plan.require(exit.destination().world(),exit.destination().x(),exit.destination().y()).spec().world());
    assertThrows(PreviewWorld.Rejected.class,()->plan.require(999,2944,1059));
    assertThrows(PreviewWorld.Rejected.class,()->plan.require(1,2000,2000));
  }
  @Test void realStartingDoorIsReachableOnlyWithEnabledAuthoredPortalException() throws Exception {
    var spec=new RegionMapLoader.Spec(0,"start",2904,1029,80,60,2944,1059);
    var path=Path.of("../../assets/maps/worldmap/worldmap.colbin");
    var ordinary=RegionMapLoader.load(path,spec,List.of());
    assertFalse(ordinary.canStand(2931-2904+.5,1056-1029+.5));
    var portal=new PortalContent.Portal(16,0,2931,1056,1,304,383);
    var enabled=RegionMapLoader.load(path,spec,List.of(portal));
    assertTrue(enabled.canStand(27.5,27.5));
    assertFalse(enabled.canStand(27.5,26.5),"Neighboring wall remains blocked");
    assertEquals(ordinary.hasLineOfSight(27.5,27.5,27.5,26.5),enabled.hasLineOfSight(27.5,27.5,27.5,26.5));
    var queue=new ArrayDeque<int[]>();var seen=new HashSet<String>();queue.add(new int[]{40,30});seen.add("40,30");
    while(!queue.isEmpty()) { var tile=queue.remove();
      for(var delta:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
        int x=tile[0]+delta[0],y=tile[1]+delta[1];
        if(enabled.canStand(x+.5,y+.5) && seen.add(x+","+y)) queue.add(new int[]{x,y});
      }
    }
    assertTrue(seen.contains("27,27"),"Door must be reachable through the actual crop");
    var dungeon=RegionMapLoader.load(Path.of("../../assets/maps/dungeon/DungeonMap.colbin"),
        new RegionMapLoader.Spec(1,"lighthaven-interior",264,353,80,60,304,383),List.of());
    assertTrue(dungeon.canStand(40,30));
  }
}
