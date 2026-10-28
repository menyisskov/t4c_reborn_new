package com.perso.T4C.session;

import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.player.Player;

/**
 * Single-player commands: today's code, moved behind the seam without changing behaviour. Each
 * intent applies its result to the local game at once.
 */
public final class LocalPlayerCommands implements PlayerCommands {
  private LocalAttackHost host;

  /** Connects the commands to the screen that owns the attack state; done once at screen build. */
  public void bind(LocalAttackHost host) {
    this.host = host;
  }

  @Override
  public void attack(BaseMonster monster) {
    if (host == null) {
      throw new IllegalStateException("local commands used before the screen bound them");
    }
    Player player = host.player();
    if (monster == null || player == null) {
      return;
    }
    if (PlayerAppearanceDefaults.hasBowEquipped(player) && !host.canFireBowAt(monster, true)) {
      host.clearCurrentAttackTarget();
      return;
    }
    host.recordAttackTarget(monster);
    if (player.isAttackReady()) {
      boolean attacked;
      if (PlayerAppearanceDefaults.hasBowEquipped(player)) {
        attacked = host.fireBow(monster, false);
      } else {
        MonsterManager monsters = host.monsters();
        attacked = monsters != null && monsters.attackMonster(monster, player);
      }
      if (attacked) {
        player.triggerAttackCooldown(host.weaponAttackSpeed());
      }
    }
  }

  @Override
  public void move(float dx, float dy, float delta) {
    if (host == null) {
      throw new IllegalStateException("local commands used before the screen bound them");
    }
    Player player = host.player();
    if (player != null) {
      player.move(dx, dy, delta);
    }
  }
}
