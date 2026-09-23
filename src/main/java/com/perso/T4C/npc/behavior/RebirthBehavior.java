package com.perso.T4C.npc.behavior;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.player.PlayerProgression;
import java.util.ArrayList;
import java.util.List;

public final class RebirthBehavior {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  private RebirthBehavior() {}

  /** Base level the Oracle asks for before a character's first rebirth. */
  public static final int FIRST_REBIRTH_LEVEL = 75;

  /** Extra levels the Oracle asks for per rebirth already taken. */
  public static final int LEVEL_PER_PREVIOUS_REBIRTH = 5;

  /** Character level the Oracle requires before the {@code rebirthNumber}-th rebirth (1-based). */
  public static int requiredLevelFor(int rebirthNumber) {

    return FIRST_REBIRTH_LEVEL + (rebirthNumber - 1) * LEVEL_PER_PREVIOUS_REBIRTH;
  }

  /** Value every attribute is reset to by the {@code rebirthNumber}-th rebirth (1-based). */
  public static int startingAttributeFor(int rebirthNumber) {

    return GameConstants.REBIRTH_BASE_ATTRIBUTE
        + rebirthNumber * GameConstants.REBIRTH_ATTRIBUTE_PER_REMORT;
  }

  /** Energy points granted by the {@code rebirthNumber}-th rebirth (1-based), spent with Alphan's
   * associates before returning to the world. */
  public static int energyPointsFor(int rebirthNumber) {

    return GameConstants.REBIRTH_REMORT_POINTS_PER_REBIRTH
        + (rebirthNumber - 1) * GameConstants.REBIRTH_REMORT_POINTS_PER_EXTRA_REMORT;
  }

  /** Whether this character is still below {@link GameConstants#REBIRTH_MAX_REMORTS}. */
  public static boolean canRebirth(Player player) {

    return player != null
        && player.getQuestFlag("__FLAG_NUMBER_OF_REMORTS") < GameConstants.REBIRTH_MAX_REMORTS;
  }

  /**
   * Rebirths the character. Returns {@code false} (and changes nothing) once the character has
   * already been reborn {@link GameConstants#REBIRTH_MAX_REMORTS} times.
   */
  public static boolean perform(Player player) {

    if (player == null) {

      throw new IllegalArgumentException("player is required");
    }

    if (!canRebirth(player)) return false;

    int remorts = player.getQuestFlag("__FLAG_NUMBER_OF_REMORTS") + 1;

    player.setQuestFlag("__FLAG_NUMBER_OF_REMORTS", remorts);

    player.setRebirthCount(remorts);

    int remortPoints = energyPointsFor(remorts);

    player.setQuestFlag("__FLAG_REMORT_POINTS", remortPoints);

    player.setQuestFlag("__FLAG_REMORT_PROCESS", 0);

    int attribute = startingAttributeFor(remorts);

    player.setStrength(attribute);

    player.setDexterity(attribute);

    player.setEndurance(attribute);

    player.setIntelligence(attribute);

    player.setWisdom(attribute);

    for (String element : List.of("fire", "water", "air", "earth", "light", "dark")) {

      player.setQuestFlag("legacy:resist:" + element, 0);

      player.setQuestFlag("legacy:power:" + element, 0);
    }

    player.setLevel(1);

    player.setCurrentXp(0);

    XpCurve.loadDefault().applyToPlayer(player);

    player.setStatPoints(0);

    player.setSkillPoints(0);

    if (player.getSkills() != null) player.getSkills().clear();

    if (player.getSpells() != null) player.getSpells().clear();

    if (player.getQuickSlots() != null) player.getQuickSlots().clear();

    int maxHp = PlayerProgression.hitPointGainBase(attribute);

    int maxMana = PlayerProgression.manaGainBase(attribute, attribute);

    player.setMaxHp(maxHp);

    player.setCurrentHp(maxHp);

    player.setMaxMana(maxMana);

    player.setMana(maxMana);

    for (Player.ActiveBuff buff : new ArrayList<>(player.getActiveBuffs()))
      player.dispelBuff(buff.getSpellName());

    player.setHiddenFor(0L);

    List<String> regalia = List.of("item.remort_white_wings", "item.ring_of_the_seraph");

    for (BodyPart slot : new ArrayList<>(player.getEquippedItems().keySet())) {

      String equippedKey = player.getEquippedItems().get(slot);

      if (equippedKey == null || !regalia.contains(equippedKey))
        InventoryService.unequip(player, slot);
    }

    for (String key : regalia) {

      ItemDefinition definition = ItemRegistry.findByKey(key);

      if (definition == null || definition.getBodyPart() == null) continue;

      if (!player.getInventory().contains(key)) InventoryService.add(player, key);

      InventoryService.equip(player, definition.getBodyPart(), key);
    }

    InventoryService.add(player, "item.torch");

    PlayerAppearanceDefaults.applyDefaults(player);

    if (player.getAnimations() != null) player.getAnimations().refresh();

    return true;
  }
}
