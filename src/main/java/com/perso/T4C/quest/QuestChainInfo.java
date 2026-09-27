package com.perso.T4C.quest;

import java.util.List;
import java.util.Map;

/**
 * T4C-0062: UI-only metadata for the two quests in this codebase that are genuinely sequential,
 * multi-stage narratives (see DESIGN_GUIDELINES.md "Quests" - "Major zone gate: give it real
 * multi-stage structure"): the Passage to Avalon chain and the Godsforged crafting chain. Every
 * other quest here is a single, standalone `QuestDef` - including the zone's own "borderwatch"
 * access quests, which the quests' own file comments explicitly call out as *separate from*, not
 * a stage of, their zone's main questline (see WindhowlBorderwatch.java/SilverskyBorderwatch.
 * java), and the two independent post-unlock Avalon quests offered by ElderOphira (avalon_wilds_
 * vigil, fading_veil_reckoning), which gate on nothing but each other's giver being reachable.
 *
 * <p>This is display metadata only: it doesn't gate anything QuestService already doesn't gate
 * (the actual chain enforcement lives in the giver NPCs' own dialogue checks, e.g.
 * HarbormasterRangor checking tideworn_shore_scouts' STATUS_COMPLETED before offering
 * passage_to_avalon). Adding a new chain here only changes what the Quest Journal displays.
 */
public final class QuestChainInfo {
  private QuestChainInfo() {}

  public record Stage(String chainName, int stageNumber, int totalStages, List<String> prerequisiteQuestIds) {}

  private static final Map<String, Stage> STAGES =
      Map.of(
          "tideworn_shore_scouts",
              new Stage("Passage to Avalon", 1, 2, List.of()),
          "passage_to_avalon",
              new Stage("Passage to Avalon", 2, 2, List.of("tideworn_shore_scouts")),
          "forge_the_godcore",
              new Stage("The Godsforged", 1, 2, List.of()),
          "bind_the_godsigil",
              new Stage("The Godsforged", 1, 2, List.of()),
          "forge_godsforged_warblade",
              new Stage(
                  "The Godsforged", 2, 2, List.of("forge_the_godcore", "bind_the_godsigil")),
          "forge_godsforged_stormbow",
              new Stage(
                  "The Godsforged", 2, 2, List.of("forge_the_godcore", "bind_the_godsigil")),
          "forge_godsforged_voidglass_rod",
              new Stage(
                  "The Godsforged", 2, 2, List.of("forge_the_godcore", "bind_the_godsigil")),
          "forge_godsforged_zephyr_wand",
              new Stage(
                  "The Godsforged", 2, 2, List.of("forge_the_godcore", "bind_the_godsigil")),
          "forge_godsforged_torc",
              new Stage(
                  "The Godsforged", 2, 2, List.of("forge_the_godcore", "bind_the_godsigil")));

  /** {@code null} for the majority of quests, which aren't part of any multi-stage chain. */
  public static Stage stageFor(String questId) {
    return questId == null ? null : STAGES.get(questId);
  }
}
