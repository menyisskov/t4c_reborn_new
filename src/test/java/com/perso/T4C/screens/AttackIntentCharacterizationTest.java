package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.render.SpellRenderer;
import com.perso.T4C.spell.SpellData;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Characterizes the player's intent to attack a monster they just clicked ({@code
 * MainGameScreen.beginAttackTarget}, the callback of {@code MonsterInputHandler}): what is
 * recorded as the current target, in which order, and what is (not) done when the player cannot
 * swing yet, the bow cannot be fired, or the target is out of reach. The screen is allocated
 * without its constructor (no GL, no assets) and only the fields this intent reads are filled in;
 * the monster manager and the projectile renderer are recording fakes. The only code that points
 * at the code under test is {@link #attack}.
 *
 * <p>Range, facing, line of sight and damage live in {@code MonsterManager.attackMonster}, which
 * is faked here: this intent only decides whether to call it and what to do with its answer. The
 * click handler (not this intent) filters out dead and protected monsters; the tests below pin that
 * the intent itself does not.
 */
class AttackIntentCharacterizationTest {
  private final List<String> calls = new ArrayList<>();
  private boolean meleeResult = true;
  private boolean projectileLaunched = true;
  private Object targetWhenSwinging;

  private final class FakeMonsters extends MonsterManager {
    FakeMonsters() {
      super(null);
    }

    @Override
    public boolean attackMonster(BaseMonster monster, Player player) {
      calls.add("attackMonster");
      targetWhenSwinging = peek("currentAttackTarget");
      attackedMonster = monster;
      attackingPlayer = player;
      return meleeResult;
    }
  }

  private BaseMonster attackedMonster;
  private Player attackingPlayer;
  private MainGameScreen currentScreen;

  private Object peek(String name) {
    try {
      Field f = MainGameScreen.class.getDeclaredField(name);
      f.setAccessible(true);
      return f.get(currentScreen);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  private static final class TestMonster extends BaseMonster {
    TestMonster(float x) throws GameException {
      super("Attack intent test", x, 0, 10, 0, 0, 0, 1, 1, 1_000L, null, null, null, null, null);
    }

    void markDead() {
      isDead = true;
    }
  }

  private MainGameScreen screen(Player player) throws Exception {
    Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    unsafeField.setAccessible(true);
    sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
    MainGameScreen screen = (MainGameScreen) unsafe.allocateInstance(MainGameScreen.class);
    SpellRenderer renderer =
        new SpellRenderer(null) {
          @Override
          public boolean launchProjectile(
              String projectileSpell,
              BaseMonster target,
              float startX,
              float startY,
              boolean flipX,
              Runnable onImpact) {
            calls.add("launchProjectile");
            return projectileLaunched;
          }
        };
    set(screen, "spellRenderer", renderer);
    set(screen, "session", new com.perso.T4C.session.LocalGameSession());
    set(screen, "player", player);
    set(screen, "monsterManager", new FakeMonsters());
    currentScreen = screen;
    // The constructor, which a real screen runs, connects the session's commands to the screen.
    var bind = MainGameScreen.class.getDeclaredMethod("bindLocalCommands");
    bind.setAccessible(true);
    bind.invoke(screen);
    return screen;
  }

  private static void set(Object target, String name, Object value) throws Exception {
    Field f = MainGameScreen.class.getDeclaredField(name);
    f.setAccessible(true);
    f.set(target, value);
  }

  private static Object get(Object target, String name) throws Exception {
    Field f = MainGameScreen.class.getDeclaredField(name);
    f.setAccessible(true);
    return f.get(target);
  }

  private static Player fighter() {
    Player player = new Player();
    player.setCurrentHp(100);
    player.setWorldPosition(0, 0, 0);
    return player;
  }

  private static void equipBow(Player player, boolean quiver) {
    var bow =
        ItemRegistry.load().stream()
            .filter(i -> i.getBodyPart() == BodyPart.WEAPON && i.isBow())
            .findFirst()
            .orElseThrow();
    player.getEquippedItems().put(BodyPart.WEAPON, bow.getKey());
    if (quiver) player.getEquippedItems().put(BodyPart.WEAPON2, "item.wooden_arrow");
  }

  // --- the one helper that points at the code under test ---

  private static void attack(MainGameScreen screen, BaseMonster monster) throws Exception {
    var m = MainGameScreen.class.getDeclaredMethod("beginAttackTarget", BaseMonster.class);
    m.setAccessible(true);
    try {
      m.invoke(screen, monster);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException re) throw re;
      throw e;
    }
  }

  // --- behaviour ---

  @Test
  void aReadyPlayerSwingsOnceAtTheClickedMonsterAndStartsTheWeaponCooldown() throws Exception {
    Player player = fighter();
    MainGameScreen screen = screen(player);
    TestMonster monster = new TestMonster(10);
    attack(screen, monster);
    assertEquals(List.of("attackMonster"), calls);
    assertSame(monster, attackedMonster);
    assertSame(player, attackingPlayer);
    assertSame(monster, get(screen, "currentAttackTarget"));
    assertFalse(player.isAttackReady(), "a landed swing starts the weapon cooldown");
  }

  @Test
  void theTargetIsRecordedBeforeTheSwingIsAttempted() throws Exception {
    MainGameScreen screen = screen(fighter());
    TestMonster monster = new TestMonster(10);
    attack(screen, monster);
    assertSame(monster, targetWhenSwinging);
  }

  @Test
  void aMissedSwingKeepsTheTargetAndDoesNotStartTheCooldown() throws Exception {
    meleeResult = false;
    Player player = fighter();
    MainGameScreen screen = screen(player);
    TestMonster monster = new TestMonster(10_000);
    attack(screen, monster);
    assertEquals(List.of("attackMonster"), calls);
    assertSame(monster, get(screen, "currentAttackTarget"));
    assertTrue(player.isAttackReady(), "an out of reach click must not cost the player a swing");
  }

  @Test
  void aPlayerStillOnCooldownOnlyBecomesTheAttackerWithoutSwinging() throws Exception {
    Player player = fighter();
    player.triggerAttackCooldown(0.01);
    MainGameScreen screen = screen(player);
    TestMonster monster = new TestMonster(10);
    attack(screen, monster);
    assertEquals(List.of(), calls);
    assertSame(monster, get(screen, "currentAttackTarget"));
  }

  @Test
  void aStunnedPlayerOnlyBecomesTheAttackerWithoutSwinging() throws Exception {
    Player player = fighter();
    player.stunFor(60_000L);
    MainGameScreen screen = screen(player);
    TestMonster monster = new TestMonster(10);
    attack(screen, monster);
    assertEquals(List.of(), calls);
    assertSame(monster, get(screen, "currentAttackTarget"));
  }

  @Test
  void aPlayerAtZeroHealthIsNotRefusedByTheIntent() throws Exception {
    Player player = fighter();
    Field hp = com.perso.T4C.model.Stats.class.getDeclaredField("currentHp");
    hp.setAccessible(true);
    hp.setInt(player, 0);
    MainGameScreen screen = screen(player);
    TestMonster monster = new TestMonster(10);
    attack(screen, monster);
    assertEquals(List.of("attackMonster"), calls);
    assertSame(monster, get(screen, "currentAttackTarget"));
  }

  @Test
  void noMonsterMeansNothingHappens() throws Exception {
    MainGameScreen screen = screen(fighter());
    TestMonster earlier = new TestMonster(10);
    set(screen, "currentAttackTarget", earlier);
    attack(screen, null);
    assertEquals(List.of(), calls);
    assertSame(earlier, get(screen, "currentAttackTarget"), "a null click keeps the old target");
  }

  @Test
  void noPlayerMeansNothingHappens() throws Exception {
    MainGameScreen screen = screen(null);
    attack(screen, new TestMonster(10));
    assertEquals(List.of(), calls);
    assertNull(get(screen, "currentAttackTarget"));
  }

  @Test
  void withoutAMonsterManagerTheTargetIsSetButNobodyIsHit() throws Exception {
    Player player = fighter();
    MainGameScreen screen = screen(player);
    set(screen, "monsterManager", null);
    TestMonster monster = new TestMonster(10);
    attack(screen, monster);
    assertEquals(List.of(), calls);
    assertSame(monster, get(screen, "currentAttackTarget"));
    assertTrue(player.isAttackReady());
  }

  @Test
  void theIntentItselfDoesNotFilterDeadOrProtectedMonsters() throws Exception {
    MainGameScreen screen = screen(fighter());
    TestMonster dead = new TestMonster(10);
    dead.markDead();
    attack(screen, dead);
    assertSame(dead, get(screen, "currentAttackTarget"));
    assertEquals(List.of("attackMonster"), calls);
  }

  @Test
  void clickingResetsAnArmedAttackSpellAndTheRetryTimer() throws Exception {
    MainGameScreen screen = screen(fighter());
    Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    unsafeField.setAccessible(true);
    sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
    set(screen, "currentAttackSpell", unsafe.allocateInstance(SpellData.class));
    Field retry = MainGameScreen.class.getDeclaredField("nextAutoSpellAttemptAtMs");
    retry.setAccessible(true);
    retry.setLong(screen, 123456L);
    attack(screen, new TestMonster(10));
    assertNull(get(screen, "currentAttackSpell"));
    assertNull(get(screen, "currentAttackNpcTarget"));
    assertEquals(0L, retry.getLong(screen));
  }

  @Test
  void outsideCombatModeTheTargetIsNotHighlightedButInCombatModeItIs() throws Exception {
    Player player = fighter();
    MainGameScreen screen = screen(player);
    TestMonster first = new TestMonster(10);
    attack(screen, first);
    assertFalse(first.isSelected());
    assertNull(get(screen, "selectedMonster"));

    player.setCombatMode(true);
    TestMonster second = new TestMonster(20);
    attack(screen, second);
    assertTrue(second.isSelected());
    assertSame(second, get(screen, "selectedMonster"));
  }

  @Test
  void aNewCombatModeTargetReleasesTheHighlightOfThePreviousOne() throws Exception {
    Player player = fighter();
    player.setCombatMode(true);
    MainGameScreen screen = screen(player);
    TestMonster first = new TestMonster(10);
    TestMonster second = new TestMonster(20);
    attack(screen, first);
    attack(screen, second);
    assertFalse(first.isSelected());
    assertTrue(second.isSelected());
  }

  @Test
  void aBowTargetOutOfRangeIsRefusedAndTheOldTargetAndCombatModeAreDropped() throws Exception {
    Player player = fighter();
    equipBow(player, true);
    player.setCombatMode(true);
    MainGameScreen screen = screen(player);
    TestMonster earlier = new TestMonster(10);
    set(screen, "currentAttackTarget", earlier);
    attack(screen, new TestMonster(1_000_000));
    assertEquals(List.of(), calls);
    assertNull(get(screen, "currentAttackTarget"));
    assertFalse(player.isCombatMode());
  }

  @Test
  void aBowWithoutAQuiverIsRefusedAndNothingIsFired() throws Exception {
    Player player = fighter();
    equipBow(player, false);
    MainGameScreen screen = screen(player);
    attack(screen, new TestMonster(10));
    assertEquals(List.of(), calls);
    assertNull(get(screen, "currentAttackTarget"));
  }

  @Test
  void aReadyArcherInRangeFiresInsteadOfSwingingAndStartsTheCooldown() throws Exception {
    Player player = fighter();
    equipBow(player, true);
    MainGameScreen screen = screen(player);
    TestMonster monster = new TestMonster(50);
    attack(screen, monster);
    assertEquals(List.of("launchProjectile"), calls);
    assertSame(monster, get(screen, "currentAttackTarget"));
    assertFalse(player.isAttackReady());
  }

  @Test
  void anArcherWhoseShotCannotBeLaunchedKeepsTheTargetWithoutACooldown() throws Exception {
    projectileLaunched = false;
    Player player = fighter();
    equipBow(player, true);
    MainGameScreen screen = screen(player);
    TestMonster monster = new TestMonster(50);
    attack(screen, monster);
    assertEquals(List.of("launchProjectile", "launchProjectile"), calls);
    assertSame(monster, get(screen, "currentAttackTarget"));
    assertTrue(player.isAttackReady());
  }
}
