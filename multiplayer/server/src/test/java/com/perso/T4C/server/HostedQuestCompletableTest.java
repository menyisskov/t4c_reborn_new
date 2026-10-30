package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.quest.QuestRules;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Gate for adding a quest to the online catalog: its giver must be a hosted, talkable NPC and the
 * hosted ground must hold enough of its target monsters inside the objective circle. Otherwise
 * the quest could be accepted but never finished, which the single-player game never allows.
 */
class HostedQuestCompletableTest {
  private static final Path WORLD=Path.of("../../assets/maps/worldmap/worldmap.colbin");

  @Test void everyCatalogedQuestHasAHostedGiverAndEnoughHostedTargetsToFinish() throws Exception {
    var plan=HostedRegionPlan.startingSlice(WORLD);
    var npcs=NpcInteractionCatalog.loadBundled();
    var quests=QuestCatalog.bundled();
    assertFalse(quests.definitions().isEmpty());
    try(var hosted=new HostedWorlds(plan,null,ItemCatalog.loadBundled(),MonsterCatalog.loadBundled())) {
      for(var quest:quests.definitions()) {
        var giver=npcs.require(quest.getGiverNpc());
        assertNotNull(hosted.require(giver.world(),giver.x()+0.5,giver.y()+0.5),"giver region "+quest.getId());
        long targets=0;
        for(var region:plan.regions()) {
          var spec=region.spec();
          var world=hosted.require(spec.world(),spec.spawnX()+0.5,spec.spawnY()+0.5).world;
          targets+=world.creatureSpawns().stream()
              .filter(s->s.definition().getName().equalsIgnoreCase(quest.getTargetMonster()))
              .filter(s->QuestRules.inObjectiveArea(quest,s.spawn().world(),s.spawn().x(),s.spawn().y())).count();
        }
        assertTrue(targets>=quest.getRequiredKills(),
            quest.getId()+" has "+targets+" hosted targets for "+quest.getRequiredKills()+" kills");
        if (quest.getId().equals("lighthaven_samaritan_rats")) {
          assertEquals("LighthavenSamaritan", giver.id());
          assertEquals(1, quest.getTargetWorldZ());
          assertEquals(15, quest.getRequiredKills());
          assertTrue(targets >= 15, "the hosted world-1 objective area needs at least 15 Brown Rats");
        }
      }
    }
  }
}
