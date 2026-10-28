package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Stage 0 safety net for the client unification: pins how single-player saves and loads a character
 * today ({@link PlayerStateStore}, the persistence the session seam will wrap as {@code
 * CharacterPersistence}). Runs headless against a throw-away directory that stands in for the
 * working directory, so the real save directory is never touched. A later refactor that moves this
 * code must keep every expectation here.
 */
class PlayerStateStoreCharacterizationTest {
  private static final XpCurve XPCURVE = XpCurve.loadDefault();
  private String originalUserDir;
  @TempDir Path dir;

  @BeforeEach
  void useTemporaryWorkingDirectory() {
    originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", dir.toString());
    PlayerStateStore.setActiveFilename("hero.json");
    PlayerStateStore.clearCompanionSupplier();
  }

  @AfterEach
  void restore() {
    System.setProperty("user.dir", originalUserDir);
    PlayerStateStore.resetActiveFilename();
    PlayerStateStore.clearCompanionSupplier();
  }

  private static Player hero() {
    Player p = new Player();
    p.setName("Goldie");
    p.setGender("female");
    p.getCoordinates().setX(100.5f * GameConstants.GRID_W);
    p.getCoordinates().setY(200.5f * GameConstants.GRID_H);
    p.getCoordinates().setZ(0);
    p.setStrength(41);
    p.setDexterity(32);
    p.setEndurance(33);
    p.setIntelligence(24);
    p.setWisdom(25);
    p.setLevel(12);
    p.setXpToNextLevel(XPCURVE.getXpToNextLevel(12));
    p.setCurrentXp(XPCURVE.getXpToNextLevel(12) / 2);
    p.setGold(4321);
    p.setMaxHp(180);
    p.setCurrentHp(111);
    p.setMaxMana(60);
    p.setMana(22);
    p.setStatPoints(3);
    p.setSkillPoints(4);
    p.setSkillLevel("attack", 120);
    p.setInventory(new java.util.ArrayList<>(List.of("item.dagger", "item.dagger")));
    p.setSpells(new java.util.ArrayList<>(List.of("spell.heal_light", "spell.light")));
    p.setQuestFlag("pin.test.flag", 7);
    return p;
  }

  @Test
  void saveWritesTheActiveFileUnderTheWorkingDirectoryAndLeavesNoTemporaryFile()
      throws IOException {
    PlayerStateStore.save(hero(), 13.5f);
    assertTrue(Files.exists(dir.resolve("hero.json")));
    try (Stream<Path> files = Files.list(dir)) {
      assertEquals(
          Set.of("hero.json"), new TreeSet<>(files.map(p -> p.getFileName().toString()).toList()));
    }
  }

  @Test
  void saveThenLoadRoundTripsTheCharacterIncludingPlaceAndDayNightHour() {
    Player original = hero();
    PlayerStateStore.save(original, 13.5f);
    PlayerStateDto loaded = PlayerStateStore.load();
    assertNotNull(loaded);
    assertEquals("Goldie", loaded.name);
    assertEquals(13.5f, loaded.dayNightHour);
    assertEquals(100.5f, loaded.x, 0.001f, "half-tile position survives");
    assertEquals(200.5f, loaded.y, 0.001f);

    Player restored = new Player();
    PlayerStateStore.applyToPlayer(loaded, restored);
    assertEquals(12, restored.getLevel());
    assertEquals(XPCURVE.getXpToNextLevel(12) / 2, restored.getCurrentXp());
    assertEquals(XPCURVE.getXpToNextLevel(12), restored.getXpToNextLevel());
    assertEquals(4321, restored.getGold());
    assertEquals(111, restored.getCurrentHp());
    assertEquals(22, restored.getMana());
    assertEquals(41, restored.getStrength());
    assertEquals(3, restored.getStatPoints());
    assertEquals(4, restored.getSkillPoints());
    assertEquals(120, restored.getSkillLevel("attack"));
    assertEquals(List.of("item.dagger", "item.dagger"), restored.getInventory());
    assertTrue(restored.getSpells().containsAll(List.of("spell.heal_light", "spell.light")));
    assertEquals(7, restored.getQuestFlags().get("pin.test.flag"));
    assertEquals(original.getEquippedItems(), restored.getEquippedItems());
  }

  @Test
  void equippedItemsRoundTrip() {
    Player original = hero();
    original.getEquippedItems().put(BodyPart.BODY, "item.dagger");
    PlayerStateStore.save(original, 7f);
    Player restored = new Player();
    PlayerStateStore.applyToPlayer(PlayerStateStore.load(), restored);
    assertEquals(
        original.getEquippedItems().get(BodyPart.BODY),
        restored.getEquippedItems().get(BodyPart.BODY));
  }

  @Test
  void aSecondSaveReplacesTheFirstAndLoadSeesTheNewest() {
    Player p = hero();
    PlayerStateStore.save(p, 7f);
    p.setGold(99);
    PlayerStateStore.save(p, 8f);
    PlayerStateDto loaded = PlayerStateStore.load();
    assertEquals(99, loaded.gold);
    assertEquals(8f, loaded.dayNightHour);
  }

  @Test
  void saveWithoutAnHourReusesTheLastHourSaved() {
    PlayerStateStore.save(hero(), 18.25f);
    PlayerStateStore.save(hero());
    assertEquals(18.25f, PlayerStateStore.load().dayNightHour);
  }

  /**
   * Characterization, not endorsement: the "skip saving a character with empty stats" guard cannot
   * fire through {@code save(Player)} because the base max HP is clamped to at least 1, so even a
   * zeroed character is written. The seam must not rely on that guard to protect a save.
   */
  @Test
  void aZeroedCharacterIsStillWrittenBecauseBaseMaxHpIsAtLeastOne() {
    Player empty = new Player();
    empty.setLevel(0);
    empty.setMaxHp(0);
    empty.setCurrentHp(0);
    empty.setMaxMana(0);
    empty.setMana(0);
    empty.setCurrentXp(0);
    empty.setXpToNextLevel(0);
    empty.setStrength(0);
    empty.setGold(0);
    PlayerStateStore.save(empty, 7f);
    assertTrue(Files.exists(dir.resolve("hero.json")));
    assertEquals(1, PlayerStateStore.load().maxHp);
  }

  @Test
  void loadOfAMissingOrCorruptFileIsNullNotAnException() throws IOException {
    assertNull(PlayerStateStore.load());
    Files.writeString(dir.resolve("hero.json"), "{ this is not json");
    assertNull(PlayerStateStore.load());
  }

  @Test
  void theActiveFilenameDecidesWhichFileIsWrittenAndBlankMeansTheDefault() {
    PlayerStateStore.setActiveFilename("slot_two.json");
    PlayerStateStore.save(hero(), 7f);
    assertTrue(Files.exists(dir.resolve("slot_two.json")));
    assertFalse(Files.exists(dir.resolve("hero.json")));
    assertFalse(Files.exists(dir.resolve(PlayerStateStore.DEFAULT_FILENAME)));

    PlayerStateStore.setActiveFilename("  ");
    assertEquals(PlayerStateStore.DEFAULT_FILENAME, PlayerStateStore.getActiveFilename());
    PlayerStateStore.setActiveFilename(null);
    assertEquals(PlayerStateStore.DEFAULT_FILENAME, PlayerStateStore.getActiveFilename());
  }

  @Test
  void saveToANamedFileDoesNotTouchTheActiveFile() {
    PlayerStateStore.save(hero(), 7f);
    PlayerStateDto other = PlayerStateMapper.fromPlayer(hero(), 7f);
    other.name = "Other";
    PlayerStateStore.save("other.json", other);
    assertEquals("Goldie", PlayerStateStore.load().name);
    assertEquals("Other", PlayerStateStore.load("other.json").name);
  }

  @Test
  void saveOrThrowCreatesMissingDirectoriesAndReportsFailuresToTheCaller() throws IOException {
    PlayerStateStore.saveOrThrow(
        "nested/deeper/hero.json", PlayerStateMapper.fromPlayer(hero(), 7f));
    assertTrue(Files.exists(dir.resolve("nested/deeper/hero.json")));
    // A directory sits where the file should go: creation paths must see the failure.
    Files.createDirectories(dir.resolve("blocked.json/inner"));
    org.junit.jupiter.api.Assertions.assertThrows(
        IOException.class,
        () ->
            PlayerStateStore.saveOrThrow("blocked.json", PlayerStateMapper.fromPlayer(hero(), 7f)));
  }

  @Test
  void theCompanionSupplierIsConsultedOnEverySave() {
    int[] asked = {0};
    PlayerStateStore.setCompanionSupplier(
        () -> {
          asked[0]++;
          return null;
        });
    PlayerStateStore.save(hero(), 7f);
    PlayerStateStore.save(hero(), 7f);
    assertEquals(2, asked[0]);
    assertNull(PlayerStateStore.load().companion);
  }

  /**
   * The public surface the session seam must wrap or keep. Adding or removing a method here is a
   * deliberate act: update this list in the same commit and explain why.
   */
  @Test
  void publicSurfaceIsPinned() {
    Set<String> methods =
        new TreeSet<>(
            Arrays.stream(PlayerStateStore.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(m -> m.getName() + "/" + m.getParameterCount())
                .toList());
    assertEquals(
        new TreeSet<>(
            List.of(
                "applyToPlayer/2",
                "clearCompanionSupplier/0",
                "getActiveFilename/0",
                "load/0",
                "load/1",
                "resetActiveFilename/0",
                "save/1",
                "save/2",
                "saveOrThrow/2",
                "setActiveFilename/1",
                "setCompanionSupplier/1")),
        methods);
  }
}
