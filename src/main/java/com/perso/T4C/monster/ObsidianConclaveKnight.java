package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "MOBOBSIDIANCONCLAVEKNIGHT",
    x = 567,
    y = 764,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "MOBOBSIDIANCONCLAVEKNIGHT",
    x = 568,
    y = 771,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "MOBOBSIDIANCONCLAVEKNIGHT",
    x = 573,
    y = 771,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "MOBOBSIDIANCONCLAVEKNIGHT",
    x = 579,
    y = 768,
    z = 0,
    stationary = false,
    aggressive = true)
public final class ObsidianConclaveKnight extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public ObsidianConclaveKnight(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public NpcScriptRuntime.Effects onDeath(Player player) {
    if (player != null && player.getQuestFlag("ADDON_SERAPH_ARMOR_QUEST") == 1) {
      String kills = "ADDON_OBSIDIAN_KNIGHTS_KILLED";
      int count = Math.min(10, player.getQuestFlag(kills) + 1);
      player.setQuestFlag(kills, count);
      if (count == 10 && player.getQuestFlag("ADDON_OBSIDIAN_MORDRED_SPAWNED") == 0
          && NpcScriptRuntime.summon("MOBMORDRED", 575 * GRID_W, 765 * GRID_H, 0))
        player.setQuestFlag("ADDON_OBSIDIAN_MORDRED_SPAWNED", 1);
    }
    return NpcScriptRuntime.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBOBSIDIANCONCLAVEKNIGHT",
        "${monster.mobobsidianconclaveknight}",
        1801,
        0,
        35,
        31845,
        82,
        187,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC!a",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        122,
        374,
        java.util.List.of(),
        false,
        0.0f,
        83,
        76,
        76,
        96,
        76,
        76,
        28,
        new int[] {75, 75, 75, 75, 75, 5000, 100, 100, 100, 100, 100, 100},
        68,
        282,
        0,
        1107820544,
        10001,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d106+81", 826, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
