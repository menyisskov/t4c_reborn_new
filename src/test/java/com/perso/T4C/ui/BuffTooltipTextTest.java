package com.perso.T4C.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class BuffTooltipTextTest {
  @Test
  void tooltipKeepsTheSpellNameWithAColonDescriptionAndCountsDown() {
    Player player = new Player();
    player.applyBuff("${spell.bless}", "Effect: protection", null, 90, false);
    Player.ActiveBuff buff = player.getActiveBuffs().getFirst();
    long start = buff.getExpiresAtMillis() - 90_000;
    String title =
        I18n.key("spell.bless")
            + "\nEffect: protection\n"
            + I18n.key("tooltip.time_remaining")
            + ": ";
    assertEquals(title + "1m 30s", BuffTooltipText.build(buff, start));
    assertEquals(title + "1m 29s", BuffTooltipText.build(buff, start + 1_000));
    assertEquals(title + "0s", BuffTooltipText.build(buff, buff.getExpiresAtMillis() + 1));
  }

  @Test
  void permanentEffectShowsAnUnlimitedDuration() {
    Player player = new Player();
    player.applyBuff("${spell.bless}", "", null, null, true);
    String text = BuffTooltipText.build(player.getActiveBuffs().getFirst(), 1000);
    assertTrue(text.startsWith(I18n.key("spell.bless") + "\n"));
    assertTrue(text.endsWith(I18n.key("ui.journal.infinite")));
  }
}
