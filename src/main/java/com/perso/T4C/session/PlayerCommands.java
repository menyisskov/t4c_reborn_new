package com.perso.T4C.session;

import com.perso.T4C.monster.core.BaseMonster;

/**
 * Every state-changing player intent. The GUI and the input handlers say what the player wants;
 * the session decides how it happens. Members follow with the responsibility that moves behind
 * them (see multiplayer/CLIENT_UNIFICATION_DESIGN.md).
 */
public interface PlayerCommands {
  /**
   * The player clicked this monster to attack it: make it the current attack target and, when the
   * weapon is ready, swing or fire at once. The caller has already resolved the click to a live,
   * attackable monster; the intent itself does not filter.
   *
   * <p>Local session: records the target, then runs today's melee or bow attempt (range, line of
   * sight, cooldown and damage rules unchanged) and applies the result immediately. Remote
   * session: sends the intent to the server and applies nothing locally; the server decides
   * range, cooldown and damage, and a refusal comes back as a session event. The monster is a
   * local object today; the remote form identifies it by server id (the design's {@code
   * TargetRef}).
   */
  void attack(BaseMonster target);

  /**
   * The player holds a direction this frame: {@code dx} and {@code dy} are the projected input
   * (the sign picks one of 8 directions, y scaled by the tile aspect) and {@code delta} is the
   * frame time in seconds.
   *
   * <p>Local session: today's {@code Player.move} (stun, exhaustion and attack gates, speed
   * buffs, collision, sliding) applied at once. Remote session: sends the input with a sequence
   * number, applies the same shared rule to the local puppet for prediction and reconciles with
   * snapshots ({@code MovementReconciler}); the server stays authoritative. The
   * input handler sends every frame's movement through this intent.
   */
  void move(float dx, float dy, float delta);
}
