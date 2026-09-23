package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

public final class AerieWyrmlingCull {
  private AerieWyrmlingCull() {}

  public static QuestDef definition() {
    return new QuestDef(
        "aerie_wyrmling_cull",
        "${quest.aerie_wyrmling_cull.title}",
        "SkywatchIlvara",
        "Kraanian Wyrmling",
        20,
        0,
        2350,
        2900,
        160,
        25000,
        2000000,
        "${quest.aerie_wyrmling_cull.offer}",
        "${quest.aerie_wyrmling_cull.completion}",
        "${quest.aerie_wyrmling_cull.completed}",
        null,
        "aeries_drakeheart_signet",
        1,
        "lesser_drakes_aerie");
  }
}
