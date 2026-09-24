package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// T4C-0033: final stage of the Godsforged crafting chain for the intelligence-mage archetype -
// see ForgeGodsforgedWarblade.java for the full mechanic.
public final class ForgeGodsforgedVoidglassRod {
  private ForgeGodsforgedVoidglassRod() {}

  public static QuestDef definition() {
    return new QuestDef(
        "forge_godsforged_voidglass_rod",
        "${quest.forge_godsforged_voidglass_rod.title}",
        "GrandmasterTholvenn",
        "",
        0,
        0,
        1330,
        1440,
        1,
        1500000,
        350000000,
        "${quest.forge_godsforged_voidglass_rod.offer}",
        "${quest.forge_godsforged_voidglass_rod.completion}",
        "${quest.forge_godsforged_voidglass_rod.completed}",
        null,
        "item.bound_godsigil",
        1,
        null,
        "godsforged_voidglass_rod");
  }
}
