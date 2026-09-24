package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// T4C-0033: final stage of the Godsforged crafting chain for the archer archetype - see
// ForgeGodsforgedWarblade.java for the full mechanic (shared Tempered Godcore/Bound Godsigil
// components, custom completion via npc/GrandmasterTholvenn.java).
public final class ForgeGodsforgedStormbow {
  private ForgeGodsforgedStormbow() {}

  public static QuestDef definition() {
    return new QuestDef(
        "forge_godsforged_stormbow",
        "${quest.forge_godsforged_stormbow.title}",
        "GrandmasterTholvenn",
        "",
        0,
        0,
        1330,
        1440,
        1,
        10000000,
        350000000,
        "${quest.forge_godsforged_stormbow.offer}",
        "${quest.forge_godsforged_stormbow.completion}",
        "${quest.forge_godsforged_stormbow.completed}",
        null,
        "item.bound_godsigil",
        1,
        null,
        "godsforged_stormbow");
  }
}
