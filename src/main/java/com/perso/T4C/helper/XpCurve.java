package com.perso.T4C.helper;

import com.perso.T4C.config.Paths;
import com.perso.T4C.player.Player;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class XpCurve {
  private static final Logger log = LoggerFactory.getLogger(XpCurve.class);
  private final Map<Integer, Entry> byLevel;

  private XpCurve(Map<Integer, Entry> byLevel) {
    this.byLevel = byLevel;
  }

  public static XpCurve loadDefault() {
    return load(Paths.XP_CURVE_BIN);
  }

  public static XpCurve load(String path) {
    File file = resolve(path);
    try {
      if (!file.exists()) {
        log.warn("XP curve file not found: {}", path);
        return new XpCurve(Collections.emptyMap());
      }
      Map<Integer, Entry> map = new HashMap<>();
      for (Entry entry : XpCurveBinaryIO.read(file)) {
        if (entry == null || entry.level <= 0) {
          continue;
        }
        map.put(entry.level, entry);
      }
      return new XpCurve(map);
    } catch (Exception e) {
      log.warn("Failed to load XP curve: {}", path, e);
    }
    return new XpCurve(Collections.emptyMap());
  }

  public static void save(List<Entry> entries) throws IOException {
    List<Entry> sorted =
        entries == null
            ? List.of()
            : entries.stream()
                .filter(entry -> entry != null && entry.level > 0)
                .sorted(Comparator.comparingInt(Entry::getLevel))
                .toList();
    XpCurveBinaryIO.write(resolve(Paths.XP_CURVE_BIN), sorted);
  }

  public List<Entry> entries() {
    List<Entry> entries = new ArrayList<>(byLevel.values());
    entries.sort(Comparator.comparingInt(Entry::getLevel));
    return Collections.unmodifiableList(entries);
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

  private static File resolve(String path) {
    File file = new File(path);
    if (!file.isAbsolute()) {
      file = new File(System.getProperty("user.dir"), path);
    }
    return file;
  }

  @Getter
  public static class Entry {
    private final int level;
    private final int xpToNextLevel;
    private final int totalXp;

    public Entry(int level, int xpToNextLevel, int totalXp) {
      this.level = level;
      this.xpToNextLevel = xpToNextLevel;
      this.totalXp = totalXp;
    }
  }
}
