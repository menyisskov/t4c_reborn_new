package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// The special access quest that unlocks Avalon itself - see npc/HarbormasterRangor.java. Every
// other NPC tied to Avalon (ElderOphira, WayfarerBryndis, ArchmageThalindra, SisterIlyndra,
// QuartermasterElenna) is stationed inside Avalon Sanctuary, and the only existing way in
// (item.scroll_of_avalon) is sold by WayfarerBryndis herself - so without this quest a fresh
// character could never reach Avalon at all. Area (1550,1300, radius 110, worldZ 0) covers every
// TidewornReaver @Spawn and CoastwardenIthrak's own spawn point.
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
        1550,
        1300,
        110,
        400000,
        200000000,
        "${quest.passage_to_avalon.offer}",
        "${quest.passage_to_avalon.completion}",
        "${quest.passage_to_avalon.completed}",
        null,
        "tideworn_avalon_chart",
        1,
        "avalon_sanctuary");
  }
}
