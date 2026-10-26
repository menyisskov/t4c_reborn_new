package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.monster.Carman;
import com.perso.T4C.monster.HarvesterOfLife;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock("NpcScriptRuntime.summonCallback")
class HarvesterOfLifeQuestTest {
  @AfterEach
  void clearSummonCallback() {
    NpcScriptRuntime.setSummonCallback(null);
  }

  @Test
  void threeSoulstonesOpenPortalAndCrystalTurnsInOnce() throws Exception {
    Player player = new Player();
    player.setLevel(50);
    player.setStrength(400);
    HarvesterPortal portal = new HarvesterPortal(new NpcContext(null));
    Filandrius filandrius = new Filandrius(new NpcContext(null));
    List<String> summoned = new ArrayList<>();
    AtomicBoolean summonAvailable = new AtomicBoolean(false);
    NpcScriptRuntime.setSummonCallback(
        (monster, x, y, z) -> {
          if (player.getCoordinates().getZ() != z || !summonAvailable.get()) return false;
          summoned.add(monster);
          return true;
        });

    portal.javaBehavior().onConversationStart(new NpcBehaviorContext(portal, player));
    assertTrue(summoned.isEmpty());
    for (String stone : List.of("hel_soulstone", "jormungand_soulstone", "fenrir_soulstone"))
      InventoryService.add(player, stone);

    portal.javaBehavior().onConversationStart(new NpcBehaviorContext(portal, player));
    assertTrue(summoned.isEmpty());
    assertEquals(0, player.getCoordinates().getZ());
    assertEquals(1, InventoryService.count(player, "hel_soulstone"));

    summonAvailable.set(true);
    portal.javaBehavior().onConversationStart(new NpcBehaviorContext(portal, player));
    assertEquals(List.of("HARVESTEROFLIFE"), summoned);
    assertEquals(1, player.getCoordinates().getZ());
    assertEquals(0, InventoryService.count(player, "hel_soulstone"));
    assertEquals(0, InventoryService.count(player, "jormungand_soulstone"));
    assertEquals(0, InventoryService.count(player, "fenrir_soulstone"));

    HarvesterOfLife boss = new HarvesterOfLife(HarvesterOfLife.definition(), 0f, 0f);
    assertEquals(
        1.0f,
        boss.getDefinition().getLoot().stream()
            .filter(drop -> "crystal_of_providence".equals(drop.getItem()))
            .findFirst()
            .orElseThrow()
            .getChance());
    boss.onDeath(player);
    InventoryService.add(player, "crystal_of_providence");
    filandrius.javaBehavior().onKeyword(new NpcBehaviorContext(filandrius, player), "Burden");
    assertEquals(1, InventoryService.count(player, "bracer_of_the_immortal"));
    assertEquals(1, InventoryService.count(player, "gem_of_the_immortals"));
    assertEquals(0, InventoryService.count(player, "crystal_of_providence"));

    filandrius.javaBehavior().onKeyword(new NpcBehaviorContext(filandrius, player), "Burden");
    assertEquals(1, InventoryService.count(player, "bracer_of_the_immortal"));
  }

  @Test
  void defeatingHelUnlocksTheWindhowlSentryReward() throws Exception {
    Player player = new Player();
    player.setLevel(50);
    player.setStrength(400);
    Carman hel = new Carman(Carman.definition(), 0f, 0f);
    WindhowlSentry sentry = new WindhowlSentry(new NpcContext(null));

    hel.onDeath(player);
    assertEquals(1, InventoryService.count(player, "hel_soulstone"));
    assertEquals(1, player.getQuestFlag("ADDON_USER_HAS_KILLED_CARMAN"));
    sentry.javaBehavior().onKeyword(new NpcBehaviorContext(sentry, player), "Reward");
    assertEquals(1, player.getQuestFlag("ADDON_USER_REWARDED_FOR_KILLING_CARMAN"));
    assertEquals(25_000, player.getGold());
  }
}
