package com.perso.T4C.helper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ResourceLock("user.dir")
class LocalCharacterStoreTest {
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
    void migratesTheLegacySaveWithoutMovingIt() throws Exception {
        Files.writeString(tempDir.resolve("player_state.json"), """
                {"name":"Ancien","gender":"FEMALE","level":12}
                """);

        List<LocalCharacterStore.CharacterSlot> roster = LocalCharacterStore.list();

        assertEquals(1, roster.size());
        assertEquals("Ancien", roster.get(0).name());
        assertEquals(LocalCharacterStore.FEMALE, roster.get(0).gender());
        assertTrue(Files.exists(tempDir.resolve("player_state.json")));
        assertTrue(Files.exists(tempDir.resolve("characters.json")));
    }

    @Test
    void createsActivatesLimitsAndDeletesLocalCharacters() throws Exception {
        CharacterCreationRules.Stats stats = new CharacterCreationRules.Stats(
                15, 14, 13, 12, 11, 28, 10);
        LocalCharacterStore.CharacterSlot first = LocalCharacterStore.create(
                "alice", LocalCharacterStore.FEMALE, stats);
        LocalCharacterStore.CharacterSlot second = LocalCharacterStore.create(
                "Bob", LocalCharacterStore.MALE, stats);

        assertEquals("Alice", first.name());
        assertThrows(IllegalArgumentException.class,
                () -> LocalCharacterStore.create("ALICE", LocalCharacterStore.MALE, stats));
        LocalCharacterStore.create("Charlie", LocalCharacterStore.MALE, stats);
        assertThrows(IllegalStateException.class,
                () -> LocalCharacterStore.create("Denis", LocalCharacterStore.MALE, stats));

        LocalCharacterStore.activate(first);
        assertEquals(first.stateFile(), PlayerStateStore.getActiveFilename());
        PlayerStateDto state = LocalCharacterStore.loadState(first);
        assertEquals("Alice", state.name);
        assertEquals(1, state.level);
        assertEquals(28, state.maxHp);
        assertEquals(2944f, state.x);
        assertEquals(1059f, state.y);
        assertEquals(0, state.z);
        assertTrue(state.spells.isEmpty(), "A new character must not start with LevelUp");
        assertTrue(state.gold >= LocalCharacterStore.MIN_STARTING_GOLD);
        assertTrue(state.gold <= LocalCharacterStore.MAX_STARTING_GOLD);
        assertEquals(List.of(
                "item.dagger",
                "item.cloth_vest",
                "item.cloth_pants",
                "item.bow",
                "item.wooden_arrow"), state.inventory);

        LocalCharacterStore.delete(second);
        assertEquals(2, LocalCharacterStore.list().size());
        assertFalse(Files.exists(tempDir.resolve(second.stateFile())));
    }

    @Test
    void addsTheStartingEquipmentOnceToAnExistingCharacter() throws Exception {
        CharacterCreationRules.Stats stats = new CharacterCreationRules.Stats(
                15, 14, 13, 12, 11, 28, 10);
        LocalCharacterStore.CharacterSlot slot = LocalCharacterStore.create(
                "Ancien", LocalCharacterStore.MALE, stats);
        PlayerStateDto legacy = LocalCharacterStore.loadState(slot);
        legacy.gold = 0;
        legacy.inventory.clear();
        legacy.questFlags.clear();
        PlayerStateStore.save(slot.stateFile(), legacy);

        PlayerStateDto migrated = LocalCharacterStore.loadState(slot);

        assertEquals(250, migrated.gold);
        assertEquals(List.of(
                "item.dagger",
                "item.cloth_vest",
                "item.cloth_pants",
                "item.bow",
                "item.wooden_arrow"), migrated.inventory);
        assertEquals(migrated.inventory, LocalCharacterStore.loadState(slot).inventory);
    }
}
