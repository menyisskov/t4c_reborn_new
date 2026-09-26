package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

public final class BastionWardenSiege {
  private BastionWardenSiege() {}

  public static QuestDef definition() {
    return new QuestDef(
        "bastion_warden_siege",
        "${quest.bastion_warden_siege.title}",
        "GrandmasterVoss",
        "Bastion Warden",
        20,
        0,
        2650,
        2880,
        170,
        500000,
        150000000,
        "${quest.bastion_warden_siege.offer}",
        "${quest.bastion_warden_siege.completion}",
        "${quest.bastion_warden_siege.completed}",
        null,
        "heartfire_of_the_greater_drake",
        1,
        "greater_drakes_bastion",
        null,
        0,
        "${quest.bastion_warden_siege.walkthrough}");
  }
}
