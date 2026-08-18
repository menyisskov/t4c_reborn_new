package com.perso.T4C.npc;

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

public final class RebirthService {

  private RebirthService() {}

  public static void perform(Player player) {

    if (player == null) {

      throw new IllegalArgumentException("player is required");
    }

    int remorts = player.getQuestFlag("__FLAG_NUMBER_OF_REMORTS") + 1;

    player.setQuestFlag("__FLAG_NUMBER_OF_REMORTS", remorts);

    player.setRebirthCount(remorts);

    player.setQuestFlag("__FLAG_REMORT_POINTS", GameConstants.REBIRTH_REMORT_POINTS_PER_REBIRTH);

    player.setQuestFlag("__FLAG_REMORT_PROCESS", 0);

    int attribute =
        GameConstants.REBIRTH_BASE_ATTRIBUTE + remorts * GameConstants.REBIRTH_ATTRIBUTE_PER_REMORT;

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

      if (!regalia.contains(player.getEquippedItems().get(slot)))
        InventoryService.unequip(player, slot);
    }

    for (String key : regalia) {

      ItemDefinition definition = ItemRegistry.findByKey(key);

      if (definition == null || definition.getBodyPart() == null) continue;

      if (!player.getInventory().contains(key)) InventoryService.add(player, key);

      InventoryService.equip(player, definition.getBodyPart(), key);
    }

    PlayerAppearanceDefaults.applyDefaults(player);

    if (player.getAnimations() != null) player.getAnimations().refresh();
  }
}
