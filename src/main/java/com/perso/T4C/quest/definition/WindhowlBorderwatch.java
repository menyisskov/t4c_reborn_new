package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// T4C-0022: a standalone access quest for Cinderreach Hills, separate from the zone's own
// emberfang_hills_bounty questline (EmberfangHillsBounty.java). Its target (Brigand) and area
// (1853,1168, radius 230) sit on the roads around Windhowl, not inside the Hills, so a player can
// earn the zone's fast-travel unlock (same unlockZoneId, see QuestService.hasUnlockedZone - any
// completed quest with a matching unlockZoneId counts) as a deliberate "before you go" checkpoint
// at the town, rather than only ever getting credit for it after already having fought their way
// into the zone.
public final class WindhowlBorderwatch {
  private WindhowlBorderwatch() {}

  public static QuestDef definition() {
    return new QuestDef(
        "windhowl_borderwatch",
        "${quest.windhowl_borderwatch.title}",
        "OutriderHalvard",
        "Brigand",
        15,
        0,
        1853,
        1168,
        230,
        1200,
        3000,
        "${quest.windhowl_borderwatch.offer}",
        "${quest.windhowl_borderwatch.completion}",
        "${quest.windhowl_borderwatch.completed}",
        null,
        null,
        0,
        "cinderreach_hills");
  }
}
