package com.perso.T4C.online;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.online.model.CharacterProgress;

/** Builds the online status line from the server-owned progress; never retypes a rule or number. */
final class OnlineHudText {
  private OnlineHudText() {}

  /** HP, mana (a character without mana shows 0 / 0), level, total XP and gold on one line. */
  static String progressLine(CharacterProgress progress, long experience, long gold) {
    var vitals = progress.vitals();
    return I18n.message(
        "online.client.progress",
        vitals.hp(), vitals.maxHp(), vitals.mana(), vitals.maxMana(),
        progress.advancement().level(), experience, gold);
  }
}
