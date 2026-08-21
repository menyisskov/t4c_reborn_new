package com.perso.T4C.npc.core;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.player.Player;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Summons and monster lifecycle effects implemented in Java. */
public final class NpcScriptRuntime {

  public static final String SUMMON_DEATH_COUNTER = "@summon.deathCounter";
  public static final String SUMMON_DEATH_ITEM = "@summon.deathItem";
  public static final String SUMMON_DEATH_MESSAGE = "@summon.deathMessage";

  private static final int ARENA_TOKEN_ITEM_ID = 41702;
  private static final Pattern ARENA_NAME = Pattern.compile("ArenaMob(?:XP)?(\\d+)", Pattern.CASE_INSENSITIVE);

  private static final Map<String, ArrayDeque<DeathEffect>> TRACKED_SUMMONS = new HashMap<>();

  private static SummonCallback summonCallback;

  private NpcScriptRuntime() {}

  public record Effects(List<String> messages, List<String> selfSpells, List<String> targetSpells) {

    public static Effects empty() {
      return new Effects(List.of(), List.of(), List.of());
    }
  }

  public record DeathEffect(String counterFlag, String itemKey, String message) {}

  @FunctionalInterface
  public interface SummonCallback {
    boolean summon(String monster, float worldX, float worldY, int worldZ);
  }

  public static void setSummonCallback(SummonCallback callback) {
    summonCallback = callback;
  }

  public static synchronized boolean summon(String monster, float worldX, float worldY, int worldZ) {
    return summon(monster, worldX, worldY, worldZ, Map.of());
  }

  public static synchronized boolean summon(
      String monster, float worldX, float worldY, int worldZ, Map<String, String> metadata) {
    boolean summoned =
        summonCallback != null && summonCallback.summon(monster, worldX, worldY, worldZ);
    String counter = metadata == null ? null : metadata.get(SUMMON_DEATH_COUNTER);
    if (counter != null && !counter.isBlank()) {
      DeathEffect effect =
          new DeathEffect(
              counter, metadata.get(SUMMON_DEATH_ITEM), metadata.get(SUMMON_DEATH_MESSAGE));
      if (summoned) {
        TRACKED_SUMMONS.computeIfAbsent(normalize(monster), ignored -> new ArrayDeque<>()).addLast(effect);
      } else {
        adjustFlag(counter, -1);
      }
    }
    return summoned;
  }

  public static synchronized DeathEffect summonedMonsterDefeated(String monster) {
    ArrayDeque<DeathEffect> effects = TRACKED_SUMMONS.get(normalize(monster));
    if (effects == null || effects.isEmpty()) return null;
    DeathEffect effect = effects.removeFirst();
    if (effects.isEmpty()) TRACKED_SUMMONS.remove(normalize(monster));
    adjustFlag(effect.counterFlag(), -1);
    return effect;
  }

  public static Effects death(DataMonster monster, Player player) {
    String identity = monsterIdentity(monster);
    if (arenaSlice(identity) != null) {
      Effects onDeath = arenaDeath(identity, player);
      arenaDestroyed();
      return onDeath;
    }
    return Effects.empty();
  }

  public static Effects attack(DataMonster monster, Player player) {
    return Effects.empty();
  }

  public static Effects spawn(DataMonster monster, Player player) {
    return Effects.empty();
  }

  public static Effects attacked(DataMonster monster, Player player) {
    return Effects.empty();
  }

  public static Effects hit(DataMonster monster, Player player) {
    return Effects.empty();
  }

  public static Effects attackHit(DataMonster monster, Player player) {
    return Effects.empty();
  }

  public static Effects arenaDeath(String monsterName, Player player) {
    Integer slice = arenaSlice(monsterName);
    if (slice == null) return Effects.empty();
    List<String> messages = new ArrayList<>();
    if (player != null) {
      boolean guaranteed = slice >= 475;
      int chance = slice + 30 - player.getLevel();
      if (guaranteed || ThreadLocalRandom.current().nextInt(1, 101) <= chance) {
        ItemDefinition token = ItemRegistry.findByNumId(ARENA_TOKEN_ITEM_ID);
        if (token != null) InventoryService.add(player, token.getKey());
        messages.add(I18n.resolve("${npc.cpp.intl.10682}"));
      }
      player.setQuestFlag("__FLAG_ARENA_LEVEL", slice);
    }
    return new Effects(List.copyOf(messages), List.of("spell.mob_arena_level_spell"), List.of());
  }

  static void arenaDestroyed() {
    int current = NpcWorldFlags.get("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA");
    if (current > 0) NpcWorldFlags.set("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA", current - 1);
  }

  public static Integer arenaSlice(String name) {
    if (name == null) return null;
    Matcher matcher = ARENA_NAME.matcher(name.trim());
    if (!matcher.matches()) return null;
    return Integer.parseInt(matcher.group(1));
  }

  private static String monsterIdentity(DataMonster monster) {
    if (monster.getDefinition() != null && monster.getDefinition().getName() != null) {
      return monster.getDefinition().getName();
    }
    return monster.getCanonicalName();
  }

  private static void adjustFlag(String flag, int delta) {
    NpcWorldFlags.set(flag, NpcWorldFlags.get(flag) + delta);
  }

  private static String normalize(String monster) {
    return monster == null ? "" : monster.trim().toLowerCase(Locale.ROOT);
  }
}
