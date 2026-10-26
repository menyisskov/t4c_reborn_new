package com.perso.T4C.ui;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;

/** The active spell's name is always first, followed by its current remaining time. */
final class BuffTooltipText {
  private BuffTooltipText() {}

  static String build(Player.ActiveBuff buff, long nowMillis) {
    String name = I18n.resolve(buff.getSpellName());
    String description = I18n.resolve(buff.getDescription());
    StringBuilder text = new StringBuilder(name == null ? "" : name);
    if (description != null && !description.isBlank() && !description.equals(name)) {
      text.append('\n').append(description);
    }
    return text.append('\n')
        .append(I18n.key("tooltip.time_remaining"))
        .append(": ")
        .append(remaining(buff.getExpiresAtMillis(), nowMillis))
        .toString();
  }

  static String remaining(long expiresAtMillis, long nowMillis) {
    if (expiresAtMillis == Long.MAX_VALUE) return I18n.key("ui.journal.infinite");
    long millis = Math.max(0, expiresAtMillis - nowMillis);
    long seconds = millis / 1000 + (millis % 1000 == 0 ? 0 : 1);
    long minutes = seconds / 60;
    return minutes > 0 ? minutes + "m " + seconds % 60 + "s" : seconds + "s";
  }
}
