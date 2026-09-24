package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

// T4C-0033: raw material for the Godsforged crafting chain's physical component (see
// npc/EmberSmithCorvain.java). Not equippable - authored as a legacy Java item rather than a
// JSON one specifically so it's exempt from ItemBalanceGuidelinesTest's gear rules, which assume
// every assets/items/*.json file is real wearable equipment. Rare drop from the world's most
// physically dangerous apex threats (see MakrshPtangh2.java, IgnarokTheEmberfang.java).
public final class WyrmforgedEmber {
  private WyrmforgedEmber() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wyrmforged_ember",
        "${item.wyrmforged_ember}",
        null,
        null,
        null,
        null,
        "V3_Orbe_Fire",
        0L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        0,
        2,
        0,
        null,
        null,
        0,
        0,
        false,
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
