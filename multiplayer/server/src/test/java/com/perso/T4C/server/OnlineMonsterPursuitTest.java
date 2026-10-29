package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.online.model.MonsterContent;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineMonsterPursuitTest {
  private static final double STEP = 1.0 / PreviewWorld.TICK_RATE;

  private static MonsterDef skeleton() throws Exception {
    return MonsterCatalog.loadBundled().monsters().get("Skeleton");
  }

  private static OnlineMonsterPursuit pursuit(PreviewMap map, boolean aggressive,
      OnlineMonsterPursuit.Seed seed) throws Exception {
    var spawn = new MonsterContent.Spawn("test", "Skeleton", 1, 3, 0, false, aggressive);
    return new OnlineMonsterPursuit(map, spawn, skeleton(), "start", seed);
  }

  private static OnlineMonsterPursuit.TargetView target(UUID id, long epoch, double x, double y) {
    return new OnlineMonsterPursuit.TargetView(id, epoch, 0, "start", x, y, true, true, true);
  }

  @Test void authoredRangedMonsterStopsAtItsSpellRangeInsteadOfClosingToMelee() throws Exception {
    var definition=MonsterCatalog.loadBundled().monsters().get("Goblin Bomberman");
    assertNotNull(definition);
    assertTrue(definition.getAttacks().stream().anyMatch(attack -> attack!=null && attack.isSpell()
        && attack.getMaxRangeTiles()>2), "The fixture must retain its authored ranged spell");
    var map=new PreviewMap(new boolean[10][30],0,0,1,3);
    var spawn=new MonsterContent.Spawn("ranged","Goblin Bomberman",1,3,0,false,true);
    UUID life=UUID.randomUUID(),character=UUID.randomUUID();
    var chase=new OnlineMonsterPursuit(map,spawn,definition,"start",
        new OnlineMonsterPursuit.Seed(life,1.5,3.5,OnlineMonsterPursuit.Mode.RETALIATION,character,7));
    var inRange=chase.step(List.of(target(character,7,4.5,3.5)),STEP,false,false);
    assertTrue(inRange.attackReady());
    assertEquals(1.5,inRange.x(),1e-8,"Ranged attacks should not force the monster to close distance");
    var outOfRange=new OnlineMonsterPursuit(map,spawn,definition,"start",
        new OnlineMonsterPursuit.Seed(life,1.5,3.5,OnlineMonsterPursuit.Mode.RETALIATION,character,7))
        .step(List.of(target(character,7,18.5,3.5)),STEP,false,false);
    assertFalse(outOfRange.attackReady(),"Authored maximum spell range still applies");
  }

  @Test void chasesThroughWallGapWithoutCrossingBlockedCells() throws Exception {
    boolean[][] cells = new boolean[9][10];
    for (int y = 0; y < 9; y++) if (y < 5 || y > 7) cells[y][4] = true;
    var map = new PreviewMap(cells, 0, 0, 1, 3);
    UUID life = UUID.randomUUID(), character = UUID.randomUUID();
    var chase = pursuit(map, false,
        new OnlineMonsterPursuit.Seed(life, 1.5, 3.5,
            OnlineMonsterPursuit.Mode.RETALIATION, character, 7));
    var player = target(character, 7, 7.5, 3.5);
    boolean reached = false;
    boolean crossedGap = false;
    for (int tick = 0; tick < 400; tick++) {
      var decision = chase.step(List.of(player), STEP, true, false);
      assertTrue(map.canStand(decision.x(), decision.y()));
      if (!crossedGap && decision.x() >= 4) {
        assertTrue(decision.y() >= 5 && decision.y() < 8,
            "the actor must enter through the clearance-safe gap");
        crossedGap = true;
      }
      if (decision.attackReady()) { reached = true; break; }
    }
    assertTrue(crossedGap);
    assertTrue(reached, "reachable provoked target must eventually enter physical reach");
  }

  @Test void oneTileGapCannotBypassOfflineCreatureClearance() throws Exception {
    boolean[][] cells = new boolean[9][10];
    for (int y = 0; y < 9; y++) if (y != 6) cells[y][4] = true;
    var map = new PreviewMap(cells, 0, 0, 1, 3);
    UUID life = UUID.randomUUID(), character = UUID.randomUUID();
    var chase = pursuit(map, false,
        new OnlineMonsterPursuit.Seed(life, 1.5, 3.5,
            OnlineMonsterPursuit.Mode.RETALIATION, character, 7));
    var player = target(character, 7, 7.5, 3.5);
    boolean gaveUp = false;
    for (int tick = 0; tick < 35; tick++) {
      var decision = chase.step(List.of(player), STEP, true, false);
      assertFalse(decision.attackReady());
      assertTrue(decision.x() < 4, "the actor cannot pass a one-tile opening");
      if (decision.mode() == OnlineMonsterPursuit.Mode.RETURN) {
        gaveUp = true;
        break;
      }
    }
    assertTrue(gaveUp, "an unreachable player path must trigger the authored return");
  }

  @Test void sealedDiagonalCannotBeCutAndUnreachableChaseReturnsWithoutHealingOrTeleport() throws Exception {
    boolean[][] cells = new boolean[5][5];
    cells[3][2] = true;
    cells[4][1] = true;
    cells[2][1] = true;
    cells[3][0] = true;
    var map = new PreviewMap(cells, 0, 0, 1, 3);
    UUID life = UUID.randomUUID(), character = UUID.randomUUID();
    var chase = pursuit(map, false,
        new OnlineMonsterPursuit.Seed(life, 1.5, 3.5,
            OnlineMonsterPursuit.Mode.RETALIATION, character, 1));
    OnlineMonsterPursuit.Decision result = null;
    for (int tick = 0; tick < 25; tick++) {
      result = chase.step(List.of(target(character, 1, 2.5, 2.5)), STEP, true, false);
      assertEquals(1.5, result.x(), 1e-8);
      assertEquals(3.5, result.y(), 1e-8);
    }
    for (int tick = 0; tick < 5; tick++) {
      result = chase.step(List.of(target(character, 1, 2.5, 2.5)), STEP, true, false);
      if (result.mode() != OnlineMonsterPursuit.Mode.RETALIATION) break;
    }
    assertEquals(OnlineMonsterPursuit.Mode.RETURN, result.mode());
    for (int tick = 0; tick < 25; tick++) {
      result = chase.step(List.of(), STEP, true, false);
      if (result.mode() == OnlineMonsterPursuit.Mode.PATROL) break;
    }
    assertEquals(OnlineMonsterPursuit.Mode.PATROL, result.mode());
    assertEquals(life, result.life());
    assertNull(result.target());
    assertEquals(1, result.x(), 1e-8);
    assertEquals(3, result.y(), 1e-8);
  }

  @Test void naturalAndRetaliationUseDifferentCurrentDistanceLeashes() throws Exception {
    var map = new PreviewMap(new boolean[8][100], 0, 0, 1, 3);
    UUID life = UUID.randomUUID(), character = UUID.randomUUID();
    var natural = pursuit(map, true,
        new OnlineMonsterPursuit.Seed(life, 1.5, 3.5,
            OnlineMonsterPursuit.Mode.NATURAL, character, 2));
    var retaliation = pursuit(map, false,
        new OnlineMonsterPursuit.Seed(life, 1.5, 3.5,
            OnlineMonsterPursuit.Mode.RETALIATION, character, 2));
    var far = target(character, 2, 40.5, 3.5);
    assertEquals(OnlineMonsterPursuit.Mode.RETURN,
        natural.step(List.of(far), STEP, false, false).mode());
    assertEquals(OnlineMonsterPursuit.Mode.RETALIATION,
        retaliation.step(List.of(far), STEP, false, false).mode());
  }

  @Test void absenceAndUnreadyLeaseSuspendRestoredTargetWithoutUsingStalePosition() throws Exception {
    var map = new PreviewMap(new boolean[8][8], 0, 0, 1, 3);
    UUID life = UUID.randomUUID(), character = UUID.randomUUID();
    var chase = pursuit(map, false,
        new OnlineMonsterPursuit.Seed(life, 1.5, 3.5,
            OnlineMonsterPursuit.Mode.RETALIATION, character, 4));
    var absent = chase.step(List.of(), STEP, true, false);
    assertTrue(absent.suspended());
    assertEquals(character, absent.target());
    assertEquals(1.5, absent.x(), 1e-8);
    var unready = new OnlineMonsterPursuit.TargetView(character, 4, 0, "start",
        2.5, 3.5, true, false, true);
    assertTrue(chase.step(List.of(unready), STEP, true, false).suspended());
    var restored = chase.step(List.of(target(character, 4, 2.5, 3.5)), STEP, true, false);
    assertFalse(restored.suspended());
    assertTrue(restored.attackReady());
    assertEquals(1.5, restored.x(), 1e-8);
    var wrongEpoch = chase.step(List.of(target(character, 5, 2.5, 3.5)), STEP, false, false);
    assertEquals(OnlineMonsterPursuit.Mode.RETURN, wrongEpoch.mode());
    assertNull(wrongEpoch.target());
    var migrated = pursuit(map, false, new OnlineMonsterPursuit.Seed(life, 1.5, 3.5,
        OnlineMonsterPursuit.Mode.RETALIATION, character, -1));
    assertTrue(migrated.step(List.of(unready), STEP, true, false).suspended(),
        "A migrated target with unknown epoch waits for a ready lease");
    assertEquals(4,migrated.step(List.of(target(character,4,2.5,3.5)),STEP,false,false).targetEpoch());
  }

  @Test void walkableSightBlockerDoesNotClearChaseButSanctuaryDoes() throws Exception {
    UUID life = UUID.randomUUID(), character = UUID.randomUUID();
    var seed = new OnlineMonsterPursuit.Seed(life, 1.5, 3.5,
        OnlineMonsterPursuit.Mode.RETALIATION, character, 9);
    var player = target(character, 9, 2.5, 3.5);
    var terrain = new byte[8][8];
    terrain[3][2] = 9; // Walkable terrain that blocks only line of sight.
    var sightBlocker = new PreviewMap(new boolean[8][8], terrain, 0, 0, 1, 3);
    assertTrue(sightBlocker.canStand(player.x(), player.y()));
    assertTrue(sightBlocker.combatEndpointsSafe(1.5, 3.5, player.x(), player.y(), false));
    assertFalse(sightBlocker.allowsCombat(1.5, 3.5, player.x(), player.y(), false));
    var chasing = pursuit(sightBlocker, false, seed).step(List.of(player), STEP, false, false);
    assertEquals(OnlineMonsterPursuit.Mode.RETALIATION, chasing.mode());
    assertEquals(character, chasing.target());
    assertFalse(chasing.attackReady());

    terrain[3][2] = 6; // Actual PvE sanctuary at the same walkable endpoint.
    var sanctuary = new PreviewMap(new boolean[8][8], terrain, 0, 0, 1, 3);
    assertFalse(sanctuary.combatEndpointsSafe(1.5, 3.5, player.x(), player.y(), false));
    var reset = pursuit(sanctuary, false, seed).step(List.of(player), STEP, false, false);
    assertEquals(OnlineMonsterPursuit.Mode.RETURN, reset.mode());
    assertNull(reset.target());
  }

  @Test void directedGoblinHostilityTracksHorseLifeButAPlayerHitTakesTheTarget() throws Exception {
    var content=MonsterCatalog.loadBundled();
    var classified=MonsterCatalog.loadClassifiedBundled(content);
    var goblin=content.monsters().get("Goblin");
    var horse=content.monsters().get("Wild Horse");
    assertNotNull(goblin);assertNotNull(horse);
    assertTrue(classified.relations().stream().anyMatch(r->r.source()==MonsterContent.Clan.GOBLIN
        && r.target()==MonsterContent.Clan.HORSE));
    var map=new PreviewMap(new boolean[10][10],0,0,3,3);
    UUID goblinLife=UUID.randomUUID(),horseLife=UUID.randomUUID(),player=UUID.randomUUID();
    var goblinSpawn=new MonsterContent.Spawn("Goblin","Goblin",3,3,0,false,true);
    var hunter=new OnlineMonsterPursuit(map,goblinSpawn,goblin,"start",
        new OnlineMonsterPursuit.Seed(goblinLife,3.5,3.5,OnlineMonsterPursuit.Mode.IDLE,null,-1),
        MonsterContent.Clan.GOBLIN,java.util.Set.of(MonsterContent.Clan.HORSE),true);
    var prey=new OnlineMonsterPursuit.ClanView("horse-life",horseLife,0,"start",4.5,3.5,true,
        MonsterContent.Clan.HORSE);
    var acquired=hunter.step(List.of(),List.of(prey),STEP,false,false);
    assertEquals(OnlineMonsterPursuit.Mode.CLAN,acquired.mode());
    assertEquals(prey.key(),acquired.targetSpawnKey());assertEquals(horseLife,acquired.targetLife());
    assertTrue(acquired.attackReady());
    var replaced=new OnlineMonsterPursuit.ClanView(prey.key(),UUID.randomUUID(),0,"start",4.5,3.5,true,
        MonsterContent.Clan.HORSE);
    var stale=hunter.step(List.of(),List.of(replaced),STEP,false,false);
    assertEquals(OnlineMonsterPursuit.Mode.RETURN,stale.mode());assertNull(stale.targetSpawnKey());
    assertFalse(stale.attackReady(),"A respawned target is a different life");
    hunter.provokeCommitted(goblinLife,target(player,4,2.5,3.5));
    var retargeted=hunter.step(List.of(target(player,4,2.5,3.5)),List.of(replaced),STEP,false,false);
    assertEquals(OnlineMonsterPursuit.Mode.RETALIATION,retargeted.mode());
    assertEquals(player,retargeted.target());assertNull(retargeted.targetSpawnKey());
    var passive=new OnlineMonsterPursuit(map,new MonsterContent.Spawn("Horse","Wild Horse",4,3,0,false,true),
        horse,"start",new OnlineMonsterPursuit.Seed(horseLife,4.5,3.5,OnlineMonsterPursuit.Mode.IDLE,null,-1),
        MonsterContent.Clan.HORSE,java.util.Set.of(MonsterContent.Clan.GOBLIN),false);
    var horseDecision=passive.step(List.of(),List.of(new OnlineMonsterPursuit.ClanView("goblin-life",goblinLife,
        0,"start",3.5,3.5,true,MonsterContent.Clan.GOBLIN)),STEP,false,false);
    assertNull(horseDecision.targetSpawnKey());assertFalse(horseDecision.attackReady());
  }
}
