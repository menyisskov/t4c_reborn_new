package com.perso.T4C.player;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.behavior.RebirthBehavior;
import java.util.ArrayList;
import java.util.List;

/** Rebuilds allocation budgets without granting another life or repeating level-up rewards. */
public final class PlayerReroll {
  private PlayerReroll() {}

  public static void perform(Player player) {
    if (player == null) throw new IllegalArgumentException("player is required");
    int rebirths =
        Math.min(
            GameConstants.REBIRTH_MAX_REMORTS,
            Math.max(player.getRebirthCount(), player.getQuestFlag("__FLAG_NUMBER_OF_REMORTS")));
    int base = RebirthBehavior.startingAttributeFor(rebirths);
    int gainedLevels = Math.max(0, player.getLevel() - 1);

    // Unequip before resetting attributes; normal transfers preserve mirrored two-handed items.
    for (BodyPart slot : new ArrayList<>(player.getEquippedItems().keySet()))
      InventoryService.unequip(player, slot);
    for (var buff : new ArrayList<>(player.getActiveBuffs()))
      player.dispelBuff(buff.getSpellName());
    player.setCombatMode(false);
    player.setMeditating(false);
    player.setHiddenFor(0);
    player.clearStun();
    player.setStrength(base);
    player.setDexterity(base);
    player.setEndurance(base);
    player.setIntelligence(base);
    player.setWisdom(base);
    player.setStatPoints(gainedLevels * 5);
    player.setSkillPoints(gainedLevels * 15);
    if (player.getSkills() != null) player.getSkills().clear();
    if (player.getSpells() != null) player.getSpells().clear();
    if (player.getQuickSlots() != null) player.getQuickSlots().clear();
    player.setRebirthCount(rebirths);
    player.setQuestFlag("__FLAG_NUMBER_OF_REMORTS", rebirths);
    RebirthBehavior.ensureStartingWeaponSkills(player);
    for (String element : List.of("fire", "water", "air", "earth", "light", "dark")) {
      player.setQuestFlag("legacy:resist:" + element, 0);
      player.setQuestFlag("legacy:power:" + element, 0);
    }
    player.setQuestFlag(
        "__FLAG_REMORT_POINTS", rebirths == 0 ? 0 : RebirthBehavior.energyPointsFor(rebirths));
    player.setQuestFlag("__FLAG_REMORT_PROCESS", 0);
    // HP/mana earned over this life stay intact; no level-ups are replayed.
    player.setCurrentHp(player.getMaxHp());
    player.setMana(player.getMaxMana());
    player.setWorldPosition(1315 * GameConstants.GRID_W, 920 * GameConstants.GRID_H, 1);
    PlayerAppearanceDefaults.applyDefaults(player);
    if (player.getAnimations() != null) player.getAnimations().refresh();
  }
}
