package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

public final class HollowMarchWights {
  private HollowMarchWights() {}

  public static QuestDef definition() {
    return new QuestDef(
        "hollow_march_wights",
        "${quest.hollow_march_wights.title}",
        "WardenCael",
        "Barrow Wight",
        20,
        0,
        1600,
        1800,
        120,
        12000,
        900000,
        "${quest.hollow_march_wights.offer}",
        "${quest.hollow_march_wights.completion}",
        "${quest.hollow_march_wights.completed}",
        null);
  }
}
