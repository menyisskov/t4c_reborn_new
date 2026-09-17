package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Ignarok", x = 1900, y = 1600, z = 0, stationary = false, aggressive = true)
public final class IgnarokTheEmberfang extends DataMonster {
  // Reuses the "Agmorkian"/Kraanian drake puppet+sound family already used by
  // Lesser/Greater/Arch Drake — a distinct, lower-tier young drake, not a recolor of them.
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Ignarok the Emberfang";

  public IgnarokTheEmberfang(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Ignarok the Emberfang",
        "${monster.ignarok}",
        1950,
        0,
        0,
        74000,
        95,
        160,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        125,
        385,
        java.util.List.of(new MonsterDef.LootDrop("ignaroks_emberfang_claw", 0.008f)),
        false,
        0.0f,
        78,
        75,
        75,
        98,
        90,
        78,
        0,
        new int[] {100, 95, 45, 240, 120, 100, 100, 100, 100, 100, 100, 100},
        70,
        290,
        0,
        85,
        0,
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
        java.util.List.of(new MonsterDef.Attack("1d150+140", 1275, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("Ignarok", "EmberfangDrake"),
        java.util.Map.of());
  }
}
