package com.perso.T4C.monster.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.exception.GameException;
import org.junit.jupiter.api.Test;

class MonsterManagerHuntingRespawnTest {
  @Test
  void denseFieldLoadsOneCreaturePerAnchorAndRefillsTheWholePack() throws Exception {
    var field = com.perso.T4C.spawn.HuntingGrounds.all().getFirst();
    MonsterManager loaded = new MonsterManager(null);
    loaded.initializeMonstersFromMap("assets/maps/worldmap/worldmap.mapbin");
    loaded.updateVisible(
        0f,
        null,
        field.x() - field.areaRadiusTiles(),
        field.x() + field.areaRadiusTiles(),
        field.y() - field.areaRadiusTiles(),
        field.y() + field.areaRadiusTiles(),
        0);
    assertEquals(
        field.targetPopulation(),
        loaded.getMonsters().stream()
            .filter(
                m ->
                    com.perso.T4C.spawn.HuntingGrounds.at(
                            m.getCanonicalName(), m.getSpawnTileX(), m.getSpawnTileY())
                        == field)
            .count(),
        "The global multiplier must not duplicate dense-field anchors");
    MonsterManager manager = new MonsterManager(null);
    for (var point : field.points()) {
      TestMonster monster = new TestMonster(field.monster(), point.x(), point.y());
      manager.addMonster(monster);
      monster.takeDamage(10);
      monster.deathTime = System.currentTimeMillis() - 13_000L;
    }
    manager.updateVisible(
        1f,
        null,
        field.x() - field.areaRadiusTiles(),
        field.x() + field.areaRadiusTiles(),
        field.y() - field.areaRadiusTiles(),
        field.y() + field.areaRadiusTiles(),
        0);
    assertEquals(
        field.targetPopulation(),
        manager.getMonsters().stream().filter(m -> !m.isDead()).count(),
        "The old local cap must not starve an expanded clearing");
  }

  @Test
  void huntingPackCanBePulledButReturnsBeforeLeavingItsDistrict() throws Exception {
    var field = com.perso.T4C.spawn.HuntingGrounds.all().getFirst();
    com.perso.T4C.helper.CollisionManager.getInstance().clear();
    java.util.List<BaseMonster> pack = new java.util.ArrayList<>();
    for (var point : field.points())
      pack.add(new TestMonster(field.monster(), point.x(), point.y()));
    var player = new com.badlogic.gdx.math.Vector2(field.x() * 32f, field.y() * 16f);
    int largestPull = 0;
    // Walk a small circuit at player speed; the enemies must gather through normal AI.
    for (int[] corner : new int[][] {{-12, -18}, {12, -18}, {12, 18}, {-12, 18}, {0, 0}}) {
      var destination =
          new com.badlogic.gdx.math.Vector2(
              (field.x() + corner[0]) * 32f, (field.y() + corner[1]) * 16f);
      while (player.dst2(destination) > 1f) {
        var step = destination.cpy().sub(player);
        if (step.len() > 12f) step.setLength(12f);
        player.add(step);
        for (var monster : pack) monster.update(.1f, player, pack);
        largestPull =
            Math.max(largestPull, (int) pack.stream().filter(BaseMonster::isAggro).count());
      }
    }
    assertTrue(
        largestPull >= 30,
        "Moving through the clearing must gather at least 30 enemies; got " + largestPull);
    player.set((field.x() + field.leashRadiusTiles() + 8) * 32f, field.y() * 16f);
    for (var monster : pack) {
      monster.update(0f, player, pack);
      assertFalse(monster.isAggro());
    }
  }

  @Test
  void returningToClearedHuntingAreaRefillsItAfterTheCorpseAnimation() throws Exception {
    MonsterManager manager = new MonsterManager(null);
    TestMonster exile = new TestMonster("Ashbound Exile");
    manager.addMonster(exile);
    manager.updateVisible(0f, null, 0, 0, 0, 0, 0);
    exile.takeDamage(10);
    exile.makeDeathSixSecondsOld();

    manager.updateVisible(1f, null, 100, 101, 100, 101, 0);
    assertTrue(exile.isDead());
    manager.updateVisible(1f, null, 0, 0, 0, 0, 0);

    assertFalse(exile.isDead());
  }

  @Test
  void returningToALegacyAreaDoesNotShortenItsRespawn() throws Exception {
    MonsterManager manager = new MonsterManager(null);
    TestMonster rat = new TestMonster("Brown Rat");
    manager.addMonster(rat);
    manager.updateVisible(0f, null, 0, 0, 0, 0, 0);
    rat.takeDamage(10);
    rat.makeDeathSixSecondsOld();

    manager.updateVisible(1f, null, 100, 101, 100, 101, 0);
    manager.updateVisible(1f, null, 0, 0, 0, 0, 0);

    assertTrue(rat.isDead());
  }

  private static final class TestMonster extends BaseMonster {
    private TestMonster(String name) throws GameException {
      this(name, 0, 0);
    }

    private TestMonster(String name, int x, int y) throws GameException {
      super(
          name,
          x * com.perso.T4C.config.GameConstants.GRID_W,
          y * com.perso.T4C.config.GameConstants.GRID_H,
          10,
          0,
          0,
          0,
          1,
          1,
          30_000L,
          null,
          null,
          null,
          null,
          null);
    }

    private void makeDeathSixSecondsOld() {
      deathTime = System.currentTimeMillis() - 6_000L;
    }
  }
}
