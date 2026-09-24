package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// T4C-0033: final stage of the Godsforged crafting chain for the hybrid-mage archetype - see
// ForgeGodsforgedWarblade.java for the full mechanic.
public final class ForgeGodsforgedZephyrWand {
  private ForgeGodsforgedZephyrWand() {}

  public static QuestDef definition() {
    return new QuestDef(
        "forge_godsforged_zephyr_wand",
        "${quest.forge_godsforged_zephyr_wand.title}",
        "GrandmasterTholvenn",
        "",
        0,
        0,
        1330,
        1440,
        1,
        10000000,
        350000000,
        "${quest.forge_godsforged_zephyr_wand.offer}",
        "${quest.forge_godsforged_zephyr_wand.completion}",
        "${quest.forge_godsforged_zephyr_wand.completed}",
        null,
        "item.bound_godsigil",
        1,
        null,
        "godsforged_zephyr_wand");
  }
}
