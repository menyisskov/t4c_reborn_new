package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.config.NativeTimingProfile;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.npc.core.NPCManager;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellEffectManager;
import com.perso.T4C.ui.FloatingDamage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Characterizes the entity half of the single-player per-frame simulation ({@code
 * MainGameScreen.updateEntities}): the fixed-step gate, and what the NPC and monster managers are
 * told, in which order, with which arguments. The screen is allocated without its constructor (no
 * GL, no assets) and only the fields this step reads are filled in; the managers are recording
 * fakes. The only code that points at the code under test is {@link #frame}.
 *
 * <p>There is no pause or GUI gate in this step: an open window, a dead player or a busy channel do
 * not stop the world, and the tests below pin that.
 */
class WorldTickCharacterizationTest {
  private static final int MARGIN = 5;
  private static final int[] BOUNDS = {3, 40, 7, 30, MARGIN};

  private final List<String> calls = new ArrayList<>();
  private Vector2 npcPosition;
  private Vector2 monsterPosition;
  private float npcDelta;
  private float monsterDelta;
  private int[] npcBounds;
  private int[] monsterBounds;
  private Player scriptPlayer;
  private RuntimeException npcFailure;

  private final class FakeNpcs extends NPCManager {
    FakeNpcs() {
      super(null, null);
    }

    @Override
    public void updateVisible(
        float delta, Vector2 pos, int startX, int endX, int startY, int endY, int margin) {
      calls.add("npc.updateVisible");
      npcDelta = delta;
      npcPosition = pos == null ? null : pos.cpy();
      npcBounds = new int[] {startX, endX, startY, endY, margin};
      if (npcFailure != null) throw npcFailure;
    }
  }

  private final class FakeMonsters extends MonsterManager {
    FakeMonsters() {
      super(null);
    }

    @Override
    public void setScriptPlayer(Player player) {
      calls.add("monster.setScriptPlayer");
      scriptPlayer = player;
    }

    @Override
    public void updateVisible(
        float delta, Vector2 pos, int startX, int endX, int startY, int endY, int margin) {
      calls.add("monster.updateVisible");
      monsterDelta = delta;
      monsterPosition = pos == null ? null : pos.cpy();
      monsterBounds = new int[] {startX, endX, startY, endY, margin};
    }
  }

  private static float step() {
    return 1f / NativeTimingProfile.current().fps();
  }

  private MainGameScreen screen(Player player, boolean npcs, boolean monsters) throws Exception {
    Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    unsafeField.setAccessible(true);
    sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
    MainGameScreen screen = (MainGameScreen) unsafe.allocateInstance(MainGameScreen.class);
    // FloatingDamage builds GL fonts in its constructor; only its (empty) entry list is used here.
    FloatingDamage damage = (FloatingDamage) unsafe.allocateInstance(FloatingDamage.class);
    Field entries = FloatingDamage.class.getDeclaredField("entries");
    entries.setAccessible(true);
    entries.set(damage, new ArrayList<>());
    set(screen, "floatingDamage", damage);
    set(screen, "spellEffectManager", new SpellEffectManager());
    set(screen, "session", new com.perso.T4C.session.LocalGameSession());
    set(screen, "playerPositionTemp", new Vector2());
    set(screen, "player", player);
    set(screen, "npcManager", npcs ? new FakeNpcs() : null);
    set(screen, "monsterManager", monsters ? new FakeMonsters() : null);
    set(screen, "renderStartX", 3);
    set(screen, "renderEndX", 40);
    set(screen, "renderStartY", 7);
    set(screen, "renderEndY", 30);
    return screen;
  }

  private static void set(Object target, String name, Object value) throws Exception {
    Field f = MainGameScreen.class.getDeclaredField(name);
    f.setAccessible(true);
    if (f.getType() == int.class) f.setInt(target, (Integer) value);
    else f.set(target, value);
  }

  private static float accumulator(MainGameScreen screen) throws Exception {
    Field f = MainGameScreen.class.getDeclaredField("simulationAccumulator");
    f.setAccessible(true);
    return f.getFloat(screen);
  }

  // --- the one helper that points at the code under test ---

  private static void frame(MainGameScreen screen, float delta) throws Exception {
    var m = MainGameScreen.class.getDeclaredMethod("updateEntities", float.class);
    m.setAccessible(true);
    try {
      m.invoke(screen, delta);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException re) throw re;
      throw e;
    }
  }

  // --- behaviour ---

  private static final List<String> FULL_TICK =
      List.of("npc.updateVisible", "monster.setScriptPlayer", "monster.updateVisible");

  @Test
  void nothingRunsUntilAFullStepHasAccumulated() throws Exception {
    MainGameScreen screen = screen(new Player(), true, true);
    frame(screen, step() * 0.4f);
    frame(screen, step() * 0.4f);
    assertEquals(List.of(), calls);
    assertEquals(step() * 0.8f, accumulator(screen), 1e-6f);
    frame(screen, step() * 0.4f);
    assertEquals(FULL_TICK, calls);
    assertEquals(step() * 0.2f, accumulator(screen), 1e-6f);
  }

  @Test
  void aTickIsAlwaysOneFixedStepLongWhateverTheFrameTime() throws Exception {
    MainGameScreen screen = screen(new Player(), true, true);
    frame(screen, 5f);
    assertEquals(step(), npcDelta);
    assertEquals(step(), monsterDelta);
    // A long frame is clamped to 0.25 s of accumulated time, then one step is consumed.
    assertEquals(0.25f - step(), accumulator(screen), 1e-6f);
  }

  @Test
  void aLongFrameRunsOnlyOneTickPerFrame() throws Exception {
    MainGameScreen screen = screen(new Player(), true, true);
    frame(screen, 0.25f);
    assertEquals(FULL_TICK, calls);
  }

  @Test
  void npcsAreTickedBeforeTheMonstersWhichGetTheScriptPlayerFirst() throws Exception {
    Player player = new Player();
    MainGameScreen screen = screen(player, true, true);
    frame(screen, step());
    assertEquals(FULL_TICK, calls);
    assertSame(player, scriptPlayer);
  }

  @Test
  void bothManagersGetTheScreenBoundsAndAMarginOfFive() throws Exception {
    MainGameScreen screen = screen(new Player(), true, true);
    frame(screen, step());
    assertArrayEquals(BOUNDS, npcBounds);
    assertArrayEquals(BOUNDS, monsterBounds);
  }

  @Test
  void bothManagersGetThePlayersPosition() throws Exception {
    Player player = new Player();
    MainGameScreen screen = screen(player, true, true);
    frame(screen, step());
    assertEquals(player.getPositionVector(), npcPosition);
    assertEquals(player.getPositionVector(), monsterPosition);
  }

  @Test
  void gmPeaceHidesThePlayerFromTheMonstersButNotFromTheNpcs() throws Exception {
    Player player = new Player();
    player.setGmPeace(true);
    MainGameScreen screen = screen(player, true, true);
    frame(screen, step());
    assertEquals(player.getPositionVector(), npcPosition);
    assertNull(monsterPosition);
    assertEquals(FULL_TICK, calls);
  }

  @Test
  void withoutAPlayerNothingIsTickedButTheStepIsStillConsumed() throws Exception {
    MainGameScreen screen = screen(null, true, true);
    frame(screen, step() * 1.5f);
    assertEquals(List.of(), calls);
    assertEquals(step() * 0.5f, accumulator(screen), 1e-6f);
  }

  @Test
  void aMissingNpcManagerSkipsOnlyTheNpcs() throws Exception {
    MainGameScreen screen = screen(new Player(), false, true);
    frame(screen, step());
    assertEquals(List.of("monster.setScriptPlayer", "monster.updateVisible"), calls);
  }

  @Test
  void aMissingMonsterManagerSkipsOnlyTheMonsters() throws Exception {
    MainGameScreen screen = screen(new Player(), true, false);
    frame(screen, step());
    assertEquals(List.of("npc.updateVisible"), calls);
  }

  @Test
  void aPlayerAtZeroHealthStillTicksTheWorld() throws Exception {
    Player player = new Player();
    Field hp = com.perso.T4C.model.Stats.class.getDeclaredField("currentHp");
    hp.setAccessible(true);
    hp.setInt(player, 0);
    MainGameScreen screen = screen(player, true, true);
    frame(screen, step());
    assertEquals(FULL_TICK, calls);
  }

  @Test
  void anNpcFailurePropagatesAndTheMonstersAreNotTickedThatFrame() throws Exception {
    MainGameScreen screen = screen(new Player(), true, true);
    npcFailure = new IllegalStateException("boom");
    assertThrows(IllegalStateException.class, () -> frame(screen, step()));
    assertEquals(List.of("npc.updateVisible"), calls);
  }
}
