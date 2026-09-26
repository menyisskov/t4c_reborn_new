package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// T4C-0035: a shortcut so a proven character doesn't have to re-trek to the Oracle's dungeon and
// its guardian gauntlet for every rebirth. Completing this quest permanently unlocks Anchorite
// Rowan's own rebirth rite (see npc/AnchoriteRowan.java) - a one-time unlock, not something to
// redo per rebirth. Deliberately independent of the Oracle's own "__FLAG_USER_HAS_DEFEATED_
// ASSISTANT" gate: this is an alternate path to prove yourself, not a duplicate of that one.
// Same Fey Warden/Avalon Wilds geofence as AvalonWildsVigil.java, just a shorter kill count and a
// minLevel floor (owner's call: locked to level 125, kills can still be gathered below it - see
// QuestService.meetsMinLevel()) since this is a level-125+ character's quality-of-life unlock,
// not an early-game quest.
public final class TheWakingRite {
  private TheWakingRite() {}

  public static QuestDef definition() {
    return new QuestDef(
        "the_waking_rite",
        "${quest.the_waking_rite.title}",
        "AnchoriteRowan",
        "Fey Warden",
        8,
        0,
        1265,
        1400,
        110,
        100000,
        5000000,
        "${quest.the_waking_rite.offer}",
        "${quest.the_waking_rite.completion}",
        "${quest.the_waking_rite.completed}",
        null,
        null,
        0,
        null,
        null,
        125,
        "${quest.the_waking_rite.walkthrough}");
  }
}
