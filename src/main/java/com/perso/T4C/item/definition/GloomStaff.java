package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GloomStaff {
  private GloomStaff() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gloom_staff",
        "${item.gloom_staff}",
        BodyPart.WEAPON,
        "PupGemStaff",
        null,
        null,
        "64kInvGemStaff",
        5741L,
        10L,
        0.0d,
        0L,
        0L,
        0L,
        20L,
        0L,
        83L,
        35L,
        1.0d,
        false,
        false,
        false,
        41396,
        1,
        295,
        "1d21+34",
        "if(1125-self.agi/250*1125/2<600?600:1125-self.agi/250*1125/2)+1d563",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10416, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(820, 24, "10", 0, 0),
            new ItemDefinition.ItemBoost(821, 23, "-10", 0, 0),
            new ItemDefinition.ItemBoost(827, 22, "10", 0, 0),
            new ItemDefinition.ItemBoost(828, 1, "15", 0, 0)),
        List.of(),
        false);
  }
}
