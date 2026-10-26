package com.perso.T4C.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.monster.r160Nightcreeper;
import com.perso.T4C.monster.r184Pilferer;
import com.perso.T4C.monster.r214SkeletalCentaur;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class StoneheimKillCountersTest {
  @Test
  void questTargetsAdvanceTheFlagsReadByTheirTurnInNpcs() throws Exception {
    Player player = new Player();
    r160Nightcreeper nightcreeper =
        new r160Nightcreeper(r160Nightcreeper.definition(), 0f, 0f);
    r184Pilferer pilferer = new r184Pilferer(r184Pilferer.definition(), 0f, 0f);
    r214SkeletalCentaur centaur =
        new r214SkeletalCentaur(r214SkeletalCentaur.definition(), 0f, 0f);

    nightcreeper.onDeath(player);
    pilferer.onDeath(player);
    centaur.onDeath(player);

    assertEquals(1, player.getQuestFlag("__FLAG_NIGHTCREEPERS_KILLED"));
    assertEquals(1, player.getQuestFlag("__FLAG_PILFERERS_KILLED"));
    assertEquals(1, player.getQuestFlag("__FLAG_SKELETAL_CENTAURS_KILLED"));
  }
}
