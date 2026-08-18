package com.perso.T4C.monster.core;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpawnGroupBinaryIO;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SpawnGroupRegistry {
  private static List<SpawnGroup> cache;
  private static Map<String, SpawnGroup> byName;

  private SpawnGroupRegistry() {}

  public static synchronized List<SpawnGroup> load() {
    if (cache != null) return cache;
    File file = new File(Paths.SPAWN_GROUPS_BIN);
    if (!file.exists()) {
      rebuild(List.of());
      return cache;
    }
    try {
      rebuild(SpawnGroupBinaryIO.read(file));
    } catch (Exception ignored) {
      rebuild(List.of());
    }
    return cache;
  }

  public static synchronized SpawnGroup findByName(String name) {
    if (byName == null) load();
    return byName.get(name);
  }

  public static synchronized void save(List<SpawnGroup> groups) throws IOException {
    SpawnGroupBinaryIO.write(new File(Paths.SPAWN_GROUPS_BIN), groups);
    rebuild(groups);
  }

  public static synchronized void invalidate() {
    cache = null;
    byName = null;
  }

  private static void rebuild(List<SpawnGroup> groups) {
    cache = Collections.unmodifiableList(groups);
    Map<String, SpawnGroup> map = new LinkedHashMap<>();
    for (SpawnGroup g : groups) {
      if (g != null && g.getName() != null) map.put(g.getName(), g);
    }
    byName = Collections.unmodifiableMap(map);
  }
}
