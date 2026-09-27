package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

public final class RenewedWards {
  private RenewedWards() {}

  public static QuestDef definition() {
    return new QuestDef(
        "renewed_wards",
        "${quest.renewed_wards.title}",
        "WardenAelric",
        "Skeleton",
        12,
        0,
        2785,
        1095,
        50,
        500,
        400,
        "${quest.renewed_wards.offer}",
        "${quest.renewed_wards.completion}",
        "${quest.renewed_wards.completed}",
        null,
        null,
        0,
        null,
        null,
        0,
        null,
        "${spell.renew_armor}");
  }
}
