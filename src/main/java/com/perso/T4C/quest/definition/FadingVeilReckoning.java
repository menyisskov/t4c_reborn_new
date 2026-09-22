package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// The Fading Veil (center 1420,1560, radius 130, worldZ 0) — every Veilbound Wraith @Spawn
// point in monster/VeilboundWraith.java sits within this circle, so recordKill's geofence
// always registers progress. Sundered Sentinel would work equally well area-wise; Veilbound
// Wraith was picked as the more direct embodiment of "the Veil" itself for this quest's theme.
public final class FadingVeilReckoning {
  private FadingVeilReckoning() {}

  public static QuestDef definition() {
    return new QuestDef(
        "fading_veil_reckoning",
        "${quest.fading_veil_reckoning.title}",
        "ElderOphira",
        "Veilbound Wraith",
        15,
        0,
        1420,
        1560,
        130,
        4000000,
        1700000000,
        "${quest.fading_veil_reckoning.offer}",
        "${quest.fading_veil_reckoning.completion}",
        "${quest.fading_veil_reckoning.completed}",
        null,
        "ysoldes_veiled_circlet",
        1,
        "fading_veil");
  }
}
