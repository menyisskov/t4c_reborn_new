package com.perso.T4C.helper;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.mapping.definition.XpCurveDefinitions;
import com.perso.T4C.player.Player;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

public final class XpCurve {
  private final Map<Integer, Entry> byLevel;

  private XpCurve(Map<Integer, Entry> byLevel) {
    this.byLevel = byLevel;
  }

  public static XpCurve loadDefault() {
    return fromEntries(XpCurveDefinitions.all());
  }

  public static XpCurve load(String path) {
    return fromEntries(XpCurveDefinitions.all());
  }

  public static void save(List<Entry> entries) throws IOException {
    throw new UnsupportedOperationException("XP curve definitions are Java source");
  }

  public List<Entry> entries() {
    List<Entry> entries = new ArrayList<>(byLevel.values());
    entries.sort(Comparator.comparingInt(Entry::getLevel));
    return Collections.unmodifiableList(entries);
  }

  public long getXpToNextLevel(int level) {
    Entry entry = byLevel.get(level);
    return entry != null ? entry.xpToNextLevel : 0;
  }

  public long getTotalXp(int level) {
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
    if (level > GameConstants.MAX_PLAYER_LEVEL) {
      level = GameConstants.MAX_PLAYER_LEVEL;
      player.setLevel(level);
    }
    long xpToNext = getXpToNextLevel(level);
    if (xpToNext > 0 || level >= GameConstants.MAX_PLAYER_LEVEL) {
      player.setXpToNextLevel(xpToNext);
    }
  }

  /**
   * Builds the curve, truncated at {@link GameConstants#MAX_PLAYER_LEVEL}: levels above the cap
   * are dropped and the cap level itself needs no more XP (xpToNextLevel 0), which is what stops
   * leveling there. The source definitions still run to level 1000 so the cap can be raised later
   * without regenerating them.
   */
  private static XpCurve fromEntries(List<Entry> entries) {
    Map<Integer, Entry> map = new HashMap<>();
    for (Entry entry : entries) {
      if (entry == null || entry.level <= 0 || entry.level > GameConstants.MAX_PLAYER_LEVEL)
        continue;
      map.put(
          entry.level,
          entry.level == GameConstants.MAX_PLAYER_LEVEL
              ? new Entry(entry.level, 0L, entry.totalXp)
              : entry);
    }
    return new XpCurve(map);
  }

  @Getter
  public static class Entry {
    private final int level;
    private final long xpToNextLevel;
    private final long totalXp;

    public Entry(int level, long xpToNextLevel, long totalXp) {
      this.level = level;
      this.xpToNextLevel = xpToNextLevel;
      this.totalXp = totalXp;
    }
  }
}
