package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.MonsterContent;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Parity with single-player's creature rule: a creature, its A* and the target it chases are judged by the static foot
 * collision of a tile (CollisionManager.hasStaticCollisionAtGrid), not by the player's stricter one-tile-above fold
 * (PreviewMap.canStand). Players already rest by the foot rule (PreviewMap.canRestOnGrid, T4C-0315), so a player can
 * stand directly under a wall where the old creature rule refused to chase, acquire or even stand.
 */
class OnlineMonsterPursuitFootRuleTest {
  private static final double STEP=1.0/PreviewWorld.TICK_RATE;
  private static final int WALL_ROW=4;

  /** Open room, one wall row. Row 5 is foot-free but folded-blocked (the tile above is the wall). */
  private static PreviewMap room() {
    boolean[][] foot=new boolean[12][24];
    for(int x=0;x<24;x++) foot[WALL_ROW][x]=true;
    boolean[][] folded=new boolean[12][24];
    byte[][] terrain=new byte[12][24];
    for(int y=0;y<12;y++) for(int x=0;x<24;x++) {
      folded[y][x]=foot[y][x] || y>0 && foot[y-1][x];
      terrain[y][x]=(byte)(foot[y][x] ? 1 : 0);
    }
    return new PreviewMap(folded,terrain,foot,null,0,0,2,9);
  }

  private static OnlineMonsterPursuit.TargetView target(UUID id,double x,double y) {
    return new OnlineMonsterPursuit.TargetView(id,1,0,"start",x,y,true,true,true);
  }

  private static OnlineMonsterPursuit skeleton(PreviewMap map,boolean aggressive,OnlineMonsterPursuit.Seed seed) throws Exception {
    var spawn=new MonsterContent.Spawn("foot",  "Skeleton",3,8,0,false,aggressive);
    return new OnlineMonsterPursuit(map,spawn,MonsterCatalog.loadBundled().monsters().get("Skeleton"),"start",seed);
  }

  @Test void theFixtureHasATileAPlayerMayRestOnThatTheOldCreatureRuleRefused() {
    var map=room();
    assertTrue(map.canRestOnGrid(10.5,5.5));
    assertFalse(map.canStand(10.5,5.5));
    assertFalse(map.blocksFoot(10,5));
    assertTrue(map.blocksFoot(10,WALL_ROW));
  }

  @Test void provokedSkeletonFollowsAPlayerRestingDirectlyUnderAWall() throws Exception {
    var map=room();
    UUID life=UUID.randomUUID(),who=UUID.randomUUID();
    var chase=skeleton(map,false,new OnlineMonsterPursuit.Seed(life,3.5,8.5,OnlineMonsterPursuit.Mode.RETALIATION,who,1));
    OnlineMonsterPursuit.Decision last=null;
    for(int tick=0;tick<600;tick++) {
      last=chase.step(List.of(target(who,10.5,5.5)),STEP,true,false);
      if(last.attackReady() || last.mode()!=OnlineMonsterPursuit.Mode.RETALIATION) break;
    }
    assertEquals(OnlineMonsterPursuit.Mode.RETALIATION,last.mode(),"the target is still eligible; no reset");
    assertTrue(last.attackReady(),"the Skeleton reaches its attack range of the player standing under the wall");
    assertTrue(Math.hypot((last.x()-10.5)*32,(last.y()-5.5)*16)<=64);
  }

  @Test void unprovokedPlayerRestingUnderAWallInsideTheAggroRangeIsNoticed() throws Exception {
    var map=room();
    UUID life=UUID.randomUUID(),who=UUID.randomUUID();
    var natural=skeleton(map,true,new OnlineMonsterPursuit.Seed(life,3.5,8.5,OnlineMonsterPursuit.Mode.IDLE,null,-1));
    var decision=natural.step(List.of(target(who,7.5,5.5)),STEP,true,false);
    assertEquals(OnlineMonsterPursuit.Mode.NATURAL,decision.mode());
    assertEquals(who,decision.target());
  }

  @Test void aCreatureMayStandWhereOnlyTheFoldBlocksButNeverOnAWallOrOffTheMap() throws Exception {
    var map=room();
    var spawn=new MonsterContent.Spawn("foot","Skeleton",3,8,0,false,true);
    var definition=MonsterCatalog.loadBundled().monsters().get("Skeleton");
    var world=new PreviewWorld(map,new MonsterContent(MonsterContent.VERSION,Map.of(definition.getName(),definition),List.of(spawn)));
    world.creaturePosition(1,10.5,5.5);
    assertEquals(10.5,world.creaturePosition(1).x());
    assertThrows(IllegalArgumentException.class,() -> world.creaturePosition(1,10.5,WALL_ROW+.5));
    assertThrows(IllegalArgumentException.class,() -> world.creaturePosition(1,-.5,5.5));
    assertThrows(IllegalArgumentException.class,() -> world.creaturePosition(1,Double.NaN,5.5));
    // A creature saved on such a tile is accepted back by the controller instead of failing the reload.
    assertDoesNotThrow(() -> skeleton(map,true,new OnlineMonsterPursuit.Seed(UUID.randomUUID(),10.5,5.5,OnlineMonsterPursuit.Mode.IDLE,null,-1)));
    assertThrows(IllegalArgumentException.class,
        () -> skeleton(map,true,new OnlineMonsterPursuit.Seed(UUID.randomUUID(),10.5,WALL_ROW+.5,OnlineMonsterPursuit.Mode.IDLE,null,-1)));
  }
}
