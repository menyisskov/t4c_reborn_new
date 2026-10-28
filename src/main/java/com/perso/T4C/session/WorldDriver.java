package com.perso.T4C.session;

/**
 * What the per-frame simulation step does (runs it locally, mirrors it remotely). Members arrive
 * with the responsibility that moves behind them.
 */
public interface WorldDriver {
  /**
   * Creates the monster and NPC registries for the screen's current map, queues that map's spawns
   * and runs the NPC popup events. Called when the world is first built and on every map switch.
   */
  WorldEntities loadEntities(WorldEntityInputs inputs);
}
