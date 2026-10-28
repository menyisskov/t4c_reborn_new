package com.perso.T4C.server;

import com.perso.T4C.online.model.CharacterProgress;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copies an immutable progress draft with one section replaced, keeping every other field as it
 * was. Callers stay responsible for keeping inventory and equipment references consistent, which
 * the CharacterProgress constructor enforces.
 */
public final class ProgressEdits {
  private ProgressEdits() {}

  public static CharacterProgress withFlags(CharacterProgress p, Map<String, Integer> questFlags) {
    return new CharacterProgress(p.version(), p.characterClass(), p.gender(), p.attributes(),
        p.vitals(), p.advancement(), p.inventory(), p.equipment(), p.skills(), p.spells(),
        questFlags, p.storage(), p.storageGold(), p.respawn(), p.buffs(), p.cooldowns(),
        p.hiddenUntil(), p.companion());
  }

  public static CharacterProgress withVitals(CharacterProgress p, CharacterProgress.Vitals vitals) {
    return new CharacterProgress(p.version(), p.characterClass(), p.gender(), p.attributes(),
        vitals, p.advancement(), p.inventory(), p.equipment(), p.skills(), p.spells(),
        p.questFlags(), p.storage(), p.storageGold(), p.respawn(), p.buffs(), p.cooldowns(),
        p.hiddenUntil(), p.companion());
  }

  public static CharacterProgress withSpells(CharacterProgress p, Set<String> spells) {
    return new CharacterProgress(p.version(), p.characterClass(), p.gender(), p.attributes(),
        p.vitals(), p.advancement(), p.inventory(), p.equipment(), p.skills(), spells,
        p.questFlags(), p.storage(), p.storageGold(), p.respawn(), p.buffs(), p.cooldowns(),
        p.hiddenUntil(), p.companion());
  }

  public static CharacterProgress withInventory(CharacterProgress p,
      List<CharacterProgress.Item> inventory) {
    return new CharacterProgress(p.version(), p.characterClass(), p.gender(), p.attributes(),
        p.vitals(), p.advancement(), inventory, p.equipment(), p.skills(), p.spells(),
        p.questFlags(), p.storage(), p.storageGold(), p.respawn(), p.buffs(), p.cooldowns(),
        p.hiddenUntil(), p.companion());
  }

  public static CharacterProgress withHiddenUntil(CharacterProgress p, long hiddenUntil) {
    return new CharacterProgress(p.version(), p.characterClass(), p.gender(), p.attributes(),
        p.vitals(), p.advancement(), p.inventory(), p.equipment(), p.skills(), p.spells(),
        p.questFlags(), p.storage(), p.storageGold(), p.respawn(), p.buffs(), p.cooldowns(),
        hiddenUntil, p.companion());
  }

  public static CharacterProgress withCooldowns(CharacterProgress p, Map<String, Long> cooldowns) {
    return new CharacterProgress(p.version(), p.characterClass(), p.gender(), p.attributes(),
        p.vitals(), p.advancement(), p.inventory(), p.equipment(), p.skills(), p.spells(),
        p.questFlags(), p.storage(), p.storageGold(), p.respawn(), p.buffs(), cooldowns,
        p.hiddenUntil(), p.companion());
  }
}
