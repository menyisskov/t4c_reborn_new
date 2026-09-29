package com.perso.T4C.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.death.DeathPenaltyService;
import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.definition.HollowDawnCampaign;
import com.perso.T4C.quest.definition.AvalonWildsVigil;
import com.perso.T4C.quest.definition.FadingVeilReckoning;
import com.perso.T4C.spawn.SpawnDefinition;
import com.perso.T4C.spawn.SpawnRegistry;
import com.perso.T4C.teleport.NamedLocations;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class HollowDawnCampaignIntegrationTest {
  @Test
  void allNewObjectivesHaveRegisteredReachableEncounterSpawns() throws Exception {
    CollisionReader collision =
        new CollisionReader(new File("assets/maps/worldmap/worldmap.colbin"));
    try (MapReader map = new MapReader(new File("assets/maps/worldmap/worldmap.mapbin"))) {
      List<String> bad = new ArrayList<>();
      for (QuestDef stage : HollowDawnCampaign.all()) {
        assertNotNull(QuestRegistry.findById(stage.getId()), stage.getId());
        assertNotNull(MonsterRegistry.findByName(stage.getTargetMonster()), stage.getId());
        List<SpawnDefinition> points =
            SpawnRegistry.monsters().stream()
                .filter(s -> stage.getTargetMonster().equals(s.type()))
                .toList();
        assertTrue(
            points.size() >= stage.getRequiredKills() / 5 || points.size() >= 1,
            stage.getId() + " needs encounter spawns");
        for (SpawnDefinition point : points) {
          if (collision.hasCollision(point.x(), point.y()))
            bad.add(point.type() + " blocked at " + point.x() + "," + point.y());
          if (DeathPenaltyService.isSafeHaven(collision.getCollision(point.x(), point.y())))
            bad.add(point.type() + " in safe zone at " + point.x() + "," + point.y());
          String ground = map.getGroundSpriteName(point.x(), point.y());
          if (ground == null || ground.startsWith("Ground_Water") || ground.equals("Black Tile"))
            bad.add(point.type() + " on water at " + point.x() + "," + point.y());
          long dx = point.x() - stage.getAreaCenterX(), dy = point.y() - stage.getAreaCenterY();
          if (dx * dx + dy * dy > (long) stage.getAreaRadiusTiles() * stage.getAreaRadiusTiles())
            bad.add(point.type() + " outside quest area at " + point.x() + "," + point.y());
        }
      }
      assertTrue(bad.isEmpty(), String.join("\n", bad));
      assertFalse(collision.hasCollision(5505, 1150), "arrival camp must be walkable");
      assertTrue(
          DeathPenaltyService.isSafeHaven(collision.getCollision(5505, 1150)),
          "arrival camp must be safe");
      assertFalse(collision.hasCollision(5650, 2290), "final arena must be walkable");
    }
  }

  @Test
  void travelRequiresBothStoryUnlocks() {
    Player player = new Player();
    assertFalse(has(player, "Threnody Reach"));
    assertFalse(has(player, "The Hollow Dawn"));
    player.setQuestFlag(QuestService.zoneUnlockFlag("threnody_reach"), 1);
    assertTrue(has(player, "Threnody Reach"));
    assertFalse(has(player, "The Hollow Dawn"));
    player.setQuestFlag(QuestService.zoneUnlockFlag("hollow_dawn"), 1);
    assertTrue(has(player, "The Hollow Dawn"));
  }

  @Test
  void campaignStagesAndJournalPrerequisitesRepresentBothPaths() {
    assertEquals(10, HollowDawnCampaign.all().size());
    for (String first : List.of("moonwake_missing", "emberglass_oath")) {
      QuestChainInfo.Stage info = QuestChainInfo.stageFor(first);
      assertNotNull(info);
      assertEquals(1, info.stageNumber());
      assertEquals(8, info.totalStages());
      assertEquals(List.of("fading_veil_reckoning"), info.prerequisiteQuestIds());
    }
    for (var route : List.of(List.of("moonwake_missing", "pale_cantor"),
        List.of("emberglass_oath", "cinder_marshal"))) {
      QuestChainInfo.Stage lieutenant = QuestChainInfo.stageFor(route.get(1));
      assertEquals(2, lieutenant.stageNumber());
      assertEquals(List.of(route.get(0)), lieutenant.prerequisiteQuestIds());
    }
    QuestChainInfo.Stage convergence = QuestChainInfo.stageFor("ashbound_exiles");
    assertEquals(3, convergence.stageNumber());
    assertEquals(List.of("pale_cantor", "cinder_marshal"), convergence.prerequisiteQuestIds());
    assertTrue(convergence.anyPrerequisite());
    List<QuestDef> shared = HollowDawnCampaign.threnody();
    for (int i = 1; i < shared.size(); i++) {
      QuestChainInfo.Stage info = QuestChainInfo.stageFor(shared.get(i).getId());
      assertEquals(i + 3, info.stageNumber());
      assertEquals(8, info.totalStages());
      assertEquals(List.of(shared.get(i - 1).getId()), info.prerequisiteQuestIds());
    }
  }

  @Test
  void avalonQuestRewardsDoNotSkipTheLevelTwoHundredToThreeHundredCurve() {
    long xpNeeded = XpCurve.loadDefault().getTotalXp(300)
        - XpCurve.loadDefault().getTotalXp(200);
    long openingXp = AvalonWildsVigil.definition().getRewardXp()
        + FadingVeilReckoning.definition().getRewardXp();
    long witnessXp = openingXp + HollowDawnCampaign.avalon().subList(0, 2)
        .stream().mapToLong(QuestDef::getRewardXp).sum();
    long warderXp = openingXp + HollowDawnCampaign.avalon().subList(2, 4)
        .stream().mapToLong(QuestDef::getRewardXp).sum();
    assertEquals(175_000_000L, witnessXp);
    assertEquals(witnessXp, warderXp);
    assertTrue(xpNeeded > 1_000_000_000L);
    assertTrue(witnessXp < xpNeeded / 4, "quest turn-ins must leave a meaningful hunting budget");
  }

  private static boolean has(Player player, String name) {
    return NamedLocations.forPlayer(player).stream()
        .anyMatch(location -> name.equals(location.displayName()));
  }
}
