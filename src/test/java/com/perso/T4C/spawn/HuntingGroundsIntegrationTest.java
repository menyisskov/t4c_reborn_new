package com.perso.T4C.spawn;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.combat.*;
import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.teleport.NamedLocations;
import java.io.File;
import java.util.*;
import org.junit.jupiter.api.Test;

class HuntingGroundsIntegrationTest {
  @Test
  void packAreasResolveToWalkableConnectedTerrainAndStayClearOfQuestWitnesses() throws Exception {
    var collision = new CollisionReader(new File("assets/maps/worldmap/worldmap.colbin"));
    var live = new HashSet<>(SpawnRegistry.monsters());
    try (var terrain = new MapReader(new File("assets/maps/worldmap/worldmap.mapbin"))) {
      for (var ground : HuntingGrounds.all()) {
        assertNotNull(MonsterRegistry.findByName(ground.monster()));
        assertEquals(ground.targetPopulation(), new HashSet<>(ground.points()).size());
        Set<HuntingGrounds.Point> reached = new HashSet<>();
        ArrayDeque<HuntingGrounds.Point> queue = new ArrayDeque<>();
        var root = new HuntingGrounds.Point(ground.x(), ground.y());
        queue.add(root);
        reached.add(root);
        while (!queue.isEmpty()) {
          var p = queue.remove();
          for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            var next = new HuntingGrounds.Point(p.x() + d[0], p.y() + d[1]);
            if (Math.abs(next.x() - root.x()) > ground.areaRadiusTiles()
                || Math.abs(next.y() - root.y()) > ground.areaRadiusTiles()
                || collision.hasCollision(next.x(), next.y())
                || !reached.add(next)) continue;
            queue.add(next);
          }
        }
        for (var point : ground.points()) {
          assertTrue(reached.contains(point), ground.monster() + " point is isolated");
          assertTrue(
              live.contains(
                  new SpawnDefinition(ground.monster(), point.x(), point.y(), 0, false, true)));
          String art = terrain.getGroundSpriteName(point.x(), point.y());
          assertNotNull(art);
          assertFalse(art.startsWith("Ground_Water") || art.equals("Black Tile"));
          for (var npc : SpawnRegistry.npcs())
            if (npc.z() == 0)
              assertTrue(
                  Math.hypot(npc.x() - point.x(), npc.y() - point.y()) >= 45,
                  "Pack too near " + npc.type());
        }
      }
    }
  }

  @Test
  void trainingChampionsHaveSurvivablePhysicalDamageAndGuaranteedScrolls() throws Exception {
    Random random = new Random(1);
    for (var camp : HuntingGrounds.camps()) {
      double armor = camp.tier() == 1 ? 150 : 250;
      var target =
          new CombatProfile(150, 100, 100, 20, 1, 1, 1, armor, false, false, false, Map.of());
      var monster =
          MonsterRegistry.create(TrainingGroundContent.boss(camp.tier(), "Tarantula"), 0, 0);
      var def = ((DataMonster) monster).getDefinition();
      var attacker = CombatProfiles.fromMonster(monster);
      int ceiling = camp.tier() == 1 ? 25 : 40;
      for (int raw = def.getHitDamageMin(); raw <= def.getHitDamageMax(); raw++) {
        var result =
            CombatResolver.resolve(
                new PhysicalAttackRequest(attacker, target, raw, 0, false), random);
        assertTrue(result.damage() <= ceiling, "Training damage ignores the intended AC");
      }
      assertTrue(def.getHealth() >= 100000);
      assertTrue(def.getAcMax() <= 20 && def.getDodge() <= 50, "Low-level attacks must land");
      assertTrue(def.getAttacks().stream().noneMatch(MonsterDef.Attack::isSpell));
      assertFalse(monster.isAggressive());
      assertEquals(
          0, def.getXpPerHit(), "Training XP cannot be farmed indefinitely without a kill");
      assertTrue(def.getXpOnDeath() >= 2000000);
      assertTrue(def.getGoldMin() >= 100000);
      assertTrue(
          monster
              .getLootTable()
              .roll(new Random(1))
              .itemNames()
              .contains("item." + camp.scrollKey()));
      assertTrue(MonsterRegistry.excludesRecoveryLoot(MonsterRegistry.findByName(def.getName())));
      var manager = new MonsterManager(null);
      manager.addMonster(monster);
      monster.takeDamage(def.getHealth());
      monster.setRespawnDelayMillis(0);
      manager.update(1f, null);
      assertFalse(monster.isDead());
      assertEquals(def.getRespawnTime(), monster.getRespawnTime());
    }
  }

  @Test
  void trainingTravelOpensWithEachIslandAndKeepsUnrelatedHostilesOutside() throws Exception {
    var collision = new CollisionReader(new File("assets/maps/worldmap/worldmap.colbin"));
    Player player = new Player();
    for (var camp : HuntingGrounds.camps()) {
      var location =
          NamedLocations.all().stream()
              .filter(l -> camp.x() == l.tileX() && camp.y() == l.tileY())
              .findFirst()
              .orElseThrow();
      assertFalse(NamedLocations.forPlayer(player).contains(location));
      player.setQuestFlag(QuestService.zoneUnlockFlag(camp.unlock()), 1);
      assertTrue(NamedLocations.forPlayer(player).contains(location));
      assertFalse(collision.hasCollision(camp.x(), camp.y()));
      for (var boss : camp.bosses()) {
        assertFalse(collision.hasCollision(boss.x(), boss.y()));
        assertTrue(
            SpawnRegistry.monsters().stream()
                .filter(
                    s ->
                        s.z() == 0
                            && s.aggressive()
                            && !s.type().startsWith("Training ")
                            && !HuntingGrounds.isHuntingType(s.type()))
                .noneMatch(s -> Math.hypot(s.x() - boss.x(), (s.y() - boss.y()) * .5) < 40),
            "High-damage wilderness enemies must stay outside training fights");
      }
    }
  }

  @Test
  void everyTrainingFieldJoinsItsMatchingBossScrollAndTravelDestination() throws Exception {
    for (var g : HuntingGrounds.all()) {
      var travel =
          NamedLocations.all().stream()
              .filter(l -> l.worldZ() == 0 && l.tileX() == g.x() && l.tileY() == g.y())
              .findFirst()
              .orElseThrow();
      assertEquals(g.unlock(), travel.unlockZoneId());
      if (g.trainingTier() == 0) continue;
      var normal = MonsterRegistry.findByName(g.monster());
      var boss = MonsterRegistry.findByName(g.bossType());
      assertEquals(normal.getWalkPattern(), boss.getWalkPattern());
      assertTrue(boss.getHealth() >= normal.getHealth() * 10);
      var spawns =
          SpawnRegistry.monsters().stream().filter(s -> s.type().equals(g.bossType())).toList();
      assertEquals(1, spawns.size(), "Exactly one boss of the matching family");
      assertTrue(
          Math.abs(spawns.get(0).x() - g.x()) <= 20 && Math.abs(spawns.get(0).y() - g.y()) <= 20);
      var monster = MonsterRegistry.create(boss, 0, 0);
      assertTrue(
          monster.getLootTable().roll(new Random(1)).itemNames().contains("item." + g.scrollKey()));
      assertTrue(com.perso.T4C.divine.DivineReturnScrolls.isBoundReturnScroll(g.scrollKey()));
    }
  }
}
