package com.perso.T4C.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.helper.CharacterCreationRules;
import com.perso.T4C.helper.LocalCharacterStore;
import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.player.Player;
import com.perso.T4C.screens.MainGameScreen;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Characterizes where single-player gets the initial character state from: the legacy save file
 * when no named character is active, the active character's own save otherwise, and nothing at all
 * (a new character) when the file is missing or unreadable. Written before the block moved behind
 * {@code CharacterSource} and kept unchanged afterwards; only the {@link #load()} and {@link
 * #applyTo} helpers below changed to point at the new home.
 */
@ResourceLock("user.dir")
class LocalCharacterSourceTest {
  @TempDir Path dir;
  private String originalUserDir;

  @BeforeEach
  void useTemporaryWorkingDirectory() {
    originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", dir.toString());
    PlayerStateStore.resetActiveFilename();
    PlayerStateStore.clearCompanionSupplier();
    clearActiveCharacter();
  }

  @AfterEach
  void restore() throws Exception {
    LocalCharacterStore.CharacterSlot active = LocalCharacterStore.getActiveCharacter();
    if (active != null) LocalCharacterStore.delete(active);
    clearActiveCharacter();
    PlayerStateStore.resetActiveFilename();
    PlayerStateStore.clearCompanionSupplier();
    System.setProperty("user.dir", originalUserDir);
  }

  private static void clearActiveCharacter() {
    try {
      Field f = LocalCharacterStore.class.getDeclaredField("activeCharacter");
      f.setAccessible(true);
      f.set(null, null);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  // --- the two helpers that point at the code under test ---

  private static PlayerStateDto load() {
    try {
      Field unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      Object unsafe = unsafeField.get(null);
      Object screen =
          unsafe
              .getClass()
              .getMethod("allocateInstance", Class.class)
              .invoke(unsafe, MainGameScreen.class);
      Method m = MainGameScreen.class.getDeclaredMethod("loadInitialPlayerState");
      m.setAccessible(true);
      return (PlayerStateDto) m.invoke(screen);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException re) throw re;
      throw new AssertionError(e.getCause());
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private static void applyTo(PlayerStateDto state, Player player) {
    PlayerStateStore.applyToPlayer(state, player);
  }

  // --- behaviour ---

  @Test
  void noActiveCharacterAndNoSaveMeansANewCharacter() {
    assertNull(load());
  }

  @Test
  void noActiveCharacterReadsTheLegacySaveFile() throws Exception {
    Files.writeString(
        dir.resolve("player_state.json"),
        "{\"name\":\"Ancien\",\"gender\":\"female\",\"level\":12,\"x\":30,\"y\":40,\"z\":0}");
    PlayerStateDto state = load();
    assertNotNull(state);
    assertEquals("Ancien", state.name);
    assertEquals("female", state.gender);
    assertEquals(12, state.level);
    assertEquals(30, state.x);
    assertEquals(40, state.y);
  }

  @Test
  void aCorruptSaveFallsBackToANewCharacter() throws Exception {
    Files.writeString(dir.resolve("player_state.json"), "{ this is not json");
    assertNull(load());
  }

  @Test
  void anActiveCharacterLoadsItsOwnSaveNotTheLegacyFile() throws Exception {
    Files.writeString(dir.resolve("player_state.json"), "{\"name\":\"Legacy\",\"level\":99}");
    LocalCharacterStore.CharacterSlot slot = createSlot("Alice", LocalCharacterStore.FEMALE);
    LocalCharacterStore.activate(slot);
    PlayerStateDto state = load();
    assertNotNull(state);
    assertEquals("Alice", state.name);
    assertEquals(LocalCharacterStore.FEMALE, state.gender);
    assertEquals(slot.stateFile(), PlayerStateStore.getActiveFilename());
  }

  @Test
  void anActiveCharacterWithoutNameOrGenderInItsSaveGetsTheSlotsValues() throws Exception {
    LocalCharacterStore.CharacterSlot slot = createSlot("Bob", LocalCharacterStore.MALE);
    Files.writeString(dir.resolve(slot.stateFile()), "{\"level\":5}");
    LocalCharacterStore.activate(slot);
    PlayerStateDto state = load();
    assertNotNull(state);
    assertEquals(5, state.level);
    assertEquals("Bob", state.name);
    assertEquals(LocalCharacterStore.MALE, state.gender);
  }

  @Test
  void anActiveCharacterKeepsTheNameAndGenderWrittenInItsSave() throws Exception {
    LocalCharacterStore.CharacterSlot slot = createSlot("Carol", LocalCharacterStore.FEMALE);
    Files.writeString(
        dir.resolve(slot.stateFile()), "{\"name\":\"Renamed\",\"gender\":\"male\",\"level\":5}");
    LocalCharacterStore.activate(slot);
    PlayerStateDto state = load();
    assertEquals("Renamed", state.name);
    assertEquals("male", state.gender);
  }

  @Test
  void anActiveCharacterWhoseSaveIsMissingFallsBackToANewCharacter() throws Exception {
    LocalCharacterStore.CharacterSlot slot = createSlot("Dave", LocalCharacterStore.MALE);
    Files.delete(dir.resolve(slot.stateFile()));
    LocalCharacterStore.activate(slot);
    assertNull(load());
  }

  @Test
  void applyingNoStateLeavesTheFreshPlayerUntouched() {
    Player player = new Player();
    int level = player.getLevel();
    int gold = player.getGold();
    applyTo(null, player);
    assertEquals(level, player.getLevel());
    assertEquals(gold, player.getGold());
  }

  @Test
  void applyingALoadedStateRestoresTheCharacter() throws Exception {
    Files.writeString(
        dir.resolve("player_state.json"),
        "{\"name\":\"Ancien\",\"level\":12,\"gold\":777,\"x\":30,\"y\":40,\"z\":0}");
    PlayerStateDto state = load();
    Player player = new Player();
    applyTo(state, player);
    assertEquals(12, player.getLevel());
    assertEquals(777, player.getGold());
    assertEquals("Ancien", player.getName());
    assertEquals(0, player.getCoordinates().getZ());
  }

  private static LocalCharacterStore.CharacterSlot createSlot(String name, String gender)
      throws Exception {
    return LocalCharacterStore.create(
        name,
        gender,
        CharacterClass.WARRIOR,
        new CharacterCreationRules.Stats(15, 14, 13, 12, 11, 28, 10),
        LocalCharacterStore.Alignment.GOOD);
  }
}
