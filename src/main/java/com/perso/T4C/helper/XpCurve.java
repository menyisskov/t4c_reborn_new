package com.perso.T4C.helper;

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

  private static XpCurve fromEntries(List<Entry> entries) {
    Map<Integer, Entry> map = new HashMap<>();
    for (Entry entry : entries) if (entry != null && entry.level > 0) map.put(entry.level, entry);
    return new XpCurve(map);
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
