package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// Avalon Wilds (center 1265,1400, radius 110, worldZ 0) — every Fey Warden @Spawn point in
// monster/FeyWarden.java sits within this circle, so recordKill's geofence always registers
// progress. Moonlit Stalker was deliberately not used here: several of its spawns sit near
// The Fading Veil, outside this quest's area, which would make some of its kills silently fail
// to count.
public final class AvalonWildsVigil {
  private AvalonWildsVigil() {}

  public static QuestDef definition() {
    return new QuestDef(
        "avalon_wilds_vigil",
        "${quest.avalon_wilds_vigil.title}",
        "ElderOphira",
        "Fey Warden",
        20,
        0,
        1265,
        1400,
        110,
        800000,
        400000000,
        "${quest.avalon_wilds_vigil.offer}",
        "${quest.avalon_wilds_vigil.completion}",
        "${quest.avalon_wilds_vigil.completed}",
        null,
        "caradocs_sundered_blade",
        1,
        "avalon_wilds",
        null,
        0,
        "${quest.avalon_wilds_vigil.walkthrough}");
  }
}
