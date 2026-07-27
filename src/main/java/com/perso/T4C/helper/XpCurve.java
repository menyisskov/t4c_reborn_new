package com.perso.T4C.helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.perso.T4C.config.Paths;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.perso.T4C.player.Player;
/**
 * Class representing XpCurve.
 */

public final class XpCurve {
    private static final Logger log = LoggerFactory.getLogger(XpCurve.class);
    private final Map<Integer, Entry> byLevel;

    private XpCurve(Map<Integer, Entry> byLevel) {
        this.byLevel = byLevel;
    }

    public static XpCurve loadDefault() {
        return load(Paths.XP_CURVE);
    }

    public static XpCurve load(String path) {
        Gson gson = new Gson();
        try (Reader reader = openReader(path)) {
            if (reader == null) {
                log.warn("XP curve file not found: {}", path);
                return new XpCurve(Collections.emptyMap());
            }
            XpCurvePayload payload = gson.fromJson(reader, XpCurvePayload.class);
            if (payload == null || payload.xpCurve == null) {
                return new XpCurve(Collections.emptyMap());
            }
            Map<Integer, Entry> map = new HashMap<>();
            for (Entry entry : payload.xpCurve) {
                if (entry == null || entry.level <= 0) {
                    continue;
                }
                map.put(entry.level, entry);
            }
            return new XpCurve(map);
        } catch (JsonSyntaxException e) {
            log.warn("Invalid XP curve JSON: {}", path, e);
        } catch (Exception e) {
            log.warn("Failed to load XP curve: {}", path, e);
        }
        return new XpCurve(Collections.emptyMap());
    }

    public int getXpToNextLevel(int level) {
        Entry entry = byLevel.get(level);
        return entry != null ? entry.xpToNextLevel : 0;
    }

    public int getTotalXp(int level) {
        Entry entry = byLevel.get(level);
        return entry != null ? entry.totalXp : 0;
    }

    public void applyToPlayer(Player player) {
        if (player == null) {
            return;
        }
        int level = player.getLevel();
        if (level <= 0) {
            level = 1;
            player.setLevel(level);
        }
        int xpToNext = getXpToNextLevel(level);
        if (xpToNext > 0) {
            player.setXpToNextLevel(xpToNext);
        }
    }

    private static Reader openReader(String path) {
        try {
            FileHandle handle = Gdx.files.internal(path);
            if (handle.exists()) {
                return new BufferedReader(new InputStreamReader(handle.read(), StandardCharsets.UTF_8));
            }
        } catch (Throwable ignored) {
        }
        File file = new File(path);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir"), path);
        }
        if (!file.exists()) {
            return null;
        }
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }
/**
 * Class representing XpCurvePayload.
 */

    private static class XpCurvePayload {
        private List<Entry> xpCurve;
    }
/**
 * Class representing Entry.
 */

    @Getter
    public static class Entry {
        private int level;
        private int xpToNextLevel;
        private int totalXp;
    }
}
