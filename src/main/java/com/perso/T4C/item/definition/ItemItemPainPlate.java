package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemPainPlate {
  private ItemItemPainPlate() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.pain_plate",
        "${item.pain_plate}",
        BodyPart.BODY,
        "PupPlateBody",
        null,
        null,
        "64kInvPlateArmorSleeves",
        0L,
        0L,
        50.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40978,
        2,
        264,
        null,
        "0",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10097, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(574, 3, "10", 0, 0),
            new ItemDefinition.ItemBoost(575, 2, "10", 0, 0),
            new ItemDefinition.ItemBoost(576, 1, "10", 0, 0),
            new ItemDefinition.ItemBoost(577, 4, "10", 0, 0),
            new ItemDefinition.ItemBoost(578, 6, "10", 0, 0)),
        List.of(),
        false);
  }
}
