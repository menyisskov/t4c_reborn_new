package com.perso.T4C.quest;

import java.util.List;
import java.util.Map;

/**
 * Journal metadata for the Witness Isles passage, Hollow Dawn, and Godsforged quest chains. Actual
 * acceptance gates live in the quest-giver dialogue. Existing active or completed quests remain
 * playable even if their saves predate a chain's prerequisite.
 */
public final class QuestChainInfo {
  private QuestChainInfo() {}

  public record Stage(
      String chainName,
      int stageNumber,
      int totalStages,
      List<String> prerequisiteQuestIds,
      boolean anyPrerequisite) {
    public Stage(
        String chainName, int stageNumber, int totalStages, List<String> prerequisiteQuestIds) {
      this(chainName, stageNumber, totalStages, prerequisiteQuestIds, false);
    }
  }

  private static final Map<String, Stage> STAGES =
      Map.ofEntries(
          Map.entry(
              "tideworn_shore_scouts",
              new Stage("${quest.chain.witness_passage}", 1, 2, List.of())),
          Map.entry(
              "passage_to_avalon",
              new Stage("${quest.chain.witness_passage}", 2, 2, List.of("tideworn_shore_scouts"))),
          Map.entry("avalon_wilds_vigil", new Stage("${quest.chain.avalon_pact}", 1, 2, List.of())),
          Map.entry(
              "fading_veil_reckoning",
              new Stage("${quest.chain.avalon_pact}", 2, 2, List.of("avalon_wilds_vigil"))),
          Map.entry(
              "moonwake_missing",
              new Stage("${quest.chain.hollow_dawn}", 1, 8, List.of("fading_veil_reckoning"))),
          Map.entry(
              "pale_cantor",
              new Stage("${quest.chain.hollow_dawn}", 2, 8, List.of("moonwake_missing"))),
          Map.entry(
              "emberglass_oath",
              new Stage("${quest.chain.hollow_dawn}", 1, 8, List.of("fading_veil_reckoning"))),
          Map.entry(
              "cinder_marshal",
              new Stage("${quest.chain.hollow_dawn}", 2, 8, List.of("emberglass_oath"))),
          Map.entry(
              "ashbound_exiles",
              new Stage("${quest.chain.hollow_dawn}", 3, 8, List.of("pale_cantor", "cinder_marshal"), true)),
          Map.entry(
              "hush_cantor",
              new Stage("${quest.chain.hollow_dawn}", 4, 8, List.of("ashbound_exiles"))),
          Map.entry(
              "nullguard_watch",
              new Stage("${quest.chain.hollow_dawn}", 5, 8, List.of("hush_cantor"))),
          Map.entry(
              "dusk_regent",
              new Stage("${quest.chain.hollow_dawn}", 6, 8, List.of("nullguard_watch"))),
          Map.entry(
              "rift_unbinding",
              new Stage("${quest.chain.hollow_dawn}", 7, 8, List.of("dusk_regent"))),
          Map.entry(
              "rhunor_hollow_dawn",
              new Stage("${quest.chain.hollow_dawn}", 8, 8, List.of("rift_unbinding"))),
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
