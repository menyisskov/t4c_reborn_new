package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

// T4C-0047: the Wyrm Scales - one rare drop per existing Elder Wyrm (Rootcrown/Pyreclaw/Mistwing/
// Duskmaw/Galecrest, T4C-0029/0038). Not equippable, same non-equippable shape as BoundGodsigil -
// pure turn-in tokens for npc/KeeperOfTheSixthSeal.java, which checks the player holds all five
// and, if so, consumes them and summons The Convergent Wyrm (assets/monsters/convergent_wyrm.json)
// at the Colosseum. See DESIGN_GUIDELINES.md "The Elder Wyrms" for the pattern this extends.
public final class WyrmScales {
  private WyrmScales() {}

  public static final String ROOTCROWN_KEY = "item.rootcrown_wyrm_scale";
  public static final String PYRECLAW_KEY = "item.pyreclaw_wyrm_scale";
  public static final String MISTWING_KEY = "item.mistwing_wyrm_scale";
  public static final String DUSKMAW_KEY = "item.duskmaw_wyrm_scale";
  public static final String GALECREST_KEY = "item.galecrest_wyrm_scale";

  public static ItemDefinition rootcrown() {
    return scale(ROOTCROWN_KEY, "${item.rootcrown_wyrm_scale}");
  }

  public static ItemDefinition pyreclaw() {
    return scale(PYRECLAW_KEY, "${item.pyreclaw_wyrm_scale}");
  }

  public static ItemDefinition mistwing() {
    return scale(MISTWING_KEY, "${item.mistwing_wyrm_scale}");
  }

  public static ItemDefinition duskmaw() {
    return scale(DUSKMAW_KEY, "${item.duskmaw_wyrm_scale}");
  }

  public static ItemDefinition galecrest() {
    return scale(GALECREST_KEY, "${item.galecrest_wyrm_scale}");
  }

  private static ItemDefinition scale(String key, String name) {
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
