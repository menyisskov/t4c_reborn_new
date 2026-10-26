package com.perso.T4C.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.r204RogueMage;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class AudienceToKingQuestTest {
  @Test
  void tenRogueMageKillsWithinThirtyMinutesGrantTheAudienceOnTurnIn() throws Exception {
    Player player = new Player();
    int start = AudienceToKingQuest.nowSeconds();
    AudienceToKingQuest.recordRogueMageKill(player, start);
    assertEquals(0, AudienceToKingQuest.kills(player));

    AudienceToKingQuest.accept(player, start);
    r204RogueMage rogueMage = new r204RogueMage(r204RogueMage.definition(), 0f, 0f);
    for (int i = 0; i < AudienceToKingQuest.REQUIRED_KILLS; i++) {
      rogueMage.onDeath(player);
    }
    assertTrue(AudienceToKingQuest.turnIn(player, AudienceToKingQuest.nowSeconds()));
    assertTrue(AudienceToKingQuest.isComplete(player));
  }

  @Test
  void expiredAttemptMustBeRestartedBeforeKillsCanCount() {
    Player player = new Player();
    int start = 1_800_000_000;
    AudienceToKingQuest.accept(player, start);
    int expired = start + AudienceToKingQuest.TIME_LIMIT_SECONDS + 1;

    AudienceToKingQuest.recordRogueMageKill(player, expired);
    assertEquals(0, AudienceToKingQuest.kills(player));
    assertFalse(AudienceToKingQuest.turnIn(player, expired));

    AudienceToKingQuest.accept(player, expired);
    AudienceToKingQuest.recordRogueMageKill(player, expired);
    assertEquals(1, AudienceToKingQuest.kills(player));
  }
}
