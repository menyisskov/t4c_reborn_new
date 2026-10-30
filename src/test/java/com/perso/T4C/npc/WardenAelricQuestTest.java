package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.quest.QuestService;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;

class WardenAelricQuestTest {
  @Test
  void retainsWelcomeBeforeRenewedWardsIsCompleted() throws Exception {
    WardenAelric npc = newNpc();

    npc.onInteractStart(new Player());

    assertEquals(I18n.resolve("${npc.welcome.wardenaelric}"), dialogText(npc));
  }

  @Test
  void completedRenewedWardsUsesTheCompletedGreetingWithoutChangingQuestOrRewardState()
      throws Exception {
    var quest = QuestRegistry.findById("renewed_wards");
    assertNotNull(quest);
    WardenAelric npc = newNpc();
    Player player = new Player();
    player.setQuestFlag(QuestService.statusFlag(quest), QuestService.STATUS_COMPLETED);
    int gold = player.getGold();
    long xp = player.getCurrentXp();
    List<String> inventory = List.copyOf(player.getInventory());

    npc.onInteractStart(player);

    assertEquals(I18n.resolve("${quest.renewed_wards.completed}"), dialogText(npc));
    assertEquals(QuestService.STATUS_COMPLETED, QuestService.statusFor(player, quest));
    assertEquals(gold, player.getGold());
    assertEquals(xp, player.getCurrentXp());
    assertEquals(inventory, player.getInventory());
  }

  private static WardenAelric newNpc() throws Exception {
    return new WardenAelric(new NpcContext(new QuestService(XpCurve.loadDefault(), null, null)));
  }

  @SuppressWarnings("unchecked")
  private static String dialogText(WardenAelric npc) throws Exception {
    Field field = npc.getClass().getSuperclass().getSuperclass().getDeclaredField("dialogLines");
    field.setAccessible(true);
    return String.join(" ", (List<String>) field.get(npc));
  }
}
