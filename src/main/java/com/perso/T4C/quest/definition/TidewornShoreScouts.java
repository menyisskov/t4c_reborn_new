package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// T4C-0032: first stage of the Passage to Avalon chain. Harbormaster Rangor won't send a
// stranger straight at Coastwarden Ithrak's whole warband, so this stage asks for a smaller,
// provable win against the same Tideworn Reavers first - same objective area as
// PassageToAvalon.java (stage two, the real assault and the actual zone unlock). See
// npc/HarbormasterRangor.java for how the two are chained through the same "avalon"/"passage"
// dialogue keyword: it checks this quest's completion before ever offering stage two.
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
        1550,
        1300,
        110,
        40000,
        20000000,
        "${quest.tideworn_shore_scouts.offer}",
        "${quest.tideworn_shore_scouts.completion}",
        "${quest.tideworn_shore_scouts.completed}",
        null,
        null,
        0,
        null,
        null,
        0,
        "${quest.tideworn_shore_scouts.walkthrough}");
  }
}
