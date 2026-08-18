package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

public final class LighthavenSamaritanRats {
  private LighthavenSamaritanRats() {}

  public static QuestDef definition() {
    return new QuestDef(
        "lighthaven_samaritan_rats",
        "${quest.lighthaven_samaritan_rats.title}",
        "LighthavenSamaritan",
        "Brown Rat",
        15,
        1,
        304,
        383,
        120,
        0,
        2500,
        "${quest.lighthaven_samaritan_rats.offer}",
        "${quest.lighthaven_samaritan_rats.completion}",
        "${quest.lighthaven_samaritan_rats.completed}",
        "__NEWBIE_QUEST");
  }
}
