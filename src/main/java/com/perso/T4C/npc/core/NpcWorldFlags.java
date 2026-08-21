package com.perso.T4C.npc.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Server-wide NPC/monster flags (arena occupancy, world quests, etc.). */
public final class NpcWorldFlags {

  private static final Map<String, Integer> FLAGS = new ConcurrentHashMap<>();

  private static final Map<Integer, String> NUMERIC =
      Map.of(
          30098, "__GLOBAL_FLAG_GOBLIN_QUEST",
          30191, "__GLOBAL_BANK_HAS_BEEN_ROBBED",
          30202, "__GLOBAL_BANK_HAS_BEEN_ROBBED_BY",
          30464, "__GLOBAL_FLAG_MAKRSH_PTANGH_DEAD_TIMER",
          30465, "__GLOBAL_FLAG_MAKRSH_PTANGH_IS_FIGHTING");

  private NpcWorldFlags() {}

  public static int get(String flag) {
    return FLAGS.getOrDefault(flag, 0);
  }

  public static int get(int id) {
    String name = NUMERIC.get(id);
    return name == null ? 0 : get(name);
  }

  public static void set(String flag, int value) {
    if (value <= 0) FLAGS.remove(flag);
    else FLAGS.put(flag, value);
  }
}
