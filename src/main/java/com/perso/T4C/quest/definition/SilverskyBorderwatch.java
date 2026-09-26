package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// T4C-0022: a standalone access quest for the Sunken Chancel, separate from the zone's own
// silversky_tide_warden questline (SilverskyTideWarden.java). Its target (Antelope) and area
// (1461,2407, radius 250) sit around Silversky's own outskirts, not inside the Chancel, so a
// player can earn the zone's fast-travel unlock (same unlockZoneId, see QuestService.
// hasUnlockedZone - any completed quest with a matching unlockZoneId counts) as a deliberate
// "before you go" checkpoint at the town, rather than only ever getting credit for it after
// already having fought their way into the zone.
public final class SilverskyBorderwatch {
  private SilverskyBorderwatch() {}

  public static QuestDef definition() {
    return new QuestDef(
        "silversky_borderwatch",
        "${quest.silversky_borderwatch.title}",
        "SentinelCorwin",
        "Antelope",
        15,
        0,
        1461,
        2407,
        250,
        800,
        2000,
        "${quest.silversky_borderwatch.offer}",
        "${quest.silversky_borderwatch.completion}",
        "${quest.silversky_borderwatch.completed}",
        null,
        null,
        0,
        "sunken_chancel",
        null,
        0,
        "${quest.silversky_borderwatch.walkthrough}");
  }
}
