package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// Protect the loyal fey from Moonlit Stalkers in the marked Avalon Wilds hunting region.
// Existing quest/status/kill flags are retained so previously earned progress is preserved.
public final class AvalonWildsVigil {
  private AvalonWildsVigil() {}

  public static QuestDef definition() {
    return new QuestDef(
        "avalon_wilds_vigil",
        "${quest.avalon_wilds_vigil.title}",
        "ElderOphira",
        "Moonlit Stalker",
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
        "item.caradocs_sundered_blade",
        1,
        "avalon_wilds",
        null,
        0,
        "${quest.avalon_wilds_vigil.walkthrough}");
  }
}
