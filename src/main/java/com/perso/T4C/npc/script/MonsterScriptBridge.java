package com.perso.T4C.npc.script;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MonsterScriptBridge {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public record Effects(List<String> messages, List<String> selfSpells, List<String> targetSpells) {

    public static Effects empty() {

      return new Effects(List.of(), List.of(), List.of());
    }
  }

  private MonsterScriptBridge() {}

  public static Effects death(DataMonster monster, Player player) {

    List<String> messages = new ArrayList<>(),
        selfSpells = new ArrayList<>(),
        targetSpells = new ArrayList<>();

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

  private static Effects event(DataMonster monster, Player player, String event) {

    List<String> messages = new ArrayList<>(),
        selfSpells = new ArrayList<>(),
        targetSpells = new ArrayList<>();

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

    for (NpcScriptEngine.SummonRequest summon : result.summons()) {

      NpcSummonBridge.summon(
          summon.monster(),
          monster.getPosition().x + offset(summon.xExpression(), GRID_W),
          monster.getPosition().y + offset(summon.yExpression(), GRID_H),
          0);
    }

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

    var m =
        java.util.regex.Pattern.compile("dice\\(1,\\s*(\\d+)\\).*?([+-]\\s*\\d+)")
            .matcher(expression);

    if (!m.find()) return 0;

    int sides = Integer.parseInt(m.group(1));

    int base = Integer.parseInt(m.group(2).replace(" ", ""));

    return (ThreadLocalRandom.current().nextInt(1, sides + 1) + base) * grid;
  }
}
