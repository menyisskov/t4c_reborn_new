package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBNEOFLARE", x = 1827, y = 2775, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBNEOFLARE", x = 1834, y = 2759, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBNEOFLARE", x = 1851, y = 2751, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBNEOFLARE", x = 1852, y = 2766, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBNEOFLARE", x = 1853, y = 2866, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBNEOFLARE", x = 1857, y = 2805, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBNEOFLARE", x = 1861, y = 2782, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBNEOFLARE", x = 1870, y = 2707, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBNEOFLARE", x = 1874, y = 2789, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBNEOFLARE", x = 1878, y = 2777, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBNEOFLARE", x = 1887, y = 2728, z = 0, stationary = false, aggressive = true)
public final class Neoflare extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public Neoflare(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public NpcScriptRuntime.Effects onSpawn(Player p) {

    return selfSpell("spell.npc_cantrip_red_wipe");
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBNEOFLARE",
        "${monster.mobneoflare}",
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
