package com.perso.T4C.quest;

import java.util.List;
import java.util.Map;

/**
 * Journal metadata for the Passage to Avalon, Avalon Pact, and Godsforged quest chains. Actual
 * acceptance gates live in the quest-giver dialogue. Existing active or completed quests remain
 * playable even if their saves predate a chain's prerequisite.
 */
public final class QuestChainInfo {
  private QuestChainInfo() {}

  public record Stage(
      String chainName, int stageNumber, int totalStages, List<String> prerequisiteQuestIds) {}

  private static final Map<String, Stage> STAGES =
      Map.ofEntries(
          Map.entry("tideworn_shore_scouts", new Stage("Passage to Avalon", 1, 2, List.of())),
          Map.entry(
              "passage_to_avalon",
              new Stage("Passage to Avalon", 2, 2, List.of("tideworn_shore_scouts"))),
          Map.entry("avalon_wilds_vigil", new Stage("${quest.chain.avalon_pact}", 1, 2, List.of())),
          Map.entry(
              "fading_veil_reckoning",
              new Stage("${quest.chain.avalon_pact}", 2, 2, List.of("avalon_wilds_vigil"))),
          Map.entry(
              "moonwake_missing",
              new Stage("${quest.chain.hollow_dawn}", 1, 10, List.of("fading_veil_reckoning"))),
          Map.entry(
              "pale_cantor",
              new Stage("${quest.chain.hollow_dawn}", 2, 10, List.of("moonwake_missing"))),
          Map.entry(
              "emberglass_oath",
              new Stage("${quest.chain.hollow_dawn}", 3, 10, List.of("pale_cantor"))),
          Map.entry(
              "cinder_marshal",
              new Stage("${quest.chain.hollow_dawn}", 4, 10, List.of("emberglass_oath"))),
          Map.entry(
              "ashbound_exiles",
              new Stage("${quest.chain.hollow_dawn}", 5, 10, List.of("cinder_marshal"))),
          Map.entry(
              "hush_cantor",
              new Stage("${quest.chain.hollow_dawn}", 6, 10, List.of("ashbound_exiles"))),
          Map.entry(
              "nullguard_watch",
              new Stage("${quest.chain.hollow_dawn}", 7, 10, List.of("hush_cantor"))),
          Map.entry(
              "dusk_regent",
              new Stage("${quest.chain.hollow_dawn}", 8, 10, List.of("nullguard_watch"))),
          Map.entry(
              "rift_unbinding",
              new Stage("${quest.chain.hollow_dawn}", 9, 10, List.of("dusk_regent"))),
          Map.entry(
              "rhunor_hollow_dawn",
              new Stage("${quest.chain.hollow_dawn}", 10, 10, List.of("rift_unbinding"))),
          Map.entry("forge_the_godcore", new Stage("The Godsforged", 1, 2, List.of())),
          Map.entry("bind_the_godsigil", new Stage("The Godsforged", 1, 2, List.of())),
          Map.entry(
              "forge_godsforged_warblade",
              new Stage("The Godsforged", 2, 2, List.of("forge_the_godcore", "bind_the_godsigil"))),
          Map.entry(
              "forge_godsforged_stormbow",
              new Stage("The Godsforged", 2, 2, List.of("forge_the_godcore", "bind_the_godsigil"))),
          Map.entry(
              "forge_godsforged_voidglass_rod",
              new Stage("The Godsforged", 2, 2, List.of("forge_the_godcore", "bind_the_godsigil"))),
          Map.entry(
              "forge_godsforged_zephyr_wand",
              new Stage("The Godsforged", 2, 2, List.of("forge_the_godcore", "bind_the_godsigil"))),
          Map.entry(
              "forge_godsforged_torc",
              new Stage(
                  "The Godsforged", 2, 2, List.of("forge_the_godcore", "bind_the_godsigil"))));

  /** {@code null} for the majority of quests, which aren't part of any multi-stage chain. */
  public static Stage stageFor(String questId) {
    return questId == null ? null : STAGES.get(questId);
  }
}
