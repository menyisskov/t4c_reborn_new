package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterLifecycle;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;

public final class r164OlinHaadGuard4 extends DataMonster implements MonsterLifecycle {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public r164OlinHaadGuard4(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  // T4C-0407: Olin Haad (npc/OlinHaad1.java) tells a player who has cleared about twenty of his
  // guards that he is ready to hear them out, reading this per-player counter; nothing counted
  // the kills before. Guard 3, 4 and 5 all count as Olin Haad guards.
  @Override
  public NpcScriptRuntime.Effects onDeath(Player player) {
    if (player != null) {
      String flag = "ADDON_NUMBER_OF_OLIN_HAAD_GUARDS_KILLED";
      player.setQuestFlag(flag, Math.min(99, player.getQuestFlag(flag) + 1));
    }
    return NpcScriptRuntime.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Olin Haad Guard 4",
        "${monster.olin_haad_guard_4}",
        69,
        0,
        1,
        59,
        4,
        10,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC!a",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        7,
        22,
        java.util.List.of(),
        false,
        0.0f,
        19,
        18,
        18,
        19,
        0,
        18,
        0,
        new int[] {92, 92, 92, 92, 61, 5000, 100, 100, 100, 100, 100, 100},
        4,
        26,
        0,
        1073741824,
        20006,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        1,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d7+3", 58, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
