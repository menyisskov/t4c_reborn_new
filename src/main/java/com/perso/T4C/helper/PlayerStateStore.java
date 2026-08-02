package com.perso.T4C.helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.perso.T4C.player.Player;
import com.perso.T4C.npc.CompanionNPC;
import java.util.function.Supplier;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tiny persistence helper for saving/loading player state between sessions.
 * Stored next to the executable/project working directory (project root in dev).
 */
public final class PlayerStateStore {
    private PlayerStateStore() {}
    public static final String DEFAULT_FILENAME = com.perso.T4C.config.Paths.PLAYER_STATE_FILE;

    private static float lastDayNightHour = 7f;
    @Setter private static Supplier<CompanionNPC> companionSupplier;

    public static void save(Player player) {
        save(player, lastDayNightHour);
    }

    public static void save(Player player, float dayNightHour) {
        lastDayNightHour = dayNightHour;
        PlayerStateDto state = PlayerStateMapper.fromPlayer(player, dayNightHour);
        CompanionNPC companion = companionSupplier == null ? null : companionSupplier.get();
        if (companion != null) {
            state.companion = companion.toSaveState();
        }
        if (!hasStats(state)) {
            try {
                Gdx.app.log("PlayerStateStore", "Skip saving player_state: stats are empty");
            } catch (Throwable ignored) {}
            return;
        }
        save(DEFAULT_FILENAME, state);
    }

    public static void save(String filename, PlayerStateDto state) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Path outPath = Paths.get(System.getProperty("user.dir"), filename);
            Files.writeString(outPath, gson.toJson(state), StandardCharsets.UTF_8);
        } catch (Exception e) {

            // Fallback: try libGDX local storage
            try {
                FileHandle fh = Gdx.files.local(filename);
                Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
                fh.writeString(gson2.toJson(state), false, "UTF-8");
                return;
            } catch (Throwable ignored) {}

            try {
                Gdx.app.error("PlayerStateStore", "Failed to save player state", e);
            } catch (Throwable ignored2) {}
        }
    }

    public static void applyToPlayer(PlayerStateDto state, Player player) {
        PlayerStateMapper.applyToPlayer(state, player);
    }

    public static PlayerStateDto load() {
        return load(DEFAULT_FILENAME);
    }

    public static PlayerStateDto load(String filename) {
        try {
            Path inPath = Paths.get(System.getProperty("user.dir"), filename);
            if (!Files.exists(inPath)) {
                try {
                    Gdx.app.log("PlayerStateStore", "No saved player_state at: " + inPath.toAbsolutePath());
                } catch (Throwable ignored) {}
                return null;
            }
            String json = Files.readString(inPath, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            PlayerStateDto st = gson.fromJson(json, PlayerStateDto.class);
            try {
                Gdx.app.log("PlayerStateStore", "Loaded player_state from: " + inPath.toAbsolutePath());
            } catch (Throwable ignored) {}
            return st;
        } catch (Exception e) {
            try {
                FileHandle fh = Gdx.files.local(filename);
                if (!fh.exists()) return null;
                String json2 = fh.readString("UTF-8");
                Gson gson2 = new Gson();
                return gson2.fromJson(json2, PlayerStateDto.class);
            } catch (Throwable ignored) {}

            try {
                Gdx.app.error("PlayerStateStore", "Failed to load player state", e);
            } catch (Throwable ignored2) {}
            return null;
        }
    }

    private static boolean hasStats(PlayerStateDto state) {
        if (state == null) {
            return false;
        }
        return state.maxHp > 0 || state.maxMana > 0 || state.xpToNextLevel > 0 || state.level > 0
                || state.currentHp > 0 || state.currentXp > 0 || state.statPoints > 0 || state.skillPoints > 0
                || state.rebirthCount > 0
                || state.karma != 0 || (state.questFlags != null && !state.questFlags.isEmpty())
                || state.strength != 0 || state.dexterity != 0 || state.endurance != 0
                || state.intelligence != 0 || state.wisdom != 0 || state.gold != 0;
    }

}
