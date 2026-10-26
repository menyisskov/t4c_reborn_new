package com.perso.T4C.divine;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

/** Reusable bound items use the normal item-spell path, including casting restrictions. */
public final class DivineReturnScrolls {
  private DivineReturnScrolls() {}

  public static List<ItemDefinition> all() {
    return DivinePantheon.all().stream().map(DivineReturnScrolls::definition).toList();
  }

  public static boolean isBoundReturnScroll(String key) {
    String canonical = ItemDefinition.normalizeKey(key);
    return DivinePantheon.all().stream()
            .anyMatch(god -> ItemDefinition.normalizeKey(god.scrollKey()).equals(canonical))
        || com.perso.T4C.spawn.HuntingGrounds.all().stream()
            .filter(g -> g.trainingTier() > 0)
            .anyMatch(g -> ItemDefinition.normalizeKey(g.scrollKey()).equals(canonical));
  }

  private static ItemDefinition definition(DivinePantheon.God god) {
    return definition(god.scrollKey(), god.itemBase() + 90, god.returnSpellId());
  }

  public static ItemDefinition definition(String key, int itemId, int spellId) {
    ItemDefinition d =
        new ItemDefinition(
            key,
            "${item." + key + "}",
            null,
            null,
            null,
            null,
            "64kInvMisc1 3",
            0L,
            0L,
            0d,
            0L,
            0L,
            0L,
            0L,
            0L,
            0L,
            0L,
            0d,
            true,
            false,
            true,
            itemId,
            5,
            0,
            null,
            "0",
            0,
            0,
            true,
            null,
            0,
            null,
            0,
            0,
            0,
            List.of(new ItemDefinition.ItemSpell(spellId, 0, 100)),
            List.of(),
            List.of(),
            true);
    d.setRarityPriced(true);
    return d;
  }
}
