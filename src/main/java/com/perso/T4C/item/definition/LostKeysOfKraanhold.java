package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

// T4C-0049, "Lost Keys of Kraanhold": twelve named keys, each a rare drop from a specific
// Kraanhold-native monster (or, for the last three, the Windhowl War-Party, T4C-0045). No quest
// log, no map marker - each key's own flavor text (assets/i18n/lang.json, "item.<key>") is the
// only hint where its lock waits. Four chest NPCs (npc/*KraanholdChest*.java), each accepting
// three of the twelve, open on the matching key and pay out; see DESIGN_GUIDELINES.md for the
// "why four chests, not twelve" call. Same non-equippable shape as BoundGodsigil/WyrmScales.
public final class LostKeysOfKraanhold {
  private LostKeysOfKraanhold() {}

  public static final String FLYERS_BARBED_KEY = "item.flyers_barbed_key";
  public static final String WORKERS_CALLOUSED_KEY = "item.workers_calloused_key";
  public static final String MILIPEDES_CHITIN_KEY = "item.milipedes_chitin_key";
  public static final String PLAGUE_EATEN_KEY = "item.plague_eaten_key";
  public static final String REAPERS_IRON_KEY = "item.reapers_iron_key";
  public static final String STOMPERS_CRACKED_KEY = "item.stompers_cracked_key";
  public static final String WYRMLINGS_TARNISHED_KEY = "item.wyrmlings_tarnished_key";
  public static final String DRAGONGUARDS_SEALED_KEY = "item.dragonguards_sealed_key";
  public static final String TOLL_TROLLS_RUSTED_KEY = "item.toll_trolls_rusted_key";
  public static final String RAIDERS_NOTCHED_KEY = "item.raiders_notched_key";
  public static final String BANNER_BEARERS_KEY = "item.banner_bearers_key";
  public static final String WARLORDS_SIGNET_KEY = "item.warlords_signet_key";

  public static List<ItemDefinition> all() {
    return List.of(
        key(FLYERS_BARBED_KEY, "${item.flyers_barbed_key}"),
        key(WORKERS_CALLOUSED_KEY, "${item.workers_calloused_key}"),
        key(MILIPEDES_CHITIN_KEY, "${item.milipedes_chitin_key}"),
        key(PLAGUE_EATEN_KEY, "${item.plague_eaten_key}"),
        key(REAPERS_IRON_KEY, "${item.reapers_iron_key}"),
        key(STOMPERS_CRACKED_KEY, "${item.stompers_cracked_key}"),
        key(WYRMLINGS_TARNISHED_KEY, "${item.wyrmlings_tarnished_key}"),
        key(DRAGONGUARDS_SEALED_KEY, "${item.dragonguards_sealed_key}"),
        key(TOLL_TROLLS_RUSTED_KEY, "${item.toll_trolls_rusted_key}"),
        key(RAIDERS_NOTCHED_KEY, "${item.raiders_notched_key}"),
        key(BANNER_BEARERS_KEY, "${item.banner_bearers_key}"),
        key(WARLORDS_SIGNET_KEY, "${item.warlords_signet_key}"));
  }

  private static ItemDefinition key(String key, String name) {
    return new ItemDefinition(
        key,
        name,
        null,
        null,
        null,
        null,
        "64kInvDestinyGem",
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
        true,
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
