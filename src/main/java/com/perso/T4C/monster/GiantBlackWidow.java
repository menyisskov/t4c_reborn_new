package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;
import java.util.ArrayList;
import java.util.List;

@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1725, y = 253, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1726, y = 221, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1727, y = 241, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1730, y = 120, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1731, y = 103, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1742, y = 88, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1745, y = 280, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1745, y = 281, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1747, y = 263, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1752, y = 141, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1753, y = 232, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1754, y = 126, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1757, y = 110, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1759, y = 102, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1764, y = 262, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1767, y = 88, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1772, y = 232, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1774, y = 248, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1847, y = 37, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1849, y = 54, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1873, y = 178, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1896, y = 318, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1905, y = 311, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1907, y = 329, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 1916, y = 322, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 2060, y = 196, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 2072, y = 201, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBGIANTBLACKWIDOW", x = 2073, y = 185, z = 2, stationary = false, aggressive = true)
public final class GiantBlackWidow extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Spider Attack.wav";
  public static final String SOUND_DEATH = "Spider Dying.wav";
  public static final String SOUND_HIT = "Spider Hit.wav";

  public GiantBlackWidow(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player p) {

    return hatch();
  }

  @Override
  public MonsterScriptBridge.Effects onAttacked(Player p) {

    return hatch();
  }

  @Override
  public MonsterScriptBridge.Effects onDeath(Player p) {

    List<String> summons = new ArrayList<>();

    int[][] offsets = {{-1, -1}, {0, -1}, {-1, 0}, {1, 0}, {0, 1}};

    for (int[] o : offsets)
      summons.add(
          "WIDOWHATCHLING@" + (int) getPosition().x + o[0] + "," + (int) getPosition().y + o[1]);

    return MonsterScriptBridge.Effects.empty();
  }

  private MonsterScriptBridge.Effects hatch() {

    return MonsterScriptBridge.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBGIANTBLACKWIDOW",
        "${monster.mobgiantblackwidow}",
        100,
        0,
        1,
        100,
        1,
        2,
        30000L,
        "",
        "",
        "",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        1,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        true,
        java.util.List.of(),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
