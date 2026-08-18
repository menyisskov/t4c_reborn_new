package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

public final class OrtanalasBridgeGoblins {
  private OrtanalasBridgeGoblins() {}

  public static QuestDef definition() {
    return new QuestDef(
        "ortanalas_bridge_goblins",
        "${quest.ortanalas_bridge_goblins.title}",
        "Ortanalas",
        "Goblin",
        15,
        0,
        2760,
        1010,
        100,
        1000,
        750,
        "${quest.ortanalas_bridge_goblins.offer}",
        "${quest.ortanalas_bridge_goblins.completion}",
        "${quest.ortanalas_bridge_goblins.completed}",
        null);
  }
}
