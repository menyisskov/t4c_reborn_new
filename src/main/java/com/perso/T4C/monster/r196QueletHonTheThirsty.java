package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class r196QueletHonTheThirsty extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r196QueletHonTheThirsty(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Quelet Hon The Thirsty",
        "${monster.quelet_hon_the_thirsty}",
        1420,
        0,
        4,
        3154,
        34,
        78,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        124,
        384,
        java.util.List.of(
            new MonsterDef.LootDrop("Skeleton bone", 0.02f),
            new MonsterDef.LootDrop("Mana elixir", 0.02f)),
        false,
        0.0f,
        35,
        46,
        46,
        75,
        0,
        44,
        0,
        new int[] {77, 77, 103, 51, 5025, 51, 100, 100, 100, 100, 100, 100},
        35,
        170,
        0,
        1076494336,
        20012,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d45+33", 430, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10119, 7, 14),
            new MonsterDef.Attack("", 0, 50, 10090, 7, 14),
            new MonsterDef.Attack("", 0, 100, 10120, 3, 6)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
