package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.MercenaryA;
import com.perso.T4C.monster.MercenaryB;
import com.perso.T4C.monster.MercenaryLeader;
import com.perso.T4C.monster.MercenaryLieutenantA;
import com.perso.T4C.monster.MercenaryLieutenantB;
import com.perso.T4C.monster.MercenaryLieutenantC;
import com.perso.T4C.monster.r280MOBMERCENARYC;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.MercenaryCampQuest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MercenaryCampQuestTest {
  @Test
  void vincentRequiresLevelEight() throws Exception {
    Player player = new Player();
    player.setLevel(7);
    VincentSwiftblade vincent = new VincentSwiftblade(new NpcContext(null));

    vincent.javaBehavior().onYesNo(new NpcBehaviorContext(vincent, player), "mission", true);

    assertEquals(0, player.getQuestFlag(MercenaryCampQuest.STAGE));
  }

  @Test
  void threeCampsLeadToRepeatableVincentReward() throws Exception {
    List<String> summons = new ArrayList<>();
    NpcScriptRuntime.setSummonCallback(
        (monster, x, y, z) -> {
          summons.add(monster);
          return true;
        });
    try {
      Player player = new Player();
      player.setLevel(8);
      VincentSwiftblade vincent = new VincentSwiftblade(new NpcContext(null));
      NpcBehaviorContext context = new NpcBehaviorContext(vincent, player);
      vincent.javaBehavior().onYesNo(context, "mission", true);
      vincent.javaBehavior().onKeyword(context, "DETAILS");
      assertEquals(2, player.getQuestFlag(MercenaryCampQuest.STAGE));

      MercenaryA first = new MercenaryA(MercenaryA.definition(), 0, 0);
      for (int i = 0; i < 15; i++) first.onDeath(player);
      new MercenaryLieutenantA(MercenaryLieutenantA.definition(), 0, 0).onDeath(player);
      assertEquals(3, player.getQuestFlag(MercenaryCampQuest.STAGE));

      MercenaryB second = new MercenaryB(MercenaryB.definition(), 0, 0);
      for (int i = 0; i < 15; i++) second.onDeath(player);
      new MercenaryLieutenantB(MercenaryLieutenantB.definition(), 0, 0).onDeath(player);
      assertEquals(4, player.getQuestFlag(MercenaryCampQuest.STAGE));

      r280MOBMERCENARYC third =
          new r280MOBMERCENARYC(r280MOBMERCENARYC.definition(), 0, 0);
      for (int i = 0; i < 15; i++) third.onDeath(player);
      new MercenaryLieutenantC(MercenaryLieutenantC.definition(), 0, 0).onDeath(player);
      assertEquals(
          List.of(
              "MOBMERCENARYLIEUTENANTA",
              "MOBMERCENARYLIEUTENANTB",
              "MOBMERCENARYLIEUTENANTC",
              "MOBMERCENARYLEADER"),
          summons);

      new MercenaryLeader(MercenaryLeader.definition(), 0, 0).onDeath(player);
      assertEquals(1, player.getQuestFlag(MercenaryCampQuest.LEADER_DEFEATED));
      long goldBefore = player.getGold();
      long xpBefore = player.getCurrentXp();
      vincent.javaBehavior().onKeyword(context, "REWARD");
      assertEquals(goldBefore + 500, player.getGold());
      assertTrue(player.getCurrentXp() >= xpBefore + 750 * 8);
      assertEquals(0, player.getQuestFlag(MercenaryCampQuest.STAGE));
    } finally {
      NpcScriptRuntime.setSummonCallback(null);
    }
  }
}
