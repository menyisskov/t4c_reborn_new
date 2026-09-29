package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// The permanent Witness Isles access quest begins on Stoneheim with Harbormaster Rangor.
// Historical quest and zone IDs stay stable for saved characters. The (420,730) objective
// encloses the Stoneheim reavers and Ithrak; the chart is guaranteed on Ithrak's defeat.
public final class PassageToAvalon {
  private PassageToAvalon() {}

  public static QuestDef definition() {
    return new QuestDef(
        "passage_to_avalon",
        "${quest.passage_to_avalon.title}",
        "HarbormasterRangor",
        "Tideworn Reaver",
        25,
        0,
        420,
        730,
        130,
        100000,
        12000000,
        "${quest.passage_to_avalon.offer}",
        "${quest.passage_to_avalon.completion}",
        "${quest.passage_to_avalon.completed}",
        null,
        "item.tideworn_avalon_chart",
        1,
        "avalon_sanctuary",
        null,
        200,
        "${quest.passage_to_avalon.walkthrough}");
  }
}
