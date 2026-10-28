package com.perso.T4C.session;

import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.player.Player;

/**
 * What the single-player attack intent needs from the game screen, which still owns the
 * attack-target state, the bow rules and the projectile renderer. Single-player only: a remote
 * session has no use for it.
 */
public interface LocalAttackHost {
  Player player();

  /** The registry of the current map; may be null before the world is loaded. */
  MonsterManager monsters();

  boolean canFireBowAt(BaseMonster monster, boolean announce);

  boolean fireBow(BaseMonster monster, boolean announce);

  double weaponAttackSpeed();

  /** Drops the current attack target and leaves combat mode. */
  void clearCurrentAttackTarget();

  /** Makes the monster the current attack target and resets any armed spell attack. */
  void recordAttackTarget(BaseMonster monster);
}
