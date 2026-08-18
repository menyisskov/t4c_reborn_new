package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAdamantiteScimitar {
  private ItemItemAdamantiteScimitar() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.adamantite_scimitar",
        "${item.adamantite_scimitar}",
        BodyPart.WEAPON,
        "PupBattleSword",
        null,
        null,
        "64kInvBattleSword",
        0L,
        8L,
        0.0d,
        0L,
        0L,
        0L,
        372L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        40358,
        1,
        277,
        "1d72+126",
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
        List.of(),
        List.of(),
        false);
  }
}
