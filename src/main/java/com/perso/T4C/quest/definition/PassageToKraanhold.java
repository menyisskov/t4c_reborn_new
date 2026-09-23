package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// T4C-0024: the access quest that unlocks Kraanhold itself - see npc/DockmasterThessaly.java.
// Every NPC/monster tied to Kraanhold's five provinces is stationed ON Kraanhold, and the new
// continent was painted into open ocean with no existing road connecting it to anywhere else, so
// without this quest a fresh character could never reach it at all - the same dead-end Avalon had
// before T4C-0019's passage_to_avalon. Reuses the existing legacy "Toll Troll" (level 85, already
// spawned in 15 places on this stretch of coast) as its kill target rather than inventing a new
// monster, the same "is there already something here to use" check every other pass this session
// made first. Area (1975,2750, radius 170, worldZ 0) covers the full existing spawn cluster.
public final class PassageToKraanhold {
  private PassageToKraanhold() {}

  public static QuestDef definition() {
    return new QuestDef(
        "passage_to_kraanhold",
        "${quest.passage_to_kraanhold.title}",
        "DockmasterThessaly",
        "Toll Troll",
        12,
        0,
        1975,
        2750,
        170,
        4000,
        250000,
        "${quest.passage_to_kraanhold.offer}",
        "${quest.passage_to_kraanhold.completion}",
        "${quest.passage_to_kraanhold.completed}",
        null,
        null,
        0,
        "windhowl_marches");
  }
}
