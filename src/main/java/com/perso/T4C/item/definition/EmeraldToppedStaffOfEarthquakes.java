package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class EmeraldToppedStaffOfEarthquakes {
  private EmeraldToppedStaffOfEarthquakes() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.emerald_topped_staff_of_earthquakes",
        "${item.emerald_topped_staff_of_earthquakes}",
        BodyPart.WEAPON,
        "PupGemStaff",
        null,
        null,
        "64kInvGemStaff",
        14266L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        25L,
        0L,
        178L,
        15L,
        1.0d,
        false,
        false,
        false,
        40765,
        1,
        295,
        "1d24+43",
        "if(1687-self.agi/250*1687/2<1687/2?1687/2:1687-self.agi/250*1687/2)+1d844",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10223, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
