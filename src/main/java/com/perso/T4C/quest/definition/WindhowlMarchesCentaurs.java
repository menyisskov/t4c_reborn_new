package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

public final class WindhowlMarchesCentaurs {
  private WindhowlMarchesCentaurs() {}

  public static QuestDef definition() {
    return new QuestDef(
        "windhowl_marches_centaurs",
        "${quest.windhowl_marches_centaurs.title}",
        "MarshalTorrhen",
        "Centaur Warrior",
        20,
        0,
        1650,
        1550,
        120,
        6000,
        400000,
        "${quest.windhowl_marches_centaurs.offer}",
        "${quest.windhowl_marches_centaurs.completion}",
        "${quest.windhowl_marches_centaurs.completed}",
        null,
        "marchwardens_crown",
        1,
        "windhowl_marches");
  }
}
