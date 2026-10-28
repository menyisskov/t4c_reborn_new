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

  /**
   * Advances the simulation of the monsters and NPCs near the player by one fixed step. The screen
   * owns the fixed-step gate and calls this once per step, only when there is a player. Local
   * sessions run the AI here: NPCs first, then the monsters (which are told the player for their
   * scripts and, unless the player is in GM peace, where to chase). Either registry may be absent
   * and is then skipped. Failures are not caught. A remote session must not simulate: the server
   * does, so its implementation applies received state instead and leaves the local AI off.
   */
  void tickEntities(EntityTick tick);
}
