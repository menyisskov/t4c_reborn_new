package com.perso.T4C.spell;

import java.util.Set;

/**
 * Spells that were taken out of the game but can still be named by older saves - in the
 * spellbook, on a quick slot, bound to a macro or as an active buff. {@code PlayerStateMapper}
 * drops every reference to them on load (no refund; the owner's call), so a character never keeps
 * a spell that no longer exists.
 *
 * <p>T4C-0084: the level-400 rung of the high-tier ladder (one attack per school) and the
 * level-400 light ward Sanctum Ward.
 */
public final class RemovedSpells {
  private RemovedSpells() {}

  public static final Set<String> NAMES =
      Set.of(
          "${spell.ashfall}",
          "${spell.tectonic_ruin}",
          "${spell.heavenfall}",
          "${spell.cataclysms_herald}",
          "${spell.solar_apotheosis}",
          "${spell.eclipse_of_ruin}",
          "${spell.sanctum_ward}");

  public static boolean isRemoved(String spellName) {
    return spellName != null && NAMES.contains(spellName);
  }
}
