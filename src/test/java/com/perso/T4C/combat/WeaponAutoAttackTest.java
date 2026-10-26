package com.perso.T4C.combat;

import static com.perso.T4C.config.GameConstants.BOW_ATTACK_RANGE;
import static com.perso.T4C.config.GameConstants.PLAYER_MELEE_ATTACK_RANGE;
import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.input.WeaponAutoAttack;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock("CollisionManager")
class WeaponAutoAttackTest {
  @TempDir Path tempDir;

  @BeforeEach
  @AfterEach
  void clearMap() {
    CollisionManager.getInstance().clear();
  }

  private static Player fighter() {
    Player player = new Player();
    player.setCurrentHp(100);
    player.setWorldPosition(0, 0, 0);
    player.setCombatMode(true);
    return player;
  }

  @Test
  void keepsTheCurrentEnemyThenAcquiresTheNearestLiveReplacement() throws Exception {
    Player player = fighter();
    TestMonster far = new TestMonster(PLAYER_MELEE_ATTACK_RANGE * .8f);
    TestMonster near = new TestMonster(PLAYER_MELEE_ATTACK_RANGE * .2f);
    TestMonster dead = new TestMonster(1);
    dead.markDead();
    TestMonster protectedEntity = new TestMonster(2);
    protectedEntity.setStationary(true);
    List<BaseMonster> monsters = List.of(far, dead, protectedEntity, near);
    assertSame(near, WeaponAutoAttack.selectTarget(player, null, monsters));
    assertSame(
        far,
        WeaponAutoAttack.selectTarget(player, far, monsters),
        "a closer arrival must not interrupt an ongoing attack");
    far.markDead();
    player.triggerAttackCooldown(.1);
    assertSame(near, WeaponAutoAttack.selectTarget(player, far, monsters));
    assertFalse(player.isAttackReady(), "changing targets must not reset the weapon cooldown");
    assertNull(
        WeaponAutoAttack.selectTarget(player, near, List.of(dead, protectedEntity)),
        "a removed encounter must not remain a target");
  }

  @Test
  void usesWeaponReachAndRequiresAQuiverForArchery() throws Exception {
    Player player = fighter();
    TestMonster target = new TestMonster((PLAYER_MELEE_ATTACK_RANGE + BOW_ATTACK_RANGE) / 2);
    assertNull(WeaponAutoAttack.selectTarget(player, null, List.of(target)));
    var bow =
        ItemRegistry.load().stream()
            .filter(i -> i.getBodyPart() == BodyPart.WEAPON && i.isBow())
            .findFirst()
            .orElseThrow();
    player.getEquippedItems().put(BodyPart.WEAPON, bow.getKey());
    assertNull(WeaponAutoAttack.selectTarget(player, null, List.of(target)));
    player.getEquippedItems().put(BodyPart.WEAPON2, "item.wooden_arrow");
    assertSame(target, WeaponAutoAttack.selectTarget(player, null, List.of(target)));
    target.getPosition().set(BOW_ATTACK_RANGE + 1, 0);
    assertNull(WeaponAutoAttack.selectTarget(player, target, List.of(target)));
    target.getPosition().set(PLAYER_MELEE_ATTACK_RANGE, 0);
    player.getEquippedItems().clear();
    assertSame(target, WeaponAutoAttack.selectTarget(player, null, List.of(target)));
  }

  @Test
  void ignoresSafeHavensAndEnemiesBehindWalls() throws Exception {
    byte[] collision = new byte[16];
    collision[1] = 1; // Wall between the fighter and the eastern monster.
    collision[4] = 6; // Safe haven directly south of the fighter.
    Path map = tempDir.resolve("auto-attack.colbin");
    CollisionMapIO.write(map.toFile(), 4, 4, collision);
    CollisionManager.getInstance().initialize(new CollisionReader(map.toFile()));
    Player player = fighter();
    TestMonster blocked = new TestMonster(PLAYER_MELEE_ATTACK_RANGE);
    assertNull(WeaponAutoAttack.selectTarget(player, null, List.of(blocked)));
    TestMonster safe = new TestMonster(0);
    safe.getPosition().set(0, com.perso.T4C.config.GameConstants.GRID_H);
    assertNull(WeaponAutoAttack.selectTarget(player, null, List.of(safe)));
    player.setWorldPosition(safe.getPosition().x, safe.getPosition().y, 0);
    assertNull(WeaponAutoAttack.selectTarget(player, null, List.of(new TestMonster(0))));
  }

  @Test
  void idleModeCanResumeButInputAndStunPauseItAndToggleOffStopsIt() throws Exception {
    Player player = fighter();
    TestMonster target = new TestMonster(1);
    assertNull(WeaponAutoAttack.selectTarget(player, null, List.of()));
    assertTrue(player.isCombatMode(), "no enemies nearby should leave the toggle enabled");
    assertFalse(WeaponAutoAttack.canRun(player, true));
    assertSame(target, WeaponAutoAttack.selectTarget(player, null, List.of(target)));
    player.stunFor(10_000);
    assertFalse(WeaponAutoAttack.canRun(player, false));
    player.clearStun();
    assertTrue(WeaponAutoAttack.canRun(player, false));
    player.setCombatMode(false);
    assertNull(WeaponAutoAttack.selectTarget(player, target, List.of(target)));
    player.setCombatMode(true);
    player.setCurrentHp(0);
    assertFalse(WeaponAutoAttack.canRun(player, false));
  }

  private static final class TestMonster extends BaseMonster {
    TestMonster(float x) throws GameException {
      super("Auto-attack test", x, 0, 10, 0, 0, 0, 1, 1, 1_000L, null, null, null, null, null);
    }

    void markDead() {
      isDead = true;
    }
  }
}
