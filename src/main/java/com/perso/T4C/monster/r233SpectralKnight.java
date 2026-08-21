package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class r233SpectralKnight extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r233SpectralKnight(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Spectral Knight",
        "${monster.spectral_knight}",
        1646,
        0,
        6,
        5592,
        76,
        173,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        115,
        352,
        java.util.List.of(
            new MonsterDef.LootDrop("Manastone", 0.01f),
            new MonsterDef.LootDrop("Mithril blade", 0.01f),
            new MonsterDef.LootDrop("Rough garnet", 0.001f),
            new MonsterDef.LootDrop("Rough ruby", 0.004f),
            new MonsterDef.LootDrop("Rough carnelian", 0.02f),
            new MonsterDef.LootDrop("Spectral helm", 0.005f),
            new MonsterDef.LootDrop("Large shield", 0.0025f)),
        false,
        0.0f,
        79,
        62,
        72,
        91,
        0,
        72,
        0,
        new int[] {76, 76, 102, 51, 5025, 63, 100, 100, 100, 100, 100, 100},
        64,
        266,
        0,
        1077936128,
        20012,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        34,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d98+75", 778, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 30, 10119, 1, 10),
            new MonsterDef.Attack("", 0, 10, 10369, 1, 10),
            new MonsterDef.Attack("", 0, 30, 10090, 1, 10),
            new MonsterDef.Attack("", 0, 5, 10384, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
