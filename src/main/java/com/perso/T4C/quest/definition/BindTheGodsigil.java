package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// T4C-0033: second stage of the Godsforged crafting chain, run in parallel with
// ForgeTheGodcore.java. Warden Seressa binds a Bound Godsigil from 5 Veiled Aether Shards - a
// rare drop from The Rootcrown Wyrm and Ysolde the Veiled Matriarch. See
// npc/WardenSeressa.java.
public final class BindTheGodsigil {
  private BindTheGodsigil() {}

  public static QuestDef definition() {
    return new QuestDef(
        "bind_the_godsigil",
        "${quest.bind_the_godsigil.title}",
        "WardenSeressa",
        "",
        0,
        0,
        1355,
        1465,
        1,
        800000,
        150000000,
        "${quest.bind_the_godsigil.offer}",
        "${quest.bind_the_godsigil.completion}",
        "${quest.bind_the_godsigil.completed}",
        null,
        "item.veiled_aether_shard",
        5,
        null,
        "item.bound_godsigil");
  }
}
