package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.definition.HollowDawnCampaign;
import com.perso.T4C.spawn.SpawnRegistry;
import java.io.File;
import org.junit.jupiter.api.Test;

class CampaignWitnessNpcTest {
  @Test
  void eachWitnessRequiresTheFirstDeedAndUnlocksOnlyItsOwnLieutenant() throws Exception {
    for (int index : new int[] {0, 2}) {
      Player player = new Player();
      QuestService quests = new QuestService(XpCurve.loadDefault(), null, null);
      ChroniclerMaelin maelin = new ChroniclerMaelin(new NpcContext(quests));
      ScriptedNpc witness = index == 0 ? new MoonwakeWitnessIlyra(new NpcContext(null))
          : new EmberglassWarderSoren(new NpcContext(null));
      String flag = index == 0 ? HollowDawnCampaign.MOONWAKE_CLUE : HollowDawnCampaign.EMBERGLASS_CLUE;
      var first = HollowDawnCampaign.avalon().get(index);
      var lieutenant = HollowDawnCampaign.avalon().get(index + 1);
      say(witness, player, "clue");
      assertEquals(0, player.getQuestFlag(flag));
      player.setQuestFlag(QuestService.statusFlag(first), QuestService.STATUS_COMPLETED);
      say(maelin, player, "accept");
      assertEquals(QuestService.STATUS_NOT_STARTED, QuestService.statusFor(player, lieutenant));
      say(witness, player, "testimony");
      assertEquals(0, player.getQuestFlag(flag), "testimony must lead to the explicit clue");
      say(witness, player, "clue");
      assertEquals(1, player.getQuestFlag(flag));
      String otherFlag = index == 0 ? HollowDawnCampaign.EMBERGLASS_CLUE : HollowDawnCampaign.MOONWAKE_CLUE;
      assertEquals(0, player.getQuestFlag(otherFlag));
      // A fresh NPC instance uses the player's durable flag, not conversation-local state.
      maelin = new ChroniclerMaelin(new NpcContext(quests));
      say(maelin, player, "accept");
      assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, lieutenant));
    }
  }

  @Test
  void existingActiveAndCompletedLieutenantsDoNotAcquireANewClueRequirement() {
    for (int index : new int[] {1, 3}) {
      var lieutenant = HollowDawnCampaign.avalon().get(index);
      for (int state : new int[] {QuestService.STATUS_ACTIVE, QuestService.STATUS_COMPLETED}) {
        Player player = new Player();
        player.setQuestFlag(QuestService.statusFlag(lieutenant), state);
        assertNull(HollowDawnCampaign.missingClue(player, lieutenant));
      }
    }
  }

  @Test
  void witnessesAreRegisteredOnWalkableGroundBeyondMonsterAggroAndLeash() throws Exception {
    CollisionReader collision = new CollisionReader(new File("assets/maps/worldmap/worldmap.colbin"));
    try (MapReader map = new MapReader(new File("assets/maps/worldmap/worldmap.mapbin"))) {
      for (String id : new String[] {MoonwakeWitnessIlyra.ID, EmberglassWarderSoren.ID}) {
        assertNotNull(NpcFactoryRegistry.create(id, new NpcContext(null)));
        var point = SpawnRegistry.npcs().stream().filter(s -> id.equals(s.type())).findFirst().orElseThrow();
        assertFalse(collision.hasCollision(point.x(), point.y()));
        String ground = map.getGroundSpriteName(point.x(), point.y());
        assertNotNull(ground);
        assertFalse(ground.startsWith("Ground_Water") || ground.equals("Black Tile"));
        for (var monster : SpawnRegistry.monsters()) {
          if (monster.z() != point.z()) continue;
          double distance = Math.hypot(monster.x() - point.x(), monster.y() - point.y());
          assertTrue(distance > 45, id + " too close to " + monster.type());
        }
      }
    }
  }

  private static void say(ScriptedNpc npc, Player player, String keyword) {
    assertTrue(npc.publicBehavior().onKeyword(new NpcBehaviorContext(npc, player), keyword));
  }
}
