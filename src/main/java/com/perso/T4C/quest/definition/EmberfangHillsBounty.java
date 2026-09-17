package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

public final class EmberfangHillsBounty {
  private EmberfangHillsBounty() {}

  public static QuestDef definition() {
    return new QuestDef(
        "emberfang_hills_bounty",
        "${quest.emberfang_hills_bounty.title}",
        "RurikCinderwatch",
        "Ashfang Stalker",
        20,
        0,
        1900,
        1600,
        140,
        2500,
        7000,
        "${quest.emberfang_hills_bounty.offer}",
        "${quest.emberfang_hills_bounty.completion}",
        "${quest.emberfang_hills_bounty.completed}",
        null);
  }
}
