package com.perso.T4C.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.config.Paths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Three-slot local character roster used when no T4C server is available. */
public final class LocalCharacterStore {
    public static final int MAX_CHARACTERS = 3;
    public static final String MALE = AppearanceDefaultsCatalog.MALE;
    public static final String FEMALE = AppearanceDefaultsCatalog.FEMALE;
    public static final int MIN_STARTING_GOLD = 201;
    public static final int MAX_STARTING_GOLD = 250;
    private static final String STARTING_TORCH = "item.torch";
    private static final int STARTING_TORCH_COUNT = 3;
    private static final int STARTING_POTION_COUNT = 3;
    private static final List<String> STARTING_POTIONS = List.of(
            "item.light_healing_potion",
            "item.potion_of_mana");

    private static final List<String> STARTING_INVENTORY = List.of(
            "item.dagger",
            "item.cloth_vest",
            "item.cloth_pants",
            "item.bow",
            "item.wooden_arrow");

    private static final Map<String, Integer> STARTING_SKILLS = Map.of(
            "attack", 15,
            "dodge", 15,
            "archery", 15);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile CharacterSlot activeCharacter;

    private LocalCharacterStore() {
    }

    public static synchronized List<CharacterSlot> list() throws IOException {
        return List.copyOf(loadRoster().characters);
    }

    public static synchronized CharacterSlot create(String rawName, String rawGender,
            CharacterCreationRules.Stats stats) throws IOException {
        String name = CharacterCreationRules.normalizeName(rawName);
        if (!CharacterCreationRules.isValidName(name)) {
            throw new IllegalArgumentException("Invalid character name");
        }
        Roster roster = loadRoster();
        if (roster.characters.size() >= MAX_CHARACTERS) {
            throw new IllegalStateException("Character roster is full");
        }
        if (roster.characters.stream().anyMatch(slot -> slot.name.equalsIgnoreCase(name))) {
            throw new IllegalArgumentException("Character name already exists");
        }

        String gender = normalizeGender(rawGender);
        String id = UUID.randomUUID().toString();
        String stateFile = Paths.CHARACTER_STATES_DIR + "/" + id + ".json";
        CharacterSlot slot = new CharacterSlot(id, name, gender, stateFile,
                System.currentTimeMillis());

        PlayerStateDto state = newInitialState(name, gender, stats);
        PlayerStateStore.save(stateFile, state);
        roster.characters.add(slot);
        try {
            saveRoster(roster);
        } catch (IOException e) {
            Files.deleteIfExists(resolve(stateFile));
            throw e;
        }
        return slot;
    }

    public static synchronized void delete(CharacterSlot slot) throws IOException {
        if (slot == null) return;
        Roster roster = loadRoster();
        boolean removed = roster.characters.removeIf(candidate -> candidate.id.equals(slot.id));
        if (!removed) return;
        saveRoster(roster);
        Files.deleteIfExists(resolve(slot.stateFile));
        if (activeCharacter != null && activeCharacter.id.equals(slot.id)) {
            activeCharacter = null;
            PlayerStateStore.resetActiveFilename();
        }
    }

    public static synchronized void activate(CharacterSlot slot) {
        if (slot == null) throw new IllegalArgumentException("Character is required");
        activeCharacter = slot;
        PlayerStateStore.setActiveFilename(slot.stateFile);
    }

    public static CharacterSlot getActiveCharacter() {
        return activeCharacter;
    }

    public static PlayerStateDto loadState(CharacterSlot slot) {
        if (slot == null) return null;
        return PlayerStateStore.load(slot.stateFile);
    }

    private static PlayerStateDto newInitialState(String name, String gender,
            CharacterCreationRules.Stats stats) {
        PlayerStateDto state = new PlayerStateDto();
        state.name = name;
        state.gender = gender;
        state.x = GameConstants.NEW_CHARACTER_START_TILE_X;
        state.y = GameConstants.NEW_CHARACTER_START_TILE_Y;
        state.z = GameConstants.NEW_CHARACTER_START_TILE_Z;
        state.strength = stats.strength();
        state.endurance = stats.endurance();
        state.dexterity = stats.dexterity();
        state.wisdom = stats.wisdom();
        state.intelligence = stats.intelligence();
        state.maxHp = stats.maxHp();
        state.currentHp = stats.maxHp();
        state.maxMana = stats.maxMana();
        state.mana = stats.maxMana();
        state.level = 1;
        // The original server rolls StartupGold from the default formula 200+1d50.
        state.gold = ThreadLocalRandom.current().nextInt(
                MIN_STARTING_GOLD, MAX_STARTING_GOLD + 1);
        state.dayNightHour = 7f;
        state.spells = new ArrayList<>();
        state.quickSlots = new ArrayList<>();
        state.activeBuffs = new ArrayList<>();
        state.inventory = new ArrayList<>(STARTING_INVENTORY);
        state.inventory.addAll(java.util.Collections.nCopies(STARTING_TORCH_COUNT, STARTING_TORCH));
        for (String potion : STARTING_POTIONS) {
            state.inventory.addAll(java.util.Collections.nCopies(STARTING_POTION_COUNT, potion));
        }
        state.equipment = new HashMap<>();
        state.skills = new HashMap<>(STARTING_SKILLS);
        state.itemCharges = new HashMap<>();
        STARTING_POTIONS.forEach(potion -> state.itemCharges.put(potion, STARTING_POTION_COUNT));
        state.questFlags = new HashMap<>();
        state.respawnPointDefined = true;
        state.respawnWorldX = GameConstants.PLAYER_RESPAWN_TILE_X * GameConstants.GRID_W;
        state.respawnWorldY = GameConstants.PLAYER_RESPAWN_TILE_Y * GameConstants.GRID_H;
        state.respawnWorldZ = GameConstants.PLAYER_RESPAWN_TILE_Z;
        return state;
    }

    private static Roster loadRoster() throws IOException {
        Path rosterPath = resolve(Paths.CHARACTER_ROSTER_FILE);
        if (!Files.isRegularFile(rosterPath)) {
            Roster migrated = new Roster();
            Path legacy = resolve(Paths.PLAYER_STATE_FILE);
            if (Files.isRegularFile(legacy)) {
                PlayerStateDto state = PlayerStateStore.load(Paths.PLAYER_STATE_FILE);
                String name = state != null && state.name != null && !state.name.isBlank()
                        ? state.name : "Personnage";
                String gender = state != null ? normalizeGender(state.gender) : MALE;
                migrated.characters.add(new CharacterSlot("legacy", name, gender,
                        Paths.PLAYER_STATE_FILE, Files.getLastModifiedTime(legacy).toMillis()));
                saveRoster(migrated);
            }
            return migrated;
        }

        String json = Files.readString(rosterPath, StandardCharsets.UTF_8);
        Roster roster = GSON.fromJson(json, Roster.class);
        if (roster == null) throw new IOException("Unreadable local character roster");
        if (roster.characters == null) roster.characters = new ArrayList<>();
        roster.characters.removeIf(slot -> slot == null || slot.id == null || slot.name == null
                || slot.stateFile == null);
        return roster;
    }

    private static void saveRoster(Roster roster) throws IOException {
        Path output = resolve(Paths.CHARACTER_ROSTER_FILE);
        Path temp = output.resolveSibling(output.getFileName() + ".tmp");
        Files.writeString(temp, GSON.toJson(roster), StandardCharsets.UTF_8);
        try {
            Files.move(temp, output, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temp, output, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String normalizeGender(String gender) {
        return FEMALE.equalsIgnoreCase(gender) ? FEMALE : MALE;
    }

    private static Path resolve(String relative) {
        return Path.of(System.getProperty("user.dir"), relative);
    }

    private static final class Roster {
        int version = 1;
        List<CharacterSlot> characters = new ArrayList<>();
    }

    public record CharacterSlot(String id, String name, String gender, String stateFile,
                                long createdAt) {
    }
}
