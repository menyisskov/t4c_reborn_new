package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemEmpyreanEarthSceptre {
  private ItemItemEmpyreanEarthSceptre() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.empyrean_earth_sceptre",
        "${item.empyrean_earth_sceptre}",
        BodyPart.WEAPON,
        "V2_Sceptre01",
        null,
        null,
        "Inv_V2_Sceptre01",
        0L,
        6L,
        0.0d,
        0L,
        0L,
        0L,
        200L,
        0L,
        0L,
        1000L,
        1.0d,
        false,
        false,
        true,
        900101,
        1,
        753,
        "1d24+43",
        "if(1687-self.agi/250*1687/2<1687/2?1687/2:1687-self.agi/250*1687/2)+1d844",
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
        List.of(
            new ItemDefinition.ItemBoost(90101, 4, "300", 0, 0),
            new ItemDefinition.ItemBoost(90102, 19, "200", 0, 0),
            new ItemDefinition.ItemBoost(90103, 23, "100", 0, 0),
            new ItemDefinition.ItemBoost(90104, 12, "50", 0, 0),
            new ItemDefinition.ItemBoost(90105, 13, "50", 0, 0),
            new ItemDefinition.ItemBoost(90106, 14, "50", 0, 0),
            new ItemDefinition.ItemBoost(90107, 15, "50", 0, 0),
            new ItemDefinition.ItemBoost(90108, 21, "50", 0, 0),
            new ItemDefinition.ItemBoost(90109, 22, "50", 0, 0),
            new ItemDefinition.ItemBoost(90110, 20, "50", 0, 0)),
        List.of(),
        false);
  }
}
