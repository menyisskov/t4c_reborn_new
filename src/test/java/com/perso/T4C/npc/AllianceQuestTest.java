package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.PIG;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.AllianceQuest;
import org.junit.jupiter.api.Test;

class AllianceQuestTest {
  @Test
  void walkthroughKeywordsAdvanceOnlyInOrderAndRewardOnce() throws Exception {
    Player player = new Player();
    player.setLevel(28);
    BaldricSilverknife baldric = new BaldricSilverknife(new NpcContext(null));
    Doremas doremas = new Doremas(new NpcContext(null));
    Grimish grimish = new Grimish(new NpcContext(null));
    PIG pig = new PIG(PIG.definition(), 0f, 0f);
    NpcBehaviorContext b = new NpcBehaviorContext(baldric, player);
    NpcBehaviorContext d = new NpcBehaviorContext(doremas, player);
    NpcBehaviorContext g = new NpcBehaviorContext(grimish, player);

    baldric.javaBehavior().onKeyword(b, "Alliance is renewed");
    assertEquals(AllianceQuest.NOT_STARTED, AllianceQuest.stage(player));
    pig.onDeath(player);
    baldric.javaBehavior().onKeyword(b, "Pig");
    assertEquals(AllianceQuest.KILL_PIGS, AllianceQuest.stage(player));
    for (int i = 0; i < 10; i++) pig.onDeath(player);
    baldric.javaBehavior().onKeyword(b, "Pig");
    baldric.javaBehavior().onKeyword(b, "Assignment");
    assertEquals(AllianceQuest.SEE_DOREMAS, AllianceQuest.stage(player));

    doremas.javaBehavior().onKeyword(d, "Grimish");
    grimish.javaBehavior().onKeyword(g, "Doremas");
    doremas.javaBehavior().onKeyword(d, "Grimish");
    grimish.javaBehavior().onKeyword(g, "Doremas");
    grimish.javaBehavior().onKeyword(g, "Nothing");
    doremas.javaBehavior().onKeyword(d, "Grimish");
    assertEquals(AllianceQuest.TELL_BALDRIC_CODE, AllianceQuest.stage(player));
    baldric.javaBehavior().onKeyword(b, "Alliance is renewed");
    assertEquals(AllianceQuest.COMPLETE, AllianceQuest.stage(player));
    long rewardXp = player.getCurrentXp();
    assertTrue(rewardXp > 0);
    baldric.javaBehavior().onKeyword(b, "Alliance is renewed");
    assertEquals(rewardXp, player.getCurrentXp());
  }
}
