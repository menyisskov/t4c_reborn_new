package com.perso.T4C.input;

import static com.perso.T4C.config.GameConstants.BOW_ATTACK_RANGE;
import static com.perso.T4C.config.GameConstants.PLAYER_MELEE_ATTACK_RANGE;
import static com.perso.T4C.config.GameConstants.QUIVER_STRUCTURE_ID;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.combat.CombatGeometry;
import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.helper.SanctuaryCombatRules;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.List;

/** Target selection only; normal weapon attacks continue to own damage and cooldowns. */
public final class WeaponAutoAttack {
  private WeaponAutoAttack() {}

  public static boolean canRun(Player player, boolean interacting) {
    return player != null
        && player.isCombatMode()
        && !interacting
        && player.getCurrentHp() > 0
        && !player.getMovement().isMoving()
        && !player.isStunned()
        && !player.isPhysicallyExhausted()
        && !player.isMeditating();
  }

  /** Keeps a valid current target; otherwise chooses the closest reachable, attackable monster. */
  public static BaseMonster selectTarget(
      Player player, BaseMonster current, List<BaseMonster> monsters) {
    if (!canRun(player, false) || monsters == null) return null;
    boolean bow = PlayerAppearanceDefaults.hasBowEquipped(player);
    if (bow) {
      var quiver = ItemRegistry.findByKey(player.getEquippedItems().get(BodyPart.WEAPON2));
      if (quiver == null || quiver.getStructure() != QUIVER_STRUCTURE_ID) return null;
    }
    Vector2 position = player.getPositionVector();
    float range = bow ? BOW_ATTACK_RANGE : PLAYER_MELEE_ATTACK_RANGE;
    if (current != null && monsters.contains(current) && eligible(current, position, range))
      return current;
    BaseMonster closest = null;
    float distance = Float.POSITIVE_INFINITY;
    for (BaseMonster candidate : monsters) {
      if (!eligible(candidate, position, range)) continue;
      float next = position.dst2(candidate.getPosition());
      if (next < distance) {
        closest = candidate;
        distance = next;
      }
    }
    return closest;
  }

  private static boolean eligible(BaseMonster monster, Vector2 position, float range) {
    return monster != null
        && !monster.isDead()
        && monster.canBeAttackedByPlayer()
        && position.dst2(monster.getPosition()) <= range * range
        && SanctuaryCombatRules.canFight(position, monster.getPosition())
        && CombatGeometry.hasLineOfSight(position, monster.getPosition());
  }
}
