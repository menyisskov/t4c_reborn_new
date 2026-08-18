package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SeraphPlate {
  private SeraphPlate() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.seraph_plate",
        "${item.seraph_plate}",
        BodyPart.BODY,
        "PupPlateBody",
        null,
        null,
        "64kInvPlateArmorSleeves",
        0L,
        5L,
        12.0d,
        20L,
        100L,
        0L,
        30L,
        30L,
        30L,
        30L,
        0.0d,
        false,
        false,
        false,
        41882,
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
        List.of(new ItemDefinition.ItemSpell(10803, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
