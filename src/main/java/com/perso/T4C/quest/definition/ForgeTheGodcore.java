package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// T4C-0033: first stage of the Godsforged crafting chain (see DESIGN_GUIDELINES.md "Godsforged: a
// tier above Legendary"). Ember-Smith Corvain tempers a Tempered Godcore from 5 Wyrmforged
// Embers - a rare drop from Makrsh P'Tangh and Ignarok the Emberfang. A pure item-turn-in quest
// (requiredKills=0, no real targetMonster/area), completed the standard way via
// QuestService.giveOrReport/turnInReadyQuests - see npc/EmberSmithCorvain.java.
public final class ForgeTheGodcore {
  private ForgeTheGodcore() {}

  public static QuestDef definition() {
    return new QuestDef(
        "forge_the_godcore",
        "${quest.forge_the_godcore.title}",
        "EmberSmithCorvain",
        "",
        0,
        0,
        1300,
        1460,
        1,
        2000000,
        150000000,
        "${quest.forge_the_godcore.offer}",
        "${quest.forge_the_godcore.completion}",
        "${quest.forge_the_godcore.completed}",
        null,
        "item.wyrmforged_ember",
        5,
        null,
        "item.tempered_godcore");
  }
}
