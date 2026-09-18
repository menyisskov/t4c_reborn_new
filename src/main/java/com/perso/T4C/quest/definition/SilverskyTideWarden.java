package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

public final class SilverskyTideWarden {
  private SilverskyTideWarden() {}

  public static QuestDef definition() {
    return new QuestDef(
        "silversky_tide_warden",
        "${quest.silversky_tide_warden.title}",
        "TideWardenBryn",
        "Drowned Acolyte",
        20,
        0,
        1750,
        2300,
        140,
        1500,
        4000,
        "${quest.silversky_tide_warden.offer}",
        "${quest.silversky_tide_warden.completion}",
        "${quest.silversky_tide_warden.completed}",
        null);
  }
}
