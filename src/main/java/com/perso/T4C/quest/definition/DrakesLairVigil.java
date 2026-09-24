package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

public final class DrakesLairVigil {
  private DrakesLairVigil() {}

  // rewardGold/rewardXp are capped well under Integer.MAX_VALUE rather than set to this zone's
  // literal curve-scaled value (QuestDef.rewardXp is `int`, same limitation flagged on
  // ArchDrake.definition() for MonsterDef.xpOnDeath) — still the single biggest quest reward in
  // the game, not a curve-accurate figure at level 1000.
  public static QuestDef definition() {
    return new QuestDef(
        "drakes_lair_vigil",
        "${quest.drakes_lair_vigil.title}",
        "OutriderKaelis",
        "Kraanian Dragonguard",
        15,
        0,
        2850,
        2780,
        180,
        1200000,
        2000000000,
        "${quest.drakes_lair_vigil.offer}",
        "${quest.drakes_lair_vigil.completion}",
        "${quest.drakes_lair_vigil.completed}",
        null,
        "archdrakes_molten_heart",
        1,
        "drakes_lair");
  }
}
