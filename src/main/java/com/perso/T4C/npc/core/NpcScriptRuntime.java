package com.perso.T4C.npc.core;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.player.Player;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/** World-side effects for {@link NpcScriptEngine} results: summons and monster event scripts. */
public final class NpcScriptRuntime {

  public static final String SUMMON_DEATH_COUNTER = "@summon.deathCounter";
  public static final String SUMMON_DEATH_ITEM = "@summon.deathItem";
  public static final String SUMMON_DEATH_MESSAGE = "@summon.deathMessage";

  private static final Pattern DICE_OFFSET =
      Pattern.compile("dice\\(1,\\s*(\\d+)\\).*?([+-]\\s*\\d+)");

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
    List<String> messages = new ArrayList<>();
    List<String> selfSpells = new ArrayList<>();
    List<String> targetSpells = new ArrayList<>();
    execute(monster, player, "OnDeath", messages, selfSpells, targetSpells);
    execute(monster, player, "OnDestroy", messages, selfSpells, targetSpells);
    return new Effects(List.copyOf(messages), List.copyOf(selfSpells), List.copyOf(targetSpells));
  }

  public static Effects attack(DataMonster monster, Player player) {
    return event(monster, player, "OnAttack");
  }

  public static Effects spawn(DataMonster monster, Player player) {
    return event(monster, player, "OnSpawn");
  }

  public static Effects attacked(DataMonster monster, Player player) {
    return event(monster, player, "OnAttacked");
  }

  public static Effects hit(DataMonster monster, Player player) {
    return event(monster, player, "OnHit");
  }

  public static Effects attackHit(DataMonster monster, Player player) {
    return event(monster, player, "OnAttackHit");
  }

  public static void applySummons(
      NpcScriptEngine.Result result, float originX, float originY, int originZ, Map<String, String> metadata) {
    if (result == null) return;
    for (NpcScriptEngine.SummonRequest summon : result.summons()) {
      summon(
          summon.monster(),
          originX + offset(summon.xExpression(), GRID_W),
          originY + offset(summon.yExpression(), GRID_H),
          originZ,
          metadata == null ? Map.of() : metadata);
    }
  }

  private static Effects event(DataMonster monster, Player player, String event) {
    List<String> messages = new ArrayList<>();
    List<String> selfSpells = new ArrayList<>();
    List<String> targetSpells = new ArrayList<>();
    execute(monster, player, event, messages, selfSpells, targetSpells);
    return new Effects(List.copyOf(messages), List.copyOf(selfSpells), List.copyOf(targetSpells));
  }

  private static void execute(
      DataMonster monster,
      Player player,
      String event,
      List<String> messages,
      List<String> selfSpells,
      List<String> targetSpells) {
    String script = monster.getSourceEvents().get(event);
    if (script == null || script.isBlank()) return;
    NpcScriptEngine.Result result =
        NpcScriptEngine.event(script, monster.getCanonicalName(), player, 0, 0);
    applySummons(result, monster.getPosition().x, monster.getPosition().y, 0, Map.of());
    messages.addAll(result.systemMessages());
    for (String spell : result.selfSpells()) {
      String spellScript = monster.getSourceEvents().get("@spell." + spell);
      if (spellScript != null && !spellScript.isBlank()) {
        NpcScriptEngine.Result spellResult =
            NpcScriptEngine.event(spellScript, monster.getCanonicalName(), player, 0, 0);
        messages.addAll(spellResult.systemMessages());
        targetSpells.addAll(spellResult.targetSpells());
      }
      selfSpells.add(spell);
    }
    targetSpells.addAll(result.targetSpells());
  }

  private static float offset(String expression, int grid) {
    if (expression == null || expression.isBlank()) return 0;
    var m = DICE_OFFSET.matcher(expression);
    if (!m.find()) return 0;
    int sides = Integer.parseInt(m.group(1));
    int base = Integer.parseInt(m.group(2).replace(" ", ""));
    return (ThreadLocalRandom.current().nextInt(1, sides + 1) + base) * grid;
  }

  private static void adjustFlag(String flag, int delta) {
    NpcScriptEngine.setGlobalFlag(flag, NpcScriptEngine.globalFlag(flag) + delta);
  }

  private static String normalize(String monster) {
    return monster == null ? "" : monster.trim().toLowerCase(Locale.ROOT);
  }
}
