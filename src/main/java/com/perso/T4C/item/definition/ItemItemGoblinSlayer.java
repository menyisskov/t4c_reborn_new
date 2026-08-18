package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGoblinSlayer {
  private ItemItemGoblinSlayer() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.goblin_slayer",
        "${item.goblin_slayer}",
        BodyPart.WEAPON,
        "PupNormalSword",
        null,
        null,
        "64kInvNormalSword",
        6962L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        97L,
        0L,
        20L,
        23L,
        1.0d,
        false,
        false,
        false,
        40286,
        1,
        2,
        "if(target.r_light=5025?1d19+41:1d19+25)",
        "if(787-self.agi/250*787/2<600?600:787-self.agi/250*787/2)+1d394",
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
        List.of(new ItemDefinition.ItemBoost(512, 3, "5", 0, 0)),
        List.of(),
        false);
  }
}
