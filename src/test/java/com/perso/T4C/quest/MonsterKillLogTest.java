package com.perso.T4C.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.player.Player;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** T4C-0062: the Monster Kill Log tallied by {@link QuestService#recordKill} independently of
 * any quest, surfaced by {@link QuestService#killLog(Player)} for the Monster Kill Log screen. */
class MonsterKillLogTest {
  private final QuestService service =
      new QuestService(XpCurve.loadDefault(), null, null, List::of);

  @Test
  void emptyForANullOrFreshPlayer() {
    assertTrue(QuestService.killLog(null).isEmpty());
    assertTrue(QuestService.killLog(new Player()).isEmpty());
  }

  @Test
  void talliesEveryKillRegardlessOfAnyQuestMatch() {
    Player player = new Player();
    service.recordKill(player, "Goblin", 0, 2760, 1010);
    service.recordKill(player, "Goblin", 0, 0, 0);
    service.recordKill(player, "Brown Rat", 1, 304, 383);

    Map<String, Integer> log = QuestService.killLog(player);
    assertEquals(2, log.get("Goblin"));
    assertEquals(1, log.get("Brown Rat"));
  }

  @Test
  void aliasedMonsterNamesShareOneCanonicalTally() {
    Player player = new Player();
    service.recordKill(player, "Rat", 1, 304, 383);
    service.recordKill(player, "Brown Rat", 1, 304, 383);

    Map<String, Integer> log = QuestService.killLog(player);
    assertEquals(1, log.size(), "both spellings must fold into one canonical monster entry");
    assertEquals(2, log.get("Brown Rat"));
  }

  @Test
  void survivesASaveRoundTrip() {
    Player player = new Player();
    service.recordKill(player, "Goblin", 0, 0, 0);

    PlayerStateDto saved = PlayerStateMapper.fromPlayer(player);
    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(saved, restored);

    assertEquals(1, QuestService.killLog(restored).get("Goblin"));
  }
}
