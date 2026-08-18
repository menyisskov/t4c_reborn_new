package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCrusher {
  private ItemItemCrusher() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.crusher",
        "${item.crusher}",
        BodyPart.WEAPON,
        "PupMace",
        null,
        null,
        "64kInvMace",
        0L,
        9L,
        4.0d,
        0L,
        0L,
        0L,
        140L,
        0L,
        0L,
        30L,
        1.0d,
        false,
        false,
        false,
        40847,
        1,
        119,
        "if(target.r_dark=5025?1d46+82:1d40+71)",
        "if(862-self.agi/250*862/2<600?600:862-self.agi/250*862/2)+1d431",
        0,
        0,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(),
        List.of(new ItemDefinition.ItemBoost(518, 8, "self.true_attack*30/100", 0, 0)),
        List.of(),
        false);
  }
}
