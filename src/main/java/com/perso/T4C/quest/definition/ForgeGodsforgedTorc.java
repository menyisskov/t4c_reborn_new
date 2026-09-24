package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// T4C-0033: final stage of the Godsforged crafting chain for the wisdom-mage archetype - see
// ForgeGodsforgedWarblade.java for the full mechanic. This one, the Torc of the First Pact, ties
// directly into Avalon's own fraying fey pact (see quest/definition/PassageToAvalon.java,
// npc/HarbormasterRangor.java) - the closest any of the Forgewrights have come to restoring a
// piece of the original working, rather than just borrowing its power.
public final class ForgeGodsforgedTorc {
  private ForgeGodsforgedTorc() {}

  public static QuestDef definition() {
    return new QuestDef(
        "forge_godsforged_torc",
        "${quest.forge_godsforged_torc.title}",
        "GrandmasterTholvenn",
        "",
        0,
        0,
        1330,
        1440,
        1,
        10000000,
        2000000000,
        "${quest.forge_godsforged_torc.offer}",
        "${quest.forge_godsforged_torc.completion}",
        "${quest.forge_godsforged_torc.completed}",
        null,
        "item.bound_godsigil",
        1,
        null,
        "godsforged_torc_of_the_first_pact");
  }
}
