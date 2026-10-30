package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;

/**
 * No database. Every hosted Skeleton on the real starter and cave collision maps, provoked, must follow a player who
 * stands 6 to 20 tiles away on any tile single-player's creature A* can reach (static foot collision, one tile of clearance
 * around intermediate cells, the goal exempt; BaseMonster / Pathfinding). Live finding (T4C-0331): a retaliating
 * Skeleton did not follow a player walking away. The server judged creatures and targets by the player's stricter
 * above-foot fold, which refused a large share of the tiles single-player lets a Skeleton walk to and a player rest on.
 */
class OnlineMonsterPursuitRealMapTest {
  private static final Path WORLD=Path.of("../../assets/maps/worldmap/worldmap.colbin");
  private static final double STEP=1.0/PreviewWorld.TICK_RATE;

  @Test void everyProvokedHostedSkeletonFollowsAPlayerOnAnyTileSinglePlayersCreatureCanReach() throws Exception {
    var plan=HostedRegionPlan.startingSlice(WORLD);
    try(var hosted=new HostedWorlds(plan,null,ItemCatalog.loadBundled(),MonsterCatalog.loadBundled())) {
      int total=0;
      var failures=new ArrayList<String>();
      for(var region:hosted.regions) {
        for(var cs:region.world.creatureSpawns()) {
          if(!"Skeleton".equals(cs.definition().getName())) continue;
          var pos=region.world.creaturePosition(cs.id());
          UUID life=UUID.randomUUID(),who=UUID.randomUUID();
          for(int[] d:new int[][]{{12,0},{-12,0},{0,12},{0,-12},{6,0},{-6,0},{0,6},{0,-6},{20,0},{-20,0},{0,20},{0,-20},{8,8},{-8,8},{8,-8},{-8,-8}}) {
            double px=Math.floor(pos.x())+d[0]+.5,py=Math.floor(pos.y())+d[1]+.5;
            if(!region.map.canRestOnGrid(px,py) || !reachable(region.map,(int)pos.x(),(int)pos.y(),(int)px,(int)py)) continue;
            total++;
            var pursuit=new OnlineMonsterPursuit(region.map,cs.spawn(),cs.definition(),region.spec.key(),
                new OnlineMonsterPursuit.Seed(life,pos.x(),pos.y(),OnlineMonsterPursuit.Mode.RETALIATION,who,1));
            var view=new OnlineMonsterPursuit.TargetView(who,1,cs.spawn().world(),region.spec.key(),px,py,true,true,true);
            OnlineMonsterPursuit.Decision last=null;
            double start=Math.hypot(px-pos.x(),py-pos.y());
            for(int tick=0;tick<900;tick++) {
              last=pursuit.step(List.of(view),List.of(),STEP,true,false);
              if(last.attackReady() || last.mode()!=OnlineMonsterPursuit.Mode.RETALIATION) break;
            }
            double end=Math.hypot(px-last.x(),py-last.y());
            if(!(last.attackReady() || last.mode()==OnlineMonsterPursuit.Mode.RETALIATION && end<start-3))
              failures.add(region.spec.key()+" skeleton "+(int)pos.x()+","+(int)pos.y()+" player "+d[0]+","+d[1]+" mode "+last.mode());
          }
        }
      }
      System.out.println("REAL_MAP_PLACEMENTS "+total+" failures "+failures.size());
      assertTrue(total>=100,"the oracle must cover many real placements: "+total);
      assertTrue(failures.isEmpty(),failures.size()+" of "+total+" reachable placements were not followed: "+failures);
    }
  }

  /** The reference reachability: single-player's Pathfinding over static foot collision with one tile of clearance. */
  private static boolean clear(PreviewMap map,int x,int y,boolean goal) {
    int radius=goal ? 0 : 1;
    for(int yy=y-radius;yy<=y+radius;yy++) for(int xx=x-radius;xx<=x+radius;xx++) if(map.blocksFoot(xx,yy)) return false;
    return true;
  }

  private static boolean reachable(PreviewMap map,int sx,int sy,int gx,int gy) {
    var seen=new HashSet<Long>();
    var queue=new ArrayDeque<int[]>();
    queue.add(new int[]{sx,sy});seen.add(sx*100000L+sy);
    while(!queue.isEmpty()) {
      var c=queue.poll();
      if(c[0]==gx && c[1]==gy) return true;
      for(int dy=-1;dy<=1;dy++) for(int dx=-1;dx<=1;dx++) {
        if(dx==0 && dy==0) continue;
        int nx=c[0]+dx,ny=c[1]+dy;
        if(Math.abs(nx-sx)>40 || Math.abs(ny-sy)>40 || !seen.add(nx*100000L+ny)) continue;
        if(!clear(map,nx,ny,nx==gx && ny==gy)) continue;
        if(dx!=0 && dy!=0 && (!clear(map,c[0]+dx,c[1],false) || !clear(map,c[0],c[1]+dy,false))) continue;
        queue.add(new int[]{nx,ny});
      }
    }
    return false;
  }
}
