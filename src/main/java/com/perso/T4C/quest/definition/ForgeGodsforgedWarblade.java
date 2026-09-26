package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// T4C-0033: final stage of the Godsforged crafting chain for the warrior archetype. Grandmaster
// Tholvenn forges the Godsforged Warblade from a Tempered Godcore (ForgeTheGodcore.java) and a
// Bound Godsigil (BindTheGodsigil.java). A single QuestDef can only natively track one required
// item (requiredItemKey below covers the sigil); the second component is checked/consumed by
// npc/GrandmasterTholvenn.java's custom javaBehavior() before it calls
// QuestService.completeCraftingQuest(), which then completes this quest and hands over the sigil
// + the item reward in one step - see that class for why this skips the normal
// accept-then-return-later flow entirely.
public final class ForgeGodsforgedWarblade {
  private ForgeGodsforgedWarblade() {}

  public static QuestDef definition() {
    return new QuestDef(
        "forge_godsforged_warblade",
        "${quest.forge_godsforged_warblade.title}",
        "GrandmasterTholvenn",
        "",
        0,
        0,
        1330,
        1440,
        1,
        1500000,
        350000000,
        "${quest.forge_godsforged_warblade.offer}",
        "${quest.forge_godsforged_warblade.completion}",
        "${quest.forge_godsforged_warblade.completed}",
        null,
        "item.bound_godsigil",
        1,
        null,
        "godsforged_warblade",
        0,
        "${quest.forge_godsforged_warblade.walkthrough}");
  }
}
