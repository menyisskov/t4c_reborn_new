package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// Stoneheim's first passage stage: the reaver scouts east of Stonecrest, followed by Ithrak's
// warband. The stable quest ID preserves completed and active character saves.
public final class TidewornShoreScouts {
  private TidewornShoreScouts() {}

  public static QuestDef definition() {
    return new QuestDef(
        "tideworn_shore_scouts",
        "${quest.tideworn_shore_scouts.title}",
        "HarbormasterRangor",
        "Tideworn Reaver",
        8,
        0,
        420,
        730,
        130,
        40000,
        3000000,
        "${quest.tideworn_shore_scouts.offer}",
        "${quest.tideworn_shore_scouts.completion}",
        "${quest.tideworn_shore_scouts.completed}",
        null,
        null,
        0,
        null,
        null,
        200,
        "${quest.tideworn_shore_scouts.walkthrough}");
  }
}
