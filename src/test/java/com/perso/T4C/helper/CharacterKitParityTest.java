package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.online.model.OnlineCharacterCreation;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Single-player's starting kit for every class the online Create form offers, compared with the one shared
 * definition {@link CharacterStartingKit#itemCounts}. The online server's side of the same comparison is
 * {@code OnlineCharacterCreatorTest.everyOfferedClassGetsExactlyItsSinglePlayerKit}.
 */
@ResourceLock("user.dir")
class CharacterKitParityTest {
  @TempDir Path tempDir;
  private String previousUserDir;

  @BeforeEach
  void useIsolatedStorage() {
    previousUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", tempDir.toString());
    PlayerStateStore.resetActiveFilename();
  }

  @AfterEach
  void restoreStorage() throws Exception {
    LocalCharacterStore.CharacterSlot active = LocalCharacterStore.getActiveCharacter();
    if (active != null) LocalCharacterStore.delete(active);
    PlayerStateStore.resetActiveFilename();
    System.setProperty("user.dir", previousUserDir);
  }

  @Test
  void theCreateFormOffersEveryCreationClassOnceAndEachMatchesTheSharedKit() throws Exception {
    assertEquals(
        java.util.Arrays.stream(CharacterClass.values()).map(CharacterClass::id).sorted().toList(),
        OnlineCharacterCreation.CLASSES.stream().sorted().toList(),
        "the online form must offer exactly the seven single-player classes");
    for (CharacterClass type : CharacterClass.values()) {
      CharacterCreationRules.Stats stats = CharacterCreationRules.roll(type, new java.util.Random(11));
      LocalCharacterStore.CharacterSlot slot =
          LocalCharacterStore.create(
              "Kit", LocalCharacterStore.MALE, type, stats, LocalCharacterStore.Alignment.GOOD);
      PlayerStateDto state = LocalCharacterStore.loadState(slot);
      Map<String, Integer> carried = new TreeMap<>();
      state.inventory.forEach(key -> carried.merge(key, 1, Integer::sum));
      state.equipment.values().forEach(key -> carried.merge(key, 1, Integer::sum));
      assertEquals(CharacterStartingKit.itemCounts(type), carried, type.id());
      assertEquals(
          type.startingEquipment().keySet().stream().map(Enum::name).sorted().toList(),
          state.equipment.keySet().stream().sorted().toList(),
          type.id() + " worn slots");
      assertTrue(
          state.gold >= CharacterStartingKit.MIN_GOLD && state.gold <= CharacterStartingKit.MAX_GOLD,
          type.id());
      assertEquals(type.startingSpellKeys().size(), state.spells.size(), type.id() + " spells");
      LocalCharacterStore.delete(slot);
    }
  }
}
