package com.perso.T4C.monster.core;

import com.perso.T4C.spawn.SpawnDefinition;
import com.perso.T4C.spawn.SpawnRegistry;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Tells bosses apart from ordinary monsters. MonsterDef has no explicit boss flag, so a monster
 * type counts as a boss when the whole world has at most {@link #BOSS_MAX_SPAWN_POINTS} spawn
 * points for it (Centaur King, the Drakes, the Elder Wyrms, the Vicars...). Monsters with no
 * registered spawn at all (scripted or summoned ones such as the Mirror Echo) are not bosses. Same
 * heuristic as the compendium's zone maps (MinimapExporter), applied world-wide.
 */
public final class MonsterRank {
  public static final int BOSS_MAX_SPAWN_POINTS = 2;
  private static volatile Map<String, Integer> spawnCounts;

  private MonsterRank() {}

  public static boolean isBoss(BaseMonster monster) {
    return monster != null && isBoss(monster.getName());
  }

  public static boolean isBoss(String monsterName) {
    int count = spawnPointCount(monsterName);
    return count > 0 && count <= BOSS_MAX_SPAWN_POINTS;
  }

  static int spawnPointCount(String monsterName) {
    if (monsterName == null) return 0;
    return counts().getOrDefault(key(monsterName), 0);
  }

  private static Map<String, Integer> counts() {
    Map<String, Integer> cached = spawnCounts;
    if (cached != null) return cached;
    synchronized (MonsterRank.class) {
      if (spawnCounts == null) {
        Map<String, Integer> built = new HashMap<>();
        for (SpawnDefinition spawn : SpawnRegistry.monsters()) {
          MonsterDef def = MonsterRegistry.findByName(spawn.type());
          String name = def != null ? def.getName() : spawn.type();
          if (name != null) built.merge(key(name), 1, Integer::sum);
        }
        spawnCounts = Map.copyOf(built);
      }
      return spawnCounts;
    }
  }

  private static String key(String name) {
    return name.trim().toLowerCase(Locale.ROOT);
  }
}
